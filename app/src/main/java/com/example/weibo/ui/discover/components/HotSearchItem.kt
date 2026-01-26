package com.example.weibo.ui.discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weibo.model.HotSearch

@Composable
fun HotSearchItem(item: HotSearch, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${item.rank}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = when (item.rank) {
                1 -> Color(0xFFFF4D4F)
                2 -> Color(0xFFFF7A45)
                3 -> Color(0xFFFFC107)
                else -> Color.Gray
            },
            modifier = Modifier.width(30.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            item.tag?.let {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF4D4F))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = item.views,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
