package com.android.solvit.shared.model.map

import com.android.solvit.shared.ui.map.MarkerData
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class MarketDataTest {
  @Test
  fun markerDataInitialization_setsCorrectValues() {
    val markerData =
        MarkerData(
            location = LatLng(45.0, 90.0),
            title = "Test Marker",
            icon = 1,
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals(45.0, markerData.location.latitude, 0.0)
    assertEquals(90.0, markerData.location.longitude, 0.0)
    assertEquals("Test Marker", markerData.title)
    assertEquals(1, markerData.icon)
    assertEquals("Test Tag", markerData.tag)
  }

  @Test
  fun markerDataInitialization_withNegativeCoordinates() {
    val markerData =
        MarkerData(
            location = LatLng(-45.0, -90.0),
            title = "Negative Marker",
            icon = 1,
            tag = "Negative Tag",
            image = null,
            onClick = {})
    assertEquals(-45.0, markerData.location.latitude, 0.0)
    assertEquals(-90.0, markerData.location.longitude, 0.0)
    assertEquals("Negative Marker", markerData.title)
    assertEquals(1, markerData.icon)
  }

  @Test
  fun getLocation_returnsCorrectLocation() {
    val markerData =
        MarkerData(
            location = LatLng(45.0, 90.0),
            title = "Test Marker",
            icon = 1,
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals(LatLng(45.0, 90.0), markerData.location)
  }

  @Test
  fun getTitle_returnsCorrectTitle() {
    val markerData =
        MarkerData(
            location = LatLng(45.0, 90.0),
            title = "Test Marker",
            icon = 1,
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals("Test Marker", markerData.title)
  }

  @Test
  fun getSnippet_returnsCorrectSnippet() {
    val markerData =
        MarkerData(
            location = LatLng(45.0, 90.0),
            title = "Test Marker",
            icon = 1,
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals(1, markerData.icon)
  }

  @Test
  fun getTag_returnsCorrectTag() {
    val markerData =
        MarkerData(
            location = LatLng(45.0, 90.0),
            title = "Test Marker",
            icon = 1,
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals("Test Tag", markerData.tag)
  }
}
