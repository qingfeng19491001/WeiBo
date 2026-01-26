package com.example.weibo.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.weibo.data.model.Post
import com.example.weibo.data.paging.PostPagingSource
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PostRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val localPosts = mutableListOf<Post>()

    private val _localPostCount = MutableStateFlow(0)
    val localPostCount: StateFlow<Int> = _localPostCount.asStateFlow()
    
    
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
            pagingSourceFactory = { PostPagingSource(isFollowFeed = false) }
        ).flow
    }
    
    
    fun localPostsPagingFlow(): Flow<PagingData<Post>> {
        
        PostPagingSource.updateLocalPosts(localPosts.toList())
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(isFollowFeed = true) }
        ).flow
    }
    
    
    suspend fun refreshPosts() {
        
    }
    
    
    suspend fun likePost(postId: String): Boolean {
        
        return true
    }
    
    
    suspend fun deletePost(postId: String): Boolean {
        
        return true
    }
    
    
    suspend fun publishPost(content: String, images: List<String>, postId: String): Boolean {
        
        
        
        return true
    }
    
    
    suspend fun addPost(content: String, images: List<String>): Boolean {
        return try {
            val postId = "local_${System.currentTimeMillis()}"
            val currentTime = System.currentTimeMillis()
            val timeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            val timestamp = timeFormat.format(Date(currentTime))
            
            
            val username = userPrefs.getString("nickname", "我") ?: "我"
            
            val avatar = userPrefs.getString("avatar", "") ?: ""
            
            val gson = Gson()
            val imagesJson = gson.toJson(images)
            
            val newPost = Post(
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
                createdAt = currentTime,
                imagesJson = imagesJson
            )
            
            
            localPosts.add(0, newPost)
            _localPostCount.value = localPosts.size
            
            
            PostPagingSource.updateLocalPosts(localPosts.toList())
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    
    fun getLocalPosts(): List<Post> = localPosts.toList()
}

