package com.example.test240402.presentation.ui

import android.Manifest
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            Log.d("MainActivity", "Permissions updated: Notification=$notificationGranted, Location=$fineLocationGranted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleGhostAlarmsOnFirstRun()
        handleIntent(intent = intent)
        checkAndRequestInitialPermissions()

        setContent {
            var showExactAlarmDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = getSystemService(AlarmManager::class.java)
                if (!alarmManager.canScheduleExactAlarms()) {
                    LaunchedEffect(Unit) { showExactAlarmDialog = true }
                }
            }

            Test240402Theme {
                if (showExactAlarmDialog) {
                    AlertDialog(
                        onDismissRequest = { showExactAlarmDialog = false },
                        title = { Text("알람 권한 필요") },
                        text = { Text("정확한 시간에 알림을 받기 위해서는 '알람 및 리마인더' 권한이 필요합니다.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showExactAlarmDialog = false
                                Intent().also { intent ->
                                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                    context.startActivity(intent)
                                }
                            }) { Text("설정으로 이동") }
                        },
                        dismissButton = { TextButton(onClick = { showExactAlarmDialog = false }) { Text("닫기") } }
                    )
                }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainAndInputScreen(mainViewModel)
                }
            }
        }
    }

    private fun checkAndRequestInitialPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(intent) }
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
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                val scheduler = AlarmSchedulerImpl(applicationContext, alarmManager)
                scheduler.cancelAllAlarms()
                prefs.edit().putBoolean("isFirstRun", false).apply()
            }
        }
    }
}

