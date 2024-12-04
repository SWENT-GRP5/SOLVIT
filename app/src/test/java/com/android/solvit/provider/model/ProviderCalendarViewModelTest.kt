package com.android.solvit.provider.model

import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ProviderCalendarViewModelTest {
  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")

  private lateinit var mockUser: User
  private lateinit var userFlow: MutableStateFlow<User?>
  private lateinit var requestsFlow: MutableStateFlow<List<ServiceRequest>>
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var calendarViewModel: ProviderCalendarViewModel

  // Test data for service requests
  private val testDate = LocalDateTime.of(2024, 1, 8, 10, 0) // A Monday at 10:00
  private val assignedRequest =
      ServiceRequest(
          uid = "request1",
          title = "Fix leaky faucet",
          type = Services.PLUMBER,
          description = "Test request",
          userId = "seeker1",
          providerId = testUserId, // Assigned to our test provider
          dueDate = Timestamp(testDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
          meetingDate = Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
          location = testLocation,
          imageUrl = null,
          packageId = null,
          agreedPrice = 100.0,
          status = ServiceRequestStatus.PENDING)

  private val unassignedRequest =
      ServiceRequest(
          uid = "request2",
          title = "Unassigned plumbing job",
          type = Services.PLUMBER,
          description = "Unassigned request",
          userId = "seeker2",
          providerId = null, // Not assigned to any provider
          dueDate = Timestamp(testDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
          meetingDate = Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
          location = testLocation,
          imageUrl = null,
          packageId = null,
          agreedPrice = null,
          status = ServiceRequestStatus.PENDING)

  private val otherProviderRequest =
      ServiceRequest(
          uid = "request3",
          title = "Other provider job",
          type = Services.PLUMBER,
          description = "Other provider request",
          userId = "seeker3",
          providerId = "other_provider_id", // Assigned to a different provider
          dueDate = Timestamp(testDate.plusDays(7).toInstant(ZoneOffset.UTC).epochSecond, 0),
          meetingDate = Timestamp(testDate.toInstant(ZoneOffset.UTC).epochSecond, 0),
          location = testLocation,
          imageUrl = null,
          packageId = null,
          agreedPrice = 150.0,
          status = ServiceRequestStatus.PENDING)

  @Before
  fun setUp() {
    // Configure mock user
    mockUser = mock()
    whenever(mockUser.uid).thenReturn(testUserId)

    // Set up user flow
    userFlow = MutableStateFlow(mockUser)

    // Configure auth view model with mock user
    authViewModel = mock()
    whenever(authViewModel.user).thenReturn(userFlow)

    // Configure service request view model with test data
    serviceRequestViewModel = mock()
    requestsFlow = MutableStateFlow(emptyList())
    whenever(serviceRequestViewModel.requests)
        .thenReturn(requestsFlow as StateFlow<List<ServiceRequest>>)
    doAnswer {
          requestsFlow.value = listOf(assignedRequest, unassignedRequest, otherProviderRequest)
          Unit
        }
        .whenever(serviceRequestViewModel)
        .getServiceRequests()

    // Initialize view model with mocked dependencies
    calendarViewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)
  }

  @Test
  fun testServiceRequests() = runBlocking {
    // Verify that getServiceRequests is called during initialization
    verify(serviceRequestViewModel).getServiceRequests()

    // Test that the flow only emits requests assigned to the current provider
    val requests = calendarViewModel.serviceRequests.first()
    assertEquals("Should only contain requests for current provider", 1, requests.size)
    assertEquals("Should be the assigned request", assignedRequest, requests[0])

    // Test filtering out unassigned requests
    requestsFlow.value = listOf(unassignedRequest)
    val unassignedRequests = calendarViewModel.serviceRequests.first()
    assertTrue("Should filter out unassigned requests", unassignedRequests.isEmpty())

    // Test filtering out requests for other providers
    requestsFlow.value = listOf(otherProviderRequest)
    val wrongProviderRequests = calendarViewModel.serviceRequests.first()
    assertTrue("Should filter out requests for other providers", wrongProviderRequests.isEmpty())
  }

  @Test
  fun testNoUserLoggedIn() = runBlocking {
    // Set user to null to simulate no logged in user
    userFlow.value = null

    // Test that no requests are returned when no user is logged in
    val requests = calendarViewModel.serviceRequests.first()
    assertTrue("Should return empty list when no user is logged in", requests.isEmpty())
  }
}
