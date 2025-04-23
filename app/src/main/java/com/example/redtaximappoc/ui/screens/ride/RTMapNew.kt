package com.example.redtaximappoc.ui.screens.ride

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.android.gms.location.LocationServices
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
fun RTMapNew() {
    val bottomSheetVisibility = remember {
        mutableStateOf(true)
    }
    var isAnimationCompleted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val cameraPositionState = rememberCameraPositionState()
    val isDragging = remember { mutableStateOf(false) }

    // Custom composable for fixed center marker
    @Composable
    fun CenterMarker() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location Marker",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .offset(y = (-20).dp) // Offset to align pin point with center
            )
        }
    }

    // Fetch and move camera to current location
    fun moveToCurrentLocation() {
        getCurrentLocation(fusedLocationProviderClient) { location ->
            val newLatLng = LatLng(location.latitude, location.longitude)
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(newLatLng, 18f))
            Log.d("Marker Position", "Initial position set - Lat: ${newLatLng.latitude}, Lng: ${newLatLng.longitude}")
        }
    }

    // Monitor camera movement and log positions
    LaunchedEffect(cameraPositionState.position) {
        val centerPos = cameraPositionState.position.target
        Log.d("Marker Position", "Camera moved - Lat: ${centerPos.latitude}, Lng: ${centerPos.longitude}")
    }

    // Monitor drag state
    LaunchedEffect(cameraPositionState.isMoving) {
        when {
            // Drag started
            cameraPositionState.isMoving &&
                    cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE -> {
                isDragging.value = true
                bottomSheetVisibility.value = false
                Log.d("Map Dragging", "Dragging started")
            }
            // Drag ended
            isDragging.value && !cameraPositionState.isMoving -> {
                isDragging.value = false
                val center = cameraPositionState.position.target
                Log.d("Map Dragging", "Drag ended at position - Lat: ${center.latitude}, Lng: ${center.longitude}")
            }
        }
    }

    // Initial location setup
    LaunchedEffect(Unit) {
        moveToCurrentLocation()
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = false
        )
    }

    val mapProperties = remember {
        MapProperties(isMyLocationEnabled = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
            // Map without actual markers
            GoogleMap(
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = {
                    Log.d("Map Status", "Map loaded, center position - Lat: ${cameraPositionState.position.target.latitude}, Lng: ${cameraPositionState.position.target.longitude}")
                }
            )

            // Fixed center marker overlay using the default marker style
            CenterMarker()

            // Menu icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    alignment = Alignment.TopStart,
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

            Image(
                painter = painterResource(R.drawable.icon_my_location),
                contentDescription = "My Location",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        moveToCurrentLocation()
                        Log.d("Location Button", "My location button clicked")
                    },
                alignment = Alignment.TopEnd
            )
        }

        // Bottom sheet with animation
        AnimatedVisibility(
            modifier = Modifier.background(Color.Transparent),
            visible = bottomSheetVisibility.value,
            enter = slideInVertically(
                initialOffsetY = { it }, // Slide in from bottom
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 30f
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }, // Slide out to bottom
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessHigh
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .clip(shape = RoundedCornerShape(topEnd = 30.dp, topStart = 30.dp))
                    .background(Color.LightGray)
                    .pointerInput(Unit) {
                        detectTapGestures { /* Consume gestures to block map interactions */ }
                    }
            ) {
                ElevatedButton(
                    onClick = {
                        bottomSheetVisibility.value = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Black)
                ) {
                    Text("Dismiss Bottom View", color = Color.White)
                }
            }
        }

        // Bottom sheet with animation
        AnimatedVisibility(
            visible = !bottomSheetVisibility.value,
            enter = slideInVertically(
                initialOffsetY = { it }, // Slide in from bottom
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 30f
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }, // Slide out to bottom
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessHigh
                )
            )
        ) {
            Button(
                onClick = {
                    // Get the current center point when confirming
                    val selectedLocation = cameraPositionState.position.target
                    Log.d("Selected Location", "Location confirmed - Lat: ${selectedLocation.latitude}, Lng: ${selectedLocation.longitude}")
                    bottomSheetVisibility.value = true
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

@Composable
@Preview(showBackground = true)
fun RTMapNewPreview() {
    RTMapNew()
}