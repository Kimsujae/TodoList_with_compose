package com.example.test240402.presentation.viewmodel

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.usecase.DeleteTodoUseCase
import com.example.test240402.domain.usecase.GetTodosUseCase
import com.example.test240402.domain.usecase.UpdateTodoUseCase
import com.example.test240402.presentation.ui.AlarmScheduler
//import com.example.test240402.model.ContentEntity
//import com.example.test240402.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
//    private val contentRepository: ContentRepository,
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val getTodosUseCase: GetTodosUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    //    val contentList = contentRepository.loadList()
//        .stateIn(
//            initialValue = emptyList(),
//            started = SharingStarted.WhileSubscribed(5000),
//            scope = viewModelScope
//        )
    init {
        cleanupExpiredAlarms()
    }

    private fun cleanupExpiredAlarms() {
        viewModelScope.launch {
            // Flow가 데이터를 로드할 때까지 잠시 기다리거나, 첫 번째 데이터를 사용합니다.
            // todoList.first()를 사용하여 초기 데이터 로드 후 한 번만 실행되도록 합니다.
            val initialList = todoList?.first { it.isNotEmpty() } // 리스트가 로드될 때까지 기다립니다.
            val currentTime = System.currentTimeMillis()

            initialList?.forEach { item ->
                // 알람이 활성화되어 있고, 알람 시간이 현재 시간보다 과거인 경우
                if (item.isAlarmEnabled && item.alarmTime != null && item.alarmTime < currentTime) {
                    Log.d("MainViewModel", "만료된 알람 정리: ${item.content}")
                    // isAlarmEnabled를 false로 변경하여 업데이트
                    val updatedItem = item.copy(isAlarmEnabled = false)
                    updateTodoUseCase(updatedItem)
                }
            }
        }
    }
    val todoList: StateFlow<List<TodoItem>> = getTodosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    fun deleteItem(item: TodoItem){
        viewModelScope.launch {
            Log.d("MainViewModel", "deleteItem called for item: ${item.content}")
            alarmScheduler.cancel(item)
            deleteTodoUseCase(item)
        }
    }
    fun updateItem(item: TodoItem){
        viewModelScope.launch {
            alarmScheduler.cancel(item)

            updateTodoUseCase(item)

            if (item.isAlarmEnabled && item.alarmTime != null && item.alarmTime > System.currentTimeMillis()) {
                Log.d("MainViewModel", "Scheduling alarm for item: ${item.content} at ${item.alarmTime}")
                alarmScheduler.schedule(item)
            }
        }
    }
    fun disableAlarmForTodoItem(itemId: Long) = viewModelScope.launch {
        // 현재 StateFlow의 값에서 ID를 가진 아이템을 찾습니다.
        val item = todoList.value.find { it.id.toLong() == itemId }

        item?.let {
            // 해당 아이템의 알람이 실제로 활성화 상태인지 확인
            if (it.isAlarmEnabled) {
                Log.d("MainViewModel", "Disabling alarm for item ID: $itemId via notification click.")

                // 1. 시스템에 예약된 알람 취소
                alarmScheduler.cancel(it)

                // 2. 아이템의 알람 상태를 false로 변경하여 DB에 업데이트
                val updatedItem = it.copy(isAlarmEnabled = false)
                updateTodoUseCase(updatedItem) // DB 업데이트
            } else {
                Log.d("MainViewModel", "Item ID $itemId alarm is already disabled.")
            }
        } ?: Log.e("MainViewModel", "Item with ID $itemId not found to disable alarm.")
    }



//    private val _contentList = MutableStateFlow<List<ContentEntity>>(emptyList())
//    val contentList: StateFlow<List<ContentEntity>> = _contentList


//    private fun getAllContent() {
//        viewModelScope.launch {
//            contentRepository.loadList().collectLatest {
//                _contentList.value = it
//            }
//        }
//    }
//
//    fun deleteItem(item: ContentEntity) {
//        viewModelScope.launch(Dispatchers.IO) {
//            contentRepository.delete(item)
//        }
//    }
//
//    fun updateItem(item: ContentEntity) {
//        viewModelScope.launch(Dispatchers.IO) {
//            contentRepository.modify(item)
//        }
//    }
}