package com.example.test240402.presentation.ui

import android.Manifest
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test240402.BuildConfig
import com.example.test240402.domain.model.TodoItem
import com.example.test240402.presentation.viewmodel.InputViewModel
import com.example.test240402.presentation.viewmodel.MainViewModel
import com.example.test240402.ui.theme.Test240402Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 사용자가 권한을 허용한 경우의 동작 (예: 로그 남기기)
                Log.d("MainActivity", "POST_NOTIFICATIONS 권한이 허용되었습니다.")
            } else {
                // 사용자가 권한을 거부한 경우의 동작 (예: 스낵바나 토스트로 안내)
                Log.d("MainActivity", "POST_NOTIFICATIONS 권한이 거부되었습니다.")
            }
        }

    // 2. 안드로이드 13(API 33) 이상에서 알림 권한을 요청하는 함수
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없다면, 사용자에게 권한 요청 팝업을 띄웁니다.
                // 사용자가 이전에 거부했는지 여부에 따라 설명을 추가하는 로직(shouldShowRequestPermissionRationale)을 넣으면 더 좋습니다.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleGhostAlarmsOnFirstRun()
        handleIntent(intent = intent)

        askNotificationPermission()

        setContent {
            var showPermissionDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            // 안드로이드 12 (S) 이상에서만 권한 확인 로직 실행
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(AlarmManager::class.java)
                // 권한이 없는 경우, 다이얼로그를 표시하도록 상태 변경
                if (!alarmManager.canScheduleExactAlarms()) {
                    // LaunchedEffect를 사용하여 Composition이 완료된 후 상태를 변경
                    LaunchedEffect(Unit) {
                        showPermissionDialog = true
                    }
                }
            }
            Test240402Theme {
                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDialog = false },
                        title = { Text("알람 권한 필요") },
                        text = { Text("정확한 시간에 알림을 받기 위해서는 '알람 및 리마인더' 권한이 필요합니다. 설정 화면으로 이동하여 권한을 허용해주세요.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showPermissionDialog = false
                                    // 사용자가 확인을 눌렀을 때 설정 화면으로 이동
                                    Intent().also { intent ->
                                        intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                        context.startActivity(intent)
                                    }
                                }
                            ) { Text("설정으로 이동") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPermissionDialog = false }) { Text("닫기") }
                        }
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
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
            val todoIdToDisable = intent.getLongExtra("ALARM_TODO_ID_TO_DISABLE", -1L)
            if (todoIdToDisable != -1L) {
                mainViewModel.disableAlarmForTodoItem(todoIdToDisable)
                this.intent.action = null
            }
        }
    }

    private fun handleGhostAlarmsOnFirstRun() {
        if (BuildConfig.DEBUG) {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val isFirstRun = prefs.getBoolean("isFirstRun", true)

            if (isFirstRun) {
                Log.d("MainActivity", "앱 설치 후 최초 실행: 모든 알람을 초기화합니다.")
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val scheduler = AlarmSchedulerImpl(applicationContext, alarmManager)
                scheduler.cancelAllAlarms()
                prefs.edit().putBoolean("isFirstRun", false).apply()
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
    val contentList by viewModel.todoList.collectAsState()
    val showContent = remember { mutableStateOf(false) }

    var itemToDelete: TodoItem? by remember { mutableStateOf(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var itemToEdit: TodoItem? by remember { mutableStateOf(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    var editTextContent by remember(itemToEdit) { mutableStateOf(itemToEdit?.content ?: "") }
    var editTextMemo by remember(itemToEdit) { mutableStateOf(itemToEdit?.memo ?: "") }
    var editIsAlarmEnabled by remember(itemToEdit) {
        mutableStateOf(
            itemToEdit?.isAlarmEnabled ?: false
        )
    }

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
                                viewModel.updateItem(updatedItem)
                            },
                            onRequestDeleteItem = { todoItemFromRow ->
                                itemToDelete = todoItemFromRow
                                showDeleteDialog = true
                            },
                            onRequestEditItem = { todoItemFromRow ->
                                itemToEdit = todoItemFromRow
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


            if (showEditDialog && itemToEdit != null) {
                Log.d("MainView", "Edit Dialog SHOULD BE VISIBLE for: ${itemToEdit?.content}")

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

                AlertDialog(
                    onDismissRequest = {
                        showEditDialog = false
                        itemToEdit = null // 다이얼로그 닫을 때 상태 초기화
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .background(AlertDialogDefaults.containerColor)
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
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomDropdownDatePicker(
                                initialDateMillis = itemToEdit?.alarmTime
                                    ?: System.currentTimeMillis(),
                                onDateSelected = { year, month, day ->
                                    // 선택된 년/월/일을 임시 상태에 저장
                                    tempSelectedYear = year
                                    tempSelectedMonth = month
                                    tempSelectedDay = day
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

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
                                if (tempSelectedYear != initialDateCalendar.get(Calendar.YEAR) ||
                                    tempSelectedMonth != (initialDateCalendar.get(Calendar.MONTH) + 1) ||
                                    tempSelectedDay != initialDateCalendar.get(Calendar.DAY_OF_MONTH) ||
                                    itemToEdit?.alarmTime != null
                                ) { 
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
                            selectedAlarmTimeMillis = null
                        }
                    },
                )
            }
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState( // Material 3 DatePickerState
                    initialSelectedDateMillis = selectedAlarmTimeMillis
                        ?: System.currentTimeMillis()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDatePicker = false
                                datePickerState.selectedDateMillis?.let {
                                    val cal = Calendar.getInstance().apply { timeInMillis = it }
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
                                val cal = Calendar.getInstance()
                                if (selectedAlarmTimeMillis != null) {
                                    cal.timeInMillis = selectedAlarmTimeMillis!! // 기존 날짜 유지
                                } else {
                                    cal.timeInMillis = System.currentTimeMillis()
                                }
                                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                cal.set(Calendar.MINUTE, timePickerState.minute)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)

                                val now =System.currentTimeMillis()
                                if (cal.timeInMillis <= now) {
                                    scope.launch { snackbarHostState.showSnackbar("알람은 현재 시간 이후로 설정해야 합니다.") }
                                    selectedAlarmTimeMillis = now
                                }else{
                                    selectedAlarmTimeMillis = cal.timeInMillis
                                }
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
}
