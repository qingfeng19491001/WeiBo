package com.example.weibo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun TopBarContainer(
    topBarBackground: TopBarBackground = TopBarBackground.Solid(Color.White),
    topBar: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val bgColor = when (topBarBackground) {
        is TopBarBackground.Solid -> topBarBackground.color
        is TopBarBackground.Image -> topBarBackground.fallbackColor
    }

    Column {
        StatusBarPlaceholder(backgroundColor = bgColor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
        ) {
            topBar()
        }
        content()
    }
}
