package com.example.weibo.ui.discover

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weibo.R
import com.example.weibo.core.ui.components.TopBarContainer
import com.example.weibo.ui.discover.model.BaiduHotSearchItem
import com.example.weibo.ui.refresh.ClassicsSwipeRefresh
import com.example.weibo.viewmodel.DiscoverViewModel
import java.util.Locale
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
    onNavigateToHotSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val searchHint by viewModel.searchHint.collectAsStateWithLifecycle()
    val hotSearchList by viewModel.baiduHotSearch.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(initialPage = 0) { 5 }

    val outerListState = rememberLazyListState()

    val density = LocalDensity.current
    val collapsibleContentHeight = remember(hotSearchList.size) {
        val headerHeight = 68.dp
        val gridHeight = if (hotSearchList.isEmpty()) 0.dp else {
            val rows = (hotSearchList.size + 1) / 2
            (rows * 80).dp
        }
        val adHeight = 200.dp
        val padding = 32.dp
        with(density) { (headerHeight + gridHeight + adHeight + padding).toPx() }
    }

    LaunchedEffect(selectedTab) {
        val targetPage = when (selectedTab) {
            TabType.HOT -> 0
            TabType.HOT_QUESTION -> 1
            TabType.HOT_FORWARD -> 2
            TabType.PUBLISH -> 3
            TabType.INDEX -> 4
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val targetTab = when (pagerState.currentPage) {
            0 -> TabType.HOT
            1 -> TabType.HOT_QUESTION
            2 -> TabType.HOT_FORWARD
            3 -> TabType.PUBLISH
            4 -> TabType.INDEX
            else -> TabType.HOT
        }
        if (selectedTab != targetTab) {
            viewModel.onTabSelected(targetTab)
        }
    }

    val nestedScrollConnection = remember(outerListState, collapsibleContentHeight) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0) {
                    val currentScroll = outerListState.firstVisibleItemScrollOffset.toFloat()
                    if (currentScroll < collapsibleContentHeight) {
                        val remaining = collapsibleContentHeight - currentScroll
                        val consumed = kotlin.math.max(available.y, -remaining)
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopBarContainer(
            topBar = {
                DiscoverSearchBar(
                    modifier = Modifier,
                    hint = when {
                        searchHint.isNullOrBlank() -> "大家正在搜：热门内容"
                        searchHint!!.startsWith("大家正在搜：") -> searchHint!!
                        else -> "大家正在搜：" + searchHint
                    },
                    onSearchClick = { query ->
                        if (query.isNotBlank()) {
                            viewModel.onSearchClicked(query)
                        }
                    }
                )
            },
            content = {
                ClassicsSwipeRefresh(
                    isRefreshing = isLoading,
                    onRefresh = {
                        if (!isLoading) {
                            viewModel.refresh()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    enabled = outerListState.firstVisibleItemIndex == 0 && outerListState.firstVisibleItemScrollOffset == 0,
                    headerHeight = 56,
                    backgroundColor = Color.White,
                    contentColor = Color(0xFFFF6600),
                    textColor = Color(0xFF666666),
                    showShadow = false
                ) {
                    LazyColumn(
                        state = outerListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                HotSearchHeader(onMoreHotClick = onNavigateToHotSearch)

                                if (hotSearchList.isNotEmpty()) {
                                    val displayItems = remember(hotSearchList) { hotSearchList.take(10) }
                                    val gridRows = (displayItems.size + 1) / 2
                                    val gridHeight = (gridRows * 42).dp 
                                    HotSearchGrid(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(gridHeight)
                                            .padding(horizontal = 16.dp).padding(bottom = 12.dp),
                                        items = displayItems,
                                        onItemClick = { onNavigateToHotSearch() }
                                    )
                                }

                                DiscoverAdCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                )
                            }
                        }

                        stickyHeader {
                            TabRow(
                                selectedTabIndex = pagerState.currentPage,
                                indicator = { tabPositions ->
                                    if (tabPositions.isNotEmpty() && pagerState.currentPage < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                            height = 2.dp,
                                            color = Color(0xFFFF8615)
                                        )
                                    } else {
                                        Box {}
                                    }
                                },
                                containerColor = Color.White,
                                contentColor = Color(0xFF666666),
                                divider = {},
                                modifier = Modifier.height(48.dp)
                            ) {
                                val tabs = listOf("热点", "热问", "热转", "发布", "指数")
                                val coroutineScope = rememberCoroutineScope()
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },

                                        text = {
                                            Text(
                                                text = title,
                                                fontSize = 14.sp,
                                                color = if (pagerState.currentPage == index) {
                                                    Color(0xFFFF8615)
                                                } else {
                                                    Color(0xFF666666)
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight) 
                                    .background(Color(0xFFF5F5F5))
                            ) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(nestedScrollConnection)
                                ) { page ->
                                    DiscoverTabContent(
                                        modifier = Modifier,
                                        tabType = when (page) {
                                            0 -> TabType.HOT
                                            1 -> TabType.HOT_QUESTION
                                            2 -> TabType.HOT_FORWARD
                                            3 -> TabType.PUBLISH
                                            4 -> TabType.INDEX
                                            else -> TabType.HOT
                                        },
                                        outerListState = outerListState,
                                        collapsibleContentHeight = collapsibleContentHeight
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = msg,
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun DiscoverSearchBar(
    modifier: Modifier = Modifier,
    hint: String,
    onSearchClick: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(Color.Transparent, RoundedCornerShape(24.dp))
                    .border(2.dp, Color(0xFFFF8615), RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearchClick(searchText) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchText.isBlank()) {
                                Text(
                                    text = hint,
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            
            Text(
                text = "搜索",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFFF6600),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { onSearchClick(searchText) }
            )
        }
    }
}

@Composable
private fun HotSearchHeader(
    onMoreHotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "微博热搜",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "更多热搜",
                fontSize = 14.sp,
                color = Color(0xFFFF8615),
                modifier = Modifier.clickable(onClick = onMoreHotClick)
            )
        }
    }
}

@Composable
private fun HotSearchGrid(
    modifier: Modifier = Modifier,
    items: List<BaiduHotSearchItem>,
    onItemClick: (BaiduHotSearchItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        userScrollEnabled = false
    ) {
        items(
            count = items.size,
            key = { index -> items[index].index }
        ) { index ->
            val item = items[index]
            HotSearchItem(
                modifier = Modifier.fillMaxWidth(),
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun HotSearchItem(
    modifier: Modifier = Modifier,
    item: BaiduHotSearchItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                color = Color(0xFF333333),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!item.hot.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.hot,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            when (item.hot) {
                                "热" -> Color(0xFFFF4D4F)
                                "新" -> Color(0xFFFF8A00)
                                "荐" -> Color(0xFFFF6600)
                                else -> Color(0xFF999999)
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DiscoverAdCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable {  },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.jingdong),
                contentDescription = "京东618手机节",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "广告",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DiscoverTabContent(
    tabType: TabType,
    outerListState: LazyListState,
    collapsibleContentHeight: Float,
    modifier: Modifier = Modifier
) {
    val mockPosts = remember(tabType) {
        buildMockPosts(tabType)
    }

    val innerListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember(outerListState, collapsibleContentHeight) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0) {
                    val currentScroll = outerListState.firstVisibleItemScrollOffset.toFloat()
                    if (currentScroll < collapsibleContentHeight) {
                        val remaining = collapsibleContentHeight - currentScroll
                        val consumed = kotlin.math.max(available.y, -remaining)
                        coroutineScope.launch {
                            outerListState.scrollBy(-consumed)
                        }
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }
        }
    }

    LazyColumn(
        state = innerListState,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(mockPosts) { post ->
            MockWeiboPostItem(
                post = post,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }
    }
}

@Composable
private fun MockWeiboPostItem(
    post: MockWeiboPost,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFE6E6E6), shape = RoundedCornerShape(19.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = post.userSubtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )

            if (post.hasImage) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFF2F2F2), shape = RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${post.time} · ${post.source}",
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = formatCount(post.repostCount, "转发"),
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = formatCount(post.commentCount, "评论"),
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = formatCount(post.likeCount, "赞"),
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

private data class MockWeiboPost(
    val id: String,
    val username: String,
    val userSubtitle: String,
    val content: String,
    val time: String,
    val source: String,
    val hasImage: Boolean = false,
    val repostCount: Int = 0,
    val commentCount: Int = 0,
    val likeCount: Int = 0
)

private fun buildMockPosts(type: TabType): List<MockWeiboPost> {
    val seed = type.ordinal * 1000 + 7
    val r = kotlin.random.Random(seed)

    val (topicPrefix, contentPool) = when (type) {
        TabType.HOT -> "#热点#" to listOf(
            "今天这条消息你怎么看？",
            "一个小细节但信息量很大。",
            "这事我站中立，大家理性讨论。",
            "热搜背后其实是用户情绪的共振。"
        )

        TabType.HOT_QUESTION -> "#热问#" to listOf(
            "求问：这种情况你们会怎么处理？",
            "有没有懂行的朋友科普一下？",
            "第一次遇到，真的很困惑。",
            "想听听大家的经验和建议。"
        )

        TabType.HOT_FORWARD -> "#热转#" to listOf(
            "转给需要的人：",
            "这条太有价值了，建议收藏。",
            "看到这个我立刻想到你。",
            "不转不是中国人（开玩笑）。"
        )

        TabType.PUBLISH -> "#发布#" to listOf(
            "刚发布一条动态，记录一下今天。",
            "这一刻的心情值得被保存。",
            "写点碎碎念：",
            "今日份总结："
        )

        TabType.INDEX -> "#指数#" to listOf(
            "指数变化有点意思，简单分析一下。",
            "数据不会说谎：",
            "趋势很明显了，后面要注意。",
            "给大家一个小图表（脑补）。"
        )
    }

    val users = listOf("阿橙同学", "小魏在路上", "一只认真猫", "数据研究员", "热心市民", "今天也要加油")
    val sources = listOf("iPhone客户端", "Android客户端", "网页")

    return (0 until 10).map { idx ->
        val user = users[(seed + idx) % users.size]
        val hasImage = idx % 3 == 0
        val content = buildString {
            append(topicPrefix)
            append(' ')
            append(contentPool[r.nextInt(contentPool.size)])
            append("\n\n")
            append("（模拟微博内容 ${idx + 1} / 10）")
        }
        MockWeiboPost(
            id = "${type.name}-$idx",
            username = user,
            userSubtitle = "关注 ${r.nextInt(500) + 50} · 粉丝 ${r.nextInt(50000) + 1000}",
            content = content,
            time = "${r.nextInt(59) + 1}分钟前",
            source = sources[r.nextInt(sources.size)],
            hasImage = hasImage,
            repostCount = r.nextInt(5000),
            commentCount = r.nextInt(5000),
            likeCount = r.nextInt(50000)
        )
    }
}

private fun formatHeat(heat: Long): String {
    return when {
        heat >= 10000 -> "${String.format(Locale.getDefault(), "%.1f", heat / 10000.0)}万热度"
        heat >= 1000 -> "${String.format(Locale.getDefault(), "%.1f", heat / 1000.0)}k热度"
        else -> "$heat 热度"
    }
}

private fun formatCount(count: Int, fallback: String): String {
    if (count <= 0) return fallback
    return if (count >= 10000) {
        String.format(Locale.getDefault(), "%.1f万", count / 10000.0)
    } else {
        count.toString()
    }
}
