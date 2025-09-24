package com.example.test240402.domain.model

data class TodoItem(
    val id: Int = 0, // 기본값은 필요에 따라 조정
    val content: String,
    val memo: String?,
    val isDone: Boolean,
    val createdAt: Long = System.currentTimeMillis(), // 생성 시간 등 추가 정보
    val alarmTime: Long? = null,         // 알람 설정 시간 (Millis)
    val isAlarmEnabled: Boolean = false  // 알람 활성화 여부
)
