package com.example.test240402.presentation.ui

import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.AlertDialogDefaults
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Checkbox
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.DatePicker
//import androidx.compose.material3.DatePickerDialog
//import androidx.compose.material3.DisplayMode
//import androidx.compose.material3.Divider
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExtendedFloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarDuration
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.SnackbarResult
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Switch
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
import androidx.compose.material3.*
//import androidx.compose.material3.TimePicker
//import androidx.compose.material3.TimePickerLayoutType
//import androidx.compose.material3.rememberDatePickerState
//import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.presentation.viewmodel.InputViewModel
import com.example.test240402.presentation.viewmodel.MainViewModel
import com.example.test240402.ui.theme.Test240402Theme
import com.example.test240402.ui.theme.Todo
import com.example.test240402.ui.theme.Icon_button_pastel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.test240402.presentation.ui.TodoItemRow
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val mainViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // 권한이 없는 경우, 사용자에게 권한 설정 화면으로 이동하도록 안내
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
                //todo
                // 여기에 사용자에게 왜 권한이 필요한지 설명하는
                // 다이얼로그나 스낵바를 띄워주면 사용자 경험이 더 좋아집니다.
            }
        }
        handleIntent(intent = intent)


        setContent {
            Test240402Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

//                    val navController = rememberNavController()
                    MainAndInputScreen(mainViewModel)

                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(intent) }
        Log.d("MainActivity", "onNewIntent called")
    }
    private fun handleIntent(intent: Intent) {
        if (intent.action == "DISABLE_ALARM_ACTION") {
            val todoIdToDisable =intent.getLongExtra("ALARM_TODO_ID_TO_DISABLE", -1L)
            if (todoIdToDisable!= -1L){
                mainViewModel.disableAlarmForTodoItem(todoIdToDisable)

                // (선택 사항) 인텐트 중복 처리를 방지하기 위해 action을 null로 설정
                this.intent.action = null
            }

        }

    }



}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val navController = rememberNavController()
    Test240402Theme {
        InputView(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    val navController = rememberNavController()
    Test240402Theme {
        MainView(navController = navController)
    }
}

@Composable
fun MainAndInputScreen(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "MainView") {
        composable("MainView") {
            MainView(
                navController = navController,
                viewModel = mainViewModel
            )
        }
        composable("InputView") {
            InputView(
                navController = navController
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MainView(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
//    val viewModel: MainViewModel = hiltViewModel()

    val contentList by viewModel.todoList.collectAsState()
    val showContent = remember { mutableStateOf(false) }

    var itemToDelete: TodoItem? by remember { mutableStateOf(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var itemToEdit: TodoItem? by remember { mutableStateOf(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // --- 수정 다이얼로그 공통 상태 (itemToEdit 변경 시 업데이트) ---
    var editTextContent by remember(itemToEdit) { mutableStateOf(itemToEdit?.content ?: "") }
    var editTextMemo by remember(itemToEdit) { mutableStateOf(itemToEdit?.memo ?: "") }
    var editIsAlarmEnabled by remember(itemToEdit) {
        mutableStateOf(
            itemToEdit?.isAlarmEnabled ?: false
        )
    }
    // editAlarmTime은 DatePicker/TimePicker 상태에서 최종적으로 계산되므로, 여기서는 직접적인 remember 상태로 불필요할 수 있음
    // 또는 초기값 전달 용도로만 사용하고, 최종 값은 Picker 상태에서 가져옴.

    val dateTimeFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(contentList) {
        showContent.value = false
        delay(300)
        showContent.value = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Todo List",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "add") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "새로운 Todo 추가") },
                onClick = { navController.navigate("InputView") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            if (contentList.isEmpty() && showContent.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "데이터가 없습니다. 추가해주세요!")
                }
            } else if (contentList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = contentList,
                        key = { todoItem -> todoItem.id }
                    ) { currentItem ->
                        TodoItemRow(
                            currentItem = currentItem,
                            onUpdateItem = { updatedItem ->
                                viewModel.updateItem(updatedItem) // ViewModel에 원본 아이템 정보도 필요할 수 있음
                            },
                            onRequestDeleteItem = { todoItemFromRow ->
                                itemToDelete = todoItemFromRow
                                showDeleteDialog = true
                            },
                            onRequestEditItem = { todoItemFromRow ->
                                itemToEdit = todoItemFromRow
                                // editIsAlarmEnabled, editTextContent, editTextMemo는 remember(itemToEdit)에 의해 자동 업데이트
                                showEditDialog = true
                            }
                        )
                        if (contentList.last() != currentItem) {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // --- 삭제 확인 다이얼로그 ---
            if (showDeleteDialog && itemToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        itemToDelete = null
                    },
                    title = { Text("삭제 확인") },
                    text = { Text("정말로 '${itemToDelete?.content}' 항목을 삭제하시겠습니까?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                itemToDelete?.let { viewModel.deleteItem(it) }
                                showDeleteDialog = false
                                itemToDelete = null
                                scope.launch { snackbarHostState.showSnackbar("삭제되었습니다.") }
                            }
                        ) { Text("삭제") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showDeleteDialog = false
                            itemToDelete = null
                        }) { Text("취소") }
                    }
                )
            }


            // --- 수정 다이얼로그 ---
            if (showEditDialog && itemToEdit != null) {
                Log.d("MainView", "Edit Dialog SHOULD BE VISIBLE for: ${itemToEdit?.content}")

                // --- DatePicker와 TimePicker 상태 (다이얼로그가 보일 때 생성) ---
                val initialSelectedDateMillis = itemToEdit?.alarmTime ?: System.currentTimeMillis()

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = initialSelectedDateMillis,
                    initialDisplayMode = DisplayMode.Picker,
                    // (선택) 과거 날짜 선택 불가 처리
                    // selectableDates = object : SelectableDates {
                    //     override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    //         val today = Calendar.getInstance().apply {
                    //             set(Calendar.HOUR_OF_DAY, 0)
                    //             set(Calendar.MINUTE, 0)
                    //             set(Calendar.SECOND, 0)
                    //             set(Calendar.MILLISECOND, 0)
                    //         }
                    //         return utcTimeMillis >= today.timeInMillis
                    //     }
                    // }
                )

                val initialDateCalendar = remember(itemToEdit) { // itemToEdit 변경 시 초기화
                    Calendar.getInstance().apply {
                        itemToEdit?.alarmTime?.let { timeInMillis = it }
                    }
                }
                var tempSelectedYear by remember(itemToEdit) {
                    mutableIntStateOf(
                        initialDateCalendar.get(
                            Calendar.YEAR
                        )
                    )
                }
                var tempSelectedMonth by remember(itemToEdit) {
                    mutableIntStateOf(
                        initialDateCalendar.get(Calendar.MONTH) + 1
                    )
                } // 1-12
                var tempSelectedDay by remember(itemToEdit) {
                    mutableIntStateOf(
                        initialDateCalendar.get(
                            Calendar.DAY_OF_MONTH
                        )
                    )
                }

                // TimePicker 상태
                val initialTimeCalendar = remember(itemToEdit) { // itemToEdit 변경 시 초기화
                    Calendar.getInstance().apply {
                        itemToEdit?.alarmTime?.let { timeInMillis = it }
                    }
                }
                var tempSelectedHour by remember(itemToEdit) {
                    mutableIntStateOf(
                        initialTimeCalendar.get(
                            Calendar.HOUR_OF_DAY
                        )
                    )
                } // 0-23
                var tempSelectedMinute by remember(itemToEdit) {
                    mutableIntStateOf(
                        initialTimeCalendar.get(Calendar.MINUTE)
                    )
                }   // 0-59
                // --- Custom Picker 들을 위한 임시 상태 변수 끝 ---

                val dateTimeFormatter =
                    remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
                val snackbarHostState = remember { SnackbarHostState() }

                var customPickerSelectedDateMillis: Long? by remember { mutableStateOf(itemToEdit?.alarmTime) }
//                var tempSelectedYear by remember { mutableIntStateOf(0) }
//                var tempSelectedMonth by remember { mutableIntStateOf(0) }
//                var tempSelectedDay by remember { mutableIntStateOf(0) }

                val calendarForTimePicker = Calendar.getInstance().apply {
                    // DatePicker에서 선택된 날짜가 있으면 그것을 사용, 없으면 초기 날짜(또는 현재) 사용
                    timeInMillis = datePickerState.selectedDateMillis ?: initialSelectedDateMillis
                }
                val initialHour = calendarForTimePicker.get(Calendar.HOUR_OF_DAY)
                val initialMinute = calendarForTimePicker.get(Calendar.MINUTE)

                val timePickerState = rememberTimePickerState(
                    initialHour = initialHour,
                    initialMinute = initialMinute,
                    is24Hour = true // 필요에 따라 false로 변경
                )
                // --- DatePicker와 TimePicker 상태 끝 ---

                // AlertDialog의 content 람다를 사용하여 내부 UI 직접 구성
                AlertDialog(
                    onDismissRequest = {
                        showEditDialog = false
                        itemToEdit = null // 다이얼로그 닫을 때 상태 초기화
                    },
//                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier
                            .background(AlertDialogDefaults.containerColor) // 다이얼로그 배경색과 동일하게
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
                            .verticalScroll(rememberScrollState()) // 내용이 길어지면 스크롤
                    ) {
                        Text("Todo 수정", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = editTextContent,
                            onValueChange = { editTextContent = it },
                            label = { Text("할 일") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editTextMemo,
                            onValueChange = { editTextMemo = it },
                            label = { Text("메모 (선택 사항)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("알람 설정", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("알람 활성화", modifier = Modifier.weight(1f))
                            Switch(
                                checked = editIsAlarmEnabled,
                                onCheckedChange = { editIsAlarmEnabled = it }
                            )
                        }

                        if (editIsAlarmEnabled) {
//                            Spacer(modifier = Modifier.height(16.dp))
//                            // 선택된 알람 시간 표시 (선택 사항)
//                            val currentSelectedDateTimeMillis = remember(
//                                datePickerState.selectedDateMillis,
//                                timePickerState.hour,
//                                timePickerState.minute
//                            ) {
//                                datePickerState.selectedDateMillis?.let { dateMillis ->
//                                    Calendar.getInstance().apply {
//                                        timeInMillis = dateMillis
//                                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
//                                        set(Calendar.MINUTE, timePickerState.minute)
//                                    }.timeInMillis
//                                }
//                            }
//                            Text(
//                                text = "선택된 알람: ${
//                                    currentSelectedDateTimeMillis?.let {
//                                        dateTimeFormatter.format(
//                                            Date(it)
//                                        )
//                                    } ?: "시간 미설정"
//                                }",
//                                style = MaterialTheme.typography.bodyMedium
//                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomDropdownDatePicker(
                                initialDateMillis = itemToEdit?.alarmTime
                                    ?: System.currentTimeMillis(),
                                onDateSelected = { year, month, day ->
                                    // 선택된 년/월/일을 임시 상태에 저장
                                    tempSelectedYear = year
                                    tempSelectedMonth = month
                                    tempSelectedDay = day
//                                    Log.d("CustomDatePicker", "Date Selected: $year-$month-$day")
//                                    val calendar = Calendar.getInstance().apply {
//                                        set(Calendar.YEAR, year)
//                                        set(Calendar.MONTH, month - 1)
//                                        set(Calendar.DAY_OF_MONTH, day) }
//                                    customPickerSelectedDateMillis =calendar.timeInMillis
//                                    Log.d("CustomDatePicker", "Date Selected: $year-$month-$day, millis: $customPickerSelectedDateMillis")
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

//                            DatePicker(
//                                state = datePickerState,
//                                modifier = Modifier.fillMaxWidth(),
//                                title = null, //  { Text("날짜 선택", modifier = Modifier.padding(16.dp)) },
//                                headline = null //  { datePickerState.selectedDateMillis?.let { Text(SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(Date(it)), modifier = Modifier.padding(start = 16.dp, top=8.dp, bottom=8.dp)) } }
//                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomDropdownTimePicker(
                                initialHour = tempSelectedHour, // remember(itemToEdit)으로 초기화된 값 사용
                                initialMinute = tempSelectedMinute, // remember(itemToEdit)으로 초기화된 값 사용
                                onTimeSelected = { hour, minute ->
                                    tempSelectedHour = hour
                                    tempSelectedMinute = minute
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val combinedDateTimeMillis = remember(
                                tempSelectedYear,
                                tempSelectedMonth,
                                tempSelectedDay,
                                tempSelectedHour,
                                tempSelectedMinute
                            ) {
                                // tempSelectedYear가 초기값(0)이 아니거나, itemToEdit?.alarmTime에 기반한 초기값이 설정되었을 때만 계산
                                if (tempSelectedYear != initialDateCalendar.get(Calendar.YEAR) || // 뭔가 변경되었거나
                                    tempSelectedMonth != (initialDateCalendar.get(Calendar.MONTH) + 1) ||
                                    tempSelectedDay != initialDateCalendar.get(Calendar.DAY_OF_MONTH) ||
                                    itemToEdit?.alarmTime != null
                                ) { // 초기 알람이 있었던 경우
                                    Calendar.getInstance().apply {
                                        set(Calendar.YEAR, tempSelectedYear)
                                        set(Calendar.MONTH, tempSelectedMonth - 1)
                                        set(Calendar.DAY_OF_MONTH, tempSelectedDay)
                                        set(Calendar.HOUR_OF_DAY, tempSelectedHour)
                                        set(Calendar.MINUTE, tempSelectedMinute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                } else {
                                    null
                                }
                            }
                            Text(
                                text = "선택된 알람: ${
                                    combinedDateTimeMillis?.let { dateTimeFormatter.format(Date(it)) } ?: "시간 미설정"
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )


//                            Box(
//                                modifier = Modifier.fillMaxWidth(),
//                                contentAlignment = Alignment.Center
//                            ) {
////                                TimePicker(
////                                    state = timePickerState,
////                                    layoutType = TimePickerLayoutType.Vertical // 또는 Horizontal
////                                )
//
//
//
//
//                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "알람이 비활성화되어 있습니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            // 레이아웃 유지를 위해 높이 Spacer (조정 필요)
                            Spacer(modifier = Modifier.height(150.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showEditDialog = false
                                    itemToEdit = null
                                }
                            ) { Text("취소") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val finalAlarmTimeMillis: Long? = if (editIsAlarmEnabled) {
                                        // tempSelectedYear가 초기값(0)이 아니거나, itemToEdit?.alarmTime에 기반한 초기값이 설정되었을 때만 계산
                                        if (tempSelectedYear != initialDateCalendar.get(Calendar.YEAR) ||
                                            tempSelectedMonth != (initialDateCalendar.get(Calendar.MONTH) + 1) ||
                                            tempSelectedDay != initialDateCalendar.get(Calendar.DAY_OF_MONTH) ||
                                            tempSelectedHour != initialTimeCalendar.get(Calendar.HOUR_OF_DAY) ||
                                            tempSelectedMinute != initialTimeCalendar.get(Calendar.MINUTE) ||
                                            itemToEdit?.alarmTime != null
                                        ) {

                                            val calendar = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, tempSelectedYear)
                                                set(Calendar.MONTH, tempSelectedMonth - 1)
                                                set(Calendar.DAY_OF_MONTH, tempSelectedDay)
                                                set(Calendar.HOUR_OF_DAY, tempSelectedHour)
                                                set(Calendar.MINUTE, tempSelectedMinute)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            if (calendar.timeInMillis > System.currentTimeMillis()) {
                                                calendar.timeInMillis
                                            } else {
                                                scope.launch { snackbarHostState.showSnackbar("알람은 현재 시간 이후로 설정해야 합니다.") }
                                                null
                                            }
                                        } else {
                                            // 사용자가 아무것도 변경하지 않았고, 초기 알람도 없었다면 null
                                            // 또는 itemToEdit?.alarmTime (기존 시간 유지 - 이 경우 위 조건 다시 생각)
                                            itemToEdit?.alarmTime?.takeIf { it > System.currentTimeMillis() } // 기존 시간이 유효하면 사용
                                        }
                                    } else {
                                        null
                                    }

                                    itemToEdit?.let { currentTodo ->
                                        val updatedTodo = currentTodo.copy(
                                            content = editTextContent,
                                            memo = editTextMemo.ifBlank { null },
                                            alarmTime = finalAlarmTimeMillis,
                                            isAlarmEnabled = editIsAlarmEnabled && (finalAlarmTimeMillis != null)
                                        )
                                        Log.d(
                                            "MainView",
                                            "Updating Todo with Custom Pickers: $updatedTodo"
                                        )
                                        viewModel.updateItem(updatedTodo)
                                    }
                                    showEditDialog = false
                                    itemToEdit = null
                                    scope.launch { snackbarHostState.showSnackbar("수정되었습니다.") }
                                },
                                enabled = editTextContent.isNotBlank()
                            ) { Text("수정") }
                        }
                    }
                }
            }
        }
    }
}


//@HiltViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputView(navController: NavController) {

    val viewModel: InputViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedAlarmTimeMillis by remember { mutableStateOf<Long?>(null) } // 초기값은 null (설정 안 함)
    var isAlarmEnabled by remember { mutableStateOf(false) }               // 초기값은 false (비활성화)

    val currentCalendar = Calendar.getInstance()

//    viewModel.initData(item = viewModel.item ?: ContentEntity(content = "", memo = ""))
    val content by viewModel.content.collectAsState()
    val memo by viewModel.memo.collectAsState()
    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Todo List",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
//            navigationIcon = {
//                IconButton(onClick = { navController.popBackStack() }) {
//                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
//                }
//            },
//            actions = {
//                IconButton(
//                    onClick = {
//                        if (content.isNotBlank()) {
//                            viewModel.insertData()
//                            navController.popBackStack()
//                        }else{
//                            scope.launch { snackbarHostState.showSnackbar("할일을 입력해주세요.") }
//                        }
//
//                    },enabled = content.isNotBlank()
//                ) {
//                    Icon(Icons.Filled.Done, contentDescription = "저장")
//                }
//            },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

            )
        }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) }, // ViewModel 함수 호출
                label = { Text("할 일") },
                placeholder = { Text("내용을 입력하세요") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = memo, // memo가 null을 허용하지 않는 String이므로 직접 사용
                onValueChange = { viewModel.updateMemo(it) }, // ViewModel 함수 호출
                label = { Text("메모 (선택 사항)") },
                placeholder = { Text("추가 메모를 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp),
                maxLines = 5
            )
            Text("알람 설정 ", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // 알람이 이미 활성화되어 있고 시간이 설정되어 있다면, 바로 시간 선택기로 갈 수도 있음
                    // 여기서는 항상 날짜부터 선택하도록 단순화
                    showDatePicker = true
                }, modifier = Modifier.fillMaxWidth()
            ) {
                val buttonText = if (selectedAlarmTimeMillis != null && isAlarmEnabled) {
                    "알람: ${
                        SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(
                            Date(
                                selectedAlarmTimeMillis!!
                            )
                        )
                    }"
                } else {
                    "알람 시간 설정하기"
                }
                Text(buttonText)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                Text("알람 활성화", modifier = Modifier.weight(1f))
                Switch(
                    checked = isAlarmEnabled,
                    onCheckedChange = { checked ->
                        isAlarmEnabled = checked
                        if (!checked) {
                            // 알람을 비활성화하면 선택된 시간도 초기화 (사용자 경험에 따라 결정)
                            selectedAlarmTimeMillis = null
                        }
                    },
                    // 시간이 먼저 설정되어야만 스위치를 활성화할지, 아니면 항상 활성화할지 결정
                    // enabled = selectedAlarmTimeMillis != null (시간 설정 후 활성화)
                    // 또는 항상 활성화하고, 시간이 없는데 켜면 시간 설정 유도
                )
            }
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState( // Material 3 DatePickerState
                    initialSelectedDateMillis = selectedAlarmTimeMillis
                        ?: System.currentTimeMillis()
                )
                DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let {
                                // 사용자가 날짜만 선택하고 시간을 아직 선택하지 않았을 수 있으므로,
                                // 선택된 날짜의 0시 0분으로 우선 설정하거나,
                                // 바로 시간 선택기를 띄워 시간을 마저 받도록 함.
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = it
                                // 기존에 시간이 설정되어 있었다면 그 시간 유지, 아니면 정오 등으로 초기화
                                val currentHour =
                                    if (selectedAlarmTimeMillis != null) Calendar.getInstance()
                                        .apply { timeInMillis = selectedAlarmTimeMillis!! }
                                        .get(Calendar.HOUR_OF_DAY) else 12
                                val currentMinute =
                                    if (selectedAlarmTimeMillis != null) Calendar.getInstance()
                                        .apply { timeInMillis = selectedAlarmTimeMillis!! }
                                        .get(Calendar.MINUTE) else 0
                                cal.set(Calendar.HOUR_OF_DAY, currentHour)
                                cal.set(Calendar.MINUTE, currentMinute)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                selectedAlarmTimeMillis = cal.timeInMillis
                                showTimePicker = true // 날짜 선택 후 바로 시간 선택기 표시
                            }
                        }) { Text("확인") }
                }, dismissButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) { Text("취소") }
                }) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePicker) {
                val initialHour = if (selectedAlarmTimeMillis != null) {
                    Calendar.getInstance().apply { timeInMillis = selectedAlarmTimeMillis!! }
                        .get(Calendar.HOUR_OF_DAY)
                } else {
                    currentCalendar.get(Calendar.HOUR_OF_DAY)
                }
                val initialMinute = if (selectedAlarmTimeMillis != null) {
                    Calendar.getInstance().apply { timeInMillis = selectedAlarmTimeMillis!! }
                        .get(Calendar.MINUTE)
                } else {
                    currentCalendar.get(Calendar.MINUTE)
                }

                val timePickerState = rememberTimePickerState(
                    initialHour = initialHour,
                    initialMinute = initialMinute,
                    is24Hour = false // 24시간 형식 사용 여부
                )
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text("알람 시간 선택") },
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            TimePicker(state = timePickerState)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // selectedAlarmTimeMillis에 날짜 정보가 이미 있어야 함 (DatePicker에서 설정됨)
                                val cal = Calendar.getInstance()
                                if (selectedAlarmTimeMillis != null) {
                                    cal.timeInMillis = selectedAlarmTimeMillis!! // 기존 날짜 유지
                                } else {
                                    // 혹시 날짜가 설정 안된 예외적 상황 (오늘 날짜로 기본 설정)
                                    cal.timeInMillis = System.currentTimeMillis()
                                }
                                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                cal.set(Calendar.MINUTE, timePickerState.minute)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)

                                selectedAlarmTimeMillis = cal.timeInMillis
                                isAlarmEnabled = true // 시간을 설정하면 알람을 자동으로 활성화 (선택 사항)
                                showTimePicker = false
                                Log.d(
                                    "InputView",
                                    "Selected Alarm DateTime: ${Date(selectedAlarmTimeMillis!!)}"
                                )
                            }) { Text("확인") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                        }) { Text("취소") }
                    })
            }
            Spacer(modifier = Modifier.weight(1f)) // 저장 버튼을 하단으로 밀기

            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        // ViewModel의 insertData 함수에 알람 정보도 함께 전달
                        viewModel.insertData( // ViewModel의 insertData 시그니처 변경 필요
                            content = content,
                            memo = memo,
                            alarmTime = if (isAlarmEnabled) selectedAlarmTimeMillis else null,
                            isAlarmEnabled = isAlarmEnabled
                        )
                        navController.popBackStack()
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("할 일을 입력해주세요.") }
                    }
                }, modifier = Modifier.fillMaxWidth(), enabled = content.isNotBlank()
            ) {
                Text("저장하기")
            }
        }


    }


