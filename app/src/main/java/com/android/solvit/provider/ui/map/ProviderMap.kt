package com.android.solvit.provider.ui.map

import android.icu.util.GregorianCalendar
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
import java.util.Calendar

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

  // Allows to bypass location permission for testing
  if (requestLocationPermission) {
    RequestLocationPermission(context, fusedLocationClient) { location -> userLocation = location }
  } else {
    // Mock location if permission is bypassed
    userLocation = LatLng(37.7749, -122.4194) // Example mocked location
  }

  // Create markers with detailed information for each provider
  val requestMarkers =
      requests.map { request ->
        val dueDate =
            request.dueDate.let {
              val calendar = GregorianCalendar()
              calendar.time = request.dueDate.toDate()
              return@let "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                  calendar.get(
                      Calendar.YEAR
                  )
              }"
            }
        MarkerData(
            location =
                LatLng(request.location?.latitude ?: 0.0, request.location?.longitude ?: 0.0),
            title = request.title,
            snippet = "${request.description} - Deadline: $dueDate",
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
