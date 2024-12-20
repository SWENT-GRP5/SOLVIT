package com.android.solvit.provider.ui.request

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.android.solvit.shared.model.request.ServiceRequest

/**
 * Launches Google Maps for navigation to a single job's location based on the provided latitude and
 * longitude.
 *
 * @param context The context used to start the map activity.
 * @param jobLatitude The latitude of the job's location.
 * @param jobLongitude The longitude of the job's location.
 *
 * **Behavior:**
 * - Opens Google Maps with turn-by-turn navigation to the specified job location.
 * - Shows a toast message if Google Maps is not installed or permissions are denied.
 */
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

/**
 * Launches Google Maps for navigation through multiple job locations, optimizing the route from the
 * first job to the last.
 *
 * @param context The context used to start the map activity.
 * @param requests A list of service requests, each containing a job's location.
 *
 * **Behavior:**
 * - Sorts jobs by their due date for optimal routing.
 * - Constructs a Google Maps URL with the last job as the destination and intermediate jobs as
 *   waypoints.
 * - Opens Google Maps with the optimal route or shows a toast if the app is not installed.
 * - Displays a toast message if there are no jobs to navigate to.
 */
fun navigateToAllSortedJobs(context: Context, requests: List<ServiceRequest>) {
  if (requests.isEmpty()) {
    Toast.makeText(context, "No jobs available for navigation", Toast.LENGTH_SHORT).show()
    return
  }

  // Ensure jobs are sorted by date and time for optimal routing
  val sortedJobs = requests.sortedWith(compareBy { it.dueDate })

  // Format the current location, destination, and waypoints for the Google Maps URL
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
