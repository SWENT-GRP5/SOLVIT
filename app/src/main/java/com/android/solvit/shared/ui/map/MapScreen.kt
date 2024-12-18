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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.android.solvit.R
import com.android.solvit.shared.ui.theme.Typography
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Composable that displays a map with markers and a bottom bar.
 *
 * @param userLocation The user's location.
 * @param markers The list of MarkerData to display on the map.
 * @param bottomBar The content of the bottom bar.
 * @param markersLoading Whether the markers data are still loading.
 */
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun MapScreen(
    userLocation: LatLng?,
    markers: List<MarkerData>,
    bottomBar: @Composable () -> Unit,
    markersLoading: Boolean
) {

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
                    .background(color = colorScheme.background, shape = RoundedCornerShape(18.dp))
                    .testTag("mapScreen"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box {
            MapContent(userLocation = userLocation, markers = markers)
            if (markersLoading) {
              Column(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)) {
                OutlinedCard(
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.outlinedCardElevation()) {
                      Row(
                          modifier = Modifier.padding(8.dp),
                          verticalAlignment = Alignment.CenterVertically) {
                            Text("Loading images  ", fontWeight = FontWeight.Bold)
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp), color = colorScheme.onBackground)
                          }
                    }
              }
            }
          }
        }
      },
      bottomBar = bottomBar,
  )
}

/**
 * Composable that displays a map with markers.
 *
 * @param userLocation The user's location.
 * @param markers The list of MarkerData to display on the map.
 * @param modifier The modifier to apply to the map.
 * @param onMapLoaded The callback to invoke when the map is loaded.
 */
@Composable
fun MapContent(
    userLocation: LatLng?,
    markers: List<MarkerData>,
    modifier: Modifier = Modifier,
    onMapLoaded: () -> Unit = {}
) {
  val cameraPositionState = rememberCameraPositionState()

  // Update camera position when userLocation changes
  LaunchedEffect(userLocation) {
    userLocation?.let { cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 10f) }
  }

  GoogleMap(
      modifier = modifier.fillMaxSize().testTag("googleMap"),
      cameraPositionState = cameraPositionState,
      properties =
          MapProperties(
              mapStyleOptions =
                  MapStyleOptions.loadRawResourceStyle(LocalContext.current, if (isSystemInDarkTheme()) R.raw.map_style_dark else R.raw.map_style)),
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

/**
 * Composable that displays a marker on the map.
 *
 * @param markerData The data to display on the marker.
 */
@Composable
fun MapMarker(markerData: MarkerData) {
  MarkerComposable(
      keys = arrayOf(markerData.tag),
      state = MarkerState(position = markerData.location),
      onClick = {
        markerData.onClick()
        true
      }) {
        OutlinedCard(
            modifier = Modifier.width(80.dp).height(100.dp).testTag(markerData.tag),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.outlinedCardElevation()) {
              Box {
                Image(
                    bitmap = markerData.image ?: ImageBitmap.imageResource(R.drawable.no_photo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                Column(
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    verticalArrangement = Arrangement.SpaceBetween) {
                      Text(
                          text = markerData.title,
                          maxLines = 2,
                          overflow = TextOverflow.Ellipsis,
                          style =
                              Typography.titleLarge.copy(
                                  fontWeight = FontWeight.Bold,
                                  fontSize = 14.sp,
                                  color = colorScheme.onPrimary,
                                  lineHeight = 16.sp,
                                  shadow = Shadow(color = colorScheme.primary, blurRadius = 4f)))
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.End) {
                            Box(
                                modifier =
                                    Modifier.background(
                                        colorScheme.background, RoundedCornerShape(8.dp))) {
                                  Icon(
                                      painter = painterResource(markerData.icon),
                                      contentDescription = null,
                                      tint = Color.Unspecified,
                                      modifier = Modifier.size(30.dp))
                                }
                          }
                    }
              }
            }
      }
}

/**
 * Composable that requests location permission and retrieves the user's location.
 *
 * @param context The context to use for requesting permission.
 * @param fusedLocationClient The FusedLocationProviderClient to use for retrieving the location.
 * @param onLocationReceived The callback to invoke when the location is received.
 */
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

/**
 * Retrieves the user's current location.
 *
 * @param fusedLocationClient The FusedLocationProviderClient to use for retrieving the location.
 * @param onLocationReceived The callback to invoke when the location is received.
 */
@SuppressLint("MissingPermission")
fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
  fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
    location?.let { onLocationReceived(it) }
  }
}

/**
 * Converts a vector drawable to a BitmapDescriptor.
 *
 * @param context The context to use for retrieving the drawable.
 * @param vectorResId The resource ID of the vector drawable.
 * @return The BitmapDescriptor created from the vector drawable.
 */
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

/**
 * Retrieves an ImageBitmap from a URL.
 *
 * @param context The context to use for loading the image.
 * @param url The URL of the image.
 * @param placeholder The resource ID of the placeholder image.
 * @return The ImageBitmap loaded from the URL.
 */
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
    val icon: Int, // Icon to display on the marker
    val tag: String, // Unique identifier for the marker
    val image: ImageBitmap?,
    val onClick: () -> Unit
)
