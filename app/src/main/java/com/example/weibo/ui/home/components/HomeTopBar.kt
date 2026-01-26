package com.example.weibo.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weibo.R
import com.example.weibo.model.Channel


@Composable
fun HomeTopBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    channels: List<Channel>,
    selectedChannelIndex: Int,
    onChannelSelected: (Int) -> Unit,
    onMoreChannelsClick: () -> Unit,
    onEditClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(start = 16.dp)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_home_publish),
                        contentDescription = "日历",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF333333)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 72.dp)
                        .fillMaxHeight()
                        .clickable { onTabSelected(0) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "推荐",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color(0xFFFF8200) else Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(3.dp)
                            .background(if (selectedTab == 0) Color(0xFFFF8200) else Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp)) 

                Column(
                    modifier = Modifier
                        .widthIn(min = 72.dp)
                        .fillMaxHeight()
                        .clickable { onTabSelected(1) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "关注",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color(0xFFFF8200) else Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(3.dp)
                            .background(if (selectedTab == 1) Color(0xFFFF8200) else Color.Transparent)
                    )
                }
            }

            
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 12.dp)
                            .clickable(onClick = onHomeClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bt_hongbao),
                            contentDescription = "红包",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                    }

                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 8.dp)
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bt_home_add),
                            contentDescription = "添加",
                            modifier = Modifier.size(26.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
        
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE0E0E0))
        )
        
        
        if (selectedTab == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                LazyRow(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    itemsIndexed(channels) { index, channel ->
                        ChannelTag(
                            channel = channel,
                            isSelected = index == selectedChannelIndex,
                            onClick = { onChannelSelected(index) }
                        )
                    }
                }
                
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                        .clickable(onClick = onMoreChannelsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more),
                        contentDescription = "更多频道",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Unspecified
                    )
                }
            }
            
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0))
            )
        }
    }
}


@Composable
private fun ChannelTag(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = channel.name,
        fontSize = 16.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color.Black else Color(0xFF666666), 
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(end = 24.dp)
    )
}

