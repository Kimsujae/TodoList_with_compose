package com.example.test240402

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test240402.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
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

    fun insertData() {
        content.value?.let { content ->
            viewModelScope.launch(Dispatchers.IO) {
                contentRepository.insert(
                    ContentEntity(content = content, memo = memo.value)
                )
                _doneEvent.postValue(Unit)
            }

        }
    }
}