package com.example.weibo.ui.profile

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weibo.R
import com.example.weibo.core.ui.components.TopBarBackground
import com.example.weibo.core.ui.components.TopBarContainer
import com.example.weibo.core.ui.components.SetupSystemBars
import com.example.weibo.core.ui.components.SystemBarsConfig
import com.example.weibo.viewmodel.MainViewModel



sealed class ProfileNavigationState {
    object Main : ProfileNavigationState()
    data class ScanQR(val onBack: () -> Unit) : ProfileNavigationState()
    data class EditProfile(val onBack: () -> Unit) : ProfileNavigationState()
}

@Composable
fun ProfileScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var navigationState by remember { mutableStateOf<ProfileNavigationState>(ProfileNavigationState.Main) }
    
    when (val state = navigationState) {
        is ProfileNavigationState.Main -> {
            ProfileMainScreen(
                viewModel = viewModel,
                modifier = modifier,
                onScanQRClick = {
                    navigationState = ProfileNavigationState.ScanQR(
                        onBack = { navigationState = ProfileNavigationState.Main }
                    )
                },
                onEditProfileClick = {
                    navigationState = ProfileNavigationState.EditProfile(
                        onBack = { navigationState = ProfileNavigationState.Main }
                    )
                },
                onNavigateToSettings = onNavigateToSettings
            )
        }
        is ProfileNavigationState.ScanQR -> {
            ScanQRScreen(
                onBack = state.onBack,
                modifier = modifier
            )
        }
        is ProfileNavigationState.EditProfile -> {
            SetupSystemBars(
                SystemBarsConfig(
                    autoStatusBarIcons = true,
                    autoStatusBarColor = true,
                    topBarBackground = TopBarBackground.Solid(Color.White),
                    statusBarIconsFallbackColor = Color.White
                )
            )
            ProfileEditScreen(
                onBack = state.onBack,
                modifier = modifier
            )
        }
    }
}


@Composable
private fun ProfileMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onScanQRClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val postCount by viewModel.localPostCount.collectAsStateWithLifecycle(initialValue = 0)
    
    
    
    val prefs = remember { 
        context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE
        )
    }
    var nickname by remember { mutableStateOf(prefs.getString("nickname", "用户名") ?: "用户名") }
    var intro by remember { mutableStateOf(prefs.getString("intro", "暂无简介") ?: "暂无简介") }

    
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "nickname" -> nickname = sharedPrefs.getString("nickname", "用户名") ?: "用户名"
                "intro" -> intro = sharedPrefs.getString("intro", "暂无简介") ?: "暂无简介"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    TopBarContainer(
        topBar = {
            ProfileTopBar(
                onAddFriendClick = {
                    
                },
                onScanQRClick = onScanQRClick,
                onSettingsClick = onNavigateToSettings
            )
        },
        content = {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
            ) {
                
                ProfileUserInfo(
                    nickname = nickname,
                    intro = intro,
                    onPerfectInfoClick = onEditProfileClick
                )

                
                ProfileBanner()

                
                ProfileStats(
                    postCount = postCount,
                    followCount = 12, 
                    fanCount = 60 
                )

                
                ProfileFunctionRow1()

                
                ProfileFunctionRow2()
            }
        }
    )
}


@Composable
private fun ProfileTopBar(
    onAddFriendClick: () -> Unit,
    onScanQRClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        IconButton(
            onClick = onAddFriendClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.add_person),
                contentDescription = "添加好友",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        
        IconButton(
            onClick = onScanQRClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_scanner),
                contentDescription = "扫描二维码",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "设置",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }
    }
}


@Composable
private fun ProfileUserInfo(
    nickname: String,
    intro: String,
    onPerfectInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Icon(
            painter = painterResource(id = R.drawable.touxiang),
            contentDescription = "头像",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )

        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            
            Text(
                text = nickname,
                fontSize = 18.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )

            
            Text(
                text = stringResource(R.string.profile_intro, intro),
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC) 
            )
        }

        
        
        Row(
            modifier = Modifier
                .background(
                    Color(0xFFF5F5F5), 
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(onClick = onPerfectInfoClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit_perfect_info),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFF8615) 
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "完善资料",
                fontSize = 14.sp,
                color = Color(0xFFFF8615) 
            )
        }
    }
}


@Composable
private fun ProfileBanner(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = R.drawable.haibao),
        contentDescription = "海报",
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        tint = Color.Unspecified
    )
}


@Composable
private fun ProfileStats(
    postCount: Int,
    followCount: Int,
    fanCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        
        ProfileStatItem(
            count = postCount.toString(),
            label = "微博",
            modifier = Modifier.weight(1f)
        )

        
        ProfileStatItem(
            count = followCount.toString(),
            label = "关注",
            modifier = Modifier.weight(1f)
        )

        
        ProfileStatItem(
            count = fanCount.toString(),
            label = "粉丝",
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ProfileStatItem(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Text(
            text = count,
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )

        
        Text(
            text = label,
            fontSize = 18.sp,
            color = Color(0xFFCCCCCC) 
        )
    }
}


@Composable
private fun ProfileFunctionRow1(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            
            ProfileFunctionItem(
                iconRes = R.drawable.my_photo,
                label = "我的相册",
                modifier = Modifier.weight(1f)
            )

            
            ProfileFunctionItem(
                iconRes = R.drawable.shoucang,
                label = "赞/收藏",
                modifier = Modifier.weight(1f)
            )

            
            ProfileFunctionItem(
                iconRes = R.drawable.jilu,
                label = "浏览记录",
                modifier = Modifier.weight(1f)
            )

            
            ProfileFunctionItem(
                iconRes = R.drawable.caogaoxiang,
                label = "草稿箱",
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun ProfileFunctionRow2(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            
            ProfileFunctionItem(
                iconRes = R.drawable.money,
                label = "我的钱包",
                modifier = Modifier.weight(1f)
            )

            
            ProfileFunctionItem(
                iconRes = R.drawable.dingdan,
                label = "我的订单",
                modifier = Modifier.weight(1f)
            )

            
            ProfileFunctionItem(
                iconRes = R.drawable.creative_center,
                label = "创作中心",
                modifier = Modifier.weight(1f)
            )

            
            ProfileFunctionItem(
                iconRes = R.drawable.toutiao,
                label = "粉丝头条",
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun ProfileFunctionItem(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(4.dp))

        
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFFCCCCCC) 
        )
    }
}
