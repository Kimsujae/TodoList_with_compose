package com.example.test240402.data.repository

import com.example.test240402.data.datasource.local.TodoDao
import com.example.test240402.data.mapper.TodoMapper.toDomain
import com.example.test240402.data.mapper.TodoMapper.toEntity
import com.example.test240402.domain.model.TodoItem as DomainTodoItem
import com.example.test240402.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {

    override fun getAllTodos(): Flow<List<DomainTodoItem>> {
        return todoDao.getAllTodos().map { todoEntities ->
            todoEntities.map { it.toDomain() }
        }
    }

    override suspend fun insertTodo(todoItem: DomainTodoItem) {
        todoDao.insertTodo(todoItem.toEntity())
    }

    override suspend fun updateTodo(todoItem: DomainTodoItem) {
        todoDao.updateTodo(todoItem.toEntity())
    }

    override suspend fun deleteTodo(todoItem: DomainTodoItem) {
        todoDao.deleteTodo(todoItem.toEntity())
    }

    // 재부팅 시 알람 재등록을 위한 메소드
    override suspend fun getActiveScheduledAlarms(currentTimeMillis: Long): List<DomainTodoItem> {
        return todoDao.getActiveScheduledAlarms(currentTimeMillis).map { it.toDomain() }
    }
}
