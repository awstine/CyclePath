package com.siaka.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.siaka.data.LocationPoint
import com.siaka.data.RouteStep

class RouteConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromLocationPointList(value: List<LocationPoint>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLocationPointList(value: String): List<LocationPoint> {
        val listType = object : TypeToken<List<LocationPoint>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromRouteStepList(value: List<RouteStep>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toRouteStepList(value: String): List<RouteStep> {
        val listType = object : TypeToken<List<RouteStep>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
