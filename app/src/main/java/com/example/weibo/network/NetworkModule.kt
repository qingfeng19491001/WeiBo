package com.example.weibo.network

import com.example.weibo.network.api.BiliHotApi
import com.example.weibo.network.api.KaiYanApi
import com.example.weibo.ui.discover.network.BiliSearchApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL_MAIN = "https://baobab.kaiyanapp.com/"
    private const val BASE_URL_BILI = "https://api.bilibili.com/"
    
    
    @Provides
    @Singleton
    fun provideRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            var attempt = 0
            val request = chain.request()
            var lastError: Exception? = null
            while (attempt < 2) {
                try {
                    val response = chain.proceed(request)
                    if (response.code in 500..599) {
                        response.close()
                        attempt++
                        continue
                    }
                    return@Interceptor response
                } catch (e: Exception) {
                    lastError = e
                    attempt++
                }
            }
            lastError?.let { throw it }
            chain.proceed(request)
        }
    }
    
    
    @Provides
    @Singleton
    @Named("main")
    fun provideMainOkHttpClient(retryInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(retryInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    
    @Provides
    @Singleton
    @Named("bili")
    fun provideBiliOkHttpClient(retryInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Referer", "https://www.bilibili.com")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(retryInterceptor)
            .build()
    }
    
    
    @Provides
    @Singleton
    @Named("main")
    fun provideMainRetrofit(@Named("main") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_MAIN)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    
    @Provides
    @Singleton
    @Named("bili")
    fun provideBiliRetrofit(@Named("bili") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_BILI)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    
    @Provides
    @Singleton
    fun provideKaiYanApi(@Named("main") retrofit: Retrofit): KaiYanApi {
        return retrofit.create(KaiYanApi::class.java)
    }
    
    
    @Provides
    @Singleton
    fun provideBiliSearchApi(@Named("bili") retrofit: Retrofit): BiliSearchApi {
        return retrofit.create(BiliSearchApi::class.java)
    }

    
    @Provides
    @Singleton
    fun provideBiliHotApi(@Named("bili") retrofit: Retrofit): BiliHotApi {
        return retrofit.create(BiliHotApi::class.java)
    }
}

