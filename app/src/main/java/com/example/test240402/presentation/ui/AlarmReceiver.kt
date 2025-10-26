package com.example.test240402.presentation.ui
// com.example.test240402.alarm.AlarmReceiver.kt (예시)
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test240402.R // 실제 R 파일 경로
import com.example.test240402.presentation.ui.MainActivity // 앱 실행 시 열릴 Activity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TODO_ID = "todo_id"
        const val EXTRA_TODO_CONTENT = "todo_content"
        const val EXTRA_TODO_MEMO = "todo_memo"
        private const val CHANNEL_ID = "todo_alarm_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getIntExtra(EXTRA_TODO_ID, -1)
        val todoContent = intent.getStringExtra(EXTRA_TODO_CONTENT) ?: "할 일이 있습니다!"
        val todoMemo = intent.getStringExtra(EXTRA_TODO_MEMO) ?: "메모가 있습니다!"
        Log.d("AlarmReceiver", "onReceive CALLED! - Time: ${System.currentTimeMillis()}")
        Log.d("AlarmReceiver", "Alarm received for Todo ID: $todoId, Content: $todoContent")

        createNotificationChannel(context)

        //알람 클릭 시 아이템 알림 활성화를 위한 intent
//        val clickIntent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            action ="DISABLE_ALARM_ACTION"
//            putExtra("todoId", todoId)
//        }



        // 알림 클릭 시 앱 실행을 위한 Intent
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action ="DISABLE_ALARM_ACTION"
             putExtra("navigate_to_todo_id", todoId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            todoId, // requestCode는 알림별로 고유하게 (알람 requestCode와 동일하게 사용 가능)
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 알림 아이콘 (실제 아이콘으로 변경)
            .setContentTitle(" ${todoContent.take(20)}") // 제목에 내용 일부 표시
            .setContentText("'${todoMemo}' 할 시간입니다.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 높음
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // 기본 알림음, 진동 등
            .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 PendingIntent
            .setAutoCancel(true) // 알림 클릭 시 자동으로 사라지도록
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 알림 ID는 고유해야 함 (여기서도 todoId 사용 가능)
        notificationManager.notify(todoId, notification)

        // TODO: (선택사항) 알람이 울린 후 해당 TodoItem의 isAlarmEnabled를 false로 업데이트하거나, 반복 알람이 아니라면 DB에서 알람 정보 제거
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Todo 알림 채널"
            val descriptionText = "Todo 리스트 알람을 위한 채널입니다."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
