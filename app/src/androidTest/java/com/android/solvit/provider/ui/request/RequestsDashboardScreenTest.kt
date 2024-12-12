package com.android.solvit.provider.ui.request

// Jetpack Compose Testing
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RequestsDashboardScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var viewModel: ServiceRequestViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var authRep: AuthRep
  private lateinit var providerRepository: ProviderRepository
  private lateinit var authViewModel: AuthViewModel
  private lateinit var providerViewModel: ListProviderViewModel

  private val request =
      ServiceRequest(
          uid = "1",
          title = "Test Job",
          type = Services.CLEANER,
          description = "Test Description",
          userId = "1",
          providerId = "-1",
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
    authRep = mock(AuthRep::class.java)
    providerRepository = mock(ProviderRepository::class.java)
    authViewModel = AuthViewModel(authRep)
    providerViewModel = ListProviderViewModel(providerRepository)
    viewModel = ServiceRequestViewModel(serviceRequestRepository)
  }

  @Test
  fun testInitialTabIsCurrent() {
    composeTestRule.setContent {
      RequestsDashboardScreen(
          navigationActions,
          serviceRequestViewModel = viewModel,
          authViewModel = authViewModel,
          listProviderViewModel = providerViewModel)
    }

    // Verify that the "Current" tab is selected by default
    composeTestRule.onNodeWithTag("statusTab_2").assertIsSelected()
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
    composeTestRule.onNodeWithTag("statusTab_0").performClick()

    // Check if jobs in the Pending section have the correct status tag
    composeTestRule.onNodeWithTag("PendingSection").assertIsDisplayed()
    viewModel.pendingRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_PENDING_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("ConfirmButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").performClick()
    }
  }

  @Test
  fun testAcceptedSectionDisplaysOnlyAcceptedJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to Accepted tab
    composeTestRule.onNodeWithTag("statusTab_1").performClick()

    // Check if jobs in the Accepted section have the correct status tag
    composeTestRule.onNodeWithTag("AcceptedSection").assertIsDisplayed()
    viewModel.acceptedRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_ACCEPTED_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("StatusText_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").performClick()
    }
  }

  @Test
  fun testScheduledSectionDisplaysOnlyCurrentJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Check if jobs in the Current section have the correct status tag
    composeTestRule.onNodeWithTag("ScheduledSection").assertIsDisplayed()
    viewModel.scheduledRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_SCHEDULED_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("NavigateButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("CancelButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CompleteButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").performClick()
    }
  }

  @Test
  fun testCompletedSectionDisplaysOnlyCompletedJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to Completed tab
    composeTestRule.onNodeWithTag("statusTab_3").performClick()

    // Check if jobs in the Completed section have the correct status tag
    composeTestRule.onNodeWithTag("CompletedSection").assertIsDisplayed()
    viewModel.completedRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_COMPLETED_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("StatusText_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").performClick()
    }
  }

  @Test
  fun testCancelledSectionDisplaysOnlyCancelledJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to Cancelled tab
    composeTestRule.onNodeWithTag("statusTab_4").performClick()

    // Check if jobs in the Cancelled section have the correct status tag
    composeTestRule.onNodeWithTag("CanceledSection").assertIsDisplayed()
    viewModel.cancelledRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_CANCELLED_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("StatusText_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").performClick()
    }
  }

  @Test
  fun testArchivedSectionDisplaysOnlyArchivedJobs() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to History tab
    composeTestRule.onNodeWithTag("statusTab_5").performScrollTo()
    composeTestRule.onNodeWithTag("statusTab_5").performClick()

    // Check if jobs in the History section have the correct status tag
    composeTestRule.onNodeWithTag("ArchivedSection").assertIsDisplayed()
    viewModel.archivedRequests.value.forEach { request ->
      composeTestRule.onNodeWithTag("JobItem_ARCHIVED_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("ChatButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("StatusText_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("CallButton_${request.uid}").performClick()
      composeTestRule.onNodeWithTag("StatusText_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").assertExists()
      composeTestRule.onNodeWithTag("LearnMoreButton_${request.uid}").performClick()
    }
  }

  @Test
  fun testConfirmPendingJobMovesToCurrent() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Switch to Pending tab
    composeTestRule.onNodeWithTag("statusTab_0").performClick()

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
  fun testArchiveCompletedJobMovesToArchive() {
    composeTestRule.setContent {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel = viewModel)
    }

    // Get the first completed job and archive it
    val completedJob = viewModel.completedRequests.value.firstOrNull()
    completedJob?.let { request ->
      composeTestRule.onNodeWithTag("ArchiveButton_${request.uid}").performClick()
      assert(
          viewModel.archivedRequests.value.any {
            it.uid == request.uid && it.status == ServiceRequestStatus.ARCHIVED
          })
    }
  }

  @Test
  fun testCancelCurrentJobMovesToCancel() {
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
    composeTestRule.setContent { JobItem(request = request) }
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
      JobItem(request = request.copy(status = ServiceRequestStatus.SCHEDULED), onNavigateToJob = {})
    }
    composeTestRule.onNodeWithTag("NavigateButton_1").assertIsDisplayed()
  }

  @Test
  fun confirmButtonIsDisplayedForPendingJobs() {
    composeTestRule.setContent {
      JobItem(request = request.copy(status = ServiceRequestStatus.PENDING), onConfirmRequest = {})
    }
    composeTestRule.onNodeWithTag("ConfirmButton_1").assertIsDisplayed()
  }

  @Test
  fun cancelButtonIsDisplayedForScheduledJobs() {
    composeTestRule.setContent {
      JobItem(request = request.copy(status = ServiceRequestStatus.SCHEDULED), onCancelRequest = {})
    }
    composeTestRule.onNodeWithTag("CancelButton_1").assertIsDisplayed()
  }

  @Test
  fun completeButtonIsDisplayedForScheduledJobs() {
    composeTestRule.setContent {
      JobItem(
          request = request.copy(status = ServiceRequestStatus.SCHEDULED), onMarkAsCompleted = {})
    }
    composeTestRule.onNodeWithTag("CompleteButton_1").assertIsDisplayed()
  }

  @Test
  fun chatButtonIsDisplayed() {
    composeTestRule.setContent { JobItem(request = request, onChat = {}) }
    composeTestRule.onNodeWithTag("ChatButton_1").assertIsDisplayed()
  }

  @Test
  fun callButtonIsDisplayed() {
    composeTestRule.setContent { JobItem(request = request, onContactCustomer = {}) }
    composeTestRule.onNodeWithTag("CallButton_1").assertIsDisplayed()
  }

  @Test
  fun navigateButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request.copy(status = ServiceRequestStatus.SCHEDULED),
          onNavigateToJob = { clicked = true })
    }
    composeTestRule.onNodeWithTag("NavigateButton_1").performClick()
    composeTestRule.waitForIdle()
    assert(clicked)
  }

  @Test
  fun confirmButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent { JobItem(request = request, onConfirmRequest = { clicked = true }) }
    composeTestRule.onNodeWithTag("ConfirmButton_1").performClick()
    composeTestRule.waitForIdle()
    assert(clicked)
  }

  @Test
  fun cancelButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request.copy(status = ServiceRequestStatus.SCHEDULED),
          onCancelRequest = { clicked = true })
    }
    composeTestRule.onNodeWithTag("CancelButton_1").performClick()
    composeTestRule.waitForIdle()
    assert(clicked)
  }

  @Test
  fun completeButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(
          request = request.copy(status = ServiceRequestStatus.SCHEDULED),
          onMarkAsCompleted = { clicked = true })
    }
    composeTestRule.onNodeWithTag("CompleteButton_1").performClick()
    composeTestRule.waitForIdle()
    assert(clicked)
  }

  @Test
  fun chatButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent { JobItem(request = request, onChat = { clicked = true }) }
    composeTestRule.onNodeWithTag("ChatButton_1").performClick()
    composeTestRule.waitForIdle()
    assert(clicked)
  }

  @Test
  fun callButtonClickTriggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      JobItem(request = request, onContactCustomer = { clicked = true })
    }
    composeTestRule.onNodeWithTag("CallButton_1").performClick()
    composeTestRule.waitForIdle()
    assert(clicked)
  }

  @Test
  fun testOnMarkAsCompleteInvokesExpectedMethods() = runTest {
    val testProvider =
        Provider(
            uid = "testProviderId",
            name = "Test Provider",
            nbrOfJobs = 5.0,
        )

    val scheduledRequests =
        listOf(
            ServiceRequest(
                uid = "test_scheduled_id",
                title = "Scheduled Job",
                providerId = "testProviderId",
                description = "Job Description",
                status = ServiceRequestStatus.SCHEDULED))

    whenever(serviceRequestRepository.getScheduledServiceRequests(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ServiceRequest>) -> Unit>(0)
      onSuccess(scheduledRequests)
    }

    whenever(providerViewModel.fetchProviderById("testProviderId")).thenReturn(testProvider)
    viewModel.getScheduledRequests()

    composeTestRule.setContent {
      ScheduledJobsSection(
          providerId = "testProviderId",
          viewModel = viewModel,
          navigationActions = navigationActions,
          listProviderViewModel = providerViewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag("CompleteButton_test_scheduled_id").isDisplayed()
    }

    // Click the Complete button
    composeTestRule.onNodeWithTag("CompleteButton_test_scheduled_id").performClick()

    composeTestRule.waitForIdle()
    // Verify that completeRequest was called with the correct request
    verify(serviceRequestRepository).saveServiceRequest(any(), any(), any())

    // Verify that the provider was fetched and updated after job completion
    verify(providerRepository).returnProvider("testProviderId")
    verify(providerRepository)
        .updateProvider(argThat { nbrOfJobs == testProvider.nbrOfJobs + 1 }, any(), any())
  }
}
