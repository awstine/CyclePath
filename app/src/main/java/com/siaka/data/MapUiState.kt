package com.siaka.data

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float? = null,
    val accuracyMeters: Float? = null,
    val speedMps: Float? = null
)

data class MapUiState(
    val userLocation: LocationPoint? = null,
    val isLocationPermissionGranted: Boolean = false,
    val generatedRoutePoints: List<LocationPoint> = emptyList(),
    val routeSteps: List<RouteStep> = emptyList(),
    val isLoadingRoute: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val distanceInput: String = "",
    val showDistanceDialog: Boolean = false,
    val shouldCenterOnLocation: Boolean = false,
    val isNavigating: Boolean = false,
    val isRouteGenerated: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
    val showSaveRouteDialog: Boolean = false,
    val routeNameInput: String = "",
    val isSaving: Boolean = false,
    
    // Offline Map state
    val offlineDownloadProgress: Float? = null,
    val isOfflineDownloading: Boolean = false,
    
    // Navigation details
    val estimatedTimeMinutes: Int = 45,
    val remainingMinutes: Int = 45,
    val remainingDistanceKm: Double = 12.4,
    val totalRouteDistanceKm: Double = 0.0,
    val eta: String = "4:25 PM",
    val etaTime: String = "4:25 PM",
    val routeDescription: String = "Via Skyline Drive and Greenway Loop",
    val nextInstruction: String = "Turn Right on Pine St",
    val distanceToNextInstructionMeters: Int = 200,

    // Real-time stats
    val isPaused: Boolean = false,
    val currentSpeedKmh: Double = 0.0,
    val altitudeMeters: Double = 0.0,
    val totalDistanceCoveredKm: Double = 0.0,
    val averageSpeedKmh: Double = 0.0,
    val elapsedTimeSeconds: Long = 0,
    
    // Summary screen state
    val showSummaryDialog: Boolean = false,
    val lastRideDistanceKm: Double = 0.0,
    val lastRideDurationSeconds: Long = 0,
    val lastRideAvgSpeedKmh: Double = 0.0
)
