package com.example.weibo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.zIndex
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.lightColors
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weibo.R
import com.example.weibo.core.ui.components.LottieBottomNavigation
import com.example.weibo.core.ui.components.BottomNavItem
import com.example.weibo.core.ui.components.SetupSystemBars
import com.example.weibo.core.ui.components.SystemBarsConfig
import com.example.weibo.core.ui.components.TopBarBackground
import com.example.weibo.core.ui.components.systemBarsConfigForTopBar
import com.example.weibo.core.ui.components.StatusBarPlaceholder
import com.example.weibo.ui.home.HomeScreen
import com.example.weibo.ui.video.VideoScreen
import com.example.weibo.ui.discover.DiscoverScreen
import com.example.weibo.ui.message.MessageScreen
import com.example.weibo.ui.profile.ProfileScreen
import com.example.weibo.viewmodel.MainViewModel
import com.example.weibo.viewmodel.VideoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MaterialTheme(
                colors = lightColors(
                    primary = Color(0xFFFF6600),
                    background = Color.White,
                    surface = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val selectedIndex by viewModel.selectedBottomNavIndex.collectAsStateWithLifecycle()


    var showSettings by remember { mutableStateOf(false) }
    var showTaskCenter by remember { mutableStateOf(false) }
    var showHotSearchScreen by remember { mutableStateOf(false) }
    var showWritePost by remember { mutableStateOf(false) }
    var showChannelManager by remember { mutableStateOf(false) }
    var channelRefreshTrigger by remember { mutableIntStateOf(0) }

    
    val bottomNavItems = listOf(
        BottomNavItem("首页", R.raw.home_nav, "home"),
        BottomNavItem("视频", R.raw.video_nav, "video"),
        BottomNavItem("发现", R.raw.search_nav, "discover"),
        BottomNavItem("消息", R.raw.message_nav, "message"),
        BottomNavItem("我的", R.raw.mine_nav, "profile")
    )

    var hotSearchScreenSystemBarsConfig by remember {
        mutableStateOf(SystemBarsConfig(statusBarDarkIcons = false))
    }

    
    val videoViewModel: VideoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val videoSelectedTab by videoViewModel.selectedTab.collectAsStateWithLifecycle()

    val systemBarsConfig = when {
        showSettings -> systemBarsConfigForTopBar(
            topBarBackground = TopBarBackground.Solid(Color.White)
        )
        showTaskCenter -> systemBarsConfigForTopBar(
            topBarBackground = TopBarBackground.Solid(Color(0xFFFFED00)),
            statusBarIconsFallbackColor = Color(0xFFFFED00),
            statusBarColorFallbackColor = Color(0xFFFFED00)
        )
        showHotSearchScreen -> {
            
            hotSearchScreenSystemBarsConfig
        }
        showWritePost -> systemBarsConfigForTopBar(
            topBarBackground = TopBarBackground.Solid(Color.White),
            statusBarIconsFallbackColor = Color.White,
            statusBarColorFallbackColor = Color.White
        ).copy(statusBarDarkIcons = true)
        showChannelManager -> systemBarsConfigForTopBar(
            topBarBackground = TopBarBackground.Solid(Color.White)
        )
        else -> when (selectedIndex) {
            0 -> systemBarsConfigForTopBar(
                topBarBackground = TopBarBackground.Solid(Color.White)
            ) 
            1 -> {
                
                
                val topBg = if (videoSelectedTab == VideoViewModel.TabType.RECOMMEND) {
                    Color.Black
                } else {
                    Color.White
                }

                systemBarsConfigForTopBar(
                    topBarBackground = TopBarBackground.Solid(topBg),
                    statusBarIconsFallbackColor = topBg,
                    statusBarColorFallbackColor = topBg
                ).copy(
                    
                    statusBarDarkIcons = (videoSelectedTab == VideoViewModel.TabType.FEATURED),
                    
                    autoStatusBarColor = true,
                    autoStatusBarIcons = false
                )
            } 
            2 -> systemBarsConfigForTopBar(
                topBarBackground = TopBarBackground.Solid(Color.White)
            ) 
            3 -> systemBarsConfigForTopBar(
                topBarBackground = TopBarBackground.Solid(Color.White)
            ) 
            4 -> systemBarsConfigForTopBar(
                topBarBackground = TopBarBackground.Solid(Color.White)
            ) 
            else -> systemBarsConfigForTopBar(
                topBarBackground = TopBarBackground.Solid(Color.White)
            )
        }
    }

    var statusBarBg by remember { mutableStateOf(Color.Transparent) }

    SetupSystemBars(
        config = systemBarsConfig,
        onFinalColorCalculated = { statusBarBg = it }
    )

    
    StatusBarPlaceholder(
        backgroundColor = statusBarBg,
        modifier = Modifier.zIndex(1000f)
    )

    
    when {
        showSettings -> {
            com.example.weibo.ui.profile.SettingsScreen(
                onBack = { showSettings = false }
            )
        }
        showTaskCenter -> {
            com.example.weibo.ui.taskcenter.TaskCenterScreen(
                onBack = { showTaskCenter = false }
            )
        }
        showHotSearchScreen -> {
            com.example.weibo.ui.discover.HotSearchScreen(
                onBack = { showHotSearchScreen = false },
                onSystemBarsConfigChange = { config ->
                    hotSearchScreenSystemBarsConfig = config
                }
            )
        }
        showWritePost -> {
            com.example.weibo.ui.post.WritePostScreen(
                onDismiss = { showWritePost = false },
                viewModel = viewModel
            )
        }
        showChannelManager -> {
            com.example.weibo.ui.channel.ChannelManagerScreen(
                onBack = {
                    showChannelManager = false
                    channelRefreshTrigger++ 
                }
            )
        }
        else -> {
            Scaffold(
                bottomBar = {
                    LottieBottomNavigation(
                        items = bottomNavItems,
                        selectedIndex = selectedIndex,
                        onItemSelected = { index ->
                            viewModel.switchBottomNav(index)
                        },
                        
                        isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    
                    val videoViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.example.weibo.viewmodel.VideoViewModel>()

                    
                    
                    LaunchedEffect(selectedIndex) {
                        if (selectedIndex == 1) {
                            
                            
                            kotlinx.coroutines.delay(100)
                            val playPosition = videoViewModel.playPosition.value
                            if (playPosition != null && playPosition >= 0) {
                                videoViewModel.requestPlayPosition(playPosition)
                            } else {
                                
                                val recommendVideos = videoViewModel.recommendVideoList.value
                                if (recommendVideos.isNotEmpty()) {
                                    videoViewModel.requestPlayPosition(0)
                                }
                            }
                        } else {
                            
                            videoViewModel.clearPlayPosition()
                        }
                    }

                    when (selectedIndex) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToTaskCenter = { showTaskCenter = true },
                            onNavigateToWritePost = { showWritePost = true },
                            onNavigateToChannelManager = { showChannelManager = true },
                            channelRefreshTrigger = channelRefreshTrigger
                        )
                        1 -> VideoScreen(viewModel = videoViewModel)
                        2 -> DiscoverScreen(
                            onNavigateToHotSearch = { showHotSearchScreen = true }
                        )
                        3 -> MessageScreen(
                            onNavigateToSettings = { showSettings = true }
                        )
                        4 -> ProfileScreen(
                            onNavigateToSettings = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}
