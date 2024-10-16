package com.android.solvit.shared.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.solvit.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(userLocation: LatLng?, markers: List<MarkerData>, bottomBar: @Composable () -> Unit) {
  Scaffold(
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = Color.White, shape = RoundedCornerShape(18.dp))
                    .testTag("mapScreen"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          MapContent(userLocation = userLocation, markers = markers)
        }
      },
      bottomBar = bottomBar,
  )
}

@Composable
fun MapContent(userLocation: LatLng?, markers: List<MarkerData>, modifier: Modifier = Modifier) {
  val cameraPositionState = rememberCameraPositionState()

  // Update camera position when userLocation changes
  LaunchedEffect(userLocation) {
    userLocation?.let { cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 10f) }
  }

  GoogleMap(
      modifier = modifier.fillMaxSize().testTag("googleMap"),
      cameraPositionState = cameraPositionState) {
        markers.forEach { markerData ->
          Marker(
              state = MarkerState(position = markerData.location),
              title = markerData.title,
              snippet = markerData.snippet,
              tag = markerData.tag)
        }

        // Display a marker at the user's location if it's available
        userLocation?.let {
          val customIcon =
              bitmapDescriptorFromVector(LocalContext.current, R.drawable.user_location_marker)
          Marker(
              state = MarkerState(position = it),
              tag = "userLocation",
              icon = customIcon,
              draggable = false,
          )
        }
      }
}

@Composable
fun RequestLocationPermission(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng?) -> Unit
) {
  val locationPermissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { isGranted ->
            if (isGranted) {
              getCurrentLocation(fusedLocationClient) { location ->
                onLocationReceived(LatLng(location.latitude, location.longitude))
              }
            }
          })

  LaunchedEffect(Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED) {
      getCurrentLocation(fusedLocationClient) { location ->
        onLocationReceived(LatLng(location.latitude, location.longitude))
      }
    } else {
      locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
  }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
  fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
    location?.let { onLocationReceived(it) }
  }
}

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
  val vectorDrawable = AppCompatResources.getDrawable(context, vectorResId)
  vectorDrawable?.let {
    it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    it.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
  }
  return null
}

data class MarkerData(
    val location: LatLng, // Position of the marker
    val title: String, // Name of the marker
    val snippet: String, // Additional info like description
    val tag: String // Unique identifier for the marker
)
