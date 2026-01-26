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
    val imagesJson: String = ""
) {

    fun getImages(): List<String> {
        if (imagesJson.isEmpty() || imagesJson == "[]") {
            return emptyList()
        }
        return try {
            val gson = Gson()
            val type: Type = TypeToken.getParameterized(List::class.java, String::class.java).type
            gson.fromJson(imagesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun copyWithImages(images: List<String>): Post {
        val gson = Gson()
        val imagesJson = gson.toJson(images)
        return copy(imagesJson = imagesJson)
    }
}















