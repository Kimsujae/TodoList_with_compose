package com.example.test240402.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.test240402.data.datasource.local.TodoDao
import com.example.test240402.data.model.TodoEntity

@Database(entities = [TodoEntity::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 위치 정보 관련 컬럼 3개 추가
                database.execSQL("ALTER TABLE todo_items ADD COLUMN latitude REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE todo_items ADD COLUMN longitude REAL DEFAULT NULL")
                database.execSQL("ALTER TABLE todo_items ADD COLUMN placeName TEXT DEFAULT NULL")
            }
        }
    }
}
