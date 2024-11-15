package com.android.solvit.provider.ui.jobs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.android.solvit.shared.model.request.ServiceRequest
import com.google.android.gms.maps.model.LatLng

fun navigateToSingleJob(context: Context, jobLatitude: Double, jobLongitude: Double) {
  val uri = Uri.parse("google.navigation:q=$jobLatitude,$jobLongitude")
  val mapIntent = Intent(Intent.ACTION_VIEW, uri)

  try {
    if (mapIntent.resolveActivity(context.packageManager) != null) {
      context.startActivity(mapIntent)
    } else {
      Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
    }
  } catch (e: SecurityException) {
    Toast.makeText(context, "Permission denied for Google Maps", Toast.LENGTH_SHORT).show()
  }
}

fun navigateToAllSortedJobs(
    context: Context,
    currentLocation: LatLng,
    requests: List<ServiceRequest>
) {
  if (requests.isEmpty()) {
    Toast.makeText(context, "No jobs available for navigation", Toast.LENGTH_SHORT).show()
    return
  }

  // Ensure jobs are sorted by date and time for optimal routing
  val sortedJobs = requests.sortedWith(compareBy({ it.dueDate }))

  // Format the current location, destination, and waypoints for the Google Maps URL
  val origin = "${currentLocation.latitude},${currentLocation.longitude}"
  val destination =
      "${sortedJobs.last().location!!.latitude},${sortedJobs.last().location!!.longitude}"
  val waypoints =
      sortedJobs.dropLast(1).joinToString("|") { job ->
        job.location!!.let { "${it.latitude},${it.longitude}" }
      }

  // Construct the Google Maps URL with optimized waypoints
  val uri =
      Uri.parse(
          "https://www.google.com/maps/dir/?api=1&destination=$destination&waypoints=$waypoints")

  val mapIntent =
      Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }

  // Check if Google Maps is available and launch the intent
  if (mapIntent.resolveActivity(context.packageManager) != null) {
    context.startActivity(mapIntent)
  } else {
    Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
  }
}
