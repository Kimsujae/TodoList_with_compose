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
    fun providesDataBase(@ApplicationContext context: Context):AppDatabase{
//        Log.d("데이터베이스생성","생성완료")
        return Room.databaseBuilder(context,AppDatabase::class.java,"todo.db")
            .fallbackToDestructiveMigration()
            .build()


    }
}