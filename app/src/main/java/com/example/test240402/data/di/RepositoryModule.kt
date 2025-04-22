package com.example.test240402.data.di

import com.example.test240402.data.dao.ContentDao
import com.example.test240402.repository.ContentRepository
import com.example.test240402.repository.ContentRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    @ViewModelScoped
    fun providesContentRepository(contentDao: ContentDao): ContentRepository
            =ContentRepositoryImpl(contentDao)
}