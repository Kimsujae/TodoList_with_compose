package com.example.test240402.presentation.ui

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.test240402.domain.model.TodoItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.format

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItemRow( // 아이템 하나를 그리는 Composable 분리 고려
    currentItem: TodoItem,
    onUpdateItem: (TodoItem) -> Unit, // 완료 상태 업데이트는 그대로 유지 가능
    onRequestDeleteItem: (TodoItem) -> Unit, // 삭제 요청 콜백
    onRequestEditItem: (TodoItem) -> Unit   // 수정 요청 콜백
) {
    val itemBackgroundColor = if (currentItem.isDone) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) // 완료 시 약간 흐리게
    } else {
        MaterialTheme.colorScheme.surface // 기본 카드 배경 또는 Todo 색상
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // 카드 간 간격
            .combinedClickable(
                onClick = {
                    // 예: 짧은 클릭 시 완료 상태 토글
                    Log.d("TodoItemRow", "Card onClick for: ${currentItem.content} (Toggle isDone)")
                    onUpdateItem(currentItem.copy(isDone = !currentItem.isDone))
                },
                onLongClick = {
                    Log.d("TodoItemRow", "Card onLongClick for DELETE: ${currentItem.content}")
                    onRequestDeleteItem(currentItem) // MainView에 삭제 요청
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = itemBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp), // 카드 내부 패딩
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = currentItem.isDone,
                onCheckedChange = { checked ->
                    Log.d("TodoItemRow", "Checkbox for '${currentItem.content}' changed to: $checked")
                    onUpdateItem(currentItem.copy(isDone = checked))
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val textColor = if (currentItem.isDone) {
                    MaterialTheme.colorScheme.onSurfaceVariant // 완료 시 텍스트도 약간 연하게
                } else {
                    MaterialTheme.colorScheme.onSurface // 기본 텍스트 색상
                }
                val textDecoration = if (currentItem.isDone) TextDecoration.LineThrough else null

                Text(
                    text = currentItem.content, // "할일: " 접두사 제거 (선택 사항, 더 깔끔해 보일 수 있음)
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    textDecoration = textDecoration,
                    maxLines = 2, // 필요에 따라 여러 줄 표시
                    overflow = TextOverflow.Ellipsis
                )
                currentItem.memo?.takeIf { it.isNotBlank() }?.let { memo ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memo, // "메모: " 접두사 제거
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentItem.isDone) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // --- 알람 시간 표시 추가 ---
                if (currentItem.isAlarmEnabled && currentItem.alarmTime != null) {
                    Spacer(modifier = Modifier.height(6.dp)) // 메모와 알람 시간 사이 간격
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "알람 설정 시간",
                            modifier = Modifier.size(16.dp), // 아이콘 크기
                            tint = if (currentItem.isDone) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = SimpleDateFormat(
                                "MM.dd HH:mm",
                                java.util.Locale.getDefault()
                            ).format(Date(currentItem.alarmTime)),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (currentItem.isDone) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary
                            ),
                            textDecoration = textDecoration // 알람 시간에도 취소선 적용
                        )
                    }
                }
                // --- 알람 시간 표시 끝 ---
            }
            Spacer(modifier = Modifier.width(8.dp)) // 텍스트와 버튼 사이 간격

            IconButton(onClick = {
                Log.d("TodoItemRow", "Edit IconButton clicked for: ${currentItem.content}")
                onRequestEditItem(currentItem) // MainView에 수정 요청
                Log.d("TodoItemRow", "Edit IconButton clicked for: ${currentItem.content}")}) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "수정",
                    tint = MaterialTheme.colorScheme.primary // 아이콘 색상도 테마에 맞게
                )
            }
        }
    }
}