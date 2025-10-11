package com.example.test240402.presentation.ui // 실제 프로젝트 경로에 맞게 수정

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import java.util.Calendar

// 시간/분 단위를 두 자리 문자열로 포맷팅 (예: 7 -> "07")
private fun formatTimeUnit(value: Int): String {
    return String.format("%02d", value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownTimePicker(
    modifier: Modifier = Modifier,
    initialHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY), // 0-23
    initialMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),    // 0-59
    is24HourFormat: Boolean = true, // 12시간제 지원 시 추가 로직 및 AM/PM 드롭다운 필요
    minuteInterval: Int = 1,        // 1, 5, 10, 15 등 분 선택 간격
    onTimeSelected: (hour: Int, minute: Int) -> Unit // 선택된 시간(0-23), 분(0-59) 전달
) {
    var selectedHourState by remember { mutableIntStateOf(initialHour) }
    var selectedMinuteState by remember { mutableIntStateOf(initialMinute) }

    // 선택 가능한 시간 범위 (is24HourFormat에 따라 달라질 수 있음)
    val hours = remember(is24HourFormat) {
        if (is24HourFormat) (0..23).toList() else (1..12).toList()
    }
    // 선택 가능한 분 범위 (minuteInterval 적용)
    val minutes = remember(minuteInterval) {
        (0..59 step minuteInterval).toList()
    }

    // AM/PM 상태 (is24HourFormat = false 일 때만 의미 있음)
    // var amPmState by remember { mutableStateOf(if (initialHour < 12 || initialHour == 24) "AM" else "PM") } // 초기 AM/PM
    // val amPmOptions = listOf("AM", "PM")

    // 부모 컴포저블에 선택된 시간/분 전달
    // LaunchedEffect는 key가 변경될 때마다 실행되므로, 초기값 설정에도 사용될 수 있음
    // 또는 onTimeSelected를 각 Dropdown의 onItemSelected 내부에서 호출할 수도 있음
    LaunchedEffect(selectedHourState, selectedMinuteState /*, amPmState - 12시간제일 경우 */) {
        // 12시간제 -> 24시간제 변환 로직 (is24HourFormat = false 일 때 필요)
        var finalHour = selectedHourState
        // if (!is24HourFormat) {
        //     if (amPmState == "PM" && selectedHourState != 12) finalHour = selectedHourState + 12
        //     if (amPmState == "AM" && selectedHourState == 12) finalHour = 0 // 12 AM is 00:xx
        // }
        onTimeSelected(finalHour, selectedMinuteState)
    }


    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround // 또는 Arrangement.spacedBy(8.dp)
    ) {
        // 시간 선택 드롭다운
        DropdownSelector(
            label = "시",
            items = hours.map { formatTimeUnit(it) }, // 표시용 문자열 리스트
            selectedItem = formatTimeUnit(selectedHourState), // 현재 선택된 값 (표시용)
            onItemSelected = { formattedHourString -> // 선택된 "문자열"
                selectedHourState = formattedHourString.toIntOrNull() ?: initialHour
            },
            modifier = Modifier.weight(1f)
        )

        Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 4.dp))

        // 분 선택 드롭다운
        DropdownSelector(
            label = "분",
            items = minutes.map { formatTimeUnit(it) },
            selectedItem = formatTimeUnit(selectedMinuteState),
            onItemSelected = { formattedMinuteString ->
                selectedMinuteState = formattedMinuteString.toIntOrNull() ?: initialMinute
            },
            modifier = Modifier.weight(1f)
        )

        // AM/PM 선택 드롭다운 (is24HourFormat = false 일 때)
        // if (!is24HourFormat) {
        //     Spacer(modifier = Modifier.width(8.dp))
        //     DropdownSelector(
        //         label = " ", // AM/PM은 레이블 없이 또는 간단히
        //         items = amPmOptions,
        //         selectedItem = amPmState,
        //         onItemSelected = { amPmState = it },
        //         modifier = Modifier.weight(0.8f)
        //     )
        // }
    }
}

// CustomDropdownDatePicker.kt 또는 별도의 custom_ui.kt

// ... (다른 import 및 코드)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = TextUnit.Unspecified,
    dropdownMenuMaxHeight: Dp = 200.dp
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = { /* 읽기 전용 */ },
            readOnly = true,
            label = { Text(label) },
            textStyle = TextStyle(fontSize = fontSize, color = MaterialTheme.colorScheme.onSurface), // 값 텍스트 색상
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                // 명시적 색상 설정
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                // textColor = MaterialTheme.colorScheme.onSurface, // textStyle에서 이미 지정
                // cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface) // 드롭다운 메뉴 배경
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = dropdownMenuMaxHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                items.forEach { itemValue ->
                    DropdownMenuItem(
                        text = { Text(itemValue, fontSize = fontSize, color = MaterialTheme.colorScheme.onSurface) }, // 아이템 텍스트 색상
                        onClick = {
                            onItemSelected(itemValue)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

// CustomDropdownDatePicker 와 CustomDropdownTimePicker 는 DropdownSelector를 사용하므로
// 위 변경사항이 자동으로 반영됩니다.

