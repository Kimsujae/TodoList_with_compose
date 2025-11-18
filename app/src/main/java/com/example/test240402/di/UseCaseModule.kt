package com.example.test240402.di

import com.example.test240402.domain.repository.TodoRepository
import com.example.test240402.domain.usecase.DeleteTodoUseCase
import com.example.test240402.domain.usecase.GetTodosUseCase
import com.example.test240402.domain.usecase.InsertTodoUseCase
import com.example.test240402.domain.usecase.UpdateTodoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideGetTodosUseCase(todoRepository: TodoRepository): GetTodosUseCase {
        return GetTodosUseCase(todoRepository)
    }

    @Provides
    fun provideInsertTodoUseCase(todoRepository: TodoRepository): InsertTodoUseCase {
        return InsertTodoUseCase(todoRepository)
    }

    @Provides
    fun provideUpdateTodoUseCase(todoRepository: TodoRepository): UpdateTodoUseCase {
        return UpdateTodoUseCase(todoRepository)
    }

    @Provides
    fun provideDeleteTodoUseCase(todoRepository: TodoRepository): DeleteTodoUseCase {
        return DeleteTodoUseCase(todoRepository)
    }
}
