import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.example.redtaximappoc.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LocationService(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(5000)
        .setMaxUpdateDelayMillis(15000)
        .build()

    private var locationCallback: LocationCallback? = null

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(onLocationResult: (Location) -> Unit) {
        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onLocationResult(location)
                    } else {
                        // Last location might be null, request location updates instead
                        requestLocationUpdates { updatedLocation ->
                            onLocationResult(updatedLocation)
                            removeLocationUpdates() // Get just one update
                        }
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(onLocationResult: (Location) -> Unit) {
        if (hasLocationPermission()) {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        onLocationResult(location)
                    }
                }
            }

            locationCallback?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    it,
                    context.mainLooper
                )
            }
        }
    }

    fun removeLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }
}

@Composable
fun RedTaxiBookRideScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Initialize location service
    val locationService = remember { LocationService(context) }

    // State for user's current location
    val currentLocation = remember { mutableStateOf<LatLng?>(null) }

    // Map state and camera position
    val defaultLatLng = LatLng(19.0760, 72.8777) // Default to Mumbai if location not available
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation.value ?: defaultLatLng, 15f)
    }

    // States for UI control
    val isMapDragging = remember { mutableStateOf(false) }
    val isLocationConfirmed = remember { mutableStateOf(true) }
    val selectedTabIndex = remember { mutableStateOf(0) }
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
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        )
    }

    // Location permission handling
    val hasLocationPermission = remember {
        mutableStateOf(locationService.hasLocationPermission())
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission.value = isGranted
        if (isGranted) {
            // Get user location after permission is granted
            fetchUserLocation(locationService, currentLocation, cameraPositionState, coroutineScope)
        }
    }

    // Initialize by fetching current location
    LaunchedEffect(Unit) {
        if (hasLocationPermission.value) {
            fetchUserLocation(locationService, currentLocation, cameraPositionState, coroutineScope)
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Clean up location updates when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            locationService.removeLocationUpdates()
        }
    }

    // Camera movement listeners
    LaunchedEffect(cameraPositionState.isMoving) {
        isMapDragging.value = cameraPositionState.isMoving
        if (!cameraPositionState.isMoving && !isLocationConfirmed.value) {
            // Delay location confirmation to make UI smoother
            kotlinx.coroutines.delay(500)
            isLocationConfirmed.value = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Google Maps
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLoaded = {
                // Map is loaded and ready
            },
            onMapClick = {
                // This will trigger when user taps on the map
                isMapDragging.value = true
                isLocationConfirmed.value = false
                // After a short delay, consider the location as confirmed
                coroutineScope.launch {
                    kotlinx.coroutines.delay(500)
                    isLocationConfirmed.value = true
                }
            }
        )

        // Top bar with menu button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(2f)
        ) {
            IconButton(
                onClick = { /* Open menu */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Red
                )
            }

            // Current location pill
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Current Location",
                        tint = Color(0xFF00AA00),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Location",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Center location marker (fixed position overlay)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Selected Location",
                    tint = Color.Red,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Blue, CircleShape)
                )
            }
        }

        // Bottom sheet with ride options
        val bottomSheetHeight by animateDpAsState(
            targetValue = if (isMapDragging.value && !isLocationConfirmed.value) 0.dp else 450.dp,
            label = "Bottom Sheet Animation"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomSheetHeight)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Tab row for ride options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RideOption(
                        icon = R.drawable.ic_innova_active,
                        title = "Local",
                        isSelected = selectedTabIndex.value == 0,
                        onClick = { selectedTabIndex.value = 0 }
                    )
                    RideOption(
                        icon = R.drawable.ic_innova_active,
                        title = "Rental",
                        isSelected = selectedTabIndex.value == 1,
                        onClick = { selectedTabIndex.value = 1 }
                    )
                    RideOption(
                        icon = R.drawable.ic_innova_active,
                        title = "Outstation",
                        isSelected = selectedTabIndex.value == 2,
                        onClick = { selectedTabIndex.value = 2 }
                    )
                    RideOption(
                        icon = R.drawable.ic_innova_active,
                        title = "Auto",
                        isSelected = selectedTabIndex.value == 3,
                        onClick = { selectedTabIndex.value = 3 }
                    )
                    RideOption(
                        icon = R.drawable.ic_innova_active,
                        title = "Bike",
                        isSelected = selectedTabIndex.value == 4,
                        onClick = { selectedTabIndex.value = 4 }
                    )
                }

                // Search bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Where do you want to go?",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                // Destination suggestions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    DestinationItem(
                        title = "Phoenix Marketcity",
                        address = "Whitefield Main Road, Devasandra Phase...",
                        isFavorite = false
                    )
                    Divider(modifier = Modifier.padding(start = 72.dp, end = 16.dp))

                    DestinationItem(
                        title = "PVR Aura",
                        address = "International Tech Park Square Madison, B...",
                        isFavorite = false
                    )
                    Divider(modifier = Modifier.padding(start = 72.dp, end = 16.dp))

                    DestinationItem(
                        title = "The Hole In The Wall Cafe",
                        address = "3, 8th Main Rd, 4th Block, Koramangala, B...",
                        isFavorite = false
                    )
                }

                // Bottom indicator line
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        // My Location button
        FloatingActionButton(
            onClick = {
                // Move camera to user's current location using Fused Location
                if (hasLocationPermission.value) {
                    fetchUserLocation(locationService, currentLocation, cameraPositionState, coroutineScope)
                } else {
                    // Request permission again
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = bottomSheetHeight + 16.dp, end = 16.dp)
                .size(48.dp),
            containerColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "My Location",
                tint = Color.Red
            )
        }
    }
}

// Helper function to fetch user location and update camera
private fun fetchUserLocation(
    locationService: LocationService,
    currentLocation: MutableState<LatLng?>,
    cameraPositionState: CameraPositionState,
    coroutineScope: CoroutineScope
) {
    locationService.getLastLocation { location ->
        val latLng = LatLng(location.latitude, location.longitude)
        currentLocation.value = latLng

        // Animate camera to the current location
        coroutineScope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            )
        }
    }
}

@Composable
fun RideOption(
    icon: Int,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color.Red else Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Red else Color.Black
        )
    }

    if (isSelected) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(32.dp)
                .height(2.dp)
                .background(Color.Red)
        )
    }
}

@Composable
fun DestinationItem(
    title: String,
    address: String,
    isFavorite: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = address,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = { /* Toggle favorite */ }) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color(0xFFFFD700) else Color.Gray
            )
        }
    }
}

// Add to your app's resources folder: res/raw/map_style.json
// This custom style file will make the map look like the one in your screenshot
/*
[
  {
    "featureType": "poi",
    "elementType": "labels.icon",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#ffffff"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca5b3"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#e8f0f9"
      }
    ]
  }
]
*/

// Don't forget to add these dependencies to your build.gradle
/*
// Google Maps and Location
implementation "com.google.maps.android:maps-compose:2.15.0"
implementation "com.google.android.gms:play-services-maps:18.2.0"
implementation "com.google.android.gms:play-services-location:21.1.0"
*/

// And add the Google Maps API key to your AndroidManifest.xml
/*
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY" />
*/

// Required permissions in AndroidManifest.xml
/*
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
*/