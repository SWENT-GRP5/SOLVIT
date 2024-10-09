package com.android.solvit.ui.map

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.solvit.model.map.Location
import com.google.android.gms.location.FusedLocationProviderClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class MapScreenTest {
  private lateinit var context: Context
  private lateinit var location: Location
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    context = mock()
    location = Location(37.7749, -122.4194, "San Francisco")
    fusedLocationClient = mock()
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { MapScreen() }
  }
}
