package com.example.weibo.data.model

import com.google.gson.annotations.SerializedName

data class BiliHotResponse(
    @SerializedName(value = "list", alternate = ["data", "words"])
    val list: List<BiliHotItem> = emptyList()
)








