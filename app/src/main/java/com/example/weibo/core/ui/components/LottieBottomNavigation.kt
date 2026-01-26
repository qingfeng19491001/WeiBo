package com.example.weibo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.animateLottieCompositionAsState


@Composable
fun LottieBottomNavigation(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            LottieNavItem(
                item = item,
                isSelected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LottieNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(item.lottieResId)
    )

    var playToken by remember { mutableIntStateOf(0) }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        
        isPlaying = isSelected && playToken > 0,
        restartOnPlay = true
    )

    
    LaunchedEffect(isSelected, progress) {
        if (isSelected && playToken > 0 && progress >= 1f) {
            playToken = 0
        }
    }
    
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                
                playToken += 1
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        
        LottieAnimation(
            composition = composition,
            progress = { if (isSelected) progress else 0f },
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        
        Text(
            text = item.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = if (isSelected) {
                if (isDarkTheme) Color.White else Color(0xFF000000)
            } else {
                Color(0xFFBDBDBD)
            }
        )
    }
}

