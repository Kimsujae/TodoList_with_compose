package com.example.test240402.data.mapper // data 모듈 내 패키지

import com.example.test240402.data.model.TodoEntity // Room Entity (기존 ContentEntity에서 이름 변경 가정)
import com.example.test240402.domain.model.TodoItem as DomainTodoItem

// DomainTodoItem -> TodoEntity
fun DomainTodoItem.toEntity(): TodoEntity {
    return TodoEntity(
        id = this.id,
        content = this.content,
        memo = this.memo,
        isDone = this.isDone
    )
}

// TodoEntity -> DomainTodoItem
fun TodoEntity.toDomain(): DomainTodoItem {
    return DomainTodoItem(
        id = this.id,
        content = this.content,
        memo = this.memo,
        isDone = this.isDone
    )
}
            