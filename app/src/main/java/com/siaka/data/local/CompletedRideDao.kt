package com.siaka.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedRideDao {
    @Query("SELECT * FROM completed_rides ORDER BY timestamp DESC")
    fun getAllCompletedRides(): Flow<List<CompletedRide>>

    @Insert
    suspend fun insertCompletedRide(ride: CompletedRide)

    @Query("SELECT SUM(distanceKm) FROM completed_rides")
    fun getTotalDistance(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM completed_rides")
    fun getRideCount(): Flow<Int>
}
