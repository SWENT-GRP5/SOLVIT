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
            snippet = "Test Snippet",
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals(45.0, markerData.location.latitude, 0.0)
    assertEquals(90.0, markerData.location.longitude, 0.0)
    assertEquals("Test Marker", markerData.title)
    assertEquals("Test Snippet", markerData.snippet)
    assertEquals("Test Tag", markerData.tag)
  }

  @Test
  fun markerDataInitialization_withNegativeCoordinates() {
    val markerData =
        MarkerData(
            location = LatLng(-45.0, -90.0),
            title = "Negative Marker",
            snippet = "Negative Snippet",
            tag = "Negative Tag",
            image = null,
            onClick = {})
    assertEquals(-45.0, markerData.location.latitude, 0.0)
    assertEquals(-90.0, markerData.location.longitude, 0.0)
    assertEquals("Negative Marker", markerData.title)
    assertEquals("Negative Snippet", markerData.snippet)
  }

  @Test
  fun getLocation_returnsCorrectLocation() {
    val markerData =
        MarkerData(
            location = LatLng(45.0, 90.0),
            title = "Test Marker",
            snippet = "Test Snippet",
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
            snippet = "Test Snippet",
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
            snippet = "Test Snippet",
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals("Test Snippet", markerData.snippet)
  }

  @Test
  fun getTag_returnsCorrectTag() {
    val markerData =
        MarkerData(
            location = LatLng(45.0, 90.0),
            title = "Test Marker",
            snippet = "Test Snippet",
            tag = "Test Tag",
            image = null,
            onClick = {})
    assertEquals("Test Tag", markerData.tag)
  }
}
