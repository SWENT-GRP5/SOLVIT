package com.android.solvit.provider.ui.jobs

// Jetpack Compose Testing
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class JobDashboardScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var viewModel: ServiceRequestViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var serviceRequestRepository: ServiceRequestRepository

  @Before
  fun setup() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    viewModel = ServiceRequestViewModel(serviceRequestRepository)
  }

  @Test
  fun testInitialTabIsCurrent() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Verify that the "Current" tab is selected by default
    composeTestRule.onNodeWithTag("Tab_Current").assertIsSelected()
  }

  @Test
  fun testNavigateToAllJobsButtonInCurrentTab() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Verify that the "Navigate to All Jobs of the Day" button is displayed and clickable
    composeTestRule.onNodeWithTag("NavigateAllJobsButton").assertIsDisplayed().performClick()
  }

  @Test
  fun testPendingSectionDisplaysOnlyPendingJobs() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
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
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
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
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
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
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
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
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
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
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
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
      JobDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Verify Chat button presence and functionality on a job in the Current tab
    val jobWithChat = viewModel.scheduledRequests.value.firstOrNull()
    jobWithChat?.let { request ->
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertIsDisplayed().performClick()
    }
  }
}
