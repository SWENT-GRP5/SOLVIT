package com.android.solvit.shared.model.provider



import com.android.solvit.shared.model.Notification
import com.android.solvit.shared.model.NotificationsRepository
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class NotificationViewModelTest {

    private lateinit var notificationsRepository: NotificationsRepository
    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var serviceRequest: ServiceRequest
    private val providerId = "provider123"

    @Before
    fun setup() {
        notificationsRepository = mock(NotificationsRepository::class.java)
        notificationsViewModel = NotificationsViewModel(notificationsRepository)
        serviceRequest = ServiceRequest(
            uid = "service123",
            title = "Tutoring",
            type = Services.TUTOR,
            description = "Math tutoring session",
            "",
            "",
            Timestamp.now(),
            Timestamp.now(),
            location = null
        )
    }

    @Test
    fun init_fetchesNotifications() = runBlocking {
        // Mock the repository behavior for getNotification
        val notificationsList = listOf(mockNotification())
        `when`(notificationsRepository.getNotification(eq(providerId), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.arguments[1] as (List<Notification>) -> Unit
                onSuccess(notificationsList)
            }

        // Initialize the ViewModel
        notificationsViewModel.init(providerId)

        // Verify that getNotification was called with the correct providerId
        verify(notificationsRepository).getNotification(eq(providerId), any(), any())

        // Verify that notifications were set correctly
        assertThat(notificationsViewModel.notifications.first(), `is`(notificationsList))
    }

    @Test
    fun sendNotifications_sendsAndFetchesUpdatedNotifications() = runBlocking {
        val matchingProviders = listOf(mockProvider())

        // Mock the sendNotification behavior
        doAnswer { invocation ->
            val onSuccess = invocation.arguments[2] as () -> Unit
            onSuccess() // Simulate success
            null
        }.`when`(notificationsRepository).sendNotification(eq(serviceRequest), eq(matchingProviders), any(), any())

        // Mock the getNotification behavior for fetching notifications
        val notificationsList = listOf(mockNotification())
        doAnswer { invocation ->
            val onSuccess = invocation.arguments[1] as (List<Notification>) -> Unit
            onSuccess(notificationsList) // Simulate fetching notifications
            null
        }.`when`(notificationsRepository).getNotification(eq(providerId), any(), any())

        // Initialize the ViewModel and simulate sending the notification
        notificationsViewModel.init(providerId) // Calls getNotifications() once
        notificationsViewModel.sendNotifications(serviceRequest, matchingProviders) // Calls getNotifications() again

        // Verify that getNotification was called twice: once in init() and once after sending notifications
        verify(notificationsRepository, times(2)).getNotification(eq(providerId), any(), any())

        // Verify that sendNotification was called once
        verify(notificationsRepository).sendNotification(eq(serviceRequest), eq(matchingProviders), any(), any())
    }

    private fun mockNotification(): Notification {
        return Notification(
            id = "notif123",
            providerId = providerId,
            title = "New Service Request",
            message = "You have a new service request for tutoring",
            timestamp = Timestamp.now(),
            isRead = false
        )
    }

    private fun mockProvider(): Provider {
        return Provider(
            uid = "provider123",
            name = "John Doe",
            service = Services.TUTOR
        )
    }
}
