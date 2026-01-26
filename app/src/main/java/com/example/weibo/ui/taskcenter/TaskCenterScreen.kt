package com.example.weibo.ui.taskcenter

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import kotlinx.coroutines.isActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weibo.R
import com.example.weibo.viewmodel.TaskCenterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskCenterScreen(
    onBack: () -> Unit,
    viewModel: TaskCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val bannerImages by viewModel.bannerImages.collectAsStateWithLifecycle()
    val bannerTitles by viewModel.bannerTitles.collectAsStateWithLifecycle()
    val currentBannerPosition by viewModel.currentBannerPosition.collectAsStateWithLifecycle()
    val signInDays by viewModel.signInDays.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val points by viewModel.points.collectAsStateWithLifecycle()
    
    
    val pagerState = rememberPagerState(initialPage = currentBannerPosition) { bannerImages.size }
    
    
    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateCurrentBannerPosition(pagerState.currentPage)
    }
    
    
    LaunchedEffect(bannerImages) {
        if (bannerImages.isEmpty()) return@LaunchedEffect
        while (isActive) {
            delay(5_000) 
            val next = (pagerState.currentPage + 1) % bannerImages.size
            pagerState.animateScrollToPage(next)
        }
    }
    

    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFFFED00)) 
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_white),
                    contentDescription = "返回",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            
            Text(
                text = "用户任务中心",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            
            IconButton(
                onClick = {
                    Toast.makeText(context, "更多功能开发中...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_white),
                    contentDescription = "更多",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color(0xFFFFED00)) 
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        
                        Text(
                            text = "任务红包",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "￥",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = String.format(java.util.Locale.getDefault(), "%.2f", balance),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        
                        Button(
                            onClick = {
                                if (balance > 0) {
                                    if (viewModel.withdraw(balance)) {
                                        Toast.makeText(context, "提现成功", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "提现失败", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "余额不足", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "提现",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFED00) 
                            )
                        }
                    }
                    
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        
                        Text(
                            text = "微博积分",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        
                        Text(
                            text = points.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        
                        Button(
                            onClick = {
                                Toast.makeText(context, "兑换好礼功能开发中...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "兑换好礼",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFED00) 
                            )
                        }
                    }
                }
            }
            
            
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Text(
                        text = "任务规则",
                        fontSize = 14.sp,
                        color = Color(0xFF666666), 
                        modifier = Modifier.weight(1f)
                    )
                    
                    
                    Text(
                        text = "微博钱包",
                        fontSize = 14.sp,
                        color = Color(0xFF666666), 
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "微博钱包功能开发中...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    
                    Text(
                        text = "|",
                        fontSize = 14.sp,
                        color = Color(0xFFCCCCCC), 
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    
                    Text(
                        text = "明细",
                        fontSize = 14.sp,
                        color = Color(0xFF666666), 
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "明细功能开发中...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            
            
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFF5F5F5)) 
                )
            }
            
            
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    
                    TaskEntry(
                        modifier = Modifier.weight(1f),
                        iconRes = R.drawable.ic_answer_challenge,
                        text = "答题挑战赛",
                        backgroundColor = Color(0xFFFFF2F2), 
                        onClick = {
                            Toast.makeText(context, "答题功能开发中...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    
                    TaskEntry(
                        iconRes = R.drawable.ic_fishpond,
                        text = "微博渔场",
                        backgroundColor = Color(0xFFF2F9FF), 
                        onClick = {
                            Toast.makeText(context, "微博渔场功能开发中...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    
                    TaskEntry(
                        iconRes = R.drawable.ic_points_mall,
                        text = "积分商城",
                        backgroundColor = Color(0xFFFFF7E6), 
                        onClick = {
                            Toast.makeText(context, "积分商城功能开发中...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            
            
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .background(Color(0xFFF5F5F5)) 
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                    ) { page ->
                        Image(
                            painter = painterResource(id = bannerImages.getOrElse(page) { R.drawable.task_center_banner1 }),
                            contentDescription = bannerTitles.getOrElse(page) { "Banner Ad" },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    
                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        bannerImages.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index == pagerState.currentPage) {
                                            Color(0xFFFFA500)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFFFA500),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
            
            
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    
                    Text(
                        text = "已连签${signInDays}天",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333) 
                    )
                    
                    
                    Text(
                        text = "再签1天，可获得连签奖0.06元",
                        fontSize = 14.sp,
                        color = Color(0xFF666666), 
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        
                        SignInDayItem(
                            dayText = "0.06元",
                            iconRes = R.drawable.sign_icon_checked1,
                            label = "第1天",
                            isToday = false,
                            isSigned = true,
                            onClick = null,
                            modifier = Modifier.weight(1f)
                        )
                        
                        
                        SignInDayItem(
                            dayText = "",
                            iconRes = R.drawable.sign_icon_unchecked1,
                            label = "今天",
                            isToday = true,
                            isSigned = false,
                            onClick = {
                                if (viewModel.signInToday()) {
                                    Toast.makeText(context, "签到成功！", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "签到失败，请重试", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        
                        SignInDayItem(
                            dayText = "0.16元",
                            iconRes = null,
                            label = "连签奖",
                            isToday = false,
                            isSigned = false,
                            onClick = null,
                            isReward = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        
                        SignInDayItem(
                            dayText = "+30",
                            iconRes = R.drawable.sign_icon_unchecked1,
                            label = "第4天",
                            isToday = false,
                            isSigned = false,
                            onClick = null,
                            modifier = Modifier.weight(1f)
                        )
                        
                        
                        SignInDayItem(
                            dayText = "0.18元",
                            iconRes = null,
                            label = "连签奖",
                            isToday = false,
                            isSigned = false,
                            onClick = null,
                            isReward = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        
                        SignInDayItem(
                            dayText = "+40",
                            iconRes = R.drawable.sign_icon_unchecked1,
                            label = "第6天",
                            isToday = false,
                            isSigned = false,
                            onClick = null,
                            modifier = Modifier.weight(1f)
                        )
                        
                        
                        SignInDayItem(
                            dayText = "0.3元",
                            iconRes = R.drawable.sign_icon_unchecked1,
                            label = "连签奖",
                            isToday = false,
                            isSigned = false,
                            onClick = null,
                            isReward = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFFFF2F2)) 
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Text(
                        text = "邀请1个好友，签到奖励最高翻20倍",
                        fontSize = 14.sp,
                        color = Color(0xFF666666), 
                        modifier = Modifier.weight(1f)
                    )
                    
                    
                    Button(
                        onClick = {
                            Toast.makeText(context, "邀请好友功能开发中...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6600) 
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "邀请好友",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            
            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFFF5F5F5)) 
                )
            }
            
            
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    
                    Text(
                        text = "日常任务",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333) 
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFFF2F2)) 
                                .clip(RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_answer_challenge),
                                contentDescription = "答题挑战赛",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            
                            Text(
                                text = "答题挑战赛",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333) 
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            
                            Text(
                                text = "参与答题，瓜分万元红包！(0/1)",
                                fontSize = 14.sp,
                                color = Color(0xFF999999) 
                            )
                        }
                        
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            
                            Text(
                                text = "+30",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B00) 
                            )
                            
                            
                            Button(
                                onClick = {
                                    Toast.makeText(context, "答题功能开发中...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF6600) 
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "去答题",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun TaskEntry(
    modifier: Modifier = Modifier,
    iconRes: Int,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(backgroundColor)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF333333) 
        )
    }
}


@Composable
private fun SignInDayItem(
    dayText: String,
    iconRes: Int?,
    label: String,
    isToday: Boolean,
    isSigned: Boolean,
    onClick: (() -> Unit)?,
    isReward: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    when {
                        isToday && !isSigned -> Color(0xFFFFF2F2) 
                        isSigned -> Color(0xFFF5F5F5) 
                        isReward -> Color(0xFFFFF2F2) 
                        else -> Color(0xFFF5F5F5) 
                    }
                )
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                if (dayText.isNotEmpty()) {
                    Text(
                        text = dayText,
                        fontSize = 12.sp,
                        color = if (isReward) {
                            Color(0xFFFF6B00) 
                        } else {
                            Color(0xFF999999) 
                        }
                    )
                }
                
                
                if (iconRes != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(
                            if (isToday) 32.dp else 20.dp 
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isToday) {
                Color(0xFFFF6B00) 
            } else {
                Color(0xFF999999) 
            }
        )
    }
}
