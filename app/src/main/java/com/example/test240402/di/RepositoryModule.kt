package com.example.test240402.di

//import com.example.test240402.data.dao.ContentDao
import com.example.test240402.data.repository.TodoRepositoryImpl
import com.example.test240402.domain.repository.TodoRepository
//import com.example.test240402.repository.ContentRepository
//import com.example.test240402.repository.ContentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTodoRepository(todoRepositoryImpl: TodoRepositoryImpl): TodoRepository
}
