package com.example.test240402.presentation.ui // 실제 패키지 경로로 수정하세요

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
import androidx.compose.ui.unit.sp
import java.util.Calendar

// 시간/분 단위를 두 자리 문자열로 포맷팅 (예: 7 -> "07") - 월, 일 표시에 사용 가능
private fun formatTwoDigits(value: Int): String {
    return String.format("%02d", value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownDatePicker(
    modifier: Modifier = Modifier,
    initialDateMillis: Long?, // 초기 날짜 (선택 사항)
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit, // 월은 1-12로 전달
    dropdownMenuMaxHeight: Dp = 200.dp // 각 드롭다운 메뉴의 최대 높이
) {
    val calendar = remember { Calendar.getInstance() } // remember로 감싸서 recomposition 방지
    // 초기 날짜 설정 (LaunchedEffect 또는 key를 사용하여 initialDateMillis 변경 시에도 반영되도록 할 수 있음)
    // 여기서는 Composable이 처음 로드될 때 또는 initialDateMillis가 non-null일 때 한 번 설정
    LaunchedEffect(initialDateMillis) {
        if (initialDateMillis != null) {
            calendar.timeInMillis = initialDateMillis
        }
        // 상태 변수들을 calendar 값으로 초기화 (initialDateMillis가 null이면 현재 날짜)
        // 이 로직은 selectedYear, selectedMonth, selectedDay의 remember 초기값으로 이동하는 것이 더 명확할 수 있습니다.
    }

    var selectedYear by remember(initialDateMillis) { // initialDateMillis 변경 시 년도 재설정
        mutableIntStateOf( (initialDateMillis?.let { calendar.apply { timeInMillis = it }.get(Calendar.YEAR) }
            ?: Calendar.getInstance().get(Calendar.YEAR)) )
    }
    var selectedMonth by remember(initialDateMillis) { // initialDateMillis 변경 시 월 재설정
        mutableIntStateOf( (initialDateMillis?.let { calendar.apply { timeInMillis = it }.get(Calendar.MONTH) + 1 }
            ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)) )
    }
    var selectedDay by remember(initialDateMillis) { // initialDateMillis 변경 시 일 재설정
        mutableIntStateOf( (initialDateMillis?.let { calendar.apply { timeInMillis = it }.get(Calendar.DAY_OF_MONTH) }
            ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) )
    }


    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    // 년도 선택 범위: 현재 년도부터 향후 10년
    val years = remember { (currentYear..currentYear + 10).toList() }
    // 월 선택 범위: 1월부터 12월
    val months = remember { (1..12).toList() }

    // 선택된 년/월에 따른 일 범위 동적 계산
    val daysInMonth = remember(selectedYear, selectedMonth) {
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(Calendar.YEAR, selectedYear)
        tempCalendar.set(Calendar.MONTH, selectedMonth - 1) // Calendar.MONTH는 0-11
        (1..tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toList()
    }

    LaunchedEffect(daysInMonth, selectedDay) {
        if (daysInMonth.isNotEmpty() && selectedDay > daysInMonth.size ) { // 조건 수정: daysInMonth 비어있지 않을 때
            selectedDay = daysInMonth.last()
        }
    }

    // 최종 선택된 날짜 콜백
    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        onDateSelected(selectedYear, selectedMonth, selectedDay)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp) // 각 드롭다운 사이 간격
    ) {
        // 년도 선택
        DropdownSelector(
            label = "년",
            items = years.map { it.toString() },
            selectedItem = selectedYear.toString(),
            onItemSelected = { selectedYear = it.toIntOrNull() ?: currentYear },
            modifier = Modifier.weight(0.38f), // 년도 비율 증가 (예: 40%)
            fontSize = 13.sp, // 년도 폰트 크기 조절 (필요시)
            dropdownMenuMaxHeight = dropdownMenuMaxHeight
        )

        // 월 선택
        DropdownSelector(
            label = "월",
            items = months.map { formatTwoDigits(it) },
            selectedItem = formatTwoDigits(selectedMonth),
            onItemSelected = { selectedMonth = it.toIntOrNull() ?: 1 },
            modifier = Modifier.weight(0.31f), // 월 비율 (예: 30%)
            fontSize = 13.sp, // 월/일 폰트 크기 조절
            dropdownMenuMaxHeight = dropdownMenuMaxHeight
        )

        // 일 선택
        DropdownSelector(
            label = "일",
            items = daysInMonth.map { formatTwoDigits(it) },
            selectedItem = formatTwoDigits(selectedDay),
            onItemSelected = { selectedDay = it.toIntOrNull() ?: 1 },
            modifier = Modifier.weight(0.31f), // 일 비율 (예: 30%)
            fontSize = 13.sp, // 월/일 폰트 크기 조절
            dropdownMenuMaxHeight = dropdownMenuMaxHeight
        )
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
            colors = TextFieldDefaults.outlinedTextFieldColors( // 명시적 색상 설정
                focusedBorderColor = MaterialTheme.colorScheme.primary,                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
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




