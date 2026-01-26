package com.example.weibo.ui.picker

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val uri: Uri,
    val isVideo: Boolean,
    val duration: Long = 0L,
    val dateAddedSec: Long = 0L
) : Parcelable


