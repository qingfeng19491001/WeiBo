package com.example.weibo.ui.preview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.weibo.core.ui.components.SetupSystemBars
import com.example.weibo.core.ui.components.SystemBarsConfig
import com.example.weibo.core.ui.components.TopBarBackground
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class MediaPreviewActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_MEDIA_LIST = "extra_media_list"
        private const val EXTRA_POSITION = "extra_position"
        private const val EXTRA_TRANSITION_NAME = "extra_transition_name"

        fun start(
            activity: Activity,
            sharedView: android.view.View?,
            transitionName: String,
            media: ArrayList<PreviewMedia>,
            position: Int
        ) {
            val intent = Intent(activity, MediaPreviewActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_MEDIA_LIST, media)
                putExtra(EXTRA_POSITION, position)
                putExtra(EXTRA_TRANSITION_NAME, transitionName)
            }
            val options = if (sharedView != null) {
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    sharedView,
                    transitionName
                )
            } else {
                ActivityOptionsCompat.makeBasic()
            }
            activity.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val mediaList = if (android.os.Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableArrayListExtra(EXTRA_MEDIA_LIST, PreviewMedia::class.java) ?: arrayListOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<PreviewMedia>(EXTRA_MEDIA_LIST) ?: arrayListOf()
        }
        val startPosition = intent.getIntExtra(EXTRA_POSITION, 0)

        if (mediaList.isEmpty()) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                SetupSystemBars(
                    SystemBarsConfig(
                        immersive = true,
                        autoStatusBarIcons = true,
                        autoStatusBarColor = true,
                        topBarBackground = TopBarBackground.Solid(Color.Black),
                        statusBarIconsFallbackColor = Color.Black
                    )
                )
                MediaPreviewScreen(
                    mediaList = mediaList,
                    startPosition = startPosition,
                    onClose = {
                        finishAfterTransition()
                    }
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0) 
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaPreviewScreen(
    mediaList: List<PreviewMedia>,
    startPosition: Int,
    onClose: () -> Unit
) {
    
    val pagerState = rememberPagerState(
        initialPage = startPosition.coerceIn(0, mediaList.size - 1),
        pageCount = { mediaList.size }
    )

    
    var currentPage by remember { mutableStateOf(pagerState.currentPage) }
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
        dragOffset = 0f 
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(1f - abs(dragOffset)) 
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val media = mediaList[page]
            val density = LocalDensity.current
            val screenHeightPx = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { 
                        val yPx = (dragOffset * screenHeightPx).roundToInt()
                        IntOffset(0, yPx)
                    }
                    .pointerInput(page) {
                        var velocityY = 0f
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                val dragAbs = abs(dragOffset)
                                val velocityAbs = abs(velocityY)
                                if (dragAbs <= 0.4f && velocityAbs <= 2000f) {
                                    dragOffset = 0f
                                } else {
                                    onClose()
                                }
                                isDragging = false
                                velocityY = 0f
                            },
                            onDragCancel = { isDragging = false; dragOffset = 0f },
                            onDrag = { change, dragAmount ->
                                val containerSize = this.size.height
                                if (containerSize > 0) {
                                    val newOffset = dragOffset + dragAmount.y / containerSize
                                    dragOffset = newOffset.coerceIn(-1f, 1f)
                                }
                                velocityY = dragAmount.y
                                change.consume()
                            }
                        )
                    }
                    .clickable { onClose() }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(media.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        
        if (mediaList.size > 1) {
            Text(
                text = "${currentPage + 1}/${mediaList.size}",
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color(0x66000000), MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .padding(bottom = 24.dp)
            )
        }

        
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(48.dp)
                .background(Color(0x80000000), MaterialTheme.shapes.small)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}