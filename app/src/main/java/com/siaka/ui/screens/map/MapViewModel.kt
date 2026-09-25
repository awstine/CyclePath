package com.siaka.ui.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siaka.data.LocationManager
import com.siaka.data.LocationPoint
import com.siaka.data.MapUiState
import com.siaka.data.MapboxRouteGenerator
import com.siaka.data.OfflineMapManager
import com.siaka.data.local.RouteDao
import com.siaka.data.local.SavedRoute
import com.siaka.data.local.CompletedRide
import com.siaka.data.local.CompletedRideDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationManager: LocationManager,
    private val routeGenerator: MapboxRouteGenerator,
    private val routeDao: RouteDao,
    private val completedRideDao: CompletedRideDao,
    private val offlineMapManager: OfflineMapManager
) : ViewModel() {

    companion object {
        private const val TAG = "MapViewModel"
        private const val MAX_TRACKING_ACCURACY_METERS = 25f
        private const val MIN_MOVING_SPEED_MPS = 1.0f // 3.6 km/h
        private const val MIN_MOVEMENT_DISTANCE_METERS = 8.0
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var statsJob: Job? = null
    private var hasAutoDownloaded = false
    private var startTimeMillis: Long = 0
    private var pausedAtMillis: Long? = null
    private var totalPausedMillis: Long = 0
    private var lastTrackedLocation: LocationPoint? = null

    init {
        viewModelScope.launch {
            offlineMapManager.downloadProgress.collect { progress ->
                _uiState.update { it.copy(offlineDownloadProgress = progress) }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted, isMyLocationEnabled = isGranted) }
        if (isGranted) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationManager.getLocationUpdates().collect { location ->
                val point = LocationPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    bearing = location.bearing,
                    accuracyMeters = location.accuracy.takeIf { location.hasAccuracy() },
                    speedMps = location.speed.takeIf { location.hasSpeed() }
                )
                _uiState.update { it.copy(userLocation = point) }

                if (_uiState.value.isNavigating) {
                    val previous = lastTrackedLocation
                    if (!_uiState.value.isPaused && previous != null) {
                        val segmentKm = distanceBetween(previous, point)
                        if (isCredibleMovement(previous, point, segmentKm)) {
                            _uiState.update { state ->
                                val distance = state.totalDistanceCoveredKm + segmentKm
                                val elapsedHours = state.elapsedTimeSeconds / 3600.0
                                state.copy(
                                    totalDistanceCoveredKm = distance,
                                    currentSpeedKmh = if (location.hasSpeed()) location.speed * 3.6 else 0.0,
                                    averageSpeedKmh = if (elapsedHours > 0) distance / elapsedHours else 0.0
                                )
                            }
                        }
                    }
                    lastTrackedLocation = point
                }
                
                // Auto-center on first location if needed
                if (_uiState.value.shouldCenterOnLocation) {
                    _uiState.update { it.copy(shouldCenterOnLocation = false) }
                }

                // Auto-download offline maps for current area once
                if (!hasAutoDownloaded) {
                    hasAutoDownloaded = true
                    // viewModelScope.launch { offlineMapManager.downloadRegion(location) }
                }
            }
        }
    }

    fun onDistanceChange(distance: String) {
        _uiState.update { it.copy(distanceInput = distance) }
    }

    fun onCenterOnLocationRequested() {
        _uiState.update { it.copy(shouldCenterOnLocation = true) }
    }

    fun onMapCentered() {
        _uiState.update { it.copy(shouldCenterOnLocation = false) }
    }

    fun onShowDistanceDialog() {
        _uiState.update { it.copy(showDistanceDialog = true) }
    }

    fun onDismissDistanceDialog() {
        _uiState.update { it.copy(showDistanceDialog = false) }
    }

    fun startNavigation() {
        if (_uiState.value.generatedRoutePoints.isEmpty()) return
        
        _uiState.update {
            it.copy(
                isNavigating = true,
                isPaused = false,
                elapsedTimeSeconds = 0,
                totalDistanceCoveredKm = 0.0,
                averageSpeedKmh = 0.0,
                currentSpeedKmh = 0.0
            )
        }
        startTimeMillis = System.currentTimeMillis()
        pausedAtMillis = null
        totalPausedMillis = 0
        lastTrackedLocation = _uiState.value.userLocation
        startStatsTracking()
    }

    fun stopNavigation() {
        // The stats coroutine only refreshes once per second. Capture the exact
        // stop time here so the summary does not depend on the last tick.
        val now = System.currentTimeMillis()
        val pausedDuration = if (_uiState.value.isPaused) {
            pausedAtMillis?.let { now - it } ?: 0L
        } else {
            0L
        }
        val finalElapsedSeconds = maxOf(
            0L,
            (now - startTimeMillis - totalPausedMillis - pausedDuration) / 1000L
        )
        val distance = _uiState.value.totalDistanceCoveredKm
        val finalAverageSpeed = if (finalElapsedSeconds > 0L) {
            distance / (finalElapsedSeconds / 3600.0)
        } else {
            0.0
        }

        _uiState.update {
            it.copy(
                isNavigating = false,
                isPaused = false,
                elapsedTimeSeconds = finalElapsedSeconds,
                averageSpeedKmh = finalAverageSpeed,
                lastRideDistanceKm = distance,
                lastRideDurationSeconds = finalElapsedSeconds,
                lastRideAvgSpeedKmh = finalAverageSpeed,
                showSummaryDialog = true
            )
        }
        stopStatsTracking()
        lastTrackedLocation = null
        saveCompletedRide()
    }

    fun togglePause() {
        val now = System.currentTimeMillis()
        if (_uiState.value.isPaused) {
            pausedAtMillis?.let { totalPausedMillis += now - it }
            pausedAtMillis = null
        } else {
            pausedAtMillis = now
        }
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    private fun startStatsTracking() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            while (true) {
                if (!_uiState.value.isPaused) {
                    _uiState.update {
                        it.copy(elapsedTimeSeconds = elapsedTimeSeconds())
                    }
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun stopStatsTracking() {
        statsJob?.cancel()
    }

    private fun saveCompletedRide() {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                completedRideDao.insertCompletedRide(
                    CompletedRide(
                        distanceKm = state.totalDistanceCoveredKm,
                        durationSeconds = state.elapsedTimeSeconds,
                        avgSpeedKmh = state.averageSpeedKmh,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error saving completed ride", e)
            }
        }
    }

    fun dismissSummary() {
        _uiState.update { it.copy(showSummaryDialog = false) }
        clearRoute()
    }

    fun clearRoute() {
        _uiState.update { 
            it.copy(
                generatedRoutePoints = emptyList(),
                routeSteps = emptyList(),
                isRouteGenerated = false,
                distanceInput = "",
                totalDistanceCoveredKm = 0.0,
                averageSpeedKmh = 0.0,
                currentSpeedKmh = 0.0,
                elapsedTimeSeconds = 0
            ) 
        }
    }

    fun showSaveRouteDialog() {
        _uiState.update { it.copy(showSaveRouteDialog = true) }
    }

    fun dismissSaveRouteDialog() {
        _uiState.update { it.copy(showSaveRouteDialog = false, routeNameInput = "") }
    }

    fun onRouteNameChange(name: String) {
        _uiState.update { it.copy(routeNameInput = name) }
    }

    fun saveRoute() {
        val name = _uiState.value.routeNameInput
        val points = _uiState.value.generatedRoutePoints
        if (name.isBlank() || points.isEmpty()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                routeDao.insertRoute(
                    SavedRoute(
                        name = name,
                        distanceKm = _uiState.value.totalRouteDistanceKm,
                        durationMinutes = _uiState.value.estimatedTimeMinutes,
                        points = points,
                        steps = _uiState.value.routeSteps,
                        timestamp = System.currentTimeMillis()
                    )
                )
                dismissSaveRouteDialog()
                _uiState.update { it.copy(isSaving = false, snackbarMessage = "Route saved successfully") }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving route", e)
                _uiState.update { it.copy(isSaving = false, snackbarMessage = "Failed to save route") }
            }
        }
    }

    fun loadSavedRoute(routeId: Long) {
        _uiState.update { it.copy(isLoadingRoute = true) }
        viewModelScope.launch {
            try {
                val route = routeDao.getRouteById(routeId)
                if (route != null) {
                    _uiState.update { 
                        it.copy(
                            generatedRoutePoints = route.points,
                            routeSteps = route.steps,
                            isRouteGenerated = true,
                            totalRouteDistanceKm = route.distanceKm,
                            estimatedTimeMinutes = route.durationMinutes,
                            isLoadingRoute = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingRoute = false, error = "Route not found") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading route", e)
                _uiState.update { it.copy(isLoadingRoute = false, error = "Failed to load route") }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun generateRoute() {
        Log.i(TAG, "==== GENERATE BUTTON CLICKED ====")
        val currentUserLocation = _uiState.value.userLocation ?: run {
            Log.e(TAG, "GENERATE FAIL: No user location available in state")
            _uiState.update { it.copy(snackbarMessage = "Waiting for GPS location...") }
            return
        }
        
        val distanceText = _uiState.value.distanceInput
        Log.i(TAG, "INPUT DISTANCE: '$distanceText'")
        
        val distance = distanceText.toDoubleOrNull() ?: run {
            Log.e(TAG, "GENERATE FAIL: Invalid distance string: '$distanceText'")
            _uiState.update { it.copy(snackbarMessage = "Please enter a valid number") }
            return
        }

        if (!distance.isFinite() || distance <= 0.0 || distance > 100.0) {
            _uiState.update { it.copy(snackbarMessage = "Enter a distance between 1 and 100 km") }
            return
        }

        Log.i(TAG, "STARTING ROUTE GENERATION: Center=${currentUserLocation.latitude},${currentUserLocation.longitude}, Distance=${distance}km")
        
        _uiState.update { it.copy(isLoadingRoute = true, error = null) }

        viewModelScope.launch {
            try {
                Log.i(TAG, "CALLING: routeGenerator.generateLoopRoute...")
                when (val result = routeGenerator.generateLoopRoute(
                    centerPoint = currentUserLocation,
                    targetDistanceKm = distance
                )) {
                    is com.siaka.data.MapboxRouteGenerator.RouteResult.Success -> {
                        val routeData = result.data
                        Log.i(TAG, "SUCCESS: Received routeData: ${routeData.points.size} points")
                        
                        _uiState.update { 
                            it.copy(
                                generatedRoutePoints = routeData.points,
                                routeSteps = routeData.steps,
                                isRouteGenerated = routeData.points.isNotEmpty(),
                                isLoadingRoute = false,
                                showDistanceDialog = false,
                                remainingDistanceKm = routeData.totalDistanceKm,
                                totalRouteDistanceKm = routeData.totalDistanceKm,
                                estimatedTimeMinutes = routeData.totalDurationMinutes,
                                remainingMinutes = routeData.totalDurationMinutes,
                                snackbarMessage = if (routeData.points.isEmpty()) "No route found for this area" else "Route generated!"
                            )
                        }
                    }
                    is com.siaka.data.MapboxRouteGenerator.RouteResult.Error -> {
                        Log.e(TAG, "FAIL: ${result.message}")
                        _uiState.update { 
                            it.copy(isLoadingRoute = false, snackbarMessage = result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "CRITICAL EXCEPTION in generateRoute", e)
                _uiState.update { 
                    it.copy(
                        isLoadingRoute = false, 
                        snackbarMessage = "System error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun elapsedTimeSeconds(): Long {
        val currentPause = pausedAtMillis?.let { System.currentTimeMillis() - it } ?: 0L
        return maxOf(0L, (System.currentTimeMillis() - startTimeMillis - totalPausedMillis - currentPause) / 1000L)
    }

    /**
     * GPS coordinates naturally wander while a phone is stationary. Only count
     * a segment when the fix is accurate, the device reports actual movement,
     * and the displacement is larger than the expected location uncertainty.
     */
    private fun isCredibleMovement(
        previous: LocationPoint,
        current: LocationPoint,
        segmentKm: Double
    ): Boolean {
        val currentAccuracy = current.accuracyMeters ?: return false
        val previousAccuracy = previous.accuracyMeters ?: return false
        val speedMps = current.speedMps ?: return false
        val segmentMeters = segmentKm * 1000.0
        val accuracyThreshold = max(
            MIN_MOVEMENT_DISTANCE_METERS,
            max(currentAccuracy, previousAccuracy).toDouble() * 1.25
        )

        return currentAccuracy <= MAX_TRACKING_ACCURACY_METERS &&
            previousAccuracy <= MAX_TRACKING_ACCURACY_METERS &&
            speedMps >= MIN_MOVING_SPEED_MPS &&
            segmentMeters >= accuracyThreshold &&
            segmentKm <= 0.2
    }

    private fun distanceBetween(first: LocationPoint, second: LocationPoint): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(second.latitude - first.latitude)
        val dLon = Math.toRadians(second.longitude - first.longitude)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(first.latitude)) * cos(Math.toRadians(second.latitude)) *
            sin(dLon / 2).pow(2)
        return earthRadiusKm * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        statsJob?.cancel()
    }
}
