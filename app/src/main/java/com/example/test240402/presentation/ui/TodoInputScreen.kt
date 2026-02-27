package com.example.test240402.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.test240402.presentation.viewmodel.InputViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            showLocationPicker = true
        } else {
            scope.launch { snackbarHostState.showSnackbar("장소를 선택하려면 위치 권한이 필요합니다.") }
        }
    }

    if (showLocationPicker) {
        LocationPickerDialog(onLocationSelected = { lat, lng, name ->
            viewModel.updateLocation(lat, lng, name); showLocationPicker = false
        }, onDismiss = { showLocationPicker = false })
    }

    if (showArcheryPicker) {
        val currentCal = Calendar.getInstance()
            .apply { timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis() }
        Dialog(
            onDismissRequest = { showArcheryPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                ArcheryTimePicker(
                    initialHour = currentCal.get(Calendar.HOUR).let { if (it == 0) 12 else it },
                    initialMinute = currentCal.get(Calendar.MINUTE),
                    initialIsAm = currentCal.get(Calendar.AM_PM) == Calendar.AM,
                    onTimeSelected = { h, m, isAm ->
                        val cal = Calendar.getInstance()
                        cal.set(
                            Calendar.HOUR_OF_DAY,
                            if (isAm) (if (h == 12) 0 else h) else (if (h == 12) 12 else h + 12)
                        )
                        cal.set(Calendar.MINUTE, m)
                        selectedAlarmTimeMillis = cal.setSecondMillisecondZero(cal.timeInMillis)
                        showArcheryPicker = false
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Todo 추가",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("할 일") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = memo,
                onValueChange = { viewModel.updateMemo(it) },
                label = { Text("메모 (선택 사항)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp),
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(24.dp)); Divider(); Spacer(
            modifier = Modifier.height(
                24.dp
            )
        )

            Text("장소 설정", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasLocationPermission) {
                        showLocationPicker = true
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.LocationOn, null); Spacer(modifier = Modifier.width(8.dp)); Text(
                selectedPlaceName ?: "장소 선택하기"
            )
            }
            Spacer(modifier = Modifier.height(24.dp)); Divider(); Spacer(
            modifier = Modifier.height(
                24.dp
            )
        )

            Text("알람 설정", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("알람 활성화", modifier = Modifier.weight(1f))
                Switch(checked = isAlarmEnabled, onCheckedChange = { isAlarmEnabled = it })
            }

            if (isAlarmEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("설정 방식: ", style = MaterialTheme.typography.bodyMedium)
                    FilterChip(
                        selected = !useArcheryMode,
                        onClick = { useArcheryMode = false },
                        label = { Text("드랍다운") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = useArcheryMode,
                        onClick = { useArcheryMode = true },
                        label = { Text("활쏘기") })
                }

                if (useArcheryMode) {
                    Button(
                        onClick = { showArcheryPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Gamepad, null); Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (selectedAlarmTimeMillis != null) SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(Date(selectedAlarmTimeMillis!!)) else "활 쏴서 시간 정하기"
                        )
                    }
                } else {
                    val currentCal = Calendar.getInstance().apply {
                        timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis()
                    }
                    CustomDropdownDatePicker(
                        initialDateMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis(),
                        onDateSelected = { y, m, d ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis()
                            }
                            cal.set(y, m - 1, d)
                            selectedAlarmTimeMillis = cal.setSecondMillisecondZero(cal.timeInMillis)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomDropdownTimePicker(
                        initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
                        initialMinute = currentCal.get(Calendar.MINUTE),
                        onTimeSelected = { h, mi ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = selectedAlarmTimeMillis ?: System.currentTimeMillis()
                            }
                            cal.set(Calendar.HOUR_OF_DAY, h)
                            cal.set(Calendar.MINUTE, mi)
                            selectedAlarmTimeMillis = cal.setSecondMillisecondZero(cal.timeInMillis)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
            Button(onClick = {
                viewModel.insertData(
                    content,
                    memo,
                    selectedAlarmTimeMillis,
                    isAlarmEnabled && selectedAlarmTimeMillis != null,
                    selectedLat,
                    selectedLng,
                    selectedPlaceName
                )
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
