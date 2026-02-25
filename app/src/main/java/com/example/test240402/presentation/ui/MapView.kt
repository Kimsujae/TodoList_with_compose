package com.example.test240402.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.test240402.presentation.viewmodel.InputViewModel
import com.example.test240402.presentation.viewmodel.MainViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapView(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    inputViewModel: InputViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val todoList by mainViewModel.todoList.collectAsState()
    
    // 위치 정보가 있는 할 일만 필터링
    val todosWithLocation = todoList.filter { it.latitude != null && it.longitude != null }

    val defaultLocation = LatLng(37.5665, 126.9780)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // 할 일 추가를 위한 상태
    var showAddDialog by remember { mutableStateOf(false) }
    var clickedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var newTodoContent by remember { mutableStateOf("") }
    var newTodoPlaceName by remember { mutableStateOf("") }

    // 화면 진입 시 현재 위치로 카메라 이동
    LaunchedEffect(Unit) {
        getCurrentLocation(context) { latLng ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 14f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("전체 할 일 지도") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = com.google.maps.android.compose.MapProperties(isMyLocationEnabled = true),
                uiSettings = com.google.maps.android.compose.MapUiSettings(myLocationButtonEnabled = true),
                onMapClick = { latLng ->
                    clickedLatLng = latLng
                    newTodoPlaceName = "" // 클릭 시 장소명 초기화
                    showAddDialog = true
                }
            ) {
                // 기존 마커들
                todosWithLocation.forEach { todo ->
                    Marker(
                        state = MarkerState(position = LatLng(todo.latitude!!, todo.longitude!!)),
                        title = todo.content,
                        snippet = todo.placeName ?: todo.memo
                    )
                }

                // 현재 클릭하여 추가하려는 임시 마커
                clickedLatLng?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "여기에 추가",
                        alpha = 0.6f
                    )
                }
            }

            // 하단 안내 텍스트
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                tonalElevation = 4.dp
            ) {
                Text(
                    "지도를 클릭하여 할 일을 추가하세요",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // 할 일 추가 다이얼로그
    if (showAddDialog && clickedLatLng != null) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                clickedLatLng = null
            },
            title = { Text("이 위치에 할 일 추가") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = newTodoContent,
                        onValueChange = { newTodoContent = it },
                        label = { Text("할 일 내용") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTodoPlaceName,
                        onValueChange = { newTodoPlaceName = it },
                        label = { Text("장소 이름 (예: 커피숍, 공원)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTodoContent.isNotBlank()) {
                            inputViewModel.insertData(
                                content = newTodoContent,
                                memo = "",
                                alarmTime = null,
                                isAlarmEnabled = false,
                                latitude = clickedLatLng!!.latitude,
                                longitude = clickedLatLng!!.longitude,
                                placeName = newTodoPlaceName.ifBlank { "지정된 위치" }
                            )
                            newTodoContent = ""
                            newTodoPlaceName = ""
                            showAddDialog = false
                            clickedLatLng = null
                        }
                    },
                    enabled = newTodoContent.isNotBlank()
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    clickedLatLng = null
                }) {
                    Text("취소")
                }
            }
        )
    }
}
