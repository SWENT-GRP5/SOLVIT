package com.android.solvit.seeker.ui.map

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.ui.map.MapScreen
import com.android.solvit.shared.ui.map.MarkerData
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.map.imageBitmapFromUrl
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_CUSTOMER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

@Composable
fun SeekerMapScreen(
    providerViewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    navigationActions: NavigationActions,
    requestLocationPermission: Boolean = true
) {

  // Get the current context
  val context = LocalContext.current
  // Lock Orientation to Portrait
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  // Initialize the FusedLocationProviderClient
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
  // State to hold the user's location
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  // Collect the list of providers from the ViewModel
  val providers by providerViewModel.providersList.collectAsState()

  // Allows to bypass location permission for testing
  if (requestLocationPermission) {
    RequestLocationPermission(context, fusedLocationClient) { location -> userLocation = location }
  } else {
    // Mock location if permission is bypassed
    userLocation = LatLng(37.7749, -122.4194) // Example mocked location
  }

  // Create markers with detailed information for each provider
  val providerMarkers = remember { mutableStateOf<List<MarkerData>>(emptyList()) }

  LaunchedEffect(providers) {
    val markers =
        providers.map { provider ->
          val imageBitmap =
              imageBitmapFromUrl(context, provider.imageUrl, R.drawable.empty_profile_img)
          MarkerData(
              location = LatLng(provider.location.latitude, provider.location.longitude),
              title = provider.name,
              snippet =
                  provider.service.toString().replace("_", " ") + "\n" + provider.rating.toString(),
              tag = "providerMarker-${provider.uid}",
              image = imageBitmap,
              onClick = {
                providerViewModel.selectProvider(provider)
                navigationActions.navigateTo(Route.PROVIDER_PROFILE)
              })
        }
    providerMarkers.value = markers
  }

  // Display the map with user location and provider markers
  MapScreen(
      userLocation = userLocation,
      markers = providerMarkers.value,
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_CUSTOMER,
            selectedItem = Route.MAP)
      })
}
