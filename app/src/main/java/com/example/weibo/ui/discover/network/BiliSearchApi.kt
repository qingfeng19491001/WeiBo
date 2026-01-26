package com.example.weibo.ui.discover.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface BiliSearchApi {

    
    @GET("x/web-interface/wbi/search/default")
    suspend fun getDefaultSearch(): Response<JsonObject>

    
    @GET("x/web-interface/wbi/search/square")
    suspend fun getHotSearchSquare(
        @Query("limit") limit: Int = 50,
        @Query("platform") platform: String = "web"
    ): Response<JsonObject>

    
    @GET("x/v2/search/trending/ranking")
    suspend fun getHotSearchRanking(
        @Query("limit") limit: Int = 20
    ): Response<JsonObject>
}











