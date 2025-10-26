package com.example.test240402.presentation.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.test240402.domain.model.TodoItem
import javax.inject.Inject

interface AlarmScheduler {
    fun schedule(item: TodoItem)
    fun cancel(item: TodoItem)
    fun cancelAllAlarms()
}

class AlarmSchedulerImpl @Inject constructor(
    private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    /**
     * 알람을 예약합니다.
     * 핵심: 예약하려는 시간이 현재 시간보다 과거이면 아무 작업도 하지 않고 로그만 남깁니다.
     */
    override fun schedule(item: TodoItem) {
        // 알람이 비활성화되었거나, 알람 시간이 설정되지 않았으면 즉시 종료
        if (!item.isAlarmEnabled || item.alarmTime == null) {
            return
        }

        // --- 핵심 방어 로직 ---
        if (item.alarmTime <= System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "ID ${item.id}의 알람 예약 시도: 시간이 이미 지났으므로 예약하지 않습니다.")
            return // 과거 시간이므로 알람을 예약하지 않고 함수 종료
        }
        // ---------------------

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // 알람 발생 시 전달할 데이터 (기존 로직 유지)
            putExtra(AlarmReceiver.EXTRA_TODO_ID, item.id)
            putExtra(AlarmReceiver.EXTRA_TODO_CONTENT, item.content)
            putExtra(AlarmReceiver.EXTRA_TODO_MEMO, item.memo)
        }

        try {
            // 정확한 알람 설정 (Doze 모드에서도 동작)
            createPendingIntent(item.id, intent)?.let {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    item.alarmTime,
                    it
                )
            }
            Log.d("AlarmScheduler", "ID ${item.id} 알람 예약 완료. 시간: ${java.util.Date(item.alarmTime)}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "알람 예약 실패: SCHEDULE_EXACT_ALARM 권한이 없습니다.", e)
        }
    }

    /**
     * 특정 아이템의 알람을 취소합니다.
     */
    override fun cancel(item: TodoItem) {
        val intent = Intent(context, AlarmReceiver::class.java)

        // 중요: 'FLAG_NO_CREATE'를 사용하여 예약된 알람이 있을 때만 PendingIntent를 가져옵니다.
        val pendingIntent = createPendingIntent(item.id, intent, PendingIntent.FLAG_NO_CREATE)

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("AlarmScheduler", "ID ${item.id} 알람 취소 완료.")
        } else {
            // 이 로그는 정상적인 상황에서도 (예: 알람이 없는 항목을 삭제할 때) 발생할 수 있습니다.
            Log.w("AlarmScheduler", "ID ${item.id}에 해당하는 알람을 찾지 못해 취소할 수 없습니다.")
        }
    }

    /**
     * 앱 설치 후 최초 실행 시 남아있는 모든 유령 알람을 제거합니다.
     */
    override fun cancelAllAlarms() {
        Log.d("GHOST_ALARM_DEBUG", "모든 유령 알람 취소를 시작합니다.")
        val intent = Intent(context, AlarmReceiver::class.java)

        // ID 범위를 넓게 잡아 기존에 생성되었을 수 있는 모든 알람을 순회하며 확인
        for (requestCode in 0..10000) {
            val pendingIntent = createPendingIntent(requestCode, intent, PendingIntent.FLAG_NO_CREATE)

            if (pendingIntent != null) {
                Log.d("GHOST_ALARM_DEBUG", "requestCode $requestCode 에서 유령 알람을 발견하여 취소합니다.")
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        Log.d("GHOST_ALARM_DEBUG", "유령 알람 취소 작업 완료.")
    }

    /**
     * PendingIntent를 생성하는 헬퍼 함수.
     * 플래그를 통일하여 생성과 취소 시 동일한 객체를 참조하도록 보장합니다.
     */
    private fun createPendingIntent(requestCode: Int, intent: Intent, flags: Int = 0): PendingIntent? {
        // requestCode를 Int로 변환 (TodoItem의 id가 Long이므로)
        val finalRequestCode = requestCode.toInt()

        val finalFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 안드로이드 12 (S) 이상에서는 FLAG_IMMUTABLE 또는 FLAG_MUTABLE 중 하나를 명시해야 함
            flags or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            flags or PendingIntent.FLAG_UPDATE_CURRENT
        }

        // FLAG_NO_CREATE가 포함된 경우, PendingIntent가 없으면 null을 반환할 수 있음
        return PendingIntent.getBroadcast(
            context,
            finalRequestCode,
            intent,
            finalFlags
        )
    }
}
