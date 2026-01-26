package com.example.weibo.ui.discover

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weibo.R
import com.example.weibo.core.ui.components.SystemBarsConfig
import com.example.weibo.core.ui.components.TopBarBackground
import com.example.weibo.core.ui.components.systemBarsConfigForTopBar
import com.example.weibo.viewmodel.HotSearchDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HotSearchScreen(
    onBack: () -> Unit,
    onSystemBarsConfigChange: (SystemBarsConfig) -> Unit,
    viewModel: HotSearchDetailViewModel = hiltViewModel(),
    discoverViewModel: com.example.weibo.viewmodel.DiscoverViewModel = hiltViewModel()
) {
    val discoverHotList by discoverViewModel.baiduHotSearch.collectAsStateWithLifecycle()
    val detailHotList by viewModel.hotSearchList.collectAsStateWithLifecycle()
    val hotSearchList = remember(discoverHotList, detailHotList) {
        val sourceList = if (discoverHotList.isNotEmpty()) discoverHotList else detailHotList
        sourceList.take(10)
    }

    LaunchedEffect(Unit) {
        if (discoverHotList.isEmpty()) discoverViewModel.loadBaiduHotSearch()
        if (detailHotList.isEmpty()) viewModel.loadHotSearchData()
    }

    val tabs = remember { listOf("我的", "热搜", "文娱", "生活", "社会", "同城") }
    val pagerState = rememberPagerState(initialPage = 1) { tabs.size }
    val scope = rememberCoroutineScope()

    val outerListState = rememberLazyListState()

    val density = LocalDensity.current
    val statusBarTopDp = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    val collapsingHeightDp = 240.dp
    val topAppBarHeightDp = 56.dp
    val tabRowHeightDp = 48.dp

    val collapseRangePx = with(density) { (collapsingHeightDp - topAppBarHeightDp).toPx() }

    val currentScrollPx = if (outerListState.firstVisibleItemIndex == 0) {
        outerListState.firstVisibleItemScrollOffset.toFloat().coerceIn(0f, collapseRangePx)
    } else {
        collapseRangePx
    }

    val fraction = if (collapseRangePx == 0f) 0f else (currentScrollPx / collapseRangePx).coerceIn(0f, 1f)
    val bgProgress = (1.15f * fraction).coerceIn(0f, 1f)
    val titleTextAlpha = (fraction * fraction * fraction).coerceIn(0f, 1f)

    val bgColor = remember(bgProgress) { blendColor(Color.Transparent, Color.White, bgProgress) }
    val iconColor = remember(bgProgress) { blendColor(Color.White, Color.Black, bgProgress) }

    val isPinned = fraction > 0.98f

    LaunchedEffect(bgColor, isPinned) {
        onSystemBarsConfigChange(
            systemBarsConfigForTopBar(
                topBarBackground = TopBarBackground.Solid(bgColor)
            ).copy(
                statusBarDarkIcons = isPinned,
                statusBarColor = bgColor
            )
        )
    }

    val nestedScrollConnection = remember(outerListState, collapseRangePx, scope) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Drag) return Offset.Zero
                val dy = available.y

                if (dy < 0f) {
                    val outerCollapsed = (outerListState.firstVisibleItemIndex > 0) ||
                        (outerListState.firstVisibleItemScrollOffset.toFloat() >= collapseRangePx)
                    if (!outerCollapsed) {
                        val remaining = collapseRangePx - outerListState.firstVisibleItemScrollOffset.toFloat()
                        val consumeY = kotlin.math.max(dy, -remaining)
                        scope.launch { outerListState.scrollBy(-consumeY) }
                        return Offset(0f, consumeY)
                    }
                }

                if (dy > 0f) {
                    val canExpandOuter = outerListState.firstVisibleItemIndex == 0 && outerListState.firstVisibleItemScrollOffset > 0
                    if (canExpandOuter) {
                        val consumeY = kotlin.math.min(dy, outerListState.firstVisibleItemScrollOffset.toFloat())
                        scope.launch { outerListState.scrollBy(-consumeY) }
                        return Offset(0f, consumeY)
                    }
                }

                return Offset.Zero
            }
        }
    }

    val appBarTranslationYPx = -currentScrollPx

    
    val tabRowYDp = with(density) {
        val yPx = collapsingHeightDp.toPx() - currentScrollPx
        val minY = (topAppBarHeightDp + statusBarTopDp).toPx()
        kotlin.math.max(yPx, minY).toDp()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = outerListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp)
        ) {
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(collapsingHeightDp)
                )
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight()
                        .nestedScroll(nestedScrollConnection)
                ) { page ->
                    val innerListState = rememberLazyListState()
                    LazyColumn(
                        state = innerListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        
                        contentPadding = PaddingValues(top = tabRowHeightDp)
                    ) {
                        itemsIndexed(
                            hotSearchList,
                            key = { _, item -> "${page}_${item.index}_${item.title}" }
                        ) { index, item ->
                            HotSearchListRow(
                                rank = index + 1,
                                title = item.title,
                                tag = item.hot,
                                onClick = {  }
                            )
                        }
                    }
                }
            }
        }

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(collapsingHeightDp)
                .graphicsLayer { translationY = appBarTranslationYPx }
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_weibo_hot),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = currentScrollPx * 0.3f },
                contentScale = ContentScale.Crop
            )
        }

        
        TopAppBar(
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "微博热搜",
                        color = iconColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(titleTextAlpha)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = iconColor
                    )
                }
            },
            actions = {
                IconButton(onClick = {  }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = iconColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = bgColor,
                scrolledContainerColor = bgColor
            ),
            scrollBehavior = null,
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
        )

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = tabRowYDp)
                
                
                .background(bgColor)
        ) {
            TabRow(
                tabs = tabs,
                selectedIndex = pagerState.currentPage,
                bgColor = bgColor,
                onSelect = { idx -> scope.launch { pagerState.animateScrollToPage(idx) } }
            )
        }
    }
}

