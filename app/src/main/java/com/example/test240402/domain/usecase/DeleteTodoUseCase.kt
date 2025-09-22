package com.example.test240402.domain.usecase // domain 모듈 내 패키지

import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.repository.TodoRepository

class DeleteTodoUseCase(private val todoRepository: TodoRepository) {
    /**
     * 할 일을 삭제합니다.
     * @param todoItem 삭제할 할 일 아이템.
     */
    suspend operator fun invoke(todoItem: TodoItem) {
        todoRepository.deleteTodo(todoItem)
    }
}