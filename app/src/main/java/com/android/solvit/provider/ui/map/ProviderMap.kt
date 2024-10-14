package com.android.solvit.provider.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.map.MapScreen
import com.android.solvit.shared.ui.map.MarkerData
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

@Composable
fun ProviderMapScreen(
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    navigationActions: NavigationActions,
    requestLocationPermission: Boolean = true
) {
  // Get the current context
  val context = LocalContext.current
  // Initialize the FusedLocationProviderClient
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
  // State to hold the user's location
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  // Collect the service requests from the ViewModel
  val requests by serviceRequestViewModel.requests.collectAsState()

  // Request location permission if required
  if (requestLocationPermission) {
    RequestLocationPermission(context, fusedLocationClient) { location -> userLocation = location }
  } else {
    // Mock location if permission is bypassed
    userLocation = LatLng(37.7749, -122.4194) // Example mocked location
  }

  // Create markers with detailed information for each request
  val requestMarkers =
      requests.map { request ->
        MarkerData(
            location =
                LatLng(request.location?.latitude ?: 0.0, request.location?.longitude ?: 0.0),
            title = request.title,
            snippet = "${request.description} - Deadline: ${request.dueDate}",
            tag = "requestMarker-${request.uid}")
      }

  // Display the map screen with the user's location and request markers
  MapScreen(
      userLocation = userLocation,
      markers = requestMarkers,
      bottomBar = {
        SeekerBottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      })
}
