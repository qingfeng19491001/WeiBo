package com.example.weibo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weibo.data.model.Post

@Database(
    entities = [Post::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
}
