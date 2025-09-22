package com.example.test240402.data

import androidx.room.Database
import androidx.room.RoomDatabase
//import com.example.test240402.data.dao.ContentDao
import com.example.test240402.data.datasource.local.TodoDao
import com.example.test240402.data.model.TodoEntity
//import com.example.test240402.model.ContentEntity

@Database(entities = [TodoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun todoDao(): TodoDao
}