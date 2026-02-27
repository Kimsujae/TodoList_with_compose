package com.example.test240402.presentation.ui

import android.Manifest
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
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test240402.BuildConfig
import com.example.test240402.presentation.viewmodel.MainViewModel
import com.example.test240402.ui.theme.Test240402Theme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint

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
