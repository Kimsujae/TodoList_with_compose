package com.example.test240402.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.usecase.InsertTodoUseCase
import com.example.test240402.presentation.ui.AlarmScheduler
import com.example.test240402.presentation.ui.GeofenceScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val geofenceScheduler: GeofenceScheduler,
    private val insertTodoUseCase: InsertTodoUseCase
) : ViewModel() {
    private val _doneEvent = MutableLiveData<Unit>()
    val doneEvent: LiveData<Unit> = _doneEvent

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _memo = MutableStateFlow("")
    val memo: StateFlow<String> = _memo.asStateFlow()

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    private val _placeName = MutableStateFlow<String?>(null)
    val placeName: StateFlow<String?> = _placeName.asStateFlow()

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun updateMemo(newMemo: String) {
        _memo.value = newMemo
    }

    fun updateLocation(lat: Double?, lng: Double?, name: String?) {
        _latitude.value = lat
        _longitude.value = lng
        _placeName.value = name
    }

    fun insertData(
        content: String,
        memo: String,
        alarmTime: Long?,
        isAlarmEnabled: Boolean,
        latitude: Double? = null,
        longitude: Double? = null,
        placeName: String? = null
    ) {
        if (content.isNotBlank() ) {
            viewModelScope.launch {
                val newTodo = TodoItem(
                    content = content,
                    memo = memo.ifEmpty { null },
                    isDone = false,
                    alarmTime = alarmTime,
                    isAlarmEnabled = isAlarmEnabled,
                    latitude = latitude ?: _latitude.value,
                    longitude = longitude ?: _longitude.value,
                    placeName = placeName ?: _placeName.value
                )
                
                // DB 저장 (id가 생성됨)
                val id = insertTodoUseCase(newTodo).toInt()
                val todoWithId = newTodo.copy(id = id)

                // 알람 등록
                if(todoWithId.isAlarmEnabled && todoWithId.alarmTime != null){
                    if(todoWithId.alarmTime > System.currentTimeMillis()){
                        alarmScheduler.schedule(todoWithId)
                    }
                }

                // 지오펜싱 등록 (위치 정보가 있을 때만)
                if (todoWithId.latitude != null && todoWithId.longitude != null) {
                    geofenceScheduler.schedule(todoWithId)
                }

                _content.value = ""
                _memo.value = ""
                updateLocation(null, null, null)
            }
        }
    }
}
