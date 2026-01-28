package com.example.weibo.data.model

sealed class PostMedia {
    data class Image(val url: String) : PostMedia()
    data class Gif(val url: String) : PostMedia()
    data class Video(val url: String, val coverUrl: String? = null) : PostMedia()
    data class LivePhoto(val imageUrl: String, val videoUrl: String) : PostMedia()
}

