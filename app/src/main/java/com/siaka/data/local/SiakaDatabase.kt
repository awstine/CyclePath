package com.siaka.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SavedRoute::class], version = 1, exportSchema = false)
@TypeConverters(RouteConverters::class)
abstract class SiakaDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}