//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .background(color = Color.White)
//        ) {
//            Column(
//                Modifier
//                    .fillMaxSize()
//                    .padding(12.dp)
//            ) {
////            Button(onClick = { navController.popBackStack() }) {
////                Text(text = "go to first")
////            }
//                Spacer(modifier = Modifier.height(10.dp))
//                OutlinedTextField(
//                    value = content.value,
////                value = viewModel.content.value?.let { if (it.isEmpty()) "" else it.toString() },
//                    onValueChange = {
//                        content.value = it
//                        viewModel.content.value = it
//                    },
//                    label = { Text(text = "할일", color = Color.Red) },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(6.dp))
//                        .align(alignment = Alignment.CenterHorizontally),
//                    placeholder = { Text(text = "내용", color = Color.LightGray) })
//
//                Spacer(modifier = Modifier.height(10.dp))
//                TextField(
//                    value = memo.value,
//                    onValueChange = {
//                        memo.value = it
//                        viewModel.memo.value = it
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(16.dp))
//                        .align(alignment = Alignment.CenterHorizontally),
//                    placeholder = { Text(text = "메모", color = Color.LightGray) })
//
//            }
//            Button(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter), onClick = {
//                    Log.d("라이브데이터확인", "제목: ${viewModel.content.value}, 메모: ${viewModel.memo.value}")
//                    viewModel.insertData()
//                    navController.popBackStack()
//                }) {
//                Text(text = "입력완료")
//            }
//
//        }
}

