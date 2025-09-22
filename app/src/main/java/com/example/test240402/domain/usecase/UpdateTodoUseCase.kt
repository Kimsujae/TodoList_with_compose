package com.example.test240402.domain.usecase // domain 모듈 내 패키지

import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.repository.TodoRepository

class UpdateTodoUseCase(private val todoRepository: TodoRepository) {
    /**
     * 기존 할 일을 수정합니다.
     * @param todoItem 수정할 할 일 아이템. id를 기준으로 기존 아이템을 찾아 수정합니다.
     */
    suspend operator fun invoke(todoItem: TodoItem) {
        // 아이템이 실제로 존재하는지 확인하는 로직 등 추가 가능 (필요하다면 repository에 getById 추가)
        todoRepository.updateTodo(todoItem)
    }
}
