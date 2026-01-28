package com.example.weibo.ui.livestream

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LiveLikeBurstOverlay(
    playToken: Int,
    origin: Offset,
    modifier: Modifier = Modifier,
    resIds: List<Int>,
    durationMs: Int = 1200,
    baseSizePx: Float = 96f
) {
    val resIdsState = rememberUpdatedState(resIds)

    data class LikeParticle(
        val id: Int,
        val resId: Int,
        val startAtMs: Long,
        val direction: Float,
        val amplitudePx: Float,
        val omega: Float,
        val phase: Float,
        val risePx: Float,
        val baseXJitterPx: Float,
        val sizePx: Float
    )

    val particles = remember { mutableStateListOf<LikeParticle>() }

    val nowMs = remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            val frame = withFrameNanos { it }
            nowMs.longValue = frame / 1_000_000L
        }
    }

    LaunchedEffect(playToken) {
        if (playToken <= 0 || resIdsState.value.isEmpty()) return@LaunchedEffect

        val now = nowMs.longValue

        val lane = Random.nextInt(0, 3) // 0:left 1:center 2:right
        val laneBase = when (lane) {
            0 -> -1f
            1 -> 0f
            else -> 1f
        }
        val direction = laneBase + (Random.nextFloat() - 0.5f) * 0.6f

        val amplitude = 32f + Random.nextFloat() * 26f // 正弦摆幅
        val omega = (2f * PI.toFloat()) * (0.85f + Random.nextFloat() * 0.55f) // 频率
        val phase = Random.nextFloat() * 2f * PI.toFloat()
        val risePx = 520f + Random.nextFloat() * 200f // 上升高度
        val baseXJitter = (Random.nextFloat() - 0.5f) * 30f
        val size = baseSizePx * (0.95f + Random.nextFloat() * 0.30f)

        particles += LikeParticle(
            id = playToken,
            resId = resIdsState.value.random(),
            startAtMs = now,
            direction = direction,
            amplitudePx = amplitude,
            omega = omega,
            phase = phase,
            risePx = risePx,
            baseXJitterPx = baseXJitter,
            sizePx = size
        )

        particles.removeAll { p -> now - p.startAtMs > durationMs + 120L }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    val drawableCache = remember { mutableMapOf<Int, android.graphics.drawable.Drawable>() }

    Canvas(modifier = modifier) {
        if (particles.isEmpty()) return@Canvas

        val now = nowMs.longValue

        particles.forEach { p ->
            val rawT = (now - p.startAtMs).toFloat() / durationMs.toFloat()
            if (rawT <= 0f || rawT >= 1f) return@forEach

            val tMove = FastOutSlowInEasing.transform(rawT.coerceIn(0f, 1f))

            val y = origin.y - p.risePx * tMove

            val x = origin.x +
                p.baseXJitterPx +
                (p.direction * 64f * tMove) +
                sin(p.omega * rawT + p.phase) * p.amplitudePx

            val bump = smoothBump(rawT, peakAt = 0.35f)
            val scale = 0.90f + bump * 0.60f // 0.90 -> 1.50 -> 0.90

            val alpha = alphaInOutSmooth(rawT, fadeInEnd = 0.22f, fadeOutStart = 0.72f)

            val drawable = drawableCache.getOrPut(p.resId) {
                requireNotNull(androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, p.resId, null))
            }

            val tailShrink = 1f - smoothStep(((rawT - 0.70f) / 0.30f).coerceIn(0f, 1f)) * 0.12f

            drawDrawableAt(
                drawable = drawable,
                center = Offset(x, y),
                sizePx = p.sizePx * scale * tailShrink,
                alpha = alpha,
                rotation = (p.direction * 18f) + (rawT * 50f)
            )
        }

        particles.removeAll { p -> now - p.startAtMs > durationMs + 120L }
    }
}

private fun smoothStep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

private fun alphaInOutSmooth(t: Float, fadeInEnd: Float, fadeOutStart: Float): Float {
    return when {
        t < fadeInEnd -> smoothStep(t / fadeInEnd)
        t > fadeOutStart -> 1f - smoothStep((t - fadeOutStart) / (1f - fadeOutStart))
        else -> 1f
    }.coerceIn(0f, 1f)
}

private fun smoothBump(t: Float, peakAt: Float): Float {
    return if (t <= peakAt) {
        smoothStep((t / peakAt).coerceIn(0f, 1f))
    } else {
        smoothStep(((1f - t) / (1f - peakAt)).coerceIn(0f, 1f))
    }
}

private fun DrawScope.drawDrawableAt(
    drawable: android.graphics.drawable.Drawable,
    center: Offset,
    sizePx: Float,
    alpha: Float,
    rotation: Float
) {
    val half = sizePx / 2f
    val left = (center.x - half).toInt()
    val top = (center.y - half).toInt()
    val right = (center.x + half).toInt()
    val bottom = (center.y + half).toInt()

    drawIntoCanvas { canvas ->
        val nc = canvas.nativeCanvas
        val save = nc.save()
        nc.rotate(rotation, center.x, center.y)
        drawable.alpha = (alpha * 255).toInt().coerceIn(0, 255)
        drawable.setBounds(left, top, right, bottom)
        drawable.draw(nc)
        nc.restoreToCount(save)
    }
}
