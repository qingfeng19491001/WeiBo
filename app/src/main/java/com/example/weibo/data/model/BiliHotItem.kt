package com.example.weibo.data.model

import com.google.gson.annotations.SerializedName

data class BiliHotItem(
    @SerializedName(value = "word", alternate = ["keyword", "name"])
    val word: String = "",
    @SerializedName(value = "score", alternate = ["hot", "hotvalue"])
    val score: String = "",
    @SerializedName(value = "type", alternate = ["tag", "remark"])
    val type: String = ""
)








