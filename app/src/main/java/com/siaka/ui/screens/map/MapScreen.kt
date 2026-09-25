package com.siaka.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.siaka.R
import com.siaka.data.MapUiState
import com.siaka.ui.theme.DangerRed
import com.siaka.ui.theme.DarkNavy
import com.siaka.ui.theme.PrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    routeId: Long? = null,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(routeId) {
        if (routeId != null) {
            viewModel.loadSavedRoute(routeId)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(fineLocationGranted || coarseLocationGranted)
    }

    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Handle back-button to exit
    BackHandler(enabled = uiState.isNavigating || uiState.isRouteGenerated) {
        if (uiState.isNavigating) {
            viewModel.stopNavigation()
        } else if (uiState.isRouteGenerated) {
            viewModel.clearRoute()
        }
    }

    MapScreenContent(
        uiState = uiState,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
    uiState: MapUiState,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val viewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(12.0)
            center(Point.fromLngLat(36.817223, -1.286389))
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(uiState.userLocation, uiState.shouldCenterOnLocation, uiState.isNavigating) {
        uiState.userLocation?.let { location ->
            val point = Point.fromLngLat(location.longitude, location.latitude)
            if (uiState.shouldCenterOnLocation || uiState.isNavigating) {
                viewportState.flyTo(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(point)
                        // Tilt and zoom more when navigating for 3D effect
                        .zoom(if (uiState.isNavigating) 18.0 else 15.0)
                        .pitch(if (uiState.isNavigating) 45.0 else 0.0)
                        .build()
                )
                if (uiState.shouldCenterOnLocation) {
                    viewModel.onMapCentered()
                }
            }
        }
    }

    LaunchedEffect(uiState.generatedRoutePoints) {
        if (uiState.generatedRoutePoints.isNotEmpty() && !uiState.isNavigating) {
            val points = uiState.generatedRoutePoints.map { Point.fromLngLat(it.longitude, it.latitude) }
            val cameraOptions = viewportState.cameraForCoordinates(
                points,
                coordinatesPadding = EdgeInsets(100.0, 100.0, 600.0, 100.0) // Extra padding at bottom for UI
            )
            // Use setCameraOptions for immediate positioning if it's the first time
            viewportState.setCameraOptions(cameraOptions)
            // Also flyTo for a smooth transition if points change later
            viewportState.flyTo(cameraOptions)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!uiState.isNavigating) {
            TopAppBar(
                title = {
                    Text(
                        text = "CyclePath",
                        color = PrimaryDark,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            // 1. Map Layer
            if (uiState.isLocationPermissionGranted) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = viewportState
                ) {
                    // Draw Polyline Route
                    if (uiState.generatedRoutePoints.isNotEmpty()) {
                        PolylineAnnotation(
                            points = uiState.generatedRoutePoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                        ) {
                            lineColor = Color(0xFF2196F3) // Bright blue
                            lineWidth = 6.0
                            lineBorderColor = Color(0xFF0D47A1)
                            lineBorderWidth = 2.0
                        }
                    }

                    // Draw User Location as Compose UI (ViewAnnotation)
                    uiState.userLocation?.let { location ->
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(Point.fromLngLat(location.longitude, location.latitude))
                                allowOverlap(true)
                            }
                        ) {
                            UserLocationMarker(
                                isNavigating = uiState.isNavigating,
                                bearing = location.bearing ?: 0f // Assumes ViewModel provides device bearing
                            )
                        }
                    }
                }
            }

            // 2. Top Navigation Banner (Turn-by-Turn)
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isNavigating,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
            ) {
                Column {
                    NavigationTopBanner(
                        instruction = uiState.nextInstruction,
                        distance = uiState.distanceToNextInstructionMeters
                    )
                    
                    // Route Progress Bar
                    val progress = remember(uiState.remainingDistanceKm, uiState.totalRouteDistanceKm) {
                        if (uiState.totalRouteDistanceKm > 0) {
                            (1f - (uiState.remainingDistanceKm / uiState.totalRouteDistanceKm).toFloat()).coerceIn(0f, 1f)
                        } else 0f
                    }
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shadow(2.dp, RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50), // Nice green
                        trackColor = Color.White.copy(alpha = 0.5f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // 3. Search Bar (When Idle)
            androidx.compose.animation.AnimatedVisibility(
                visible = !uiState.isNavigating && !uiState.isRouteGenerated,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                SearchBarOverlay(onClick = viewModel::onShowDistanceDialog)
            }

            // 4. Map Action Buttons (Right side)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = if (uiState.isNavigating) 140.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapControlCircleButton(R.drawable.zoom_in) {
                    viewportState.cameraState?.let {
                        viewportState.flyTo(
                            com.mapbox.maps.CameraOptions.Builder()
                                .zoom(it.zoom + 1.0)
                                .build()
                        )
                    }
                }
                MapControlCircleButton(R.drawable.zoom_out) {
                    viewportState.cameraState?.let {
                        viewportState.flyTo(
                            CameraOptions.Builder()
                                .zoom(it.zoom - 1.0)
                                .build()
                        )
                    }
                }
                MapControlCircleButton(R.drawable.gps) { viewModel.onCenterOnLocationRequested() }
            }

            // 5. Automatic Offline Progress Indicator (Subtle)
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isOfflineDownloading,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { uiState.offlineDownloadProgress ?: 0f },
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryDark
                        )
                        Text(
                            "Saving offline map...",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryDark
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()) // Lift above NavigationBar
            ) {
                // State: Route has been generated but not started
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.isRouteGenerated && !uiState.isNavigating,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    RouteActionButtons(
                        onRegenerate = viewModel::onShowDistanceDialog,
                        onStart = viewModel::startNavigation,
                        onSave = viewModel::showSaveRouteDialog,
                        onClose = viewModel::clearRoute
                    )
                }

                // State: Actively Navigating
                if (uiState.isNavigating) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Real-time Stats Dashboard
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                label = "Speed",
                                value = String.format("%.1f", uiState.currentSpeedKmh),
                                unit = "km/h",
                                icon = Icons.Default.Speed,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Altitude",
                                value = String.format("%.0f", uiState.altitudeMeters),
                                unit = "m",
                                icon = Icons.Default.Terrain,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = "Time",
                                value = formatElapsedTime(uiState.elapsedTimeSeconds),
                                unit = "",
                                icon = Icons.Default.Timer,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pause/Resume Button
                            Button(
                                onClick = viewModel::togglePause,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isPaused) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (uiState.isPaused) "Resume" else "Pause",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // End Navigation Button
                            Button(
                                onClick = viewModel::stopNavigation,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "End",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (uiState.showDistanceDialog) {
            DistanceInputDialog(
                distance = uiState.distanceInput,
                onDistanceChange = viewModel::onDistanceChange,
                onConfirm = viewModel::generateRoute,
                onDismiss = viewModel::onDismissDistanceDialog
            )
        }

        if (uiState.showSaveRouteDialog) {
            SaveRouteDialog(
                routeName = uiState.routeNameInput,
                onRouteNameChange = viewModel::onRouteNameChange,
                onConfirm = viewModel::saveRoute,
                onDismiss = viewModel::dismissSaveRouteDialog,
                isSaving = uiState.isSaving
            )
        }

        if (uiState.showSummaryDialog) {
            RideSummaryDialog(
                distance = uiState.lastRideDistanceKm,
                durationSeconds = uiState.lastRideDurationSeconds,
                avgSpeed = uiState.lastRideAvgSpeedKmh,
                onDismiss = viewModel::dismissSummary
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RideSummaryDialog(
    distance: Double,
    durationSeconds: Long,
    avgSpeed: Double,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ride Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Great job! Here's how you did.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatItem(
                        label = "Distance",
                        value = String.format("%.2f", distance),
                        unit = "km",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatItem(
                        label = "Avg Speed",
                        value = String.format("%.1f", avgSpeed),
                        unit = "km/h",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatItem(
                        label = "Time",
                        value = formatElapsedTime(durationSeconds),
                        unit = "",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatItem(
                        label = "Elevation",
                        value = "---", // Could track total elevation gain later
                        unit = "m",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Close")
                    }
                    
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF5F5F7), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

fun formatElapsedTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

@Composable
fun UserLocationMarker(isNavigating: Boolean, bearing: Float = 0f) {
    if (isNavigating) {
        // Navigation Chevron Match
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, DarkNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "User Location",
                tint = DarkNavy,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(bearing) // Rotates arrow based on direction
            )
        }
    } else {
        // Standard Blue Dot with white-border
        Box(
            modifier = Modifier
                .size(24.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF2196F3))
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
        )
    }
}

@Composable
fun SearchBarOverlay(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Enter loop distance (km)...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NavigationTopBanner(instruction: String, distance: Int) {
    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Turn Icon Box
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.25f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.TurnRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = instruction.ifEmpty { "Proceed on route" },
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$distance",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "M",
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RouteActionButtons(
    onRegenerate: () -> Unit,
    onStart: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(24.dp))

            // Start Navigation (Primary Focus)
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Navigation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onRegenerate,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regenerate", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Route", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}
}

@Composable
fun MapControlCircleButton(iconResId: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DistanceInputDialog(
    distance: String,
    onDistanceChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Set Loop Distance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "How many kilometers would you like to cycle today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = distance,
                    onValueChange = onDistanceChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Distance in km") },
                    placeholder = { Text("1.4") },
                    suffix = { Text("km") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Generate", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun SaveRouteDialog(
    routeName: String,
    onRouteNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Save Your Route",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Give your journey a name to find it easily later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = routeName,
                    onValueChange = onRouteNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Route Name") },
                    placeholder = { Text("e.g. Morning Forest Loop") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isSaving
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MapScreenPreview() {
//    CyclePathTheme {
//        MapScreenContent(
//            uiState = MapUiState(
//                isLocationPermissionGranted = true,
//                userLocation = LocationPoint(-1.286389, 36.817223)
//            ),
//            onPermissionResult = {},
//            onMapCentered = {},
//            onCenterOnLocationRequested = {},
//            onStopNavigation = {},
//            onClearRoute = {},
//            onGenerateRoute = {},
//            onStartNavigation = {},
//            onSaveRoute = {},
//            onConfirmSaveRoute = {},
//            onDismissSaveRoute = {},
//            onRouteNameChange = {},
//            onDistanceInputChange = {},
//            onShowDistanceDialog = {},
//            onDismissDistanceDialog = {},
//            onSnackbarDismissed = {}
//        )
//    }
//}

