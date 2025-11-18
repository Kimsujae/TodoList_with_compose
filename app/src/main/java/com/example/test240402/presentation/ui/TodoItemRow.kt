package com.example.test240402.presentation.ui

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.test240402.domain.model.TodoItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItemRow(
    currentItem: TodoItem,
    onUpdateItem: (TodoItem) -> Unit,
    onRequestDeleteItem: (TodoItem) -> Unit,
    onRequestEditItem: (TodoItem) -> Unit
) {
    val itemBackgroundColor = if (currentItem.isDone) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) // 완료 시 약간 흐리게
    } else {
        MaterialTheme.colorScheme.surface // 기본 카드 배경
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // 카드 간 간격
            .combinedClickable(
                onClick = {
                    // 짧은 클릭 시 완료 상태 토글
                    Log.d("TodoItemRow", "Card onClick for: ${currentItem.content} (Toggle isDone)")
                    onUpdateItem(currentItem.copy(isDone = !currentItem.isDone))
                },
                onLongClick = {
                    Log.d("TodoItemRow", "Card onLongClick for DELETE: ${currentItem.content}")
                    onRequestDeleteItem(currentItem)
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
                    text = currentItem.content,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    textDecoration = textDecoration,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                currentItem.memo?.takeIf { it.isNotBlank() }?.let { memo ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (currentItem.isDone) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                                Locale.getDefault()
                            ).format(Date(currentItem.alarmTime)),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (currentItem.isDone) textColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary
                            ),
                            textDecoration = textDecoration // 알람 시간에도 취소선 적용
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp)) // 텍스트와 버튼 사이 간격

            IconButton(onClick = {
                Log.d("TodoItemRow", "Edit IconButton clicked for: ${currentItem.content}")
                onRequestEditItem(currentItem)
            }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "수정",
                    tint = MaterialTheme.colorScheme.primary // 아이콘 색상도 테마에 맞게
                )
            }
        }
    }
}