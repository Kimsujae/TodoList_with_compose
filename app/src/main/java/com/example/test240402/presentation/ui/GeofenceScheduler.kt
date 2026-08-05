package com.example.test240402.presentation.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.test240402.domain.model.TodoItem
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

interface GeofenceScheduler {
    fun schedule(item: TodoItem)
    fun cancel(item: TodoItem)
}

class GeofenceSchedulerImpl(private val context: Context) : GeofenceScheduler {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    override fun schedule(item: TodoItem) {
        if (item.latitude == null || item.longitude == null) return

        val geofence = Geofence.Builder()
            .setRequestId(item.id.toString())
            .setCircularRegion(item.latitude, item.longitude, 100f) // 100미터 반경
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        // 할 일 내용을 인텐트에 담아 전달하기 위해 리시버용 인텐트 재생성 (필요 시)
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            putExtra("TODO_CONTENT", item.content)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        geofencingClient.addGeofences(request, pendingIntent).run {
            addOnSuccessListener { Log.d("Geofence", "Successfully added for ${item.content}") }
            addOnFailureListener { Log.e("Geofence", "Failed to add: ${it.message}") }
        }
    }

    override fun cancel(item: TodoItem) {
        geofencingClient.removeGeofences(listOf(item.id.toString())).run {
            addOnSuccessListener { Log.d("Geofence", "Successfully removed for ${item.id}") }
            addOnFailureListener { Log.e("Geofence", "Failed to remove: ${it.message}") }
        }
    }
}
