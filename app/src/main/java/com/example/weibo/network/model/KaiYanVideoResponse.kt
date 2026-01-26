package com.example.weibo.network.model

import com.google.gson.annotations.SerializedName


data class KaiYanVideoResponse(
    @SerializedName("itemList")
    val itemList: List<KaiYanVideoItem>,
    @SerializedName("count")
    val count: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("nextPageUrl")
    val nextPageUrl: String?,
    @SerializedName("adExist")
    val adExist: Boolean
)


data class KaiYanVideoItem(
    @SerializedName("type")
    val type: String,
    @SerializedName("data")
    val data: KaiYanVideoData,
    @SerializedName("id")
    val id: Int
)


data class KaiYanVideoData(
    @SerializedName("dataType")
    val dataType: String,
    @SerializedName("content")
    val content: KaiYanVideoContent?
)


data class KaiYanVideoContent(
    @SerializedName("type")
    val type: String,
    @SerializedName("data")
    val data: KaiYanVideoDetail?
)


data class KaiYanVideoDetail(
    @SerializedName("dataType")
    val dataType: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("descriptionPgc")
    val descriptionPgc: String?,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("cover")
    val cover: KaiYanCover?,
    @SerializedName("playUrl")
    val playUrl: String?,
    @SerializedName("author")
    val author: KaiYanAuthor?,
    @SerializedName("consumption")
    val consumption: KaiYanConsumption?
)


data class KaiYanCover(
    @SerializedName("feed")
    val feed: String?
)


data class KaiYanAuthor(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("icon")
    val icon: String?
)


data class KaiYanConsumption(
    @SerializedName("collectionCount")
    val collectionCount: Int,
    @SerializedName("shareCount")
    val shareCount: Int,
    @SerializedName("replyCount")
    val replyCount: Int
)















