package com.example.weibo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    val id: String,
    val username: String,
    val avatar: String,
    val timestamp: String,
    val source: String,
    val content: String,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val views: Int,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val imagesJson: String = "",
    val mediaJson: String = ""
) {
    fun getImages(): List<String> {
        if (imagesJson.isEmpty() || imagesJson == "[]") {
            return emptyList()
        }
        return try {
            val gson = Gson()
            val type: Type = TypeToken.getParameterized(List::class.java, String::class.java).type
            gson.fromJson(imagesJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun copyWithImages(images: List<String>): Post {
        val gson = Gson()
        val newImagesJson = gson.toJson(images)
        return copy(imagesJson = newImagesJson)
    }

    fun getMedia(): List<PostMedia> {
        if (mediaJson.isNotEmpty() && mediaJson != "[]") {
            return try {
                val gson = Gson()
                val listType: Type = object : TypeToken<List<Map<String, Any?>>>() {}.type
                val raw: List<Map<String, Any?>> = gson.fromJson(mediaJson, listType) ?: emptyList()
                raw.mapNotNull { item ->
                    val type = (item["type"] as? String)?.lowercase()
                    when (type) {
                        "image" -> (item["url"] as? String)?.let { PostMedia.Image(it) }
                        "gif" -> (item["url"] as? String)?.let { PostMedia.Gif(it) }
                        "video" -> {
                            val url = item["url"] as? String
                            val coverUrl = item["coverUrl"] as? String
                            if (url != null) PostMedia.Video(url, coverUrl) else null
                        }
                        "livephoto" -> {
                            val imageUrl = item["imageUrl"] as? String
                            val videoUrl = item["videoUrl"] as? String
                            if (imageUrl != null && videoUrl != null) PostMedia.LivePhoto(imageUrl, videoUrl) else null
                        }
                        else -> null
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        return getImages().map { PostMedia.Image(it) }
    }

    fun copyWithMedia(media: List<PostMedia>): Post {
        val gson = Gson()

        val raw = media.map { m ->
            when (m) {
                is PostMedia.Image -> mapOf(
                    "type" to "image",
                    "url" to m.url
                )
                is PostMedia.Gif -> mapOf(
                    "type" to "gif",
                    "url" to m.url
                )
                is PostMedia.Video -> mapOf(
                    "type" to "video",
                    "url" to m.url,
                    "coverUrl" to m.coverUrl
                )
                is PostMedia.LivePhoto -> mapOf(
                    "type" to "livephoto",
                    "imageUrl" to m.imageUrl,
                    "videoUrl" to m.videoUrl
                )
            }
        }

        val newMediaJson = gson.toJson(raw)

        val newImages = media.mapNotNull {
            when (it) {
                is PostMedia.Image -> it.url
                is PostMedia.Gif -> it.url
                else -> null
            }
        }
        val newImagesJson = gson.toJson(newImages)

        return copy(mediaJson = newMediaJson, imagesJson = newImagesJson)
    }
}
