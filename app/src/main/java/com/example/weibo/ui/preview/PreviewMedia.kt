package com.example.weibo.ui.preview

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PreviewMedia(
    val id: String,
    val url: String,
    val thumbUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val isLongImage: Boolean? = null,
    val isLargeImage: Boolean? = null,
    val videoUrl: String? = null,
    val type: PreviewMediaType = PreviewMediaType.IMAGE
) : Parcelable {
    fun uri(): Uri {
        return Uri.parse(url)
    }
}


@Parcelize
enum class PreviewMediaType : Parcelable {
    IMAGE,
    GIF,
    LARGE_IMAGE,
    VIDEO
}















