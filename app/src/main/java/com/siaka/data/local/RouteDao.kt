package com.siaka.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM saved_routes ORDER BY timestamp DESC")
    fun getAllRoutes(): Flow<List<SavedRoute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: SavedRoute)

    @Delete
    suspend fun deleteRoute(route: SavedRoute)

    @Query("SELECT * FROM saved_routes WHERE id = :id")
    suspend fun getRouteById(id: Long): SavedRoute?
}
