package com.android.solvit.provider.ui.map

import android.icu.util.GregorianCalendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.map.MapScreen
import com.android.solvit.shared.ui.map.MarkerData
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.map.imageBitmapFromUrl
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
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
  val requestMarkers = remember { mutableStateOf<List<MarkerData>>(emptyList()) }

  LaunchedEffect(requests) {
    val markers =
        requests.map { request ->
          val dueDate =
              with(GregorianCalendar().apply { time = request.dueDate.toDate() }) {
                "${get(Calendar.DAY_OF_MONTH)}/${get(Calendar.MONTH) + 1}/${get(Calendar.YEAR)}"
              }
          val imageBitmap =
              imageBitmapFromUrl(context, request.imageUrl ?: "", R.drawable.empty_profile_img)
          MarkerData(
              location =
                  LatLng(request.location?.latitude ?: 0.0, request.location?.longitude ?: 0.0),
              title = request.title,
              snippet = request.type.toString().replace("_", " ") + "\n" + dueDate,
              tag = "requestMarker-${request.uid}",
              image = imageBitmap,
              onClick = { /*TODO: Navigate to request details screen*/})
        }
    requestMarkers.value = markers
  }

  // Display the map screen with the user's location and request markers
  MapScreen(
      userLocation = userLocation,
      markers = requestMarkers.value,
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = Route.MAP_OF_SEEKERS)
      })
}
