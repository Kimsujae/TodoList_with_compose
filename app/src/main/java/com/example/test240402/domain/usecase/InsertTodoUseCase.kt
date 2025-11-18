package com.example.test240402.domain.usecase

import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.repository.TodoRepository

class InsertTodoUseCase (private val todoRepository: TodoRepository) {
    /**
     * 새로운 할 일을 추가합니다.
     * @param todoItem 추가할 할 일 아이템.
     */
    suspend operator fun invoke(todoItem: TodoItem) {
        if (todoItem.content.isBlank()) {
            return
        }
        todoRepository.insertTodo(todoItem)
    }
}