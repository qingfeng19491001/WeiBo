package com.example.weibo.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.weibo.data.local.PostDao
import com.example.weibo.data.model.Post
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    @ApplicationContext private val context: Context
) {

    private val userPrefs: SharedPreferences
        get() = context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE
        )

    fun postsPagingFlow(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { com.example.weibo.data.paging.PostPagingSource(isFollowFeed = false) }
        ).flow
    }

    fun followPostsPagingFlow(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { postDao.pagingSource() }
        ).flow
    }

    suspend fun addPost(content: String, images: List<String>): Boolean {
        return try {
            val postId = "local_${System.currentTimeMillis()}"
            val currentTime = System.currentTimeMillis()
            val timeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            val timestamp = timeFormat.format(Date(currentTime))

            val username = userPrefs.getString("nickname", "我") ?: "我"
            val avatar = userPrefs.getString("avatar", "") ?: ""

            val basePost = Post(
                id = postId,
                username = username,
                avatar = avatar,
                timestamp = timestamp,
                source = "手机客户端",
                content = content,
                likes = 0,
                comments = 0,
                shares = 0,
                views = 0,
                isLiked = false,
                createdAt = currentTime
            )

            val media = images.map { com.example.weibo.data.model.PostMedia.Image(it) }
            val newPost = basePost.copyWithMedia(media)

            postDao.insert(newPost)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            postDao.deleteById(postId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun likePost(@Suppress("UNUSED_PARAMETER") postId: String): Boolean {
        return true
    }

    suspend fun refreshPosts() {
    }

    fun localPostCountFlow(): Flow<Int> = postDao.countFlow()

    suspend fun countLocalPosts(): Int = postDao.count()
}
