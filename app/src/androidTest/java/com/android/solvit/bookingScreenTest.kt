package com.android.solvit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.ui.ServiceBookingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookingSceenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displayAllcomponents() {
    composeTestRule.setContent { ServiceBookingScreen() }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rating_star_icon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_image_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rating_value").assertIsDisplayed()
    composeTestRule.onNodeWithTag("price_appointment_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("address_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("google_map_container").assertIsDisplayed()
  }
}
