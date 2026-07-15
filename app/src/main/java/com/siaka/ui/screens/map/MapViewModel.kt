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
    private val completedRideDao: com.siaka.data.local.CompletedRideDao,
    private val offlineMapManager: OfflineMapManager
) : ViewModel() {

    companion object {
        private const val TAG = "MapViewModel"
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var statsJob: Job? = null
    private var hasAutoDownloaded = false
    private var startTimeMillis: Long = 0
    private var lastTrackedLocation: LocationPoint? = null

    init {
        viewModelScope.launch {
            offlineMapManager.downloadProgress.collect { progress ->
                _uiState.update { it.copy(offlineDownloadProgress = progress) }
            }
        }
        viewModelScope.launch {
            offlineMapManager.isDownloading.collect { isDownloading ->
                _uiState.update { it.copy(isOfflineDownloading = isDownloading) }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update {
            it.copy(
                isLocationPermissionGranted = isGranted,
                isMyLocationEnabled = isGranted
            )
        }
        if (isGranted) {
            startLocationTracking()
        }
    }

    private fun startLocationTracking() {
        // Prevent launching duplicate collection jobs
        locationJob?.cancel()

        locationJob = viewModelScope.launch {
            locationManager.getLocationUpdates(intervalMillis = 2000).collect { location ->
                val userPoint = LocationPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    bearing = if (location.hasBearing()) location.bearing else null
                )
                
                val speedKmh = if (location.hasSpeed()) location.speed * 3.6 else 0.0
                val altitude = if (location.hasAltitude()) location.altitude else 0.0

                _uiState.update { state ->
                    if (state.isPaused) return@update state
                    
                    val distanceDeltaKm = lastTrackedLocation?.let { 
                        calculateDistance(it, userPoint) / 1000.0 
                    } ?: 0.0
                    
                    // Only count movement if speed is > 1km/h to avoid GPS jitter while stationary
                    val validDistanceDelta = if (speedKmh > 1.0) distanceDeltaKm else 0.0
                    lastTrackedLocation = userPoint

                    var updatedState = state.copy(
                        userLocation = userPoint,
                        currentSpeedKmh = speedKmh,
                        altitudeMeters = altitude,
                        totalDistanceCoveredKm = state.totalDistanceCoveredKm + validDistanceDelta
                    )
                    if (state.isNavigating) {
                        updatedState = updateNavigationDetails(updatedState, userPoint)
                    }
                    updatedState
                }
            }
        }
    }

    private fun startStatsTimer() {
        statsJob?.cancel()
        startTimeMillis = System.currentTimeMillis()
        statsJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (!_uiState.value.isPaused) {
                    val elapsed = _uiState.value.elapsedTimeSeconds + 1
                    _uiState.update { it.copy(elapsedTimeSeconds = elapsed) }
                }
            }
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    private fun updateNavigationDetails(state: MapUiState, userLocation: LocationPoint): MapUiState {
        val points = state.generatedRoutePoints
        val steps = state.routeSteps
        if (points.isEmpty() || steps.isEmpty()) return state

        // 1. Find the next instruction
        val nextStep = steps.firstOrNull { step ->
            calculateDistance(userLocation, step.location) > 30 
        } ?: steps.last()

        val distanceToNext = calculateDistance(userLocation, nextStep.location).toInt()

        // 2. Calculate REAL remaining distance to finish
        // Find the index of the point on the route closest to the user
        var closestIdx = 0
        var minDistance = Double.MAX_VALUE
        points.forEachIndexed { index, point ->
            val d = calculateDistance(userLocation, point)
            if (d < minDistance) {
                minDistance = d
                closestIdx = index
            }
        }

        // Sum distance from user to the closest point, then from there to the end
        var remainingMeters = calculateDistance(userLocation, points[closestIdx])
        for (i in closestIdx until points.size - 1) {
            remainingMeters += calculateDistance(points[i], points[i + 1])
        }

        return state.copy(
            nextInstruction = nextStep.instruction,
            distanceToNextInstructionMeters = distanceToNext,
            remainingDistanceKm = remainingMeters / 1000.0
        )
    }

    private fun calculateDistance(p1: LocationPoint, p2: LocationPoint): Double {
        val r = 6371e3 // Earth radius in meters
        val phi1 = Math.toRadians(p1.latitude)
        val phi2 = Math.toRadians(p2.latitude)
        val dPhi = Math.toRadians(p2.latitude - p1.latitude)
        val dLambda = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(dPhi / 2) * sin(dPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(dLambda / 2) * sin(dLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    fun onDistanceInputChange(distance: String) {
        _uiState.update { it.copy(distanceInput = distance) }
    }

    fun onShowDistanceDialog() {
        _uiState.update { it.copy(showDistanceDialog = true) }
    }

    fun onDismissDistanceDialog() {
        _uiState.update { it.copy(showDistanceDialog = false) }
    }

    fun onCenterOnLocationRequested() {
        _uiState.update { it.copy(shouldCenterOnLocation = true) }
    }

    fun onShowSaveRouteDialog() {
        _uiState.update { it.copy(showSaveRouteDialog = true, routeNameInput = "") }
    }

    fun onDismissSaveRouteDialog() {
        _uiState.update { it.copy(showSaveRouteDialog = false) }
    }

    fun onRouteNameInputChange(name: String) {
        _uiState.update { it.copy(routeNameInput = name) }
    }

    fun onMapCentered() {
        _uiState.update { it.copy(shouldCenterOnLocation = false) }
    }

    fun startNavigation() {
        if (_uiState.value.generatedRoutePoints.isNotEmpty()) {
            lastTrackedLocation = _uiState.value.userLocation
            _uiState.update { 
                it.copy(
                    isNavigating = true, 
                    isPaused = false,
                    shouldCenterOnLocation = true,
                    elapsedTimeSeconds = 0,
                    totalDistanceCoveredKm = 0.0,
                    totalRouteDistanceKm = it.remainingDistanceKm
                ) 
            }
            startStatsTimer()
        }
    }

    fun stopNavigation() {
        statsJob?.cancel()
        val currentState = _uiState.value
        val avgSpeed = if (currentState.elapsedTimeSeconds > 0) {
            currentState.totalDistanceCoveredKm / (currentState.elapsedTimeSeconds / 3600.0)
        } else 0.0

        viewModelScope.launch {
            try {
                completedRideDao.insertCompletedRide(
                    com.siaka.data.local.CompletedRide(
                        distanceKm = currentState.totalDistanceCoveredKm,
                        durationSeconds = currentState.elapsedTimeSeconds,
                        avgSpeedKmh = avgSpeed
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error saving completed ride", e)
            }
        }

        _uiState.update { 
            it.copy(
                isNavigating = false,
                generatedRoutePoints = emptyList(),
                routeSteps = emptyList(),
                isRouteGenerated = false,
                showSummaryDialog = true,
                lastRideDistanceKm = currentState.totalDistanceCoveredKm,
                lastRideDurationSeconds = currentState.elapsedTimeSeconds,
                lastRideAvgSpeedKmh = avgSpeed
            ) 
        }
        lastTrackedLocation = null
    }

    fun onDismissSummary() {
        _uiState.update { it.copy(showSummaryDialog = false) }
    }

    fun clearRoute() {
        _uiState.update { 
            it.copy(
                generatedRoutePoints = emptyList(),
                routeSteps = emptyList(),
                isRouteGenerated = false,
                isNavigating = false
            )
        }
    }

    fun saveRoute() {
        val points = _uiState.value.generatedRoutePoints
        val steps = _uiState.value.routeSteps
        val distance = _uiState.value.remainingDistanceKm
        val duration = _uiState.value.estimatedTimeMinutes
        val routeName = _uiState.value.routeNameInput.ifEmpty { "My Ride" }

        if (points.isNotEmpty()) {
            _uiState.update { it.copy(isSaving = true) }
            viewModelScope.launch {
                try {
                    val savedRoute = SavedRoute(
                        distanceKm = distance,
                        durationMinutes = duration,
                        points = points,
                        steps = steps,
                        name = routeName
                    )
                    routeDao.insertRoute(savedRoute)
                    Log.d(TAG, "Route saved successfully to database")
                    _uiState.update { 
                        it.copy(
                            snackbarMessage = "Route saved successfully!",
                            showSaveRouteDialog = false,
                            isSaving = false
                        ) 
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving route", e)
                    _uiState.update { 
                        it.copy(
                            snackbarMessage = "Failed to save route",
                            isSaving = false
                        ) 
                    }
                }
            }
        }
    }

    fun loadSavedRoute(routeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRoute = true) }
            try {
                val route = routeDao.getRouteById(routeId)
                if (route != null) {
                    _uiState.update {
                        it.copy(
                            generatedRoutePoints = route.points,
                            routeSteps = route.steps,
                            isRouteGenerated = true,
                            remainingDistanceKm = route.distanceKm,
                            estimatedTimeMinutes = route.durationMinutes,
                            remainingMinutes = route.durationMinutes,
                            isLoadingRoute = false,
                            shouldCenterOnLocation = true
                        )
                    }
                    Log.d(TAG, "Loaded saved route: ${route.name}")
                } else {
                    _uiState.update { it.copy(isLoadingRoute = false, error = "Route not found") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading route", e)
                _uiState.update { it.copy(isLoadingRoute = false, error = "Failed to load route") }
            }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun generateRoute() {
        val currentUserLocation = _uiState.value.userLocation ?: run {
            Log.e(TAG, "Cannot generate route: No user location")
            return
        }
        
        val distanceText = _uiState.value.distanceInput.ifEmpty { "1.4" }
        val distance = distanceText.toDoubleOrNull() ?: run {
            Log.e(TAG, "Cannot generate route: Invalid distance: $distanceText")
            return
        }

        // Add a small buffer/adjustment to the input distance to be more realistic
        // Often Mapbox/OSRM finds routes that are slightly longer than the waypoints suggest.
        // We already have circuityFactor in the generator, but we can also cap or adjust here if needed.

        Log.d(TAG, "Generating route from: ${currentUserLocation.latitude}, ${currentUserLocation.longitude} for ${distance}km")
        
        _uiState.update { it.copy(isLoadingRoute = true, error = null) }

        viewModelScope.launch {
            try {
                val routeData = routeGenerator.generateLoopRoute(
                    centerPoint = currentUserLocation,
                    targetDistanceKm = distance
                )
                
                if (routeData != null) {
                    Log.d(TAG, "Route generated with ${routeData.points.size} points and ${routeData.steps.size} steps")
                    
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
                            error = if (routeData.points.isEmpty()) "No route found" else null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoadingRoute = false, error = "No route found")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Route generation failed", e)
                _uiState.update { 
                    it.copy(
                        isLoadingRoute = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
    }
}
