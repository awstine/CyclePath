package com.siaka.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.siaka.data.LocationPoint
import com.siaka.data.RouteStep

@Entity(tableName = "saved_routes")
data class SavedRoute(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val distanceKm: Double,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val points: List<LocationPoint>,
    val steps: List<RouteStep>,
    val name: String = "Morning Ride" // Default name
)
