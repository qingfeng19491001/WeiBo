package com.example.weibo.ui.video

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.weibo.R
import com.example.weibo.database.entity.VideoBean
import com.example.weibo.util.TimeUtils
import com.example.weibo.util.VideoUrlResolver
import com.example.weibo.video.player.ExoVideoPlayerManager
import com.example.weibo.video.player.ExoVideoPlayerView
import com.example.weibo.viewmodel.VideoViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VideoScreen(
    viewModel: VideoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val recommendVideos by viewModel.recommendVideoList.collectAsStateWithLifecycle()
    val featuredVideos by viewModel.featuredVideoList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = 0) { 2 }

    LaunchedEffect(selectedTab) {
        val targetPage = when (selectedTab) {
            VideoViewModel.TabType.RECOMMEND -> 0
            VideoViewModel.TabType.FEATURED -> 1
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val targetTab = when (pagerState.currentPage) {
            0 -> VideoViewModel.TabType.RECOMMEND
            1 -> VideoViewModel.TabType.FEATURED
            else -> VideoViewModel.TabType.RECOMMEND
        }
        if (selectedTab != targetTab) {
            viewModel.selectTab(targetTab)
        }
    }

    val topBarBg = if (selectedTab == VideoViewModel.TabType.RECOMMEND) Color.Black else Color.White

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VideoTabBar(
            selectedTab = selectedTab,
            backgroundColor = topBarBg,
            onTabSelected = { tab ->
                viewModel.selectTab(tab)
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> VideoRecommendScreen(
                    videos = recommendVideos,
                    isLoading = isLoading,
                    error = error,
                    onRefresh = { viewModel.refreshRecommend() },
                    onVideoClick = { position ->
                        viewModel.requestPlayPosition(position)
                    },
                    onLikeClick = { position ->
                        viewModel.toggleLike(position)
                    },
                    onCommentClick = { _ -> },
                    onShareClick = { _ -> },
                    onLoadMore = {
                        viewModel.loadMore()
                    },
                    viewModel = viewModel
                )

                1 -> VideoFeaturedScreen(
                    videos = featuredVideos,
                    isLoading = isLoading,
                    error = error,
                    onRefresh = { viewModel.refreshFeatured() },
                    onVideoClick = { position, video ->
                        val recommendList = recommendVideos
                        val recommendPosition = recommendList.indexOfFirst { it.resolvedBvid() == video.resolvedBvid() }
                        if (recommendPosition >= 0) {
                            viewModel.selectTab(VideoViewModel.TabType.RECOMMEND)
                            viewModel.requestPlayPosition(recommendPosition)
                        } else {
                            viewModel.selectTab(VideoViewModel.TabType.RECOMMEND)
                            viewModel.requestPlayPosition(0)
                        }
                    },
                    onLoadMore = {
                        viewModel.loadMore()
                    }
                )
            }
        }
    }
}

