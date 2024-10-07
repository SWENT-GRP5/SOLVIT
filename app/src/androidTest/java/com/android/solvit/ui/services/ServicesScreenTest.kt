package com.android.solvit.ui.services

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ServicesScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { ServicesScreen() }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesGrid").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addRequestButton").assertIsDisplayed()
  }

  @Test
  fun allServicesDisplayed() {
    for (service in SERVICES_LIST.take(8)) {
      composeTestRule.onNodeWithTag(service.service.toString() + "Item").assertIsDisplayed()
    }
  }

  @Test
  fun addRequestButtonNavigatesToRequestScreen() {
    composeTestRule.onNodeWithTag("addRequestButton").performClick()
    /*TODO*/
  }

  @Test
  fun clickServiceItemNavigatesToProvidersScreen() {
    val service = SERVICES_LIST[0]
    composeTestRule.onNodeWithTag(service.service.toString() + "Item").performClick()
    /*TODO*/
  }
}
