package com.example.weibo.network.api

import com.example.weibo.data.model.BiliHotResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BiliHotApi {
    @GET("main/hotword")
    suspend fun getHotwords(
        @Query("main_ver") mainVer: String = "v3",
        @Query("search_type") searchType: String = "web",
        @Query("bangumi_num") bangumiNum: Int = 0
    ): Response<BiliHotResponse>
}







