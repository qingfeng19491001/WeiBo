package com.example.weibo.ui.discover.model


data class BaiduHotSearchItem(
    val index: Int,
    val title: String,
    val hot: String? = null,
    val url: String = "",
    val subtitle: String? = null,
    val desc: String? = null,
    val heat: Long? = null
)

