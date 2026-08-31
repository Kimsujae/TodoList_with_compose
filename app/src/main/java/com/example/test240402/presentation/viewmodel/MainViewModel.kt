package com.example.test240402.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.usecase.DeleteTodoUseCase
import com.example.test240402.domain.usecase.GetTodosUseCase
import com.example.test240402.domain.usecase.UpdateTodoUseCase
import com.example.test240402.presentation.ui.AlarmScheduler
import com.example.test240402.presentation.ui.GeofenceScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateTodoUseCase: UpdateTodoUseCase,
    private val getTodosUseCase: GetTodosUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceScheduler: GeofenceScheduler
) : ViewModel() {

    private val _todoListStream = getTodosUseCase()
    val todoList: StateFlow<List<TodoItem>> = getTodosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        _todoListStream.onEach { items ->
            cleanupExpiredAlarms(items)
        }.launchIn(viewModelScope)
    }

    private fun cleanupExpiredAlarms(items: List<TodoItem>) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            items.forEach { item ->
                // 알람 시간이 지났고, 아직 완료되지 않았으며, 아직 'missed' 처리되지 않은 경우
                if (item.isAlarmEnabled && item.alarmTime != null && item.alarmTime < currentTime && !item.isDone && !item.isMissed) {
                    updateTodoUseCase(item.copy(isAlarmEnabled = false, isMissed = true))
                } else if (item.isAlarmEnabled && item.alarmTime != null && item.alarmTime < currentTime) {
                    // 이미 완료된 항목인데 알람만 켜져있는 경우 등은 알람만 끔
                    updateTodoUseCase(item.copy(isAlarmEnabled = false))
                }
            }
        }
    }

    fun deleteItem(item: TodoItem) {
        viewModelScope.launch {
            alarmScheduler.cancel(item)
            geofenceScheduler.cancel(item) // 지오펜싱 해제
            deleteTodoUseCase(item)
        }
    }

    fun updateItem(item: TodoItem) {
        viewModelScope.launch {
            alarmScheduler.cancel(item)
            geofenceScheduler.cancel(item) // 기존 지오펜싱 해제

            updateTodoUseCase(item)

            // 알람 재등록
            if (item.isAlarmEnabled && item.alarmTime != null && item.alarmTime > System.currentTimeMillis()) {
                alarmScheduler.schedule(item)
            }
            
            // 지오펜싱 재등록 (위치 정보가 있는 경우)
            if (item.latitude != null && item.longitude != null) {
                geofenceScheduler.schedule(item)
            }
        }
    }

    fun disableAlarmForTodoItem(itemId: Long) = viewModelScope.launch {
        val item = todoList.value.find { it.id.toLong() == itemId }
        item?.let {
            if (it.isAlarmEnabled) {
                alarmScheduler.cancel(it)
                updateTodoUseCase(it.copy(isAlarmEnabled = false))
            }
        }
    }
}
