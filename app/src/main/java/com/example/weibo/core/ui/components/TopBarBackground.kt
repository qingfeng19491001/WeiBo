package com.example.weibo.core.ui.components

import androidx.compose.ui.graphics.Color

sealed class TopBarBackground {
    data class Solid(val color: Color) : TopBarBackground()

    data class Image(
        val model: Any,
        val fallbackColor: Color = Color.Black
    ) : TopBarBackground()
}

