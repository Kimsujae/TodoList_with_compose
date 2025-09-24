package com.example.test240402.presentation.ui// com.example.test240402.alarm.AlarmScheduler.kt (예시)
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.test240402.domain.model.TodoItem
import javax.inject.Inject


interface AlarmScheduler {
    fun schedule(item: TodoItem)
    fun cancel(item: TodoItem)
}

class AlarmSchedulerImpl @Inject constructor (
    private val context: Context
): AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(item: TodoItem) {
        if (!item.isAlarmEnabled || item.alarmTime == null || item.alarmTime <= System.currentTimeMillis()) {
            // 이미 시간이 지났거나 알람이 비활성화된 경우 스케줄링하지 않음
            return
        }
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // 알람 발생 시 전달할 데이터 (예: Todo 아이템 ID, 내용)
            putExtra(AlarmReceiver.EXTRA_TODO_ID, item.id)
            putExtra(AlarmReceiver.EXTRA_TODO_CONTENT, item.content)
        }

        // 각 알람마다 고유한 requestCode가 필요 (여기서는 TodoItem의 id 사용)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id, // requestCode: 각 알람을 구별하기 위한 고유 ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // 정확한 알람 설정 (API 31+ 에서는 SCHEDULE_EXACT_ALARM 권한 필요)
            // setExactAndAllowWhileIdle : Doze 모드에서도 알람이 울리도록 함
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, // 실제 시간 기준, 기기가 꺼져있으면 깨워서 알람 실행
                item.alarmTime,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarm scheduled for item ID ${item.id} at ${item.alarmTime}")
        } catch (se: SecurityException) {
            Log.e("AlarmScheduler", "Missing SCHEDULE_EXACT_ALARM permission?", se)
            // 사용자에게 권한 요청 안내 또는 대체 알람(부정확한 알람) 고려
        }
    }

    override fun cancel(item: TodoItem) {
        val intent = Intent(context, AlarmReceiver::class.java)
        // 스케줄링 시 사용한 동일한 requestCode로 PendingIntent 생성
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Alarm canceled for item ID ${item.id}")
    }
}
