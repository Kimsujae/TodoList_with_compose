package com.example.test240402.domain.usecase

import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class GetTodosUseCase(private val todoRepository: TodoRepository) {
    operator fun invoke(): Flow<List<TodoItem>> { // invoke 연산자 오버로딩으로 간결하게 호출 가능
        return todoRepository.getAllTodos()
    }
}