package com.android.solvit.provider.ui.jobs

// Jetpack Compose Testing
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.model.jobs.JobDashboardRepository
import com.android.solvit.shared.model.jobs.JobDashboardViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class JobDashboardScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var viewModel: JobDashboardViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var jobRepository: JobDashboardRepository

  @Before
  fun setup() {
    jobRepository = mock(JobDashboardRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    viewModel = JobDashboardViewModel(jobRepository)
  }

  @Test
  fun testInitialTabIsCurrent() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Verify that the "Current" tab is selected by default
    composeTestRule.onNodeWithTag("Tab_Current").assertIsSelected()
  }

  @Test
  fun testNavigateToAllJobsButtonInCurrentTab() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Verify that the "Navigate to All Jobs of the Day" button is displayed and clickable
    composeTestRule.onNodeWithTag("NavigateAllJobsButton").assertIsDisplayed().performClick()
  }

  @Test
  fun testPendingSectionDisplaysOnlyPendingJobs() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Switch to Pending tab
    composeTestRule.onNodeWithTag("Tab_Pending").performClick()

    // Check if jobs in the Pending section have the correct status tag
    composeTestRule.onNodeWithTag("PendingJobsSection").assertIsDisplayed()
    viewModel.pendingJobs.value.forEach { job ->
      composeTestRule.onNodeWithTag("JobItem_PENDING_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("ConfirmButton_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${job.id}").assertExists()
    }
  }

  @Test
  fun testCurrentSectionDisplaysOnlyCurrentJobs() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Check if jobs in the Current section have the correct status tag
    composeTestRule.onNodeWithTag("CurrentJobsSection").assertIsDisplayed()
    viewModel.currentJobs.value.forEach { job ->
      composeTestRule.onNodeWithTag("JobItem_CURRENT_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("NavigateButton_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("CancelButton_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("CompleteButton_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${job.id}").assertExists()
    }
  }

  @Test
  fun testHistorySectionDisplaysOnlyHistoryJobs() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Switch to History tab
    composeTestRule.onNodeWithTag("Tab_History").performClick()

    // Check if jobs in the History section have the correct status tag
    composeTestRule.onNodeWithTag("HistoryJobsSection").assertIsDisplayed()
    viewModel.historyJobs.value.forEach { job ->
      composeTestRule.onNodeWithTag("JobItem_HISTORY_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("StatusText_${job.id}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${job.id}").assertExists()
    }
  }

  @Test
  fun testConfirmPendingJobMovesToCurrent() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Switch to Pending tab
    composeTestRule.onNodeWithTag("Tab_Pending").performClick()

    // Get the first pending job and confirm it
    val pendingJob = viewModel.pendingJobs.value.firstOrNull()
    pendingJob?.let { job ->
      composeTestRule.onNodeWithTag("ConfirmButton_${job.id}").performClick()
      assert(viewModel.currentJobs.value.any { it.id == job.id })
    }
  }

  @Test
  fun testCompleteCurrentJobMovesToHistory() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Get the first current job and mark it as completed
    val currentJob = viewModel.currentJobs.value.firstOrNull()
    currentJob?.let { job ->
      composeTestRule.onNodeWithTag("CompleteButton_${job.id}").performClick()
      assert(viewModel.historyJobs.value.any { it.id == job.id && it.status == "COMPLETED" })
    }
  }

  @Test
  fun testCancelCurrentJobMovesToHistoryAsCancelled() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Get the first current job and mark it as cancelled
    val currentJob = viewModel.currentJobs.value.firstOrNull()
    currentJob?.let { job ->
      composeTestRule.onNodeWithTag("CancelButton_${job.id}").performClick()
      assert(viewModel.historyJobs.value.any { it.id == job.id && it.status == "CANCELED" })
    }
  }

  @Test
  fun testChatButtonShowsToast() {
    composeTestRule.setContent {
      JobDashboardScreen(navigationActions, jobDashboardViewModel = viewModel)
    }

    // Verify Chat button presence and functionality on a job in the Current tab
    val jobWithChat = viewModel.currentJobs.value.firstOrNull()
    jobWithChat?.let { job ->
      composeTestRule.onNodeWithTag("ChatButton_${job.id}").assertIsDisplayed().performClick()
    }
  }
}
