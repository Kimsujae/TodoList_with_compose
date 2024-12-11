package com.example.test240402.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.test240402.data.dao.ContentDao
import com.example.test240402.ContentEntity

@Database(entities = [ContentEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun contentDao(): ContentDao
}