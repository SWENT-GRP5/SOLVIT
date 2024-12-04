package com.android.solvit.provider.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.shared.model.Notification
import com.android.solvit.shared.model.NotificationsRepository
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.GregorianCalendar
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

class NotificationsScreenTest {
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var notificationsViewModel: NotificationsViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  private val serviceRequest =
      ServiceRequest(
          uid = "uid",
          title = "title",
          type = Services.CLEANER,
          description = "description",
          userId = "-1",
          dueDate = Timestamp(GregorianCalendar(2024, 0, 1).time),
          location = Location(37.7749, -122.4194, "San Francisco"),
          imageUrl = "imageUrl",
          status = ServiceRequestStatus.PENDING)

  private val testNotifications =
      listOf(
          Notification(
              uid = "1",
              providerId = "provider1",
              title = "New Request",
              message = "You have a new request",
              timestamp = Timestamp.now(),
              serviceRequest,
              isRead = false),
          Notification(
              uid = "2",
              providerId = "provider1",
              title = "Booking Confirmed",
              message = "Your booking is confirmed",
              timestamp = Timestamp.now(),
              serviceRequest,
              isRead = false))

  @Before
  fun setUp() {
    // Mock repositories
    notificationsRepository = Mockito.mock(NotificationsRepository::class.java)

    // ViewModel Initialization
    notificationsViewModel = NotificationsViewModel(notificationsRepository)

    // Mock Navigation
    navController = Mockito.mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    // Mock Repository Behavior
    whenever(notificationsRepository.getNotification(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<(List<Notification>) -> Unit>(1)
      onSuccess(testNotifications)
    }

    whenever(notificationsRepository.sendNotification(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(2)
      onSuccess()
    }
  }

  @Test
  fun displayAllNotifications() {

    composeTestRule.setContent {
      NotificationScreen(
          viewModel = notificationsViewModel,
          providerId = "provider1",
          navigationActions = navigationActions)
    }
    assertTrue(notificationsViewModel.notifications.value.isNotEmpty())

    // Verify that the notifications are displayed
    composeTestRule.onNodeWithTag("notificationsList").assertIsDisplayed()

    composeTestRule.onNodeWithTag("notificationItem_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("notificationItem_2").assertIsDisplayed()

    // Check each notification's title, message, and timestamp
    composeTestRule.onNodeWithTag("notificationTitle_1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("notificationMessage_1", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("notificationTimestamp_1", useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag("notificationTitle_2", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("notificationMessage_2", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("notificationTimestamp_2", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitInvalidNotification() {
    // Simulate an invalid scenario (e.g., no notifications)
    whenever(notificationsRepository.getNotification(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<(List<Notification>) -> Unit>(1)
      onSuccess(emptyList())
    }

    composeTestRule.setContent {
      NotificationScreen(
          viewModel = notificationsViewModel,
          providerId = "provider1",
          navigationActions = navigationActions)
    }

    // Verify the empty state
    composeTestRule.onNodeWithTag("noNotificationsText").assertIsDisplayed()
  }

  @Test
  fun verifyGetNotificationsCalled() {
    composeTestRule.setContent {
      NotificationScreen(
          viewModel = notificationsViewModel,
          providerId = "provider1",
          navigationActions = navigationActions)
    }

    // Verify the repository's getNotification function was called with the correct arguments
    Mockito.verify(notificationsRepository)
        .getNotification(
            eq("provider1"), // Use eq() to match the specific value
            any(), // Use matchers for the other arguments
            any())
  }
}
