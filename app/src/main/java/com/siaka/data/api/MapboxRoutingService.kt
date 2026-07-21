package com.siaka.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class MapboxRouteResponse(
    @SerializedName("code") val code: String,
    @SerializedName("routes") val routes: List<MapboxRoute>?,
    @SerializedName("waypoints") val waypoints: List<MapboxWaypoint>?
)

data class MapboxRoute(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("geometry") val geometry: String, // Encoded polyline
    @SerializedName("legs") val legs: List<MapboxLeg>?
)

data class MapboxLeg(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("summary") val summary: String,
    @SerializedName("steps") val steps: List<MapboxStep>?
)

data class MapboxStep(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("geometry") val geometry: String,
    @SerializedName("name") val name: String,
    @SerializedName("maneuver") val maneuver: MapboxManeuver
)

data class MapboxManeuver(
    @SerializedName("instruction") val instruction: String,
    @SerializedName("type") val type: String,
    @SerializedName("modifier") val modifier: String?
)

data class MapboxWaypoint(
    @SerializedName("location") val location: List<Double>,
    @SerializedName("name") val name: String
)

interface MapboxRoutingService {
    @GET("directions/v5/mapbox/cycling/{coordinates}")
    suspend fun getCyclingRoute(
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("geometries") geometries: String = "polyline6",
        @Query("overview") overview: String = "full",
        @Query("steps") steps: Boolean = true
    ): Response<MapboxRouteResponse>
}
