package com.android.solvit.seeker.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.map.MapScreen
import com.android.solvit.shared.ui.map.MarkerData
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.map.imageBitmapFromUrl
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_SEEKER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

/**
 * Composable function that displays the map screen for the seeker.
 *
 * @param providerViewModel ViewModel to fetch the list of providers
 * @param navigationActions Actions to navigate to different screens
 * @param requestLocationPermission Flag to request location permission
 */
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
  val providers by providerViewModel.providersList.collectAsStateWithLifecycle()

  // Allows to bypass location permission for testing
  if (requestLocationPermission) {
    RequestLocationPermission(context, fusedLocationClient) { location -> userLocation = location }
  } else {
    // Mock location if permission is bypassed
    userLocation = LatLng(37.7749, -122.4194) // Example mocked location
  }

  // Create markers with detailed information for each provider
  val providerMarkers = remember { mutableStateOf<List<MarkerData>>(emptyList()) }

  val markersLoading = remember { mutableStateOf(true) }

  LaunchedEffect(providers) {
    val markers =
        providers.map { provider ->
          val imageBitmap =
              imageBitmapFromUrl(
                  context, provider.imageUrl, Services.getProfileImage(provider.service))
          val icon = Services.getIcon(provider.service)
          MarkerData(
              location = LatLng(provider.location.latitude, provider.location.longitude),
              title = provider.name,
              icon = icon,
              tag = "providerMarker-${provider.uid}",
              image = imageBitmap,
              onClick = {
                providerViewModel.selectProvider(provider)
                navigationActions.navigateTo(Route.PROVIDER_INFO)
              })
        }
    providerMarkers.value = markers
    markersLoading.value = false
  }

  // Display the map with user location and provider markers
  MapScreen(
      userLocation = userLocation,
      markers = providerMarkers.value,
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION_SEEKER,
            selectedItem = Route.MAP)
      },
      markersLoading = markersLoading.value)
}
