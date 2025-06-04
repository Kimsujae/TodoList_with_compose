package com.example.test240402

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.model.ContentEntity
import com.example.test240402.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputViewModel @Inject constructor(private val contentRepository: ContentRepository) :
    ViewModel() {
    private val _doneEvent = MutableLiveData<Unit>()
    val doneEvent: LiveData<Unit> = _doneEvent

    var content = MutableLiveData<String>()
    var memo = MutableLiveData<String?>()
    var item: ContentEntity? = null

    fun initData(item: ContentEntity) {
        this.item = item
        content.value = item.content
        memo.value = item.memo
    }

    fun insertData() {
        content.value?.let { content ->
            viewModelScope.launch(Dispatchers.IO) {
//                initData(item ?: ContentEntity(content = content, memo = memo.value))
                contentRepository.insert(
                    item?.copy(content = content, memo = memo.value)
                        ?: ContentEntity(content = content, memo = memo.value)
                )
                Log.d("데이터베이스 저장", "저장확인 $content,${memo.value}")
                _doneEvent.postValue(Unit)
            }

        }
    }
}