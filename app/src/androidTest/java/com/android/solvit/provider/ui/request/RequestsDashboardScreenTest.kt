package com.android.solvit.provider.ui.request

// Jetpack Compose Testing
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class RequestsDashboardScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var viewModel: ServiceRequestViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var serviceRequestRepository: ServiceRequestRepository

  private val request =
      ServiceRequest(
          uid = "1",
          title = "Test Job",
          type = Services.CLEANER,
          description = "Test Description",
          userId = "1",
          providerId = "1",
          dueDate = Timestamp(GregorianCalendar(2024, 0, 1, 12, 1).time),
          meetingDate = Timestamp(GregorianCalendar(2024, 0, 1, 12, 1).time),
          location = Location(37.7749, -122.4194, "Test Location"),
          packageId = "1",
          agreedPrice = 100.0,
          imageUrl = "imageUrl",
          status = ServiceRequestStatus.PENDING)

  @Before
  fun setup() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    viewModel = ServiceRequestViewModel(serviceRequestRepository)
  }

  @Test
  fun testInitialTabIsCurrent() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Verify that the "Current" tab is selected by default
    composeTestRule.onNodeWithTag("Tab_Current").assertIsSelected()
  }

  @Test
  fun testNavigateToAllJobsButtonInCurrentTab() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Verify that the "Navigate to All Jobs of the Day" button is displayed and clickable
    composeTestRule.onNodeWithTag("NavigateAllJobsButton").assertIsDisplayed().performClick()
  }

  @Test
  fun testPendingSectionDisplaysOnlyPendingJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to Pending tab
    composeTestRule.onNodeWithTag("Tab_Pending").performClick()

    // Check if jobs in the Pending section have the correct status tag
    composeTestRule.onNodeWithTag("PendingJobsSection").assertIsDisplayed()
    viewModel.pendingRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_PENDING_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ConfirmButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
    }
  }

  @Test
  fun testCurrentSectionDisplaysOnlyCurrentJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Check if jobs in the Current section have the correct status tag
    composeTestRule.onNodeWithTag("CurrentJobsSection").assertIsDisplayed()
    viewModel.scheduledRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_CURRENT_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("NavigateButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CancelButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CompleteButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
    }
  }

  @Test
  fun testHistorySectionDisplaysOnlyHistoryJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to History tab
    composeTestRule.onNodeWithTag("Tab_History").performClick()

    // Check if jobs in the History section have the correct status tag
    composeTestRule.onNodeWithTag("HistoryJobsSection").assertIsDisplayed()
    viewModel.archivedRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_HISTORY_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("StatusText_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
    }
  }

  @Test
  fun testConfirmPendingJobMovesToCurrent() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to Pending tab
    composeTestRule.onNodeWithTag("Tab_Pending").performClick()

    // Get the first pending job and confirm it
    val pendingJob = viewModel.pendingRequests.value.firstOrNull()
    pendingJob?.let { request ->
      composeTestRule.onNodeWithTag("ConfirmButton_${request.uid}").performClick()
      assert(viewModel.scheduledRequests.value.any { it.uid == request.uid })
    }
  }

  @Test
  fun testCompleteCurrentJobMovesToHistory() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Get the first current job and mark it as completed
    val currentJob = viewModel.scheduledRequests.value.firstOrNull()
    currentJob?.let { request ->
      composeTestRule.onNodeWithTag("CompleteButton_${request.uid}").performClick()
      assert(
          viewModel.completedRequests.value.any {
            it.uid == request.uid && it.status == ServiceRequestStatus.COMPLETED
          })
    }
  }

  @Test
  fun testCancelCurrentJobMovesToHistoryAsCancelled() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Get the first current job and mark it as cancelled
    val currentJob = viewModel.scheduledRequests.value.firstOrNull()
    currentJob?.let { request ->
      composeTestRule.onNodeWithTag("CancelButton_${request.uid}").performClick()
      assert(
          viewModel.archivedRequests.value.any {
            it.uid == request.uid && it.status == ServiceRequestStatus.ARCHIVED
          })
    }
  }

  @Test
  fun testChatButtonShowsToast() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Verify Chat button presence and functionality on a job in the Current tab
    val jobWithChat = viewModel.scheduledRequests.value.firstOrNull()
    jobWithChat?.let { request ->
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertIsDisplayed().performClick()
    }
  }

  @Test
  fun displaysTexts() {
    composeTestRule.setContent { JobItem(request = request, status = ServiceRequestStatus.PENDING) }
    composeTestRule.onNodeWithTag("JobItem_PENDING_1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Job").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Description").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Location").assertIsDisplayed()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = dateFormat.format(request.meetingDate?.toDate() ?: 0)
    val time = timeFormat.format(request.meetingDate?.toDate() ?: 0)
    composeTestRule.onNodeWithText("Scheduled: $date at $time").assertIsDisplayed()
  }

  @Test
  fun navigateButtonIsDisplayedForScheduledJobs() {
    composeTestRule.setContent {
      JobItem(request = request, status = ServiceRequestStatus.SCHEDULED, onNavigateToJob = {})
    }
    composeTestRule.onNodeWithTag("NavigateButton_1").assertIsDisplayed()
  }

  @Test
  fun confirmButtonIsDisplayedForPendingJobs() {
    composeTestRule.setContent {
      JobItem(request = request, status = ServiceRequestStatus.PENDING, onConfirmRequest = {})
    }
    composeTestRule.onNodeWithTag("ConfirmButton_1").assertIsDisplayed()
  }

  @Test
  fun cancelButtonIsDisplayedForScheduledJobs() {
    composeTestRule.setContent {
      JobItem(request = request, status = ServiceRequestStatus.SCHEDULED, onCancelRequest = {})
    }
    composeTestRule.onNodeWithTag("CancelButton_1").assertIsDisplayed()
  }

  @Test
  fun completeButtonIsDisplayedForScheduledJobs() {
    composeTestRule.setContent {
      JobItem(request = request, status = ServiceRequestStatus.SCHEDULED, onMarkAsCompleted = {})
    }
    composeTestRule.onNodeWithTag("CompleteButton_1").assertIsDisplayed()
  }

  @Test
  fun chatButtonIsDisplayed() {
    composeTestRule.setContent {
      JobItem(request = request, status = ServiceRequestStatus.SCHEDULED, onChat = {})
    }
    composeTestRule.onNodeWithTag("ChatButton_1").assertIsDisplayed()
  }

  @Test
  fun callButtonIsDisplayed() {
    composeTestRule.setContent {
      JobItem(request = request, status = ServiceRequestStatus.SCHEDULED, onContactCustomer = {})
    }
    composeTestRule.onNodeWithTag("CallButton_1").assertIsDisplayed()
  }

  @Test
  fun navigateButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request,
          status = ServiceRequestStatus.SCHEDULED,
          onNavigateToJob = { clicked = true })
    }
    composeTestRule.onNodeWithTag("NavigateButton_1").performClick()
    assert(clicked)
  }

  @Test
  fun confirmButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request,
          status = ServiceRequestStatus.PENDING,
          onConfirmRequest = { clicked = true })
    }
    composeTestRule.onNodeWithTag("ConfirmButton_1").performClick()
    assert(clicked)
  }

  @Test
  fun cancelButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request,
          status = ServiceRequestStatus.SCHEDULED,
          onCancelRequest = { clicked = true })
    }
    composeTestRule.onNodeWithTag("CancelButton_1").performClick()
    assert(clicked)
  }

  @Test
  fun completeButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request,
          status = ServiceRequestStatus.SCHEDULED,
          onMarkAsCompleted = { clicked = true })
    }
    composeTestRule.onNodeWithTag("CompleteButton_1").performClick()
    assert(clicked)
  }

  @Test
  fun chatButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request, status = ServiceRequestStatus.SCHEDULED, onChat = { clicked = true })
    }
    composeTestRule.onNodeWithTag("ChatButton_1").performClick()
    assert(clicked)
  }

  @Test
  fun callButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request,
          status = ServiceRequestStatus.SCHEDULED,
          onContactCustomer = { clicked = true })
    }
    composeTestRule.onNodeWithTag("CallButton_1").performClick()
    assert(clicked)
  }
}