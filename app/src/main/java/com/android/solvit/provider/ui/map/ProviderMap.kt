package com.android.solvit.provider.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.map.MapScreen
import com.android.solvit.shared.ui.map.MarkerData
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.map.imageBitmapFromUrl
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

/**
 * A composable function that displays the provider map screen, showing service request locations
 * and the provider's current location on a map. It also includes navigation actions and permission
 * requests for location access.
 *
 * @param serviceRequestViewModel ViewModel responsible for managing and fetching service requests.
 * @param navigationActions Provides navigation actions for switching between app screens.
 * @param requestLocationPermission Flag indicating whether location permission should be requested.
 *   Defaults to `true`.
 *
 * **Features:**
 * - **Location Tracking:**
 *     - Requests location permission and retrieves the provider's current location using
 *       `FusedLocationProviderClient`.
 *     - Supports a mocked location if permission is bypassed (for testing).
 * - **Service Request Markers:**
 *     - Displays service requests as interactive map markers.
 *     - Loads marker icons and images asynchronously.
 *     - Handles marker clicks, navigating to service request details when clicked.
 * - **Map Display:**
 *     - Displays the provider's current location and service request locations on the map.
 *     - Shows a loading indicator while markers are being processed.
 * - **Bottom Navigation Bar:** Allows switching between app sections.
 *
 * **State Management:**
 * - Collects service request data reactively using `collectAsStateWithLifecycle()`.
 * - Manages loading indicators and location permissions dynamically.
 */
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
  val requests by serviceRequestViewModel.pendingRequests.collectAsStateWithLifecycle()

  // Allows to bypass location permission for testing
  if (requestLocationPermission) {
    RequestLocationPermission(context, fusedLocationClient) { location -> userLocation = location }
  } else {
    // Mock location if permission is bypassed
    userLocation = LatLng(37.7749, -122.4194) // Example mocked location
  }

  // Create markers with detailed information for each provider
  val requestMarkers = remember { mutableStateOf<List<MarkerData>>(emptyList()) }

  val markersLoading = remember { mutableStateOf(true) }

  LaunchedEffect(requests) {
    val markers =
        requests.map { request ->
          val imageBitmap = imageBitmapFromUrl(context, request.imageUrl ?: "", R.drawable.no_photo)
          val icon = Services.getIcon(request.type)
          MarkerData(
              location =
                  LatLng(request.location?.latitude ?: 0.0, request.location?.longitude ?: 0.0),
              title = request.title,
              icon = icon,
              tag = "requestMarker-${request.uid}",
              image = imageBitmap,
              onClick = {
                serviceRequestViewModel.selectRequest(request)
                navigationActions.navigateTo(Route.BOOKING_DETAILS)
              })
        }
    requestMarkers.value = markers
    markersLoading.value = false
  }

  // Display the map screen with the user's location and request markers
  MapScreen(
      userLocation = userLocation,
      markers = requestMarkers.value,
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = Route.MAP)
      },
      markersLoading = markersLoading.value)
}
