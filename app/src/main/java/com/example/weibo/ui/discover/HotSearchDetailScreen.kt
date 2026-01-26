package com.example.weibo.ui.discover

import android.content.Intent
import androidx.compose.ui.graphics.Color
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weibo.ui.discover.model.BaiduHotSearchItem
import com.example.weibo.viewmodel.HotSearchDetailViewModel
import kotlin.math.abs
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HotSearchDetailScreen(
    onBack: () -> Unit,
    viewModel: HotSearchDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val hotSearchList by viewModel.hotSearchList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    
    val tabTitles = remember {
        listOf("我的", "热搜", "文娱", "生活", "社会", "同城")
    }
    
    val pagerState = rememberPagerState(
        initialPage = tabTitles.indexOf("热搜").takeIf { it >= 0 } ?: 0
    ) { tabTitles.size }
    
    
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    var totalScrollRange by remember { mutableFloatStateOf(1f) }
    
    
    val fraction = if (totalScrollRange == 0f) 0f else (abs(scrollOffset) / totalScrollRange).coerceIn(0f, 1f)
    val bgProgress = (1.15f * fraction).coerceIn(0f, 1f)
    val titleTextAlpha = (fraction * fraction * fraction).coerceIn(0f, 1f)
    
    
    val bgColor = remember(bgProgress) {
        blendColor(Color.Transparent, Color.White, bgProgress)
    }
    val iconColor = remember(bgProgress) {
        blendColor(Color.White, Color.Black, bgProgress)
    }
    
    
    val isPinned = fraction > 0.98f
    val statusBarColor = if (isPinned) Color.White else Color.Transparent
    
    
    LaunchedEffect(Unit) {
        viewModel.loadHotSearchData()
    }
    
    
    SideEffect {
        val window = (context as? android.app.Activity)?.window
        window?.statusBarColor = statusBarColor.toArgb()
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(
            window!!,
            window.decorView
        )
        insetsController.isAppearanceLightStatusBars = isPinned
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            
            
            
            TopAppBar(
                title = {
                    Text(
                        text = "微博热搜",
                        color = iconColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(titleTextAlpha)
                    )
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
                    containerColor = bgColor.copy(alpha = bgProgress)
                ),
                modifier = Modifier.alpha(bgProgress)
            )
            
            
            val scope = rememberCoroutineScope()

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = bgColor.copy(alpha = bgProgress),
                contentColor = MaterialTheme.colorScheme.onSurface,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty() && pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            height = 2.dp,
                            color = Color(0xFFFF8615) 
                        )
                    }
                },
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            
                            scope.launch {
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
            
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                
                HotSearchTabContentScreen(
                    tabTitle = tabTitles[page],
                    hotSearchList = hotSearchList,
                    onItemClick = { item ->
                        
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                        context.startActivity(intent)
                    },
                    onScrollOffsetChanged = { offset, total ->
                        if (page == pagerState.currentPage) {
                            scrollOffset = offset
                            totalScrollRange = total
                        }
                    }
                )
            }
        }
    }
}


@Composable
private fun HotSearchTabContentScreen(
    tabTitle: String,
    hotSearchList: List<BaiduHotSearchItem>,
    onItemClick: (BaiduHotSearchItem) -> Unit,
    onScrollOffsetChanged: (Float, Float) -> Unit
) {
    
    val listState = rememberLazyListState()
    
    
    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        val offset = if (listState.firstVisibleItemIndex == 0) {
            listState.firstVisibleItemScrollOffset.toFloat()
        } else {
            
            240f
        }
        
        val total = 240f
        onScrollOffsetChanged(offset, total)
    }
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(
            count = hotSearchList.size,
            key = { index -> hotSearchList[index].index }
        ) { index ->
            val item = hotSearchList[index]
            HotSearchDetailItem(
                item = item,
                rank = index + 1,
                onClick = { onItemClick(item) }
            )
        }
    }
}


@Composable
private fun HotSearchDetailItem(
    item: BaiduHotSearchItem,
    rank: Int,
    onClick: () -> Unit
) {
    
    val rankBgColor = when (rank) {
        1 -> Color(0xFFFF0000) 
        2 -> Color(0xFFFF6600) 
        3 -> Color(0xFFFFCC00) 
        4, 5 -> Color(0xFFFFE5CC) 
        6, 7, 8 -> Color(0xFFFFF4CC) 
        else -> Color(0xFFFFD700) 
    }
    
    
    val tagBgColor = when (item.hot) {
        "热" -> Color(0xFFFF0000) 
        "新" -> Color(0xFFFF69B4) 
        "回应" -> Color(0xFFCCCCCC) 
        else -> Color(0xFFCCCCCC) 
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    rankBgColor,
                    androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                
                
                if (!item.hot.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                tagBgColor,
                                androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.hot,
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            
            if (!item.subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
    
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color(0xFFE5E5E5),
        thickness = 0.5.dp
    )
}


private fun blendColor(from: Color, to: Color, ratio: Float): Color {
    val inverseRatio = 1f - ratio
    val a = (from.alpha * inverseRatio + to.alpha * ratio).coerceIn(0f, 1f)
    val r = (from.red * inverseRatio + to.red * ratio).coerceIn(0f, 1f)
    val g = (from.green * inverseRatio + to.green * ratio).coerceIn(0f, 1f)
    val b = (from.blue * inverseRatio + to.blue * ratio).coerceIn(0f, 1f)
    return Color(r, g, b, a)
}

