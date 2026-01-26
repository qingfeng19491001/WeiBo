package com.example.weibo.database.entity


data class VideoBean(
    val id: String,              
    val coverUrl: String,        
    val videoUrl: String,        
    val avatarUrl: String,       
    val username: String,        
    val description: String,     
    val musicName: String = "",  
    var likeCount: Long = 0,     
    val commentCount: Long = 0,  
    val shareCount: Long = 0,    
    val durationSec: Int = 0,    
    var isLiked: Boolean = false, 
    val bvid: String = ""        
) {
    
    fun resolvedBvid(): String = if (bvid.isNotEmpty()) bvid else id
}

