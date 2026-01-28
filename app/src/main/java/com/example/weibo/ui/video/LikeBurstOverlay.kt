package com.example.weibo.ui.video

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LikeBurstOverlay(
    play: Int,
    center: Offset,
    modifier: Modifier = Modifier,
    startRadiusPx: Float = 28f,
    endRadiusPx: Float = 180f,
    colors: List<Color> = listOf(
        Color(0xFFFF5A5F),
        Color(0xFFFFC107),
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFFF9800)
    )
) {
    val progress = remember(play) { Animatable(0f) }

    LaunchedEffect(play) {
        if (play > 0) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
    }

    val p = progress.value

    Canvas(modifier = modifier) {
        if (p <= 0f || p >= 1f) return@Canvas

        val ringRadius = lerp(startRadiusPx, endRadiusPx, p)
        val ringAlpha = (1f - p).coerceIn(0f, 1f)
        drawCircle(
            color = Color.White.copy(alpha = ringAlpha * 0.28f),
            radius = ringRadius,
            center = center,
            style = Stroke(width = lerp(10f, 2f, p))
        )

        val particleCount = 12
        val baseRadius = 10f

        for (i in 0 until particleCount) {
            val angle = (i.toFloat() / particleCount) * (Math.PI.toFloat() * 2f)
            val dist = lerp(startRadiusPx, endRadiusPx, p)
            val x = center.x + cos(angle) * dist
            val y = center.y + sin(angle) * dist

            val color = colors[i % colors.size]
            val alpha = (1f - p).coerceIn(0f, 1f)
            val r = lerp(baseRadius, baseRadius * 0.2f, p)
            drawCircle(color = color.copy(alpha = alpha), radius = r, center = Offset(x, y))
        }

        val dotCount = 8
        for (i in 0 until dotCount) {
            val angle = (i.toFloat() / dotCount) * (Math.PI.toFloat() * 2f) + 0.2f
            val dist = lerp(startRadiusPx * 1.05f, endRadiusPx, p)
            val x = center.x + cos(angle) * dist
            val y = center.y + sin(angle) * dist

            val color = colors[(i * 2) % colors.size]
            val alpha = (1f - p).coerceIn(0f, 1f)
            val r = lerp(baseRadius * 0.45f, baseRadius * 0.12f, p)
            drawCircle(color = color.copy(alpha = alpha), radius = r, center = Offset(x, y))
        }
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

