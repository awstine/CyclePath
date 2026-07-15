package com.siaka.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_rides")
data class CompletedRide(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val distanceKm: Double,
    val durationSeconds: Long,
    val avgSpeedKmh: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val startLocationName: String = "Unknown Start",
    val endLocationName: String = "Unknown End"
)
