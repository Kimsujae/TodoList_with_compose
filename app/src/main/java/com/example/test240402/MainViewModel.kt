package com.example.test240402

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.model.ContentEntity
import com.example.test240402.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    //    val contentList = contentRepository.loadList()
//        .stateIn(
//            initialValue = emptyList(),
//            started = SharingStarted.WhileSubscribed(5000),
//            scope = viewModelScope
//        )
    init {
        getAllContent()
    }

    private val _contentList = MutableStateFlow<List<ContentEntity>>(emptyList())
    val contentList: StateFlow<List<ContentEntity>> = _contentList


    private fun getAllContent() {
        viewModelScope.launch {
            contentRepository.loadList().collectLatest {
                _contentList.value = it
            }
        }
    }

    fun deleteItem(item: ContentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            contentRepository.delete(item)
        }
    }

    fun updateItem(item: ContentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            contentRepository.modify(item)
        }
    }
}