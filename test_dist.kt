
import kotlin.math.*

data class LocationPoint(val latitude: Double, val longitude: Double)

fun calculateDistance(p1: LocationPoint, p2: LocationPoint): Double {
    val r = 6371.0 // Earth's radius in km
    val lat1 = Math.toRadians(p1.latitude)
    val lon1 = Math.toRadians(p1.longitude)
    val lat2 = Math.toRadians(p2.latitude)
    val lon2 = Math.toRadians(p2.longitude)
    
    val dLat = lat2 - lat1
    val dLon = lon2 - lon1
    
    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return r * c
}

fun generateCircularWaypoints(center: LocationPoint, targetDistanceKm: Double): List<LocationPoint> {
    val KM_PER_DEGREE_LAT = 111.32
    val circuityFactor = 2.2
    val adjustedDistance = targetDistanceKm / circuityFactor
    val radiusKm = adjustedDistance / (2 * Math.PI)
    
    val radiusLatDeg = radiusKm / KM_PER_DEGREE_LAT
    val radiusLonDeg = radiusKm / (KM_PER_DEGREE_LAT * cos(Math.toRadians(center.latitude)))
    
    val randomRotation = 0.0 // Keep it 0 for testing
    val waypoints = mutableListOf<LocationPoint>()
    
    // Start at user position
    waypoints.add(center)
    
    val numPoints = 5
    for (i in 0 until numPoints) {
        val angle = (2 * Math.PI * i / numPoints) + randomRotation
        val lat = center.latitude + (radiusLatDeg * sin(angle))
        val lon = center.longitude + (radiusLonDeg * cos(angle))
        waypoints.add(LocationPoint(lat, lon))
    }
    
    // End at user position
    waypoints.add(center)
    return waypoints
}

fun main() {
    val center = LocationPoint(52.5200, 13.4050) // Berlin
    val targetDist = 3.0
    val waypoints = generateCircularWaypoints(center, targetDist)
    
    var totalStraightDist = 0.0
    for (i in 0 until waypoints.size - 1) {
        val d = calculateDistance(waypoints[i], waypoints[i+1])
        totalStraightDist += d
        println("Segment $i: ${d} km")
    }
    
    println("Target Distance: $targetDist km")
    println("Total Straight-line Distance: $totalStraightDist km")
    println("Ratio (Target / Straight): ${targetDist / totalStraightDist}")
}
