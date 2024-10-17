package com.android.solvit.seeker.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.ui.map.MapScreen
import com.android.solvit.shared.ui.map.MarkerData
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_CUSTOMMER
import com.android.solvit.shared.ui.navigation.NavigationActions
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
  val providerMarkers =
      providers.map { provider ->
        MarkerData(
            location = LatLng(provider.location.latitude, provider.location.longitude),
            title = provider.name,
            snippet = "${provider.description} - Rating: ${provider.rating}",
            tag = "providerMarker-${provider.uid}")
      }

  // Display the map with user location and provider markers
  MapScreen(
      userLocation = userLocation,
      markers = providerMarkers,
      bottomBar = {
        SeekerBottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_CUSTOMMER,
            selectedItem = navigationActions.currentRoute())
      })
}
