package com.example.test240402.data.mapper

import com.example.test240402.data.model.TodoEntity
import com.example.test240402.domain.model.TodoItem as DomainTodoItem

object TodoMapper {
    fun TodoEntity.toDomain(): DomainTodoItem {
        return DomainTodoItem(
            id = this.id,
            content = this.content,
            memo = this.memo,
            isDone = this.isDone,
            createdAt = this.createdAt,
            alarmTime = this.alarmTime,
            isAlarmEnabled = this.isAlarmEnabled
        )
    }

    fun DomainTodoItem.toEntity(): TodoEntity {
        return TodoEntity(
            id = this.id,
            content = this.content,
            memo = this.memo,
            isDone = this.isDone,
            createdAt = this.createdAt,
            alarmTime = this.alarmTime,
            isAlarmEnabled = this.isAlarmEnabled
        )
    }
//    fun mapToDomainList(entities: List<TodoEntity>): List<DomainTodoItem> {
//        return entities.map { mapToDomain(it) }
//    }
//
//    fun mapToEntityList(domainModels: List<DomainTodoItem>): List<TodoEntity> {
//        return domainModels.map { mapToEntity(it) }
//    }
}