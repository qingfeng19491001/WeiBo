package com.example.weibo.ui.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.weibo.core.ui.components.TopBarContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.weibo.data.model.Post
import com.example.weibo.model.Channel

import com.example.weibo.ui.home.components.*
import com.example.weibo.ui.home.dialogs.*
import com.example.weibo.ui.home.preview.ImagePreviewScreen
import com.example.weibo.ui.livestream.LiveStreamActivity
import com.example.weibo.viewmodel.MainViewModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTaskCenter: () -> Unit = {},
    onNavigateToWritePost: () -> Unit = {},
    onNavigateToChannelManager: () -> Unit = {},
    channelRefreshTrigger: Int = 0
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    
    val pagerState = rememberPagerState(initialPage = currentTab) { 2 }

    LaunchedEffect(currentTab) {
        if (pagerState.currentPage != currentTab) {
            pagerState.animateScrollToPage(currentTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (currentTab != pagerState.currentPage) {
            viewModel.switchTab(pagerState.currentPage)
        }
    }
    val feedPaging = viewModel.feedPaging.collectAsLazyPagingItems()
    val followPaging = viewModel.followPaging.collectAsLazyPagingItems()

    
    val channels = remember {
        mutableStateListOf<Channel>()
    }

    var selectedChannelIndex by remember { mutableIntStateOf(0) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showImagePreview by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }

    
    LaunchedEffect(channelRefreshTrigger) {
        channels.clear()
        val loaded = loadChannelsFromPrefs(context)
        if (loaded.isEmpty()) {
            channels.addAll(getDefaultChannels())
            saveChannelsToPrefs(context, channels)
        } else {
            channels.addAll(loaded)
        }
        
        if (selectedChannelIndex >= channels.size) {
            selectedChannelIndex = 0
        }
    }

    
    val currentPostContent = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TopBarContainer(
                topBar = {
                    HomeTopBar(
                        selectedTab = currentTab,
                        onTabSelected = { index ->
                            viewModel.switchTab(index)
                        },
                        channels = channels,
                        selectedChannelIndex = selectedChannelIndex,
                        onChannelSelected = { index ->
                            selectedChannelIndex = index
                            
                            if (currentTab == 0) {
                                feedPaging.refresh()
                            }
                        },
                        onMoreChannelsClick = {
                            onNavigateToChannelManager()
                        },
                        onEditClick = {
                            
                        },
                        onHomeClick = {
                            onNavigateToTaskCenter()
                        },
                        onAddClick = {
                            showAddMenu = true
                        }
                    )
                },
                content = {
                    
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> {
                                
                                ChannelPostList(
                                    pagingItems = feedPaging,
                                    viewModel = viewModel,
                                    channelId = channels.getOrNull(selectedChannelIndex)?.id ?: "hot",
                                    onCommentClick = { postId ->
                                        showCommentDialog = postId
                                    },
                                    onShareClick = { postId ->
                                        
                                        val post = getPostFromPaging(feedPaging, postId)
                                        currentPostContent.value = post?.content ?: ""
                                        shareViaSystem(context, currentPostContent.value)
                                    },
                                    onImageClick = { images, index ->
                                        showImagePreview = Pair(images, index)
                                    }
                                )
                            }

                            1 -> {
                                
                                FollowPostList(
                                    pagingItems = followPaging,
                                    viewModel = viewModel,
                                    onCommentClick = { postId ->
                                        showCommentDialog = postId
                                    },
                                    onShareClick = { postId ->
                                        val post = getPostFromPaging(followPaging, postId)
                                        currentPostContent.value = post?.content ?: ""
                                        shareViaSystem(context, currentPostContent.value)
                                    },
                                    onDeleteClick = { postId ->
                                        showDeleteDialog = postId
                                    },
                                    onImageClick = { images, index ->
                                        showImagePreview = Pair(images, index)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    
    if (showAddMenu) {
        AddMenuPopup(
            onDismiss = { showAddMenu = false },
            onWritePost = {
                onNavigateToWritePost()
                showAddMenu = false
            },
            onAlbum = {
                
                showAddMenu = false
            },
            onCheckIn = {
                
                showAddMenu = false
            },
            onLive = {
                
                val intent = Intent(context, LiveStreamActivity::class.java)
                context.startActivity(intent)
                showAddMenu = false
            }
        )
    }

    
    showCommentDialog?.let { postId ->
        CommentDialog(
            postId = postId,
            onDismiss = { showCommentDialog = null },
            onCommentSent = { _ ->
                viewModel.sharePost(postId) 
            }
        )
    }

    
    showDeleteDialog?.let { postId ->
        DeleteDialog(
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deletePost(postId)
            }
        )
    }

    
    showImagePreview?.let { (images, index) ->
        ImagePreviewScreen(
            imageUrls = images,
            currentPosition = index,
            onDismiss = { showImagePreview = null }
        )
    }
}

@Composable
private fun ChannelPostList(
    pagingItems: androidx.paging.compose.LazyPagingItems<Post>,
    viewModel: MainViewModel,
    channelId: String,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit
) {
    val context = LocalContext.current

    
    LaunchedEffect(channelId) {
        when (channelId) {
            "hot" -> {
                
                if (pagingItems.itemCount == 0) {
                    viewModel.refreshHotFromApifox()
                }
            }

            "campus_love" -> {
                android.widget.Toast.makeText(context, "高校情感内容开发中", android.widget.Toast.LENGTH_SHORT).show()
            }

            "realtime" -> {
                android.widget.Toast.makeText(context, "实时内容开发中", android.widget.Toast.LENGTH_SHORT).show()
            }

            "game" -> {
                android.widget.Toast.makeText(context, "游戏内容开发中", android.widget.Toast.LENGTH_SHORT).show()
            }

            "local" -> {
                android.widget.Toast.makeText(context, "同城内容开发中", android.widget.Toast.LENGTH_SHORT).show()
            }

            "stars" -> {
                android.widget.Toast.makeText(context, "明星内容开发中", android.widget.Toast.LENGTH_SHORT).show()
            }

            "video" -> {
                android.widget.Toast.makeText(context, "视频内容开发中", android.widget.Toast.LENGTH_SHORT).show()
            }

            "rankings" -> {
                android.widget.Toast.makeText(context, "榜单内容开发中", android.widget.Toast.LENGTH_SHORT).show()
            }

            else -> {
                
                
            }
        }
    }

    PostList(
        modifier = Modifier,
        pagingItems = pagingItems,
        onLikeClick = { postId ->
            viewModel.likePost(postId)
        },
        onCommentClick = onCommentClick,
        onShareClick = onShareClick,
        onImageClick = onImageClick,
        showDeleteButton = false,
        showUpdateBar = channelId == "hot", 
        onRefresh = {
            if (channelId == "hot") {
                viewModel.refreshHotFromApifox()
            } else {
                viewModel.refreshPosts()
            }
        }
    )
}

@Composable
private fun FollowPostList(
    pagingItems: androidx.paging.compose.LazyPagingItems<Post>,
    viewModel: MainViewModel,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onImageClick: (List<String>, Int) -> Unit
) {
    if (pagingItems.itemCount == 0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "还没有关注的内容，去发布一条吧",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    } else {
        PostList(
            modifier = Modifier,
            pagingItems = pagingItems,
            onLikeClick = { postId ->
                viewModel.likePost(postId)
            },
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            onDeleteClick = onDeleteClick,
            onImageClick = onImageClick,
            showDeleteButton = true,
            showUpdateBar = false,
            onRefresh = {
                viewModel.refreshPosts()
            }
        )
    }
}


private fun getDefaultChannels(): List<Channel> {
    return listOf(
        Channel("hot", "热门", true, true),
        Channel("local", "同城", true, false),
        Channel("realtime", "实时", true, false),
        Channel("rankings", "榜单", true, false),
        Channel("video", "视频", true, false),
        Channel("game", "游戏", true, false),
        Channel("stars", "明星", true, false),
        Channel("funny", "搞笑", true, false),
        Channel("emotion", "情感", true, false),
        Channel("short_drama", "短剧", true, false),
        Channel("impression", "印象", true, false),
        Channel("weekend", "周末", true, false),
        Channel("postgraduate", "考研", true, false),
        Channel("selected", "精选", true, false),
        Channel("movie", "电影", true, false),
        Channel("society", "社会", true, false),
        Channel("tv_series", "电视剧", true, false),
        Channel("food", "美食", true, false),
        Channel("photography", "摄影", true, false),
        Channel("technology", "科技", true, false),
        Channel("anime", "动漫", true, false)
    )
}

private fun getAllChannels(): List<Channel> {
    return listOf(
        Channel("hot", "热门", false, true),
        Channel("local", "同城", false, false),
        Channel("realtime", "实时", false, false),
        Channel("rankings", "榜单", false, false),
        Channel("video", "视频", false, false),
        Channel("game", "游戏", false, false),
        Channel("stars", "明星", false, false),
        Channel("campus_love", "高校情感", false, false),
        Channel("international", "国际", false, false),
        Channel("depth", "深度", false, false),
        Channel("finance", "财经", false, false),
        Channel("reading", "读书", false, false),
        Channel("car", "汽车", false, false),
        Channel("appearance", "颜值", false, false),
        Channel("sports", "体育", false, false),
        Channel("digital", "数码", false, false),
        Channel("variety", "综艺", false, false),
        Channel("fashion", "时尚", false, false),
        Channel("military", "军事", false, false),
        Channel("stock", "股市", false, false),
        Channel("fitness", "运动健身", false, false),
        Channel("travel", "旅游", false, false),
        Channel("goods", "好物", false, false),
        Channel("beauty", "美妆", false, false),
        Channel("law", "法律", false, false),
        Channel("design", "设计", false, false),
        Channel("new_era", "新时代", false, false),
        Channel("campus", "校园", false, false),
        Channel("collection", "收藏", false, false),
        Channel("government", "政务", false, false),
        Channel("parenting", "育儿", false, false),
        Channel("marriage", "婚恋", false, false),
        Channel("dance", "舞蹈", false, false),
        Channel("rumor_refute", "辟谣", false, false),
        Channel("charity", "公益", false, false),
        Channel("agriculture", "三农", false, false)
    )
}

private fun saveChannelsToPrefs(context: Context, channels: List<Channel>) {
    val prefs = context.getSharedPreferences("channel_prefs", Context.MODE_PRIVATE)
    val ids = channels.joinToString(",") { it.id }
    val names = channels.joinToString(",") { it.name }
    prefs.edit()
        .putString("my_channel_ids", ids)
        .putString("my_channel_names", names)
        .apply()
}

private fun loadChannelsFromPrefs(context: Context): List<Channel> {
    val prefs = context.getSharedPreferences("channel_prefs", Context.MODE_PRIVATE)
    val idsStr = prefs.getString("my_channel_ids", null)
    val namesStr = prefs.getString("my_channel_names", null)

    if (idsStr.isNullOrEmpty() || namesStr.isNullOrEmpty()) {
        return emptyList()
    }

    val ids = idsStr.split(",")
    val names = namesStr.split(",")

    if (ids.size != names.size) {
        return emptyList()
    }

    return ids.mapIndexed { index, id ->
        Channel(id, names[index], false, false)
    }
}

private fun getPostFromPaging(
    pagingItems: androidx.paging.compose.LazyPagingItems<Post>,
    postId: String
): Post? {
    for (i in 0 until pagingItems.itemCount) {
        val post = pagingItems[i]
        if (post?.id == postId) {
            return post
        }
    }
    return null
}

private fun shareViaSystem(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享至"))
}
