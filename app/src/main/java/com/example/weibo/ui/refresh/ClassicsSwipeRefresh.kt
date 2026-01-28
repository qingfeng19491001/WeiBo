@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.weibo.ui.refresh

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.example.weibo.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class RefreshUiState { Idle, Pulling, Refreshing, Finished }

@Composable
fun ClassicsSwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    headerHeight: Int = 56,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color(0xFFFF6600),
    textColor: Color = Color(0xFF666666),
    showShadow: Boolean = false,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val headerHeightPx = with(LocalDensity.current) { headerHeight.dp.toPx() }

    var uiState by remember { mutableStateOf(RefreshUiState.Idle) }
    var offsetPx by remember { mutableFloatStateOf(0f) }

    val connection = remember(enabled, scope) {
        ClassicsRefreshNestedScrollConnection(
            enabled = enabled,
            headerHeightPx = headerHeightPx,
            scope = scope,
            onRefresh = { onRefresh() },
            getOffset = { offsetPx },
            onOffsetChanged = { offsetPx = it },
            onUiStateChanged = { uiState = it }
        )
    }

    val maxRefreshingMs = 1200L

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            if (uiState != RefreshUiState.Refreshing) {
                uiState = RefreshUiState.Refreshing
                scope.launch {
                    animate(offsetPx, headerHeightPx, animationSpec = tween(200)) { v, _ -> offsetPx = v }
                }
            }
        } else {
            if (uiState == RefreshUiState.Refreshing) {
                uiState = RefreshUiState.Finished
                scope.launch {
                    kotlinx.coroutines.delay(450)
                    animate(offsetPx, 0f, animationSpec = tween(260)) { v, _ -> offsetPx = v }
                    uiState = RefreshUiState.Idle
                }
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState == RefreshUiState.Refreshing) {
            kotlinx.coroutines.delay(maxRefreshingMs)
            if (uiState == RefreshUiState.Refreshing) {
                uiState = RefreshUiState.Finished
                scope.launch {
                    kotlinx.coroutines.delay(500)
                    animate(offsetPx, 0f, animationSpec = tween(300)) { v, _ -> offsetPx = v }
                    uiState = RefreshUiState.Idle
                }
            }
        }
    }

    Box(modifier = modifier.nestedScroll(connection)) {
        if (offsetPx > 0f || uiState == RefreshUiState.Refreshing || uiState == RefreshUiState.Finished) {
            ClassicsHeader(
                modifier = Modifier.align(Alignment.TopCenter),
                uiState = uiState,
                offsetPx = offsetPx,
                headerHeightPx = headerHeightPx,
                headerHeight = headerHeight,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                textColor = textColor,
                showShadow = showShadow
            )
        }

        Box(modifier = Modifier.offset { IntOffset(0, offsetPx.roundToInt()) }) {
            content()
        }
    }
}

private class ClassicsRefreshNestedScrollConnection(
    private val enabled: Boolean,
    private val headerHeightPx: Float,
    private val scope: CoroutineScope,
    private val onRefresh: () -> Unit,
    private val getOffset: () -> Float,
    private val onOffsetChanged: (Float) -> Unit,
    private val onUiStateChanged: (RefreshUiState) -> Unit
) : NestedScrollConnection {

    private var isHorizontalDrag = false

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (!enabled || source != NestedScrollSource.Drag) return Offset.Zero

        val offset = getOffset()

        if (abs(available.x) > abs(available.y) * 1.2f) {
            isHorizontalDrag = true
            return Offset.Zero
        }
        if (abs(available.y) > abs(available.x) * 1.2f) {
            isHorizontalDrag = false
        }
        if (isHorizontalDrag) return Offset.Zero

        if (available.y < 0f && offset > 0f) {
            val newOffset = (offset + available.y).coerceAtLeast(0f)
            val consumedY = newOffset - offset // negative
            onOffsetChanged(newOffset)
            if (newOffset == 0f) onUiStateChanged(RefreshUiState.Idle)
            return Offset(0f, consumedY)
        }

        return Offset.Zero
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (!enabled || source != NestedScrollSource.Drag || isHorizontalDrag) return Offset.Zero

        if (available.y > 0f) {
            val currentOffset = getOffset()

            val progress = (currentOffset / headerHeightPx).coerceIn(0f, 2f)
            val resistance = 1.2f + progress * 1.6f // 1.2 -> 4.4
            val delta = available.y / resistance

            val max = headerHeightPx * 1.35f
            val newOffset = (currentOffset + delta).coerceIn(0f, max)
            onOffsetChanged(newOffset)
            if (newOffset > 0f) onUiStateChanged(RefreshUiState.Pulling)
            return Offset(0f, available.y)
        }

        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (!enabled) return Velocity.Zero
        if (isHorizontalDrag) {
            isHorizontalDrag = false
            return Velocity.Zero
        }

        val offset = getOffset()
        if (offset <= 0f) return Velocity.Zero

        if (offset >= headerHeightPx) {
            onUiStateChanged(RefreshUiState.Refreshing)
            scope.launch {
                animate(offset, headerHeightPx, animationSpec = tween(160)) { v, _ -> onOffsetChanged(v) }
                onRefresh()
            }
        } else {
            scope.launch {
                animate(offset, 0f, animationSpec = tween(190)) { v, _ -> onOffsetChanged(v) }
                onUiStateChanged(RefreshUiState.Idle)
            }
        }

        return Velocity.Zero
    }
}

@Composable
private fun ClassicsHeader(
    modifier: Modifier,
    uiState: RefreshUiState,
    offsetPx: Float,
    headerHeightPx: Float,
    headerHeight: Int,
    backgroundColor: Color,
    contentColor: Color,
    textColor: Color,
    showShadow: Boolean
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    
    val pullProgress = (offsetPx / headerHeightPx).coerceIn(0f, 1f)
    val pullRotation by animateFloatAsState(
        targetValue = pullProgress * 180f,
        animationSpec = tween(100),
        label = "pullRotation"
    )
    
    // 刷新时的旋转动画（类似backup项目的WeiboRefreshHeader）
    // 注意：使用 key 控制协程生命周期，避免无限循环导致无法进入“刷新成功”
    LaunchedEffect(uiState) {
        if (uiState == RefreshUiState.Refreshing) {
            while (uiState == RefreshUiState.Refreshing) {
                rotationAngle += 360f
                kotlinx.coroutines.delay(1000)
            }
        } else {
            rotationAngle = 0f
        }
    }
    
    // 使用animateFloatAsState来实现平滑的旋转动画
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(1000),
        label = "refreshRotation"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight.dp)
            .offset { IntOffset(0, (offsetPx - headerHeightPx).roundToInt()) },
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = if (showShadow && offsetPx > 0) 2.dp else 0.dp
    ) {
        val releaseToRefresh = offsetPx >= headerHeightPx

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            if (uiState == RefreshUiState.Refreshing) {
                // 刷新时显示旋转的图标（类似backup项目的动画效果）
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(animatedRotation),
                    tint = contentColor
                )
            } else {
                // 下拉时根据进度旋转图标（0-180度）
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(pullRotation),
                    tint = contentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = when {
                    uiState == RefreshUiState.Refreshing -> "正在刷新..."
                    uiState == RefreshUiState.Finished -> "刷新成功"
                    releaseToRefresh -> "释放立即刷新"
                    else -> "下拉刷新"
                },
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}
