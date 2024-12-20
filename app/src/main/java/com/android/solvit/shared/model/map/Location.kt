package com.android.solvit.shared.model.map

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Data class representing a geographic location with coordinates and a descriptive name.
 *
 * @property latitude The latitude coordinate of the location.
 * @property longitude The longitude coordinate of the location.
 * @property name A descriptive name for the location.
 */
data class Location(var latitude: Double, var longitude: Double, var name: String)

/**
 * Calculates the Haversine distance between two geographic points specified by their latitude and
 * longitude coordinates. This function returns the great-circle distance between two points on the
 * Earth's surface.
 *
 * @param lat1 Latitude of the starting point in degrees.
 * @param lon1 Longitude of the starting point in degrees.
 * @param lat2 Latitude of the target point in degrees.
 * @param lon2 Longitude of the target point in degrees.
 * @return The distance between the two points in kilometers.
 */
fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
  val earthRadius = 6371.0 // Radius of Earth in kilometers

  val dLat = Math.toRadians(lat2 - lat1)
  val dLon = Math.toRadians(lon2 - lon1)

  val a =
      sin(dLat / 2) * sin(dLat / 2) +
          cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
  val c = 2 * atan2(sqrt(a), sqrt(1 - a))

  return earthRadius * c
}
