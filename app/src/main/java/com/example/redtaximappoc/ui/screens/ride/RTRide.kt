package com.example.redtaximappoc.ui.screens.ride

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun RTRide() {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val currentLocationMarkerState = rememberMarkerState()
    val cameraPositionState = rememberCameraPositionState()

    // Define your fixed offset (0.010 degrees latitude in this case)
    val fixedOffset = remember { LatLng(-0.0010, 0.0) }

    // Function to apply the offset to a location
    fun applyOffset(location: LatLng): LatLng {
        return LatLng(location.latitude + fixedOffset.latitude, location.longitude + fixedOffset.longitude)
    }

    LaunchedEffect(Unit) {
        getCurrentLocation(fusedLocationProviderClient) { location ->
            val offsetLocation = applyOffset(location)
            currentLocationMarkerState.position = LatLng(location.latitude, location.longitude)
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(offsetLocation, 18f))
        }
    }

    LaunchedEffect(cameraPositionState.position) {
        // If you want the marker to follow the camera with offset
        currentLocationMarkerState.position = cameraPositionState.position.target
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false
            ),
        ) {
            Marker(
                title = "Current Location",
                draggable = true,
                state = currentLocationMarkerState,
            )
        }

        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .clickable {
                    getCurrentLocation(fusedLocationProviderClient) { location ->
                        val offsetLocation = applyOffset(location)
                        currentLocationMarkerState.position = LatLng(location.latitude, location.longitude)
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                offsetLocation,
                                18f
                            )
                        )
                    }
                }
        )
    }
}


@SuppressLint("MissingPermission") // Ensure permissions are handled before calling this function
fun getCurrentLocation(
    fusedLocationProviderClient: FusedLocationProviderClient,
    onLocationRetrieved: (LatLng) -> Unit
) {
    try {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(location.latitude, location.longitude)
                    onLocationRetrieved(latLng)
                } ?: requestNewLocationData(fusedLocationProviderClient, onLocationRetrieved)

            }
            .addOnFailureListener { e ->
                Log.e("LocationError", "Error getting location: ${e.localizedMessage}")
            }
    } catch (e: SecurityException) {
        Log.e("LocationError", "Permission not granted: ${e.localizedMessage}")
    }
}

@SuppressLint("MissingPermission")
fun requestNewLocationData(
    fusedLocationProviderClient: FusedLocationProviderClient,
    onLocationRetrieved: (LatLng) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
        .setWaitForAccurateLocation(true)
        .setMinUpdateIntervalMillis(1000)
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                onLocationRetrieved(latLng)
                fusedLocationProviderClient.removeLocationUpdates(this)
            }
        }
    }

    fusedLocationProviderClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        null
    )
}