package com.example.test240402.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    initialLocation: LatLng? = null,
    initialPlaceName: String = "",
    onLocationSelected: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedLatLng by remember { mutableStateOf(initialLocation ?: LatLng(37.5665, 126.9780)) }
    var placeName by remember { mutableStateOf(initialPlaceName) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
    }

    LaunchedEffect(Unit) {
        if (initialLocation == null) {
            getCurrentLocation(context) { latLng ->
                selectedLatLng = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("장소 선택", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = {
                            selectedLatLng = it
                        }) { Marker(state = MarkerState(position = selectedLatLng)) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = { Text("장소 이름 (예: 우리집, 회사)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("취소") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onLocationSelected(
                            selectedLatLng.latitude,
                            selectedLatLng.longitude,
                            placeName.ifBlank { "선택된 위치" })
                    }, enabled = placeName.isNotBlank()) { Text("선택 완료") }
                }
            }
        }
    }
}
