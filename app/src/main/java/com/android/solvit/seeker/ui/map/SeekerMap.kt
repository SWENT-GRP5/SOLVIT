package com.android.solvit.seeker.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun SeekerMapScreen(providerViewModel: ListProviderViewModel, navigationActions: NavigationActions) {
  val context = LocalContext.current
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  val providers by providerViewModel.providersList.collectAsState()

  // Location permission launcher
  val locationPermissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { isGranted ->
            if (isGranted) {
              getCurrentLocation(fusedLocationClient) { location ->
                userLocation = LatLng(location.latitude, location.longitude)
              }
            }
          })

  // Check permission and request location
  LaunchedEffect(Unit) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED) {
      getCurrentLocation(fusedLocationClient) { location ->
        userLocation = LatLng(location.latitude, location.longitude)
      }
    } else {
      locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
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
          val cameraPositionState = rememberCameraPositionState()

          // Update camera position when userLocation changes
          LaunchedEffect(userLocation) {
            userLocation?.let {
              cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 10f)
            }
          }

          GoogleMap(
              modifier = Modifier.fillMaxSize().testTag("googleMap"),
              cameraPositionState = cameraPositionState) {
                providers.forEach { provider ->
                  Marker(
                      state =
                          MarkerState(
                              position =
                                  LatLng(provider.location.latitude, provider.location.longitude)),
                      title = provider.name,
                      tag = "marker${provider.uid}",
                      snippet = provider.description,
                  )
                }

                // Display a marker at the user's location if it's available
                userLocation?.let {
                  Circle(
                      center = it,
                      radius = 200.0,
                      fillColor = Color(0x220000FF),
                      strokeColor = Color(0x660000FF),
                      strokeWidth = 2f)
                  Circle(
                      center = it,
                      radius = 10.0,
                      fillColor = Color(0xFF0000FF),
                      strokeColor = Color(0xFF0000FF),
                      strokeWidth = 2f)
                }
              }
        }
      },
      bottomBar = {
        SeekerBottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
  )
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