@Composable
private fun VideoTabBar(
    selectedTab: VideoViewModel.TabType,
    backgroundColor: Color,
    onTabSelected: (VideoViewModel.TabType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VideoTab(
                text = "推荐",
                isSelected = selectedTab == VideoViewModel.TabType.RECOMMEND,
                isDarkBackground = backgroundColor == Color.Black,
                onClick = { onTabSelected(VideoViewModel.TabType.RECOMMEND) },
                modifier = Modifier.weight(1f)
            )

            VideoTab(
                text = "精选",
                isSelected = selectedTab == VideoViewModel.TabType.FEATURED,
                isDarkBackground = backgroundColor == Color.Black,
                onClick = { onTabSelected(VideoViewModel.TabType.FEATURED) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VideoTab(
    text: String,
    isSelected: Boolean,
    isDarkBackground: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = if (isDarkBackground) Color.White else Color(0xFFFF8200)
    val unselectedColor = if (isDarkBackground) Color(0xFFB0B0B0) else Color(0xFF666666)

    val textColor = if (isSelected) selectedColor else unselectedColor
    val indicatorColor = if (isSelected) selectedColor else Color.Transparent

    var textWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = if (isSelected) 18.sp else 16.sp,
            fontWeight = fontWeight,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp),
            onTextLayout = { layoutResult: TextLayoutResult ->
                textWidth = with(density) { layoutResult.size.width.toDp() }
            }
        )

        Box(
            modifier = Modifier
                .then(
                    if (textWidth > 0.dp) {
                        Modifier.width(textWidth)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
                .height(3.dp)
                .background(indicatorColor)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoRecommendScreen(
    videos: List<VideoBean>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onVideoClick: (Int) -> Unit,
    onLikeClick: (Int) -> Unit,
    onCommentClick: (Int) -> Unit,
    onShareClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val playPosition by viewModel.playPosition.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videos.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "暂无视频",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        } else if (videos.isNotEmpty()) {
            var didPreResolveFirst by remember { mutableStateOf(false) }
            LaunchedEffect(videos.size) {
                if (!didPreResolveFirst && videos.isNotEmpty()) {
                    didPreResolveFirst = true
                    viewModel.preResolveAround(0, radius = 1)
                }
            }

            val verticalPagerState = rememberPagerState(initialPage = 0) { videos.size }

            LaunchedEffect(verticalPagerState.currentPage) {
                val currentPage = verticalPagerState.currentPage
                if (currentPage in videos.indices) {
                    viewModel.preResolveAround(currentPage, radius = 1)
                }
            }

            var lastSettledPage by remember { mutableIntStateOf(0) }
            var preloadJob by remember { mutableStateOf<Job?>(null) }
            LaunchedEffect(verticalPagerState.isScrollInProgress) {
                if (!verticalPagerState.isScrollInProgress) {
                    val currentPage = verticalPagerState.currentPage
                    val direction = currentPage - lastSettledPage
                    lastSettledPage = currentPage

                    val targetIndex = when {
                        direction > 0 -> currentPage + 1
                        direction < 0 -> currentPage - 1
                        else -> currentPage + 1
                    }

                    if (targetIndex in videos.indices) {
                        preloadJob?.cancel()
                        preloadJob = launch {
                            viewModel.preResolveAround(targetIndex, radius = 0)

                            val targetVideo = videos[targetIndex]
                            val tag = "douyin_video_${targetVideo.resolvedBvid()}"
                            val start = System.currentTimeMillis()
                            var resolved: String? = null
                            while (isActive && resolved == null && System.currentTimeMillis() - start < 2000) {
                                resolved = viewModel.getResolvedUrlFromCache(targetVideo)
                                if (resolved == null) {
                                    delay(50)
                                }
                            }

                            if (!resolved.isNullOrBlank()) {
                                ExoVideoPlayerManager.getInstance(context).preloadVideo(resolved!!, tag)
                            }
                        }
                    }
                }
            }

            var pausedByScroll by remember { mutableStateOf(false) }

            LaunchedEffect(verticalPagerState.isScrollInProgress) {
                if (verticalPagerState.isScrollInProgress) {
                    pausedByScroll = true
                    viewModel.clearPlayPosition()
                } else {
                    pausedByScroll = false
                }
            }

            VerticalPager(
                state = verticalPagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 0.dp
            ) { page ->
                VideoRecommendItem(
                    video = videos[page],
                    position = page,
                    isCurrentPage = page == verticalPagerState.currentPage && !pausedByScroll,
                    pagerState = verticalPagerState,
                    modifier = Modifier.fillMaxSize(),
                    onLikeClick = { onLikeClick(page) },
                    onCommentClick = { onCommentClick(page) },
                    onShareClick = { onShareClick(page) },
                    viewModel = viewModel,
                    onLoadMore = onLoadMore
                )
            }

            LaunchedEffect(verticalPagerState.currentPage) {
                if (verticalPagerState.currentPage >= videos.size - 2) {
                    onLoadMore()
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoRecommendItem(
    video: VideoBean,
    position: Int,
    isCurrentPage: Boolean,
    pagerState: androidx.compose.foundation.pager.PagerState,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    viewModel: VideoViewModel,
    onLoadMore: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var playerView by remember { mutableStateOf<ExoVideoPlayerView?>(null) }

    LaunchedEffect(isCurrentPage, playerView) {
        if (!isCurrentPage) {
            playerView?.onPlaybackEnded = null
            playerView?.pausePlay()
        }
    }

    var resolvedVideoUrl by remember { mutableStateOf<String?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    var showPlayPauseIcon by remember { mutableStateOf(false) }

    val likeAnimationScale = remember { Animatable(0.5f) }
    val likeAnimationAlpha = remember { Animatable(0f) }
    var showLikeAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(video.videoUrl, isCurrentPage) {
        if (isCurrentPage && resolvedVideoUrl == null && video.videoUrl.isNotEmpty()) {
            resolvedVideoUrl = viewModel.getResolvedUrlFromCache(video) ?: VideoUrlResolver.resolvePlayUrl(video.videoUrl)
        }
    }

    LaunchedEffect(isCurrentPage, resolvedVideoUrl, playerView) {
        if (isCurrentPage && resolvedVideoUrl != null && playerView != null) {
            playerView?.let { view ->
                val viewTag = "douyin_video_${video.resolvedBvid()}"
                view.setVideoData(resolvedVideoUrl!!, video.coverUrl, viewTag, true)
                view.startPlay()

                view.onPlaybackEnded = fun() {
                    if (!isCurrentPage) {
                        return
                    }
                    val currentTag = ExoVideoPlayerManager.getInstance(context).getCurrentPlayingTag()
                    if (currentTag != viewTag) {
                        return
                    }

                    val currentVideos = viewModel.recommendVideoList.value
                    val nextPosition = position + 1
                    if (nextPosition in currentVideos.indices) {
                        scope.launch {
                            delay(100)
                            pagerState.animateScrollToPage(nextPosition)
                        }
                    } else {
                        onLoadMore()
                    }
                }
            }
        } else if (!isCurrentPage && playerView != null) {
            playerView?.pausePlay()
        }
    }

    LaunchedEffect(showLikeAnimation) {
        if (showLikeAnimation) {
            likeAnimationScale.snapTo(0.5f)
            likeAnimationAlpha.snapTo(1f)
            likeAnimationScale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
            likeAnimationScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
            likeAnimationAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
            delay(400)
            showLikeAnimation = false
        }
    }

    LaunchedEffect(showPlayPauseIcon) {
        if (showPlayPauseIcon) {
            delay(500)
            showPlayPauseIcon = false
        }
    }

    val likeButtonScale = remember { Animatable(1f) }
    var burstToken by remember { mutableIntStateOf(0) }

    var overlayBurstToken by remember { mutableIntStateOf(0) }
    var lastDoubleTapOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var lastDoubleTapAtMs by remember { mutableStateOf(0L) }

    fun triggerLikeAnimations() {
        scope.launch {
            likeButtonScale.stop()
            likeButtonScale.snapTo(0.85f)
            likeButtonScale.animateTo(1.25f, tween(120, easing = FastOutSlowInEasing))
            likeButtonScale.animateTo(1f, tween(160, easing = FastOutSlowInEasing))
        }
        burstToken += 1
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            
    ) {
        AndroidView(
            factory = { ctx ->
                ExoVideoPlayerView(ctx).apply {
                    playerView = this
                    bindLifecycle(lifecycleOwner)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            lastDoubleTapAtMs = System.currentTimeMillis()
                            lastDoubleTapOffset = offset
                            overlayBurstToken += 1
                            showLikeAnimation = true

                            triggerLikeAnimations()

                            if (!video.isLiked) {
                                viewModel.like(position)
                            }
                        },
                        onTap = {
                            val tapAt = System.currentTimeMillis()
                            scope.launch {
                                delay(1000)
                                if (lastDoubleTapAtMs < tapAt) {
                                    playerView?.let { view ->
                                        view.togglePlayPause()
                                        showPlayPauseIcon = true
                                        view.showControlBarWithAutoHide()
                                    }
                                }
                            }
                        }
                    )
                }
        )

        val density = LocalDensity.current
        val burstStartPx = with(density) { 28.dp.toPx() }
        val burstEndPx = with(density) { 180.dp.toPx() }
        val likeHalfSizePx = with(density) { 60.dp.toPx() }

        LikeBurstOverlay(
            play = overlayBurstToken,
            center = lastDoubleTapOffset,
            modifier = Modifier.fillMaxSize(),
            startRadiusPx = burstStartPx,
            endRadiusPx = burstEndPx
        )

        if (showLikeAnimation) {
            Icon(
                painter = painterResource(id = R.drawable.ic_like),
                contentDescription = null,
                modifier = Modifier
                    .offset(
                        x = with(density) { (lastDoubleTapOffset.x - likeHalfSizePx).toDp() },
                        y = with(density) { (lastDoubleTapOffset.y - likeHalfSizePx).toDp() }
                    )
                    .size(120.dp)
                    .scale(likeAnimationScale.value)
                    .alpha(likeAnimationAlpha.value),
                tint = Color(0xFFFF8200)
            )
        }

        var isPausedState by remember { mutableStateOf(false) }

        LaunchedEffect(playerView, isCurrentPage) {
            if (playerView != null && isCurrentPage) {
                while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                    val isPlaying = playerView?.isPlaying() ?: false
                    isPausedState = !isPlaying
                    delay(100)
                }
            }
        }

        if (showPlayPauseIcon) {
            Icon(
                painter = painterResource(
                    id = if (isPausedState) R.drawable.ic_play else R.drawable.ic_pause
                ),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                tint = Color.White
            )
        }

        var showMuteButton by remember { mutableStateOf(false) }
        LaunchedEffect(showMuteButton) {
            if (showMuteButton) {
                delay(2000)
                showMuteButton = false
            }
        }

        if (showMuteButton) {
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    showMuteButton = true
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_up
                    ),
                    contentDescription = "静音",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    LikeBurstEffect(
                        play = burstToken,
                        modifier = Modifier
                            .matchParentSize()
                    )

                    IconButton(
                        onClick = {
                            if (video.isLiked) {
                                viewModel.unlike(position)
                            } else {
                                triggerLikeAnimations()
                                viewModel.like(position)
                            }
                        },
                        modifier = Modifier
                            .matchParentSize()
                            .scale(likeButtonScale.value)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timeline_icon_unlike),
                            contentDescription = "点赞",
                            modifier = Modifier.size(24.dp),
                            tint = if (video.isLiked) Color(0xFFFF8200) else Color.White
                        )
                    }
                }

                Text(
                    text = TimeUtils.formatCount(video.likeCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.timeline_icon_comment),
                        contentDescription = "评论",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                Text(
                    text = TimeUtils.formatCount(video.commentCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.timeline_icon_redirect),
                        contentDescription = "分享",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                Text(
                    text = TimeUtils.formatCount(video.shareCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                AsyncImage(
                    model = video.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.avatar_placeholder)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "@${video.username}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = video.description,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (video.musicName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_music),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = video.musicName,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VideoFeaturedScreen(
    videos: List<VideoBean>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onVideoClick: (Int, VideoBean) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (videos.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "暂无视频",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(videos) { index, video ->
                    VideoFeaturedItem(
                        video = video,
                        onClick = { onVideoClick(index, video) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    LaunchedEffect(Unit) {
                        if (videos.isNotEmpty()) {
                            onLoadMore()
                        }
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun VideoFeaturedItem(
    video: VideoBean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = video.coverUrl,
                    contentDescription = "视频封面",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (video.durationSec > 0) {
                    Text(
                        text = TimeUtils.formatDuration(video.durationSec),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color(0x80000000))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .padding(top = 4.dp, end = 4.dp)
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = "播放",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                        .background(
                            color = Color(0xCC000000),
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Text(
                    text = video.description.ifEmpty { "视频标题" },
                    color = Color.Black,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    AsyncImage(
                        model = video.avatarUrl,
                        contentDescription = "作者头像",
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = "@${video.username}",
                        color = Color(0xFF666666),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.timeline_icon_unlike),
                            contentDescription = "点赞",
                            tint = if (video.isLiked) Color(0xFFFF8200) else Color(0xFF999999),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = TimeUtils.formatCount(video.likeCount),
                            color = Color(0xFF999999),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
