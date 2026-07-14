package com.siaka.data

import android.util.Log
import com.mapbox.bindgen.Value
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.Style
import com.mapbox.maps.StylePackLoadOptions
import com.mapbox.maps.TilesetDescriptorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class OfflineMapManager @Inject constructor() {

    private val tileStore = TileStore.create()
    private val offlineManager = OfflineManager()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    fun downloadRegion(center: LocationPoint, radiusKm: Double = 5.0) {
        if (_isDownloading.value) return

        _isDownloading.value = true
        _downloadProgress.value = 0f

        val points = createBoundingBox(center, radiusKm)
        val geometry = Polygon.fromLngLats(listOf(points))

        // 1. Load Style Pack (Icons, Fonts, etc.)
        val stylePackOptions = StylePackLoadOptions.Builder()
            .build()

        offlineManager.loadStylePack(
            Style.MAPBOX_STREETS,
            stylePackOptions,
            { progress ->
                Log.d("OfflineMapManager", "Style Pack progress: ${progress.completedResourceCount}/${progress.requiredResourceCount}")
            },
            { result ->
                if (result.isValue) {
                    Log.d("OfflineMapManager", "Style Pack loaded successfully")
                    startTileDownload(geometry)
                } else {
                    Log.e("OfflineMapManager", "Style Pack failed: ${result.error}")
                    _isDownloading.value = false
                    _downloadProgress.value = null
                }
            }
        )
    }

    private fun startTileDownload(geometry: Polygon) {
        val tilesetDescriptorOptions = TilesetDescriptorOptions.Builder()
            .styleURI(Style.MAPBOX_STREETS)
            .minZoom(10)
            .maxZoom(16)
            .build()

        val tilesetDescriptor = offlineManager.createTilesetDescriptor(tilesetDescriptorOptions)

        val loadOptions = TileRegionLoadOptions.Builder()
            .geometry(geometry)
            .descriptors(listOf(tilesetDescriptor))
            .metadata(Value.valueOf("Siaka Offline Map"))
            .build()

        tileStore.loadTileRegion(
            "siaka-region",
            loadOptions,
            { progress ->
                val total = progress.requiredResourceCount.toFloat()
                if (total > 0) {
                    val p = progress.completedResourceCount / total
                    _downloadProgress.value = p
                    Log.d("OfflineMapManager", "Tile Region progress: $p")
                }
            },
            { result ->
                _isDownloading.value = false
                _downloadProgress.value = null
                if (result.isValue) {
                    Log.d("OfflineMapManager", "Tile Region loaded successfully")
                } else {
                    Log.e("OfflineMapManager", "Tile Region failed: ${result.error}")
                }
            }
        )
    }

    private fun createBoundingBox(center: LocationPoint, radiusKm: Double): List<Point> {
        val kmInDegree = 1.0 / 111.32
        val latOffset = radiusKm * kmInDegree
        val lonOffset = radiusKm * kmInDegree / cos(Math.toRadians(center.latitude))

        return listOf(
            Point.fromLngLat(center.longitude - lonOffset, center.latitude - latOffset),
            Point.fromLngLat(center.longitude + lonOffset, center.latitude - latOffset),
            Point.fromLngLat(center.longitude + lonOffset, center.latitude + latOffset),
            Point.fromLngLat(center.longitude - lonOffset, center.latitude + latOffset),
            Point.fromLngLat(center.longitude - lonOffset, center.latitude - latOffset)
        )
    }
}
