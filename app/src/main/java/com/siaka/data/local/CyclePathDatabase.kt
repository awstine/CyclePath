package com.siaka.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SavedRoute::class, CompletedRide::class], version = 2, exportSchema = false)
@TypeConverters(RouteConverters::class)
abstract class CyclePathDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
    abstract fun completedRideDao(): CompletedRideDao
}
