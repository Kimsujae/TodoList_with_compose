package com.example.test240402.data.repository // data 모듈 내 패키지

//import com.example.test240402.data.dao.ContentDao
import android.util.Log
import com.example.test240402.data.datasource.local.TodoDao
import com.example.test240402.data.mapper.toDomain // Domain 모델로 매핑하는 확장 함수
import com.example.test240402.data.mapper.toEntity // Room 엔티티로 매핑하는 확장 함수
import com.example.test240402.domain.model.TodoItem as DomainTodoItem // Domain 모델 import alias
import com.example.test240402.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {

    override fun getAllTodos(): Flow<List<DomainTodoItem>> {
        return todoDao.getAllTodos().map { todoEntities ->
            todoEntities.map { it.toDomain() } // Room 엔티티 리스트를 Domain 모델 리스트로 매핑
        }.onEach { domainTodoItems -> // 매핑된 Domain 모델 리스트를 로깅
            Log.d("!로그Repository", "Todos from DB (Domain Models): \n $domainTodoItems")
            if (domainTodoItems.isEmpty()) {
                Log.d("!로그Repository", "No todos found in the database.")
            } else {
                domainTodoItems.forEachIndexed { index, item ->
                    Log.d("!로그Repository", "Item $index: ${item.content}") // 예시: 각 아이템의 제목 로깅
                }
            }
        }
    }

    override suspend fun insertTodo(todoItem: DomainTodoItem) {
        todoDao.insertTodo(todoItem.toEntity())
        Log.d("!로그Repository", "Todo inserted: ${todoItem.content}") // Domain 모델을 Room 엔티티로 매핑하여 저장
    }

    override suspend fun updateTodo(todoItem: DomainTodoItem) {
        todoDao.updateTodo(todoItem.toEntity())
    }

    override suspend fun deleteTodo(todoItem: DomainTodoItem) {
        todoDao.deleteTodo(todoItem.toEntity())
    }
}
            