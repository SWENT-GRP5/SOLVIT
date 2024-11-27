package com.android.solvit.provider.ui.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProviderCalendarComponentsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createTestRequest(
      uid: String,
      title: String,
      description: String,
      status: ServiceRequestStatus,
      meetingDate: Timestamp?
  ): ServiceRequest {
    return ServiceRequest(
        uid = uid,
        title = title,
        type = Services.OTHER,
        description = description,
        userId = "test-user-id",
        dueDate = Timestamp(Date()),
        location = Location(0.0, 0.0, "Test Location"),
        status = status,
        meetingDate = meetingDate)
  }

  @Test
  fun `calculateDayStatus returns correct status list`() {
    // Given
    val now = Timestamp(Date())
    val requests =
        listOf(
            createTestRequest(
                uid = "1",
                title = "Request 1",
                description = "Description 1",
                status = ServiceRequestStatus.PENDING,
                meetingDate = now),
            createTestRequest(
                uid = "2",
                title = "Request 2",
                description = "Description 2",
                status = ServiceRequestStatus.ACCEPTED,
                meetingDate = now),
            createTestRequest(
                uid = "3",
                title = "Request 3",
                description = "Description 3",
                status = ServiceRequestStatus.CANCELED,
                meetingDate = now),
            createTestRequest(
                uid = "4",
                title = "Request 4",
                description = "Description 4",
                status = ServiceRequestStatus.COMPLETED,
                meetingDate = now),
            createTestRequest(
                uid = "5",
                title = "Request 5",
                description = "Description 5",
                status = ServiceRequestStatus.PENDING,
                meetingDate = now))

    // When
    val result = calculateDayStatus(requests)

    // Then
    assert(result.size == 4) { "Expected 4 status indicators, got ${result.size}" }
    assert(result[0] == ServiceRequestStatus.PENDING)
    assert(result[1] == ServiceRequestStatus.ACCEPTED)
    assert(result[2] == ServiceRequestStatus.CANCELED)
    assert(result[3] == ServiceRequestStatus.COMPLETED)
  }

  @Test
  fun `calculateDayStatus handles empty request list`() {
    // Given
    val requests = emptyList<ServiceRequest>()

    // When
    val result = calculateDayStatus(requests)

    // Then
    assert(result.isEmpty()) { "Expected empty list, got ${result.size} items" }
  }

  @Test
  fun `calculateDayStatus handles requests without meeting date`() {
    // Given
    val now = Timestamp(Date())
    val requests =
        listOf(
            createTestRequest(
                uid = "1",
                title = "Request 1",
                description = "Description 1",
                status = ServiceRequestStatus.PENDING,
                meetingDate = null),
            createTestRequest(
                uid = "2",
                title = "Request 2",
                description = "Description 2",
                status = ServiceRequestStatus.ACCEPTED,
                meetingDate = now))

    // When
    val result = calculateDayStatus(requests)

    // Then
    assert(result.size == 1) { "Expected 1 status indicator, got ${result.size}" }
    assert(result[0] == ServiceRequestStatus.ACCEPTED)
  }

  @Test
  fun `TimeSlotItem displays correct information`() {
    // Given
    val now = Timestamp(Date())
    val request =
        createTestRequest(
            uid = "1",
            title = "Test Request",
            description = "Test Description",
            status = ServiceRequestStatus.PENDING,
            meetingDate = now)

    // When
    composeTestRule.setContent {
      MaterialTheme {
        TimeSlotItem(
            request = request,
            textColor = Color.Black,
            showDescription = true,
            currentView = CalendarView.DAY)
      }
    }

    // Then
    composeTestRule.onNodeWithText("Test Request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Description").assertIsDisplayed()
  }

  @Test
  fun `TimeSlotItem hides description when showDescription is false`() {
    // Given
    val now = Timestamp(Date())
    val request =
        createTestRequest(
            uid = "1",
            title = "Test Request",
            description = "Test Description",
            status = ServiceRequestStatus.PENDING,
            meetingDate = now)

    // When
    composeTestRule.setContent {
      MaterialTheme {
        TimeSlotItem(
            request = request,
            textColor = Color.Black,
            showDescription = false,
            currentView = CalendarView.MONTH)
      }
    }

    // Then
    composeTestRule.onNodeWithText("Test Request").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Description").assertDoesNotExist()
  }

  @Composable
  private fun TestTheme(content: @Composable () -> Unit) {
    MaterialTheme { content() }
  }
}
