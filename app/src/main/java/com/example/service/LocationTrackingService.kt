package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.location.LocationManager
import kotlinx.coroutines.*

/**
 * Foreground Service for Xavier Babu's Sovereign AI that keeps the GPS feed active
 * even when the screen is off or the app is in the background.
 */
class LocationTrackingService : Service() {

    private lateinit var locationManager: LocationManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "SovereignLocationChannel"
        const val NOTIFICATION_ID = 9999
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = LocationManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Sovereign AI GPS Background Radar Active")
        startForeground(NOTIFICATION_ID, notification)

        // Start tracking
        if (locationManager.hasLocationPermissions()) {
            locationManager.startRealTimeTracking()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sovereign GPS Tracking Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains continuous GPS location tracking in background for Xavier Babu."
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sovereign Personal AI")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopRealTimeTracking()
        serviceScope.cancel()
    }
}
