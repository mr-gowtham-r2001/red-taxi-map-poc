package com.example.redtaximappoc.ui.screens.ride

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.redtaximappoc.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun RTMap() {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val currentLocationMarkerState = rememberMarkerState()
    val cameraPositionState = rememberCameraPositionState()


    // Track if bottom sheet should be shown
    var showBottomSheet by remember { mutableStateOf(true) }

    // Track if this is the initial load
    var isConfirmLocationButtonClicked by remember { mutableStateOf(false) }

    // Track camera position changes
    var lastCameraPosition by remember { mutableStateOf<CameraPosition?>(null) }

    // Define your fixed offset for camera position
    val fixedOffset = remember { LatLng(-0.0015, 0.0) }

    // Function to apply the offset to a location
    fun applyOffset(location: LatLng): LatLng {
        return LatLng(
            location.latitude + fixedOffset.latitude,
            location.longitude + fixedOffset.longitude
        )
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false
        )
    }
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true,
        )
    }

    // Initial location setup
    LaunchedEffect(Unit) {
        getCurrentLocation(fusedLocationProviderClient) { location ->
            val offsetLocation = applyOffset(location)
            currentLocationMarkerState.position = LatLng(location.latitude, location.longitude)
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(offsetLocation, 18f))
        }
    }

    // Handle camera position changes
    LaunchedEffect(cameraPositionState.position) {
        // Update marker position to follow camera with offset
        val targetMarker = cameraPositionState.position.target
        currentLocationMarkerState.position = LatLng(targetMarker.latitude + 0.0015, targetMarker.longitude)

        // Save current camera position for future comparison
        lastCameraPosition = cameraPositionState.position
    }


        when (cameraPositionState.cameraMoveStartedReason) {
            CameraMoveStartedReason.GESTURE -> {
                // User started dragging the map
                if (isConfirmLocationButtonClicked) {
                    isConfirmLocationButtonClicked = false
                    Log.d("MapDrag", "isConfirmLocationButtonClicked Map drag started by gesture")
                } else {
                    showBottomSheet = false
                    Log.d("MapDrag", "showBottomSheet Map drag started by gesture")
                }
            }

            else -> {
                // Initial state or reason unknown
            }
        }


    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier
                .fillMaxSize()

            ,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapClick = {
                // Hide bottom sheet if map is clicked
                if (showBottomSheet) {
                    showBottomSheet = false
                }
            }
        ) {
            Marker(
                title = "Current Location",
                draggable = true,
                state = currentLocationMarkerState,
            )
        }

        // Menu icon
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Image(
                painter = painterResource(R.drawable.icon_menu),
                contentDescription = "Menu",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // Menu action
                    }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
        ) {
            // My location button
            Image(
                painter = painterResource(R.drawable.icon_my_location),
                contentDescription = "My Location",
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        getCurrentLocation(fusedLocationProviderClient) { location ->
                            val offsetLocation = applyOffset(location)
                            currentLocationMarkerState.position =
                                LatLng(location.latitude, location.longitude)
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(
                                    offsetLocation,
                                    18f
                                )
                            )
                            // Reset dragging state when returning to current location
                        }
                    },
            )

            // Bottom sheet with animation
            AnimatedVisibility(
                visible = showBottomSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .clip(shape = RoundedCornerShape(topEnd = 30.dp, topStart = 30.dp))
                        .background(Color.White)
                        .pointerInput(Unit) {
                            detectTapGestures { /* Consume gestures to block map interactions */ }
                        }
                ) {
                    ElevatedButton(
                        onClick = {
                            showBottomSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = Black)
                    ) {
                        Text("Dismiss Bottom View", color = Color.White)
                    }

                    // Add your bottom sheet content here
                }
            }

            // Confirm Location button - show only when bottom sheet is hidden
            AnimatedVisibility(
                visible = !showBottomSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                ElevatedButton(
                    onClick = {
                        // Show bottom sheet when location is confirmed
                        isConfirmLocationButtonClicked = true
                        showBottomSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Black)
                ) {
                    Text("Confirm Location", color = Color.White)
                }
            }
        }
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

@Composable
@Preview(showBackground = true)
fun RTRidePreview() {
    RTMap()
}