package com.example.test240402.domain.model

data class TodoItem(
    val id: Int = 0, // 기본값은 필요에 따라 조정
    val content: String,
    val memo: String?,
    val isDone: Boolean
)
