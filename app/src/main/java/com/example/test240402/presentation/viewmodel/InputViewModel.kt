package com.example.test240402.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.domain.usecase.InsertTodoUseCase
import com.example.test240402.domain.usecase.UpdateTodoUseCase
//import com.example.test240402.model.ContentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(
    private val insertTodoUseCase: InsertTodoUseCase
) : ViewModel() {
    private val _doneEvent = MutableLiveData<Unit>()
    val doneEvent: LiveData<Unit> = _doneEvent

    var content = mutableStateOf("")
    var memo = mutableStateOf("")
//    var content = MutableLiveData<String>()
//    var memo = MutableLiveData<String?>()
//    var item: ContentEntity? = null

//    fun initData(item: ContentEntity) {
//        this.item = item
//        content.value = item.content
//        memo.value = item.memo
//    }
    fun insertData() {
        if (content.value.isNotBlank() ) {
            val newTodo = TodoItem( // id는 Repository 또는 DB에서 자동 생성되도록 할 수 있음
                content = content.value,
                memo = memo.value.ifEmpty { null }, // 비어있으면 null로
                isDone = false
            )
            viewModelScope.launch {
                Log.d("!로그 데이터베이스저장","$newTodo")
                insertTodoUseCase(newTodo)
                content.value = ""
                memo.value = ""
                // _doneEvent.postValue(Unit) // 또는 tương tự
            }
        }
    }
//    fun insertData() {
//        content.value?.let { content ->
//            viewModelScope.launch(Dispatchers.IO) {
////                initData(item ?: ContentEntity(content = content, memo = memo.value))
//                contentRepository.insert(
//                    item?.copy(content = content, memo = memo.value)
//                        ?: ContentEntity(content = content, memo = memo.value)
//                )
//                Log.d("데이터베이스 저장", "저장확인 $content,${memo.value}")
//                _doneEvent.postValue(Unit)
//            }
//
//        }
//    }
}