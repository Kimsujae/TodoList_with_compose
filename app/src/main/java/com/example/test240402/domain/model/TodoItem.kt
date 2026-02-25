package com.example.test240402.domain.model

data class TodoItem(
    val id: Int = 0,
    val content: String,
    val memo: String?,
    val isDone: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val alarmTime: Long? = null,
    val isAlarmEnabled: Boolean = false,
    val latitude: Double? = null,    // 위도 추가
    val longitude: Double? = null,   // 경도 추가
    val placeName: String? = null    // 장소 이름 추가
)
