package com.example.weibo.network.api

import com.example.weibo.network.model.KaiYanVideoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface KaiYanApi {
    
    
    @GET("api/v6/community/tab/follow")
    suspend fun getFollowVideos(
        @Query("start") start: Int = 0,
        @Query("num") num: Int = 10,
        @Query("newest") newest: Boolean = true
    ): Response<KaiYanVideoResponse>
}















