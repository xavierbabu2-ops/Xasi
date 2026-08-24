package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Optimized LocationManager for background service execution in Xavier Babu's Sovereign AI.
 * Initializes FusedLocationProviderClient, handles coarse/fine location permissions with callbacks,
 * and exposes a StateFlow<Location?> for real-time tracking.
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _trackingStatus = MutableStateFlow("Location Manager Initialized (Background Optimized)")
    val trackingStatus: StateFlow<String> = _trackingStatus.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                _currentLocation.value = location
            }
        }
    }

    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCoarseLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermissions(): Boolean {
        return hasFineLocationPermission() || hasCoarseLocationPermission()
    }

    /**
     * Starts real-time tracking with callback verification for permissions.
     */
    fun startRealTimeTracking(onPermissionDenied: () -> Unit = {}) {
        if (!hasLocationPermissions()) {
            _trackingStatus.value = "Permission Denied: Coarse or Fine location required."
            onPermissionDenied()
            return
        }

        val priority = if (hasFineLocationPermission()) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationRequest = LocationRequest.Builder(priority, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            _trackingStatus.value = "Background Real-Time GPS Tracking Active (${if (priority == Priority.PRIORITY_HIGH_ACCURACY) "Fine" else "Coarse"})"
        } catch (e: SecurityException) {
            _trackingStatus.value = "Security Exception: ${e.message}"
        }
    }

    fun stopRealTimeTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            _trackingStatus.value = "Real-Time GPS Tracking Paused."
        } catch (e: Exception) {
            _trackingStatus.value = "Error stopping updates: ${e.message}"
        }
    }

    fun formatLocationForFirestore(ownerEmail: String): Map<String, Any>? {
        val loc = _currentLocation.value ?: return null
        return mapOf(
            "ownerEmail" to ownerEmail,
            "latitude" to loc.latitude,
            "longitude" to loc.longitude,
            "altitude" to loc.altitude,
            "accuracy" to loc.accuracy,
            "timestamp" to System.currentTimeMillis(),
            "permissionType" to if (hasFineLocationPermission()) "FINE" else "COARSE",
            "backgroundOptimized" to true
        )
    }
}
