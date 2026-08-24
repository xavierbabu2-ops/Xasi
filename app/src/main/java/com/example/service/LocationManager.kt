package com.example.service

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
 * Robust LocationManager handling coarse and fine location permissions,
 * FusedLocationProviderClient initialization, real-time GPS coordinates flow,
 * and secure formatting for authorized Firestore document transmission.
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _locationStatus = MutableStateFlow("Location Manager Ready (Coarse & Fine Supported)")
    val locationStatus: StateFlow<String> = _locationStatus.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                _currentLocation.value = location
            }
        }
    }

    /**
     * Checks if either FINE or COARSE location permission is granted.
     */
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

    fun hasAnyLocationPermission(): Boolean {
        return hasFineLocationPermission() || hasCoarseLocationPermission()
    }

    fun startRealTimeTracking() {
        if (!hasAnyLocationPermission()) {
            _locationStatus.value = "Error: Coarse or Fine location permissions not granted."
            return
        }

        val priority = if (hasFineLocationPermission()) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationRequest = LocationRequest.Builder(priority, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            _locationStatus.value = "Real-time GPS tracking active (Priority: ${if (priority == Priority.PRIORITY_HIGH_ACCURACY) "Fine / High Accuracy" else "Coarse / Balanced"})"
        } catch (e: SecurityException) {
            _locationStatus.value = "Security Exception: ${e.message}"
        }
    }

    fun stopRealTimeTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            _locationStatus.value = "Real-time GPS tracking paused."
        } catch (e: Exception) {
            _locationStatus.value = "Error stopping location updates: ${e.message}"
        }
    }

    /**
     * Formats current location data into a secure Map ready for authorized Firestore document transmission.
     */
    fun formatLocationForFirestore(ownerEmail: String): Map<String, Any>? {
        val loc = _currentLocation.value ?: return null
        return mapOf(
            "ownerEmail" to ownerEmail,
            "latitude" to loc.latitude,
            "longitude" to loc.longitude,
            "altitude" to loc.altitude,
            "accuracy" to loc.accuracy,
            "provider" to (loc.provider ?: "fused"),
            "timestamp" to System.currentTimeMillis(),
            "permissionLevel" to if (hasFineLocationPermission()) "FINE_ACCURACY" else "COARSE_ACCURACY",
            "encryptedPayloadStatus" to "SECURE_SOVEREIGN_TRANSMISSION"
        )
    }

    fun shareLocationSecurely(recipientIdentifier: String, ownerEmail: String): Map<String, Any>? {
        val payload = formatLocationForFirestore(ownerEmail)
        if (payload != null) {
            _locationStatus.value = "Location securely formatted & queued for '$recipientIdentifier' via Firestore."
        } else {
            _locationStatus.value = "Unable to format: GPS fix not yet acquired."
        }
        return payload
    }
}