@Composable
private fun TabRow(
    tabs: List<String>,
    selectedIndex: Int,
    bgColor: Color,
    onSelect: (Int) -> Unit
) {
    
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        edgePadding = 0.dp,

        indicator = { tabPositions ->
            if (tabPositions.isNotEmpty() && selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 2.dp,
                    color = Color(0xFFFF8615)
                )
            }
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                text = {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = if (selectedIndex == index) Color(0xFFFF8615) else Color(0xFF666666),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            )
        }
    }
}

@Composable
private fun HotSearchListRow(
    rank: Int,
    title: String,
    tag: String?,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val circleColor = when (rank) {
            1 -> Color(0xFFFF3B30)
            2 -> Color(0xFFFF9500)
            3 -> Color(0xFFFFCC00)
            4, 5 -> Color(0xFFFFE5CC)
            else -> Color(0xFFFFF4CC)
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) Color.White else Color(0xFF999999)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = Color(0xFF222222),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (!tag.isNullOrBlank()) {
            Spacer(modifier = Modifier.size(12.dp))
            val tagBg = when (tag) {
                "热" -> Color(0xFFFF4D4F)
                "新" -> Color(0xFFFF66B2)
                "荐" -> Color(0xFFFF8615)
                else -> Color(0xFFCCCCCC)
            }
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .background(tagBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tag,
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun blendColor(from: Color, to: Color, ratio: Float): Color {
    val inverseRatio = 1f - ratio
    val a = (from.alpha * inverseRatio + to.alpha * ratio).coerceIn(0f, 1f)
    val r = (from.red * inverseRatio + to.red * ratio).coerceIn(0f, 1f)
    val g = (from.green * inverseRatio + to.green * ratio).coerceIn(0f, 1f)
    val b = (from.blue * inverseRatio + to.blue * ratio).coerceIn(0f, 1f)
    return Color(red = r, green = g, blue = b, alpha = a)
}
