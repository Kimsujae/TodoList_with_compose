package com.example.test240402.data.datasource.local // 또는 data.local.dao 등 적절한 패키지

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.test240402.data.model.TodoEntity // Room 엔티티 클래스
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    /**
     * 데이터베이스에 저장된 모든 할 일 아이템을 가져옵니다.
     * ID를 기준으로 내림차순 정렬하여 최신 아이템이 먼저 오도록 합니다.
     * Flow를 반환하여 데이터 변경 시 자동으로 UI에 반영될 수 있도록 합니다.
     */
    @Query("SELECT * FROM todo_items ORDER BY id DESC") // "todo_items"는 TodoEntity에 정의된 테이블 이름
    fun getAllTodos(): Flow<List<TodoEntity>>

    /**
     * 새로운 할 일 아이템을 데이터베이스에 삽입합니다.
     * 만약 동일한 ID를 가진 아이템이 이미 존재한다면, 기존 아이템을 새로운 아이템으로 대체합니다 (REPLACE 전략).
     * @param todoEntity 삽입할 할 일 엔티티.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todoEntity: TodoEntity)

    /**
     * 기존 할 일 아이템을 업데이트합니다.
     * @param todoEntity 업데이트할 할 일 엔티티. 엔티티의 ID를 기준으로 기존 아이템을 찾아 업데이트합니다.
     */
    @Update
    suspend fun updateTodo(todoEntity: TodoEntity)

    /**
     * 할 일 아이템을 데이터베이스에서 삭제합니다.
     * @param todoEntity 삭제할 할 일 엔티티. 엔티티의 ID를 기준으로 기존 아이템을 찾아 삭제합니다.
     */
    @Delete
    suspend fun deleteTodo(todoEntity: TodoEntity)

    // 필요에 따라 특정 ID로 아이템을 가져오는 메서드 등을 추가할 수 있습니다.
    // 예:
    // @Query("SELECT * FROM todo_items WHERE id = :id")
    // suspend fun getTodoById(id: Int): TodoEntity?
}
