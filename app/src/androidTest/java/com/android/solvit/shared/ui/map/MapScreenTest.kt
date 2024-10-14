package com.android.solvit.shared.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var context: Context
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var location: Location

  @Before
  fun setUp() {
    context = mock(Context::class.java)
    fusedLocationClient = mock(FusedLocationProviderClient::class.java)
    location = mock(Location::class.java)
  }

  @Test
  fun locationPermissionGranted_locationReceived() {
    `when`(location.latitude).thenReturn(37.7749)
    `when`(location.longitude).thenReturn(-122.4194)
    `when`(fusedLocationClient.lastLocation).thenReturn(Tasks.forResult(location))
    `when`(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
        .thenReturn(PackageManager.PERMISSION_GRANTED)

    var receivedLocation: LatLng? = null
    composeTestRule.setContent {
      RequestLocationPermission(context, fusedLocationClient) { receivedLocation = it }
    }

    assert(receivedLocation == LatLng(37.7749, -122.4194))
  }

  @Test
  fun locationPermissionGranted_noLocationAvailable() {
    `when`(fusedLocationClient.lastLocation).thenReturn(Tasks.forResult(null))
    `when`(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
        .thenReturn(PackageManager.PERMISSION_GRANTED)

    var receivedLocation: LatLng? = null
    composeTestRule.setContent {
      RequestLocationPermission(context, fusedLocationClient) { receivedLocation = it }
    }

    assert(receivedLocation == null)
  }
}
