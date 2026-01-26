package com.example.weibo.ui.message

import com.example.weibo.core.ui.components.TopBarContainer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weibo.R
import kotlinx.coroutines.launch


data class DynamicItem(
    val id: Int,
    val username: String,
    val content: String,
    val time: String,
    val hasImage: Boolean = false,
    val hasVideo: Boolean = false,
    val videoDuration: String = ""
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MessageScreen(
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    TopBarContainer(
        topBar = {
            Column {
                MessageTopBar(
                    modifier = Modifier,
                    onSettingsClick = onNavigateToSettings
                )
                MessageTabBar(
                    selectedIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        selectedTabIndex = index
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
                HorizontalDivider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp
                )
            }
        },
        content = {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> DynamicsScreen()
                    1 -> MessageListScreen()
                }
            }
        }
    )
}

@Composable
private fun MessageTopBar(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "消息",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_message),
                    contentDescription = "设置",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun MessageTabBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MessageTab(
                text = "动态",
                isSelected = selectedIndex == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            MessageTab(
                text = "消息",
                isSelected = selectedIndex == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MessageTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var textWidth by remember { mutableStateOf(0.dp) }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFFF6600) else Color(0xFF666666),
            modifier = Modifier
                .height(48.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            onTextLayout = { layoutResult: TextLayoutResult ->
                textWidth = with(density) { layoutResult.size.width.toDp() }
            },
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (textWidth > 0.dp) textWidth else 1.dp)
                .background(
                    if (isSelected) Color(0xFFFF6600) else Color.Transparent
                )
        )
    }
}

@Composable
fun DynamicsScreen(
    modifier: Modifier = Modifier
) {
    val dynamics = remember {
        listOf(
            DynamicItem(1, "多小鹿CAT", content = "谢谢大家来看…", time = "昨天"),
            DynamicItem(2, "王主要User", content = "开发者：为宝软件有限公司", time = "8-31", hasImage = true),
            DynamicItem(3, "央视新闻", content = "#九三…", time = "9-3", hasImage = true),
            DynamicItem(4, "闲白精彩推荐", content = "Video post", time = "8-31", hasVideo = true, videoDuration = "00:54")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(dynamics.size) { index ->
            DynamicItem(
                item = dynamics[index],
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MessageListScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MessageSearchBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            MessageListItem(
                iconRes = R.drawable.ic_at,
                iconBgColor = Color(0xFF1E88E5),
                title = "@我的",
                onClick = {  }
            )

            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 68.dp)
            )

            MessageListItem(
                iconRes = R.drawable.timeline_icon_comment,
                iconBgColor = Color(0xFF4CAF50),
                title = "评论",
                onClick = {  }
            )

            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 68.dp)
            )

            MessageListItem(
                iconRes = R.drawable.ic_like,
                iconBgColor = Color(0xFFFF9800),
                title = "赞",
                onClick = {  }
            )

            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 68.dp)
            )

            MessageListItemWithSubtitle(
                iconRes = R.drawable.timeline_icon_comment,
                iconBgColor = Color(0xFFE74C3C),
                title = "留言板",
                subtitle = "新手指南: http://t.cn/Ai3l7DVVu",
                time = "22-6-5",
                onClick = {  }
            )

            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 68.dp)
            )
        }
    }
}

@Composable
private fun MessageSearchBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF999999)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "搜索聊天记录、群号",
                fontSize = 14.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun MessageListItem(
    iconRes: Int,
    iconBgColor: Color,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun MessageListItemWithSubtitle(
    iconRes: Int,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, shape = androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            Text(
                text = time,
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun DynamicItem(
    item: DynamicItem,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF45B7D1),
        Color(0xFF96CEB4),
        Color(0xFFFFEAA7),
        Color(0xFFDDA0DD)
    )
    val avatarColor = colors[(item.id % colors.size).toInt()]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(avatarColor, shape = androidx.compose.foundation.shape.CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = item.time,
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }

                IconButton(
                    onClick = {  },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_more),
                        contentDescription = "更多",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF999999)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.content,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )

            if (item.hasImage) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF0F0F0))
                ) {
                    Text(
                        text = "图片",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
            }

            if (item.hasVideo) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF0F0F0))
                        .clickable {  }
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    Color(0x80000000),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_play),
                                contentDescription = "播放",
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.videoDuration,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0x80000000))
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {  },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_comment_small),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "评论",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {  },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_repost),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "转发",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {  },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_like_small),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "赞",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}
