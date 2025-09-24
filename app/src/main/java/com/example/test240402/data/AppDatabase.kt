package com.example.test240402.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
//import com.example.test240402.data.dao.ContentDao
import com.example.test240402.data.datasource.local.TodoDao
import com.example.test240402.data.model.TodoEntity
//import com.example.test240402.model.ContentEntity

    @Database(entities = [TodoEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase: RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        // 예시: 버전 1에서 버전 2로의 마이그레이션
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // todo_items 테이블에 alarmTime과 isAlarmEnabled 컬럼 추가
//                // alarmTime은 INTEGER 타입, NULL 허용, 기본값 NULL
//                // isAlarmEnabled는 INTEGER 타입 (Boolean 표현), NOT NULL, 기본값 0 (false)
////                database.execSQL("ALTER TABLE todo_items ADD COLUMN alarmTime INTEGER DEFAULT NULL")
////                database.execSQL("ALTER TABLE todo_items ADD COLUMN isAlarmEnabled INTEGER NOT NULL DEFAULT 0")
//            }
//        }
//
//
//        val MIGRATION_2_3 = object : Migration(2, 3) { // 시작 버전과 종료 버전이 2와 3으로 설정
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE todo_items ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
//                // todo_items 테이블에 alarmTime 컬럼 추가
//                database.execSQL("ALTER TABLE todo_items ADD COLUMN alarmTime INTEGER DEFAULT NULL")
//                // todo_items 테이블에 isAlarmEnabled 컬럼 추가
//                database.execSQL("ALTER TABLE todo_items ADD COLUMN isAlarmEnabled INTEGER NOT NULL DEFAULT 0")
//            }
//        }

    }

}