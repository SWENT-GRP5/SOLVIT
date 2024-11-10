package com.android.solvit.shared.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.shared.model.map.Location
import org.junit.Rule
import org.junit.Test

class GetDirectionsBubbleTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testGetDirectionsBubble_DisplaysLocationName() {
    val location = Location(name = "Test Location", latitude = 0.0, longitude = 0.0)
    composeTestRule.setContent { GetDirectionsBubble(location = location, onDismiss = {}) }

    composeTestRule.onNodeWithText("Test Location").assertIsDisplayed()
  }

  @Test
  fun testGetDirectionsBubble_GetDirectionsButton() {
    val location = Location(name = "Test Location", latitude = 0.0, longitude = 0.0)
    composeTestRule.setContent { GetDirectionsBubble(location = location, onDismiss = {}) }

    composeTestRule.onNodeWithText("Get Directions").assertIsDisplayed()
  }

  @Test
  fun testGetDirectionsBubble_ClickGetDirectionsButton() {
    val location = Location(name = "Test Location", latitude = 0.0, longitude = 0.0)
    composeTestRule.setContent { GetDirectionsBubble(location = location, onDismiss = {}) }

    composeTestRule.onNodeWithText("Get Directions").performClick()

    val expectedUri = Uri.parse("google.navigation:q=0.0,0.0")
    val expectedIntent =
        Intent(Intent.ACTION_VIEW, expectedUri).apply { setPackage("com.google.android.apps.maps") }

    val context = ApplicationProvider.getApplicationContext<Context>()
    val resolvedActivity = expectedIntent.resolveActivity(context.packageManager)
    assert(resolvedActivity != null)
  }

  @Test
  fun testGetDirectionsBubble_FallbackToBrowser() {
    val location = Location(name = "Test Location", latitude = 0.0, longitude = 0.0)
    composeTestRule.setContent { GetDirectionsBubble(location = location, onDismiss = {}) }

    composeTestRule.onNodeWithText("Get Directions").performClick()

    val expectedUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=0.0,0.0")
    val expectedIntent = Intent(Intent.ACTION_VIEW, expectedUri)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val resolvedActivity = expectedIntent.resolveActivity(context.packageManager)
    assert(resolvedActivity != null)
  }

  @Test
  fun testGetDirectionsBubble_DismissOnButtonClick() {
    var dismissed = false
    val location = Location(name = "Test Location", latitude = 0.0, longitude = 0.0)
    composeTestRule.setContent {
      GetDirectionsBubble(location = location, onDismiss = { dismissed = true })
    }

    composeTestRule.onNodeWithText("Get Directions").performClick()
    assert(dismissed)
  }
}
