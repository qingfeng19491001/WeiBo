package com.example.weibo.ui.channel

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weibo.R
import com.example.weibo.core.ui.components.TopBarContainer
import com.example.weibo.model.Channel


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
        return getDefaultChannels()
    }

    val ids = idsStr.split(",")
    val names = namesStr.split(",")

    if (ids.size != names.size) {
        return getDefaultChannels()
    }

    return ids.mapIndexed { index, id ->
        Channel(id, names[index], false, false)
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


@Composable
fun ChannelManagerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    
    var myChannels by remember {
        mutableStateOf<List<Channel>>(emptyList())
    }
    var allChannelsList by remember {
        mutableStateOf<List<Channel>>(emptyList())
    }

    
    LaunchedEffect(Unit) {
        val loaded = loadChannelsFromPrefs(context)
        myChannels = if (loaded.isEmpty()) {
            val default = getDefaultChannels()
            saveChannelsToPrefs(context, default)
            default
        } else {
            loaded
        }
        allChannelsList = getAllChannels().filter { channel ->
            !myChannels.any { it.id == channel.id }
        }
    }

    var isEditMode by remember { mutableStateOf(false) }

    TopBarContainer(
        topBar = {
            
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            
                            saveChannelsToPrefs(context, myChannels)
                            onBack()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "返回",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "频道管理",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    TextButton(
                        onClick = { isEditMode = !isEditMode },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF6600)
                        ),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(text = if (isEditMode) "完成" else "编辑", fontSize = 14.sp)
                    }
                }
            }
        },
        content = {
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "我的频道",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = if (isEditMode) "拖拽排序" else "点击进入频道",
                            fontSize = 12.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                
                item {
                    val rows = ((myChannels.size + 3) / 4).coerceAtLeast(1)
                    val gridHeight = (rows * 36).dp + ((rows - 1) * 4).dp + 8.dp

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        userScrollEnabled = false,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        itemsIndexed(myChannels) { index, channel ->
                            MyChannelItem(
                                channel = channel,
                                isEditMode = isEditMode,
                                onClick = {
                                    if (!isEditMode) {
                                        saveChannelsToPrefs(context, myChannels)
                                        onBack()
                                    }
                                },
                                onRemove = {
                                    if (isEditMode && !channel.isFixed) {
                                        val removedChannel = myChannels[index]
                                        myChannels = myChannels.filterIndexed { i, _ -> i != index }
                                        allChannelsList = allChannelsList + removedChannel
                                    }
                                }
                            )
                        }
                    }
                }

                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "全部频道",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "点击添加频道",
                            fontSize = 12.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                
                item {
                    val rows = ((allChannelsList.size + 3) / 4).coerceAtLeast(1)
                    val gridHeight = (rows * 36).dp + ((rows - 1) * 4).dp + 8.dp

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        userScrollEnabled = false,
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        itemsIndexed(allChannelsList) { index, channel ->
                            AllChannelItem(
                                channel = channel,
                                onClick = {
                                    myChannels = myChannels + channel
                                    allChannelsList = allChannelsList.filterIndexed { i, _ -> i != index }
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}


@Composable
private fun MyChannelItem(
    channel: Channel,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(36.dp)
            .background(
                color = if (channel.isFixed) Color(0xFFFF6600) else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = channel.name,
            fontSize = 14.sp,
            color = if (channel.isFixed) Color.White else Color(0xFF333333),
            maxLines = 1
        )

        if (isEditMode && !channel.isFixed) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "删除",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun AllChannelItem(
    channel: Channel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(36.dp)
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus_white),
                contentDescription = "添加",
                tint = Color(0xFF999999),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = channel.name,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                maxLines = 1
            )
        }
    }
}
