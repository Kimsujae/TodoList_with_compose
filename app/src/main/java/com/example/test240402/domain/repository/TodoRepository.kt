package com.example.test240402.domain.repository // domain 모듈 내 패키지

import com.example.test240402.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<TodoItem>>
    suspend fun insertTodo(todoItem: TodoItem)
    suspend fun updateTodo(todoItem: TodoItem)
    suspend fun deleteTodo(todoItem: TodoItem)

    suspend fun getActiveScheduledAlarms(currentTimeMillis: Long): List<TodoItem>
    // 필요하다면 ID로 Todo 가져오기 등 추가
    // suspend fun getTodoById(id: Int): TodoItem?
}
            