package com.android.solvit.provider.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.android.solvit.provider.ui.calendar.ProviderCalendarScreen
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ProviderCalendarScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { ProviderCalendarScreen(navigationActions = navigationActions) }
  }

  @Test
  fun testInitialCalendarView() {
    composeTestRule.onNodeWithTag("calendarTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("menuButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("calendarViewToggle").assertIsDisplayed()
    // composeTestRule.onNodeWithTag("monthView").assertIsDisplayed()
  }

  @Test
  fun testCalendarViewNavigation() {
    // Test Month View
    composeTestRule.onNodeWithTag("toggleButton_month").performClick()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }

    // Test Week View
    composeTestRule.onNodeWithTag("toggleButton_week").performClick()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }

    // Test Day View
    composeTestRule.onNodeWithTag("toggleButton_day").performClick()
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeLeft() }
    composeTestRule.onNodeWithTag("calendarColumn").performTouchInput { swipeRight() }
  }

  @Test
  fun testDaySelection() {
    // composeTestRule.onNodeWithTag("monthView").assertIsDisplayed()
    val today = java.time.LocalDate.now().toString()
    val todayTag = "dayItem_$today"
    composeTestRule.onNodeWithTag(todayTag).performClick()
    composeTestRule.onNodeWithTag("bottomSheetDayView").assertIsDisplayed()
    // composeTestRule.onNodeWithTag("bottomSheet").assertIsDisplayed()
  }

  @Test
  fun testDatePicker() {
    // Open the date picker
    composeTestRule.onNodeWithTag("monthHeader").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").assertIsDisplayed()

    // Select a specific date (e.g., 15th of the current month)
    composeTestRule.onNodeWithText("15").performClick()

    // Confirm the selection
    composeTestRule.onNodeWithTag("confirmDateButton").performClick()

    // Assert that the date picker dialog is no longer displayed
    composeTestRule.onNodeWithTag("datePickerDialog").assertDoesNotExist()

    // Optional: Assert that the selected date is reflected in the UI
    // This depends on how you update the UI after date selection
    // composeTestRule.onNodeWithTag("selectedDateDisplay").assertTextContains("15")

    // Test cancelling the date picker
    composeTestRule.onNodeWithTag("monthHeader").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").assertIsDisplayed()

    // Cancel the selection
    composeTestRule.onNodeWithTag("cancelDateButton").performClick()
  }
}
