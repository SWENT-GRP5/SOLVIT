package com.android.solvit.shared.model.map

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Location(var latitude: Double, var longitude: Double, var name: String)

// Calculate distance(in km) between a starting point and a target point
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
