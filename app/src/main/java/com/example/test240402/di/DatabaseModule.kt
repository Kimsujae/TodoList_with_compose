package com.example.test240402.di

import android.content.Context
import androidx.room.Room
import com.example.test240402.data.AppDatabase
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
    fun providesDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "todo.db")
            .addMigrations(AppDatabase.MIGRATION_2_3) // 버전 2 -> 3 마이그레이션 추가
            .fallbackToDestructiveMigration() // 적절한 마이그레이션이 없는 경우만 초기화
            .build()
    }
}
