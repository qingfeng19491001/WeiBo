package com.example.weibo.di

import android.content.Context
import androidx.room.Room
import com.example.weibo.data.local.AppDatabase
import com.example.weibo.data.local.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "weibo_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePostDao(db: AppDatabase): PostDao = db.postDao()
}

