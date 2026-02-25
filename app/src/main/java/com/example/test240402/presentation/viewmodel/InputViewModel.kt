package com.example.test240402.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.usecase.InsertTodoUseCase
import com.example.test240402.presentation.ui.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
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
            viewModelScope.launch {
                Log.d("!로그 데이터베이스저장","$newTodo")
                insertTodoUseCase(newTodo)

                if(newTodo.isAlarmEnabled && newTodo.alarmTime!= null){
                    if(newTodo.alarmTime > System.currentTimeMillis()){
                        alarmScheduler.schedule(newTodo)
                    }
                }

                _content.value = ""
                _memo.value = ""
                updateLocation(null, null, null)
            }
        }
    }
}
