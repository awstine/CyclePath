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
    private val offlineMapManager: OfflineMapManager
) : ViewModel() {

    companion object {
        private const val TAG = "MapViewModel"
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var hasAutoDownloaded = false

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
                
                // Auto-download offline map on first valid location
                if (!hasAutoDownloaded) {
                    hasAutoDownloaded = true
                    offlineMapManager.downloadRegion(userPoint)
                }

                _uiState.update { state ->
                    var updatedState = state.copy(userLocation = userPoint)
                    if (state.isNavigating) {
                        updatedState = updateNavigationDetails(updatedState, userPoint)
                    }
                    updatedState
                }
            }
        }
    }

    private fun updateNavigationDetails(state: MapUiState, userLocation: LocationPoint): MapUiState {
        val steps = state.routeSteps
        if (steps.isEmpty()) return state

        // Find the first step that is ahead of the user (e.g., more than 30m away)
        // This is a simple heuristic for mock navigation
        val nextStep = steps.firstOrNull { step ->
            calculateDistance(userLocation, step.location) > 30 
        } ?: steps.last()

        val distanceToNext = calculateDistance(userLocation, nextStep.location).toInt()

        return state.copy(
            nextInstruction = nextStep.instruction,
            distanceToNextInstructionMeters = distanceToNext,
            // Update remaining distance (simplified)
            remainingDistanceKm = max(0.0, state.remainingDistanceKm - 0.01) 
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
            _uiState.update { it.copy(isNavigating = true, shouldCenterOnLocation = true) }
        }
    }

    fun stopNavigation() {
        _uiState.update { 
            it.copy(
                isNavigating = false,
                generatedRoutePoints = emptyList(),
                routeSteps = emptyList(),
                isRouteGenerated = false
            ) 
        }
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
