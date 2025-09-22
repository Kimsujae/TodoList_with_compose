package com.example.test240402.domain.usecase

import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.repository.TodoRepository

class InsertTodoUseCase (private val todoRepository: TodoRepository) {
    /**
     * 새로운 할 일을 추가합니다.
     * @param todoItem 추가할 할 일 아이템.
     */
    suspend operator fun invoke(todoItem: TodoItem) {
        // 유효성 검사 또는 추가적인 비즈니스 로직이 있다면 여기에 추가 가능
        // 예를 들어, content가 비어있지 않은지 확인 등
        if (todoItem.content.isBlank()) {
            // 적절한 예외를 발생시키거나, 특정 결과를 반환할 수 있습니다.
            // 여기서는 간단히 로깅만 하거나, 특정 Error sealed class를 반환할 수 있습니다.
            println("InsertTodoUseCase: 할 일 내용이 비어있습니다.")
            // throw IllegalArgumentException("할 일 내용은 비어 있을 수 없습니다.")
            return // 또는 특정 Result 타입을 반환
        }
        todoRepository.insertTodo(todoItem)
    }
}