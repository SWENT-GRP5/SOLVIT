package com.android.solvit.shared.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.android.solvit.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(userLocation: LatLng?, markers: List<MarkerData>, bottomBar: @Composable () -> Unit) {

  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

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
fun MapContent(userLocation: LatLng?, markers: List<MarkerData>, modifier: Modifier = Modifier, onMapLoaded: () -> Unit = {}) {
  val context = LocalContext.current
  val cameraPositionState = rememberCameraPositionState()

  // Update camera position when userLocation changes
  LaunchedEffect(userLocation) {
    userLocation?.let { cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 10f) }
  }

  GoogleMap(
      modifier = modifier.fillMaxSize().testTag("googleMap"),
      cameraPositionState = cameraPositionState,
      onMapLoaded = onMapLoaded) {
        markers.forEach { markerData -> MapMarker(markerData) }

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
fun MapMarker(markerData: MarkerData) {
  MarkerComposable(
      keys = arrayOf(markerData.tag),
      state = MarkerState(position = markerData.location),
      onClick = {
        markerData.onClick()
        true
      }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier.clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    .testTag(markerData.tag)) {
              Image(
                  markerData.image,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier =
                      Modifier.padding(top = 4.dp, start = 6.dp, end = 6.dp)
                          .size(50.dp)
                          .clip(CircleShape)
                          .border(2.dp, Color.Black, CircleShape)
                          .testTag(markerData.tag+"Image"))
              Text(
                  maxLines = 1,
                  textAlign = TextAlign.Center,
                  text = markerData.title,
                  modifier = Modifier.width(60.dp).padding(4.dp).testTag(markerData.tag+"Title"),
                  style = MaterialTheme.typography.labelSmall)
              Text(
                  textAlign = TextAlign.Center,
                  text = markerData.snippet,
                  modifier = Modifier.width(60.dp).padding(4.dp).testTag(markerData.tag+"Snippet"),
                  style = MaterialTheme.typography.bodySmall)
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

suspend fun imageBitmapFromUrl(context: Context, url: String, placeholder: Int): ImageBitmap {
  val loader = ImageLoader(context)
  val request =
      ImageRequest.Builder(context)
          .data(url)
          .allowHardware(false) // Disable hardware bitmaps.
          .error(placeholder)
          .build()

  return when (val result = loader.execute(request)) {
    is SuccessResult -> result.drawable.toBitmap().asImageBitmap()
    is ErrorResult -> result.drawable!!.toBitmap().asImageBitmap()
    else -> throw IllegalStateException("Unexpected result type")
  }
}

data class MarkerData(
    val location: LatLng, // Position of the marker
    val title: String, // Name of the marker
    val snippet: String, // Additional info like description
    val tag: String, // Unique identifier for the marker
    val image: ImageBitmap,
    val onClick: () -> Unit
)
