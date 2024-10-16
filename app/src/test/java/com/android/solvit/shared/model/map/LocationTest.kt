package com.android.solvit.shared.model.map

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationTest {

  @Test
  fun locationInitialization_setsCorrectValues() {
    val location = Location(45.0, 90.0, "Test Location")
    assertEquals(45.0, location.latitude, 0.0)
    assertEquals(90.0, location.longitude, 0.0)
    assertEquals("Test Location", location.name)
  }

  @Test
  fun locationInitialization_withNegativeCoordinates() {
    val location = Location(-45.0, -90.0, "Negative Location")
    assertEquals(-45.0, location.latitude, 0.0)
    assertEquals(-90.0, location.longitude, 0.0)
    assertEquals("Negative Location", location.name)
  }

  @Test
  fun locationInitialization_withZeroCoordinates() {
    val location = Location(0.0, 0.0, "Zero Location")
    assertEquals(0.0, location.latitude, 0.0)
    assertEquals(0.0, location.longitude, 0.0)
    assertEquals("Zero Location", location.name)
  }

  @Test
  fun locationInitialization_withEmptyName() {
    val location = Location(45.0, 90.0, "")
    assertEquals(45.0, location.latitude, 0.0)
    assertEquals(90.0, location.longitude, 0.0)
    assertEquals("", location.name)
  }

  @Test
  fun setName_setsCorrectName() {
    val location = Location(45.0, 90.0, "Test Location")
    location.name = "New Location"
    assertEquals("New Location", location.name)
  }

  @Test
  fun setLatitude_setsCorrectLatitude() {
    val location = Location(45.0, 90.0, "Test Location")
    location.latitude = 0.0
    assertEquals(0.0, location.latitude, 0.0)
  }

  @Test
  fun setLongitude_setsCorrectLongitude() {
    val location = Location(45.0, 90.0, "Test Location")
    location.longitude = 0.0
    assertEquals(0.0, location.longitude, 0.0)
  }
}
