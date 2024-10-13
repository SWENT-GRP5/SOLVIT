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
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

@Composable
fun ProviderMapScreen(
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  val requests by serviceRequestViewModel.requests.collectAsState()

  RequestLocationPermission(context, fusedLocationClient) { location -> userLocation = location }

  val markers = requests.map { LatLng(it.location?.latitude ?: 0.0, it.location?.longitude ?: 0.0) }
  MapScreen(
      userLocation = userLocation,
      markers = markers,
      bottomBar = {
        SeekerBottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      })
}