@Composable
fun MainAndInputScreen(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "MainView") {
        composable("MainView") { MainView(navController = navController, viewModel = mainViewModel) }
        composable("InputView") { InputView(navController = navController) }
        composable("MapView") { MapView(navController = navController) }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocationFetched: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            onLocationFetched(LatLng(location.latitude, location.longitude))
        } else {
            onLocationFetched(LatLng(37.5665, 126.9780))
        }
    }.addOnFailureListener {
        onLocationFetched(LatLng(37.5665, 126.9780))
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    var editIsAlarmEnabled by remember(itemToEdit) { mutableStateOf(itemToEdit?.isAlarmEnabled ?: false) }
    var editLatitude by remember(itemToEdit) { mutableStateOf(itemToEdit?.latitude) }
    var editLongitude by remember(itemToEdit) { mutableStateOf(itemToEdit?.longitude) }
    var editPlaceName by remember(itemToEdit) { mutableStateOf(itemToEdit?.placeName) }
    
    var editAlarmTimeMillis by remember(itemToEdit) { mutableStateOf(itemToEdit?.alarmTime) }
    var showEditLocationPicker by remember { mutableStateOf(false) }
    var showEditArcheryPicker by remember { mutableStateOf(false) }
    var useArcheryMode by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(contentList) {
        showContent.value = false
        delay(300)
        showContent.value = true
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            navController.navigate("MapView")
        } else {
            scope.launch { snackbarHostState.showSnackbar("지도를 보려면 위치 권한이 필요합니다.") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Todo List", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                actions = {
                    IconButton(onClick = {
                        val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (hasLocationPermission) {
                            navController.navigate("MapView")
                        } else {
                            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    }) {
                        Icon(Icons.Filled.Place, contentDescription = "지도 보기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer, titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer, actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(text = { Text(text = "add") }, icon = { Icon(Icons.Filled.Add, contentDescription = "새로운 Todo 추가") }, onClick = { navController.navigate("InputView") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(color = MaterialTheme.colorScheme.background)) {
            if (contentList.isEmpty() && showContent.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "데이터가 없습니다. 추가해주세요!") }
            } else if (contentList.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(items = contentList, key = { todoItem -> todoItem.id }) { currentItem ->
                        TodoItemRow(currentItem = currentItem, onUpdateItem = { viewModel.updateItem(it) }, onRequestDeleteItem = { itemToDelete = it; showDeleteDialog = true }, onRequestEditItem = { itemToEdit = it; showEditDialog = true })
                        if (contentList.last() != currentItem) { Divider(modifier = Modifier.padding(horizontal = 16.dp)) }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }

            if (showDeleteDialog && itemToDelete != null) {
                AlertDialog(onDismissRequest = { showDeleteDialog = false; itemToDelete = null }, title = { Text("삭제 확인") }, text = { Text("정말로 '${itemToDelete?.content}' 항목을 삭제하시겠습니까?") }, confirmButton = { Button(onClick = { itemToDelete?.let { viewModel.deleteItem(it) }; showDeleteDialog = false; itemToDelete = null; scope.launch { snackbarHostState.showSnackbar("삭제되었습니다.") } }) { Text("삭제") } }, dismissButton = { Button(onClick = { showDeleteDialog = false; itemToDelete = null }) { Text("취소") } })
            }

            if (showEditDialog && itemToEdit != null) {
                val scrollState = rememberScrollState()

                if (showEditLocationPicker) {
                    LocationPickerDialog(
                        initialLocation = LatLng(editLatitude ?: 37.5665, editLongitude ?: 126.9780),
                        initialPlaceName = editPlaceName ?: "",
                        onLocationSelected = { lat, lng, name -> editLatitude = lat; editLongitude = lng; editPlaceName = name; showEditLocationPicker = false },
                        onDismiss = { showEditLocationPicker = false }
                    )
                }
                
                if (showEditArcheryPicker) {
                    val currentCal = Calendar.getInstance().apply { editAlarmTimeMillis?.let { timeInMillis = it } }
                    Dialog(onDismissRequest = { showEditArcheryPicker = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            ArcheryTimePicker(
                                initialHour = currentCal.get(Calendar.HOUR).let { if(it == 0) 12 else it },
                                initialMinute = currentCal.get(Calendar.MINUTE),
                                initialIsAm = currentCal.get(Calendar.AM_PM) == Calendar.AM,
                                onTimeSelected = { h, m, isAm ->
                                    val newCal = Calendar.getInstance()
                                    newCal.set(Calendar.HOUR_OF_DAY, if (isAm) (if (h == 12) 0 else h) else (if (h == 12) 12 else h + 12))
                                    newCal.set(Calendar.MINUTE, m)
                                    editAlarmTimeMillis = newCal.timeInMillis
                                    showEditArcheryPicker = false
                                }
                            )
                        }
                    }
                }

                AlertDialog(onDismissRequest = { showEditDialog = false; itemToEdit = null }) {
                    Column(modifier = Modifier.background(AlertDialogDefaults.containerColor).padding(20.dp).verticalScroll(scrollState)) {
                        Text("Todo 수정", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = editTextContent, onValueChange = { editTextContent = it }, label = { Text("할 일") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editTextMemo, onValueChange = { editTextMemo = it }, label = { Text("메모 (선택 사항)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                        Spacer(modifier = Modifier.height(24.dp)); Divider(); Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("장소 설정", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { showEditLocationPicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text(editPlaceName ?: "장소 선택하기")
                        }
                        if (editPlaceName != null) { TextButton(onClick = { editLatitude = null; editLongitude = null; editPlaceName = null }) { Text("장소 삭제", color = Color.Red) } }
                        
                        Spacer(modifier = Modifier.height(24.dp)); Divider(); Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("알람 설정", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { 
                            Text("알람 활성화", modifier = Modifier.weight(1f))
                            Switch(checked = editIsAlarmEnabled, onCheckedChange = { editIsAlarmEnabled = it }) 
                        }
                        
                        if (editIsAlarmEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("설정 방식: ", style = MaterialTheme.typography.bodyMedium)
                                FilterChip(selected = !useArcheryMode, onClick = { useArcheryMode = false }, label = { Text("드랍다운") })
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(selected = useArcheryMode, onClick = { useArcheryMode = true }, label = { Text("활쏘기") })
                            }

                            if (useArcheryMode) {
                                Button(onClick = { showEditArcheryPicker = true }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.Gamepad, null); Spacer(modifier = Modifier.width(8.dp))
                                    Text(if(editAlarmTimeMillis != null) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(editAlarmTimeMillis!!)) else "활 쏴서 시간 정하기")
                                }
                            } else {
                                val currentCal = Calendar.getInstance().apply { timeInMillis = editAlarmTimeMillis ?: System.currentTimeMillis() }
                                CustomDropdownDatePicker(
                                    initialDateMillis = editAlarmTimeMillis ?: System.currentTimeMillis(),
                                    onDateSelected = { y, m, d ->
                                        val cal = Calendar.getInstance().apply { timeInMillis = editAlarmTimeMillis ?: System.currentTimeMillis() }
                                        cal.set(y, m - 1, d)
                                        editAlarmTimeMillis = cal.timeInMillis
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CustomDropdownTimePicker(
                                    initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
                                    initialMinute = currentCal.get(Calendar.MINUTE),
                                    onTimeSelected = { h, mi ->
                                        val cal = Calendar.getInstance().apply { timeInMillis = editAlarmTimeMillis ?: System.currentTimeMillis() }
                                        cal.set(Calendar.HOUR_OF_DAY, h)
                                        cal.set(Calendar.MINUTE, mi)
                                        editAlarmTimeMillis = cal.timeInMillis
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showEditDialog = false; itemToEdit = null }) { Text("취소") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                itemToEdit?.let { currentTodo -> viewModel.updateItem(currentTodo.copy(content = editTextContent, memo = editTextMemo, latitude = editLatitude, longitude = editLongitude, placeName = editPlaceName, isAlarmEnabled = editIsAlarmEnabled, alarmTime = editAlarmTimeMillis)) }
                                showEditDialog = false; itemToEdit = null; scope.launch { snackbarHostState.showSnackbar("수정되었습니다.") }
                            }, enabled = editTextContent.isNotBlank()) { Text("수정") }
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
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isAlarmEnabled by remember { mutableStateOf(false) }
    var useArcheryMode by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showArcheryPicker by remember { mutableStateOf(false) }
    var selectedAlarmTimeMillis by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }

    val content by viewModel.content.collectAsState()
    val memo by viewModel.memo.collectAsState()
    val selectedLat by viewModel.latitude.collectAsState()
    val selectedLng by viewModel.longitude.collectAsState()
    val selectedPlaceName by viewModel.placeName.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) { showLocationPicker = true }
        else { scope.launch { snackbarHostState.showSnackbar("장소를 선택하려면 위치 권한이 필요합니다.") } }
    }

    if (showLocationPicker) {
        LocationPickerDialog(onLocationSelected = { lat, lng, name -> viewModel.updateLocation(lat, lng, name); showLocationPicker = false }, onDismiss = { showLocationPicker = false })
    }
    
    if (showArcheryPicker) {
        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis() }
        Dialog(onDismissRequest = { showArcheryPicker = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ArcheryTimePicker(
                    initialHour = currentCal.get(Calendar.HOUR).let { if(it == 0) 12 else it },
                    initialMinute = currentCal.get(Calendar.MINUTE),
                    initialIsAm = currentCal.get(Calendar.AM_PM) == Calendar.AM,
                    onTimeSelected = { h, m, isAm ->
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, if (isAm) (if (h == 12) 0 else h) else (if (h == 12) 12 else h + 12))
                        cal.set(Calendar.MINUTE, m)
                        selectedAlarmTimeMillis = cal.setSecondMillisecondZero(cal.timeInMillis)
                        showArcheryPicker = false
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Todo 추가", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer, titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer)) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(color = MaterialTheme.colorScheme.background).verticalScroll(scrollState).padding(16.dp)) {
            OutlinedTextField(value = content, onValueChange = { viewModel.updateContent(it) }, label = { Text("할 일") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = memo, onValueChange = { viewModel.updateMemo(it) }, label = { Text("메모 (선택 사항)") }, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 120.dp), maxLines = 5)
            Spacer(modifier = Modifier.height(24.dp)); Divider(); Spacer(modifier = Modifier.height(24.dp))
            
            Text("장소 설정", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (hasLocationPermission) { showLocationPicker = true }
                else { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                Icon(Icons.Default.LocationOn, null); Spacer(modifier = Modifier.width(8.dp)); Text(selectedPlaceName ?: "장소 선택하기")
            }
            Spacer(modifier = Modifier.height(24.dp)); Divider(); Spacer(modifier = Modifier.height(24.dp))
            
            Text("알람 설정", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { 
                Text("알람 활성화", modifier = Modifier.weight(1f))
                Switch(checked = isAlarmEnabled, onCheckedChange = { isAlarmEnabled = it }) 
            }
            
            if (isAlarmEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("설정 방식: ", style = MaterialTheme.typography.bodyMedium)
                    FilterChip(selected = !useArcheryMode, onClick = { useArcheryMode = false }, label = { Text("드랍다운") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = useArcheryMode, onClick = { useArcheryMode = true }, label = { Text("활쏘기") })
                }

                if (useArcheryMode) {
                    Button(onClick = { showArcheryPicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Gamepad, null); Spacer(modifier = Modifier.width(8.dp))
                        Text(if(selectedAlarmTimeMillis != null) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(selectedAlarmTimeMillis!!)) else "활 쏴서 시간 정하기")
                    }
                } else {
                    val currentCal = Calendar.getInstance().apply { timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis() }
                    CustomDropdownDatePicker(
                        initialDateMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis(),
                        onDateSelected = { y, m, d ->
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis() }
                            cal.set(y, m - 1, d)
                            selectedAlarmTimeMillis = cal.setSecondMillisecondZero(cal.timeInMillis)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomDropdownTimePicker(
                        initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
                        initialMinute = currentCal.get(Calendar.MINUTE),
                        onTimeSelected = { h, mi ->
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis() }
                            cal.set(Calendar.HOUR_OF_DAY, h)
                            cal.set(Calendar.MINUTE, mi)
                            selectedAlarmTimeMillis = cal.setSecondMillisecondZero(cal.timeInMillis)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
            Button(onClick = {
                viewModel.insertData(content, memo, selectedAlarmTimeMillis, isAlarmEnabled && selectedAlarmTimeMillis != null, selectedLat, selectedLng, selectedPlaceName)
                navController.popBackStack()
            }, modifier = Modifier.fillMaxWidth(), enabled = content.isNotBlank()) { Text("저장하기") }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun Calendar.setSecondMillisecondZero(millis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(initialLocation: LatLng? = null, initialPlaceName: String = "", onLocationSelected: (Double, Double, String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedLatLng by remember { mutableStateOf(initialLocation ?: LatLng(37.5665, 126.9780)) }
    var placeName by remember { mutableStateOf(initialPlaceName) }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f) }

    LaunchedEffect(Unit) {
        if (initialLocation == null) {
            getCurrentLocation(context) { latLng ->
                selectedLatLng = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().height(500.dp), shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("장소 선택", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, onMapClick = { selectedLatLng = it }) { Marker(state = MarkerState(position = selectedLatLng)) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = placeName, onValueChange = { placeName = it }, label = { Text("장소 이름 (예: 우리집, 회사)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("취소") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onLocationSelected(selectedLatLng.latitude, selectedLatLng.longitude, placeName.ifBlank { "선택된 위치" }) }, enabled = placeName.isNotBlank()) { Text("선택 완료") }
                }
            }
        }
    }
}
