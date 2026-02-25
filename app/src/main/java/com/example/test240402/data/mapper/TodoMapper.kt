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
            isAlarmEnabled = this.isAlarmEnabled,
            latitude = this.latitude,
            longitude = this.longitude,
            placeName = this.placeName
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
            isAlarmEnabled = this.isAlarmEnabled,
            latitude = this.latitude,
            longitude = this.longitude,
            placeName = this.placeName
        )
    }
}
