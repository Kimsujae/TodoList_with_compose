package com.example.test240402.domain.usecase

import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.repository.TodoRepository

class UpdateTodoUseCase(private val todoRepository: TodoRepository) {
    /**
     * 기존 할 일을 수정합니다.
     * @param todoItem 수정할 할 일 아이템. id를 기준으로 기존 아이템을 찾아 수정합니다.
     */
    suspend operator fun invoke(todoItem: TodoItem) {
        todoRepository.updateTodo(todoItem)
    }
}
