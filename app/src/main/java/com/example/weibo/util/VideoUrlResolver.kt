package com.example.weibo.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


object VideoUrlResolver {
    private const val TAG = "VideoUrlResolver"

    
    suspend fun resolvePlayUrl(playUrl: String): String = withContext(Dispatchers.IO) {
        if (playUrl.isEmpty()) {
            return@withContext playUrl
        }
        
        
        if (playUrl.startsWith("http") && 
            !playUrl.contains("api.bilibili.com") && 
            !playUrl.contains("baobab.kaiyanapp.com/api/v1/playUrl")) {
            return@withContext playUrl
        }

        
        if (playUrl.contains("api.bilibili.com")) {
            try {
                val connection = URL(playUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.instanceFollowRedirects = true

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val data = json.optJSONObject("data")
                    val durlArray = data?.optJSONArray("durl")
                    if (durlArray != null && durlArray.length() > 0) {
                        val firstDurl = durlArray.optJSONObject(0)
                        val url = firstDurl?.optString("url")
                        if (!url.isNullOrEmpty()) {
                            Log.d(TAG, "Resolved Bilibili URL: ${url.take(100)}")
                            return@withContext url
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resolving Bilibili URL: ${e.message}", e)
            }
        }
        
        
        if (playUrl.contains("baobab.kaiyanapp.com/api/v1/playUrl")) {
            try {
                val connection = URL(playUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.instanceFollowRedirects = true

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    
                    val urlsArray = json.optJSONArray("urls")
                    if (urlsArray != null && urlsArray.length() > 0) {
                        val firstUrlObj = urlsArray.optJSONObject(0)
                        val url = firstUrlObj?.optString("url")
                        if (!url.isNullOrEmpty()) {
                            Log.d(TAG, "Resolved KaiYan URL: ${url.take(100)}")
                            return@withContext url
                        }
                    }
                    
                    val directUrl = json.optString("url")
                    if (directUrl.isNotEmpty()) {
                        Log.d(TAG, "Resolved KaiYan URL (direct): ${directUrl.take(100)}")
                        return@withContext directUrl
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resolving KaiYan URL: ${e.message}", e)
            }
        }
        
        return@withContext playUrl 
    }
}

