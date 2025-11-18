package com.example.test240402.di

import com.example.test240402.data.AppDatabase
import com.example.test240402.data.datasource.local.TodoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Singleton
    @Provides
    fun providesTodoDao(appDatabase: AppDatabase): TodoDao = appDatabase.todoDao()
}