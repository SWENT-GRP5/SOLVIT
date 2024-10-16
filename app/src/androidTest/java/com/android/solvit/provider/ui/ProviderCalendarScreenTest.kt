package com.android.solvit.provider.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
  fun testCalendarViewToggle() {
    composeTestRule.onNodeWithTag("toggleButton_week").performClick()
    // composeTestRule.onNodeWithTag("weekView").assertIsDisplayed()

    composeTestRule.onNodeWithTag("toggleButton_day").performClick()
    // composeTestRule.onNodeWithTag("dayView").assertIsDisplayed()

    composeTestRule.onNodeWithTag("toggleButton_month").performClick()
    // composeTestRule.onNodeWithTag("monthView").assertIsDisplayed()
  }

  @Test
  fun testDaySelection() {
    // composeTestRule.onNodeWithTag("monthView").assertIsDisplayed()
    composeTestRule.onAllNodes(hasClickAction()).onFirst().performClick()
    // composeTestRule.onNodeWithTag("bottomSheet").assertIsDisplayed()
  }

  @Test
  fun testTimeSlots() {
    composeTestRule.onNodeWithTag("toggleButton_day").performClick()
    // composeTestRule.onNodeWithTag("dayView").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("timeSlotItem").assertCountEquals(4)
  }

  @Test
  fun testDatePicker() {
    // composeTestRule.onNodeWithTag("monthView").assertIsDisplayed()
    // Assuming there's a way to trigger the date picker, e.g., by clicking on the month-year text
    composeTestRule.onNodeWithTag("monthYearText").performClick()
    // composeTestRule.onNodeWithTag("datePickerDialog").assertIsDisplayed()
  }
}
