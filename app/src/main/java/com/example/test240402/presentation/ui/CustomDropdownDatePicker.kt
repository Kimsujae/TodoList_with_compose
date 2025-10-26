package com.example.test240402.presentation.ui // 실제 패키지 경로로 수정하세요

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
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
    // ... (이 부분의 코드는 기존과 동일) ...
    val calendar = remember { Calendar.getInstance() }
    LaunchedEffect(initialDateMillis) {
        if (initialDateMillis != null) {
            calendar.timeInMillis = initialDateMillis
        }
    }
    var selectedYear by remember(initialDateMillis) {
        mutableIntStateOf( (initialDateMillis?.let { calendar.apply { timeInMillis = it }.get(Calendar.YEAR) }
            ?: Calendar.getInstance().get(Calendar.YEAR)) )
    }
    var selectedMonth by remember(initialDateMillis) {
        mutableIntStateOf( (initialDateMillis?.let { calendar.apply { timeInMillis = it }.get(Calendar.MONTH) + 1 }
            ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)) )
    }
    var selectedDay by remember(initialDateMillis) {
        mutableIntStateOf( (initialDateMillis?.let { calendar.apply { timeInMillis = it }.get(Calendar.DAY_OF_MONTH) }
            ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) )
    }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val years = remember { (currentYear..currentYear + 10).toList() }
    val months = remember { (1..12).toList() }
    val daysInMonth = remember(selectedYear, selectedMonth) {
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(Calendar.YEAR, selectedYear)
        tempCalendar.set(Calendar.MONTH, selectedMonth - 1)
        (1..tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toList()
    }
    LaunchedEffect(daysInMonth, selectedDay) {
        if (daysInMonth.isNotEmpty() && selectedDay > daysInMonth.size ) {
            selectedDay = daysInMonth.last()
        }
    }
    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        onDateSelected(selectedYear, selectedMonth, selectedDay)
    }
    // -------------------------------------------------------------------

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // --- 1. 핵심 수정: 함수 호출 이름을 변경 ---
        DateDropdownSelector(
            label = "년",
            items = years.map { it.toString() },
            selectedItem = selectedYear.toString(),
            onItemSelected = { selectedYear = it.toIntOrNull() ?: currentYear },
            modifier = Modifier.weight(0.38f),
            fontSize = 13.sp,
            dropdownMenuMaxHeight = dropdownMenuMaxHeight
        )
        DateDropdownSelector(
            label = "월",
            items = months.map { formatTwoDigits(it) },
            selectedItem = formatTwoDigits(selectedMonth),
            onItemSelected = { selectedMonth = it.toIntOrNull() ?: 1 },
            modifier = Modifier.weight(0.31f),
            fontSize = 13.sp,
            dropdownMenuMaxHeight = dropdownMenuMaxHeight
        )
        DateDropdownSelector(
            label = "일",
            items = daysInMonth.map { formatTwoDigits(it) },
            selectedItem = formatTwoDigits(selectedDay),
            onItemSelected = { selectedDay = it.toIntOrNull() ?: 1 },
            modifier = Modifier.weight(0.31f),
            fontSize = 13.sp,
            dropdownMenuMaxHeight = dropdownMenuMaxHeight
        )
    }
}

// --- 2. 핵심 수정: 함수 정의 부분의 이름을 변경 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDropdownSelector(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    dropdownMenuMaxHeight: Dp = 200.dp
) {
    var expanded by remember { mutableStateOf(false) }

    var internalText by remember(label, selectedItem) { mutableStateOf(selectedItem) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (!expanded) {
                onItemSelected(internalText)
            }
            expanded = !expanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = internalText,
            onValueChange = { userInput ->
                if (userInput.all { it.isDigit() }) {
                    internalText = userInput
                }
            },
            readOnly = false,
            label = { Text(label) },
            textStyle = TextStyle(fontSize = fontSize, color = MaterialTheme.colorScheme.onSurface),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // --- 여기가 핵심 수정 사항입니다 ---
        // 년, 월, 일에 따라 다른 필터링 로직을 적용합니다.
        val filteringOptions = when (label) {
            "년" -> {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                items.filter {
                    // 1. 사용자가 입력한 숫자로 시작하고 (예: '202' -> 2020, 2021...)
                    // 2. 현재 년도보다 크거나 같은 미래의 년도만 필터링합니다.
                    it.startsWith(internalText) && (it.toIntOrNull() ?: 0) >= currentYear
                }
            }
            "월", "일" -> {
                // 월과 일은 기존 방식대로 필터링합니다.
                items.filter { it.startsWith(internalText, ignoreCase = true) }
            }
            else -> items // 그 외의 경우는 모든 아이템을 보여줍니다.
        }
        // -----------------------------------

        if (filteringOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    onItemSelected(internalText)
                    expanded = false
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = dropdownMenuMaxHeight)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteringOptions.forEach { itemValue ->
                        DropdownMenuItem(
                            text = { Text(itemValue, fontSize = fontSize, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                internalText = itemValue
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
}

