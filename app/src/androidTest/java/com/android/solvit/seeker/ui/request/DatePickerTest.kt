package com.android.solvit.seeker.ui.request

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class DatePickerTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun datePickerModal_showsAndSelectsDate() {
    var selectedDate = ""
    composeTestRule.setContent {
      DatePickerFieldToModal(dueDate = "", onDateChange = { selectedDate = it })
    }

    composeTestRule.onNodeWithTag("inputRequestDate").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").performClick()
    composeTestRule.onNodeWithText("OK").performClick()
    assert(selectedDate != "")
  }

  @Test
  fun datePickerModal_dismissesWithoutSelectingDate() {
    var selectedDate = ""
    composeTestRule.setContent {
      DatePickerFieldToModal(dueDate = "", onDateChange = { selectedDate = it })
    }

    composeTestRule.onNodeWithTag("inputRequestDate").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").performClick()
    composeTestRule.onNodeWithText("Cancel").performClick()
    assert(selectedDate == "")
  }
}
