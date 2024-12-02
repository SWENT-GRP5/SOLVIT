package com.android.solvit.provider.model

import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.*
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.*

class ProviderCalendarViewModelTest {
  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")

  // Create a test schedule with regular hours and exceptions
  private val testSchedule =
      Schedule(
          regularHours =
              mutableMapOf(
                  DayOfWeek.MONDAY.name to
                      mutableListOf(TimeSlot(9, 0, 12, 0), TimeSlot(13, 0, 17, 0)),
                  DayOfWeek.WEDNESDAY.name to mutableListOf(TimeSlot(10, 0, 16, 0))),
          exceptions =
              mutableListOf(
                  ScheduleException(
                      LocalDateTime.of(2024, 1, 1, 0, 0),
                      listOf(TimeSlot(14, 0, 18, 0)),
                      ExceptionType.EXTRA_TIME)))

  private val testProvider =
      Provider(
          uid = testUserId,
          name = "Test Provider",
          service = Services.PLUMBER,
          imageUrl = "",
          companyName = "",
          phone = "",
          location = testLocation,
          description = "",
          popular = false,
          rating = 0.0,
          price = 0.0,
          deliveryTime = Timestamp.now(),
          languages = listOf(Language.ENGLISH),
          schedule = testSchedule)

  private lateinit var providerRepository: ProviderRepository
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var providerCalendarViewModel: ProviderCalendarViewModel

  @Before
  fun setup() {
    providerRepository = Mockito.mock(ProviderRepository::class.java)
    val authRepository = Mockito.mock(AuthRepository::class.java)
    serviceRequestViewModel = Mockito.mock(ServiceRequestViewModel::class.java)

    // Mock auth repository behavior
    val user = Mockito.mock(User::class.java)
    Mockito.`when`(user.uid).thenReturn(testUserId)
    doAnswer { invocation ->
          val callback = invocation.getArgument<(User?) -> Unit>(0)
          callback(user)
          null
        }
        .`when`(authRepository)
        .init(any())

    authViewModel = AuthViewModel(authRepository)

    // Mock provider repository behavior
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
          onSuccess(testProvider)
          null
        }
        .`when`(providerRepository)
        .getProvider(any(), any(), any())

    // Mock provider repository update behavior
    doAnswer { invocation ->
          val onComplete = invocation.getArgument<() -> Unit>(1)
          onComplete()
          null
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    providerCalendarViewModel =
        ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)
  }

  @Test
  fun testProviderLoadsOnInit() = runTest {
    // Provider should be loaded in init
    verify(providerRepository).getProvider(any(), any(), any())
    assert(providerCalendarViewModel.isAvailable(LocalDateTime.of(2024, 1, 1, 15, 0)))
  }

  @Test
  fun testProviderNotLoadedWithoutUser() = runTest {
    // Create new auth view model with null user
    val nullAuthRepo = Mockito.mock(AuthRepository::class.java)
    val nullUser = Mockito.mock(User::class.java)
    Mockito.`when`(nullUser.uid).thenReturn(null)
    doAnswer { invocation ->
          val callback = invocation.getArgument<(User?) -> Unit>(0)
          callback(nullUser)
          null
        }
        .`when`(nullAuthRepo)
        .init(any())
    val nullAuthViewModel = AuthViewModel(nullAuthRepo)

    // Create new provider repository for this test
    val testProviderRepo = Mockito.mock(ProviderRepository::class.java)

    // Create new view model with null user
    val viewModel =
        ProviderCalendarViewModel(testProviderRepo, nullAuthViewModel, serviceRequestViewModel)

    // Then
    verify(testProviderRepo, never()).getProvider(any(), any(), any())
  }

  @Test
  fun testAddExtraTimeException() = runTest {
    // Given
    val extraTimeSlot = TimeSlot(14, 0, 18, 0)
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)
    var successResult = false
    var feedbackMessage = ""

    // When
    providerCalendarViewModel.addExtraTimeException(date, listOf(extraTimeSlot)) { success, feedback
      ->
      successResult = success
      feedbackMessage = feedback
    }

    // Then
    verify(providerRepository).updateProvider(any(), any(), any())
    assert(successResult)
    assert(feedbackMessage.isNotEmpty())
  }

  @Test
  fun testAddOffTimeException() = runTest {
    // Given
    val offTimeSlot = TimeSlot(9, 0, 17, 0)
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)
    var successResult = false
    var feedbackMessage = ""

    // When
    providerCalendarViewModel.addOffTimeException(date, listOf(offTimeSlot)) { success, feedback ->
      successResult = success
      feedbackMessage = feedback
    }

    // Then
    verify(providerRepository).updateProvider(any(), any(), any())
    assert(successResult)
    assert(feedbackMessage.isNotEmpty())
  }

  @Test
  fun testSetRegularHours() = runTest {
    // Given
    val timeSlots = listOf(TimeSlot(9, 0, 17, 0))
    var successResult = false

    // When
    providerCalendarViewModel.setRegularHours(DayOfWeek.TUESDAY.name, timeSlots) { success ->
      successResult = success
    }

    // Then
    verify(providerRepository).updateProvider(any(), any(), any())
    assert(successResult)
  }

  @Test
  fun testClearRegularHours() = runTest {
    // Given
    var successResult = false

    // When
    providerCalendarViewModel.clearRegularHours(DayOfWeek.MONDAY.name) { success ->
      successResult = success
    }

    // Then
    verify(providerRepository).updateProvider(any(), any(), any())
    assert(successResult)
  }

  @Test
  fun testUpdateException() = runTest {
    // Given
    val timeSlots = listOf(TimeSlot(10, 0, 15, 0))
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)
    var successResult = false
    var feedbackMessage = ""

    // When
    providerCalendarViewModel.updateException(date, timeSlots, ExceptionType.OFF_TIME) {
        success,
        feedback ->
      successResult = success
      feedbackMessage = feedback
    }

    // Then
    verify(providerRepository).updateProvider(any(), any(), any())
    assert(successResult)
    assert(feedbackMessage.isNotEmpty())
  }

  @Test
  fun testGetExceptions() = runTest {
    // Given
    val startDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val endDate = LocalDateTime.of(2024, 1, 7, 0, 0)

    // When
    val exceptions = providerCalendarViewModel.getExceptions(startDate, endDate)

    // Then
    assert(exceptions.isNotEmpty())
    assert(exceptions.first().type == ExceptionType.EXTRA_TIME)
  }

  @Test
  fun testGetExceptionsWithType() = runTest {
    // Given
    val startDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val endDate = LocalDateTime.of(2024, 1, 7, 0, 0)

    // When
    val extraTimeExceptions =
        providerCalendarViewModel.getExceptions(startDate, endDate, ExceptionType.EXTRA_TIME)
    val offTimeExceptions =
        providerCalendarViewModel.getExceptions(startDate, endDate, ExceptionType.OFF_TIME)

    // Then
    assert(extraTimeExceptions.isNotEmpty())
    assert(offTimeExceptions.isEmpty())
    assert(extraTimeExceptions.all { it.type == ExceptionType.EXTRA_TIME })
  }

  @Test
  fun testMergeOverlappingExceptions() = runTest {
    // Given
    val existingSlot = TimeSlot(9, 0, 13, 0)
    val newSlot = TimeSlot(12, 0, 17, 0)
    val date = LocalDateTime.of(2024, 1, 1, 0, 0)
    var successResult = false
    var feedbackMessage = ""

    // Mock provider repository to return updated provider
    doAnswer { invocation ->
          val provider = invocation.getArgument<Provider>(0)
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
          doAnswer { innerInvocation ->
                val onSuccess = innerInvocation.getArgument<(Provider?) -> Unit>(1)
                onSuccess(provider)
                null
              }
              .`when`(providerRepository)
              .getProvider(any(), any(), any())
          null
        }
        .`when`(providerRepository)
        .updateProvider(any(), any(), any())

    // When
    providerCalendarViewModel.addException(date, listOf(existingSlot), ExceptionType.EXTRA_TIME) {
        success,
        _ ->
      if (success) {
        providerCalendarViewModel.addException(date, listOf(newSlot), ExceptionType.EXTRA_TIME) {
            mergeSuccess,
            mergeFeedback ->
          successResult = mergeSuccess
          feedbackMessage = mergeFeedback
        }
      }
    }

    // Then
    verify(providerRepository, times(2)).updateProvider(any(), any(), any())
    assert(successResult)
    assert(feedbackMessage.contains("merged"))
  }

  @Test
  fun testCheckServiceRequestNoConflict() = runTest {
    // Given
    val meetingDate = LocalDateTime.of(2024, 1, 1, 10, 0) // Monday at 10:00
    val timestamp = Timestamp(Date.from(meetingDate.atZone(ZoneId.systemDefault()).toInstant()))
    val serviceRequest =
        ServiceRequest(
            uid = "test_request",
            title = "Test Request",
            type = Services.PLUMBER,
            description = "",
            userId = "test_customer",
            providerId = testUserId,
            dueDate = Timestamp.now(),
            meetingDate = timestamp,
            location = null,
            status = ServiceRequestStatus.PENDING)

    // Mock service request view model to return empty list
    val emptyFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
    doReturn(emptyFlow).`when`(serviceRequestViewModel).acceptedRequests

    // When
    val result = providerCalendarViewModel.checkServiceRequestConflict(serviceRequest)

    // Then
    assert(!result.hasConflict)
    assert(result.reason == "No conflicts")
  }

  @Test
  fun testCheckServiceRequestOutsideRegularHours() = runTest {
    // Given
    val meetingDate = LocalDateTime.of(2024, 1, 1, 8, 0) // Monday at 8:00 (before regular hours)
    val timestamp = Timestamp(Date.from(meetingDate.atZone(ZoneId.systemDefault()).toInstant()))
    val serviceRequest =
        ServiceRequest(
            uid = "test_request",
            title = "Test Request",
            type = Services.PLUMBER,
            description = "",
            userId = "test_customer",
            providerId = testUserId,
            dueDate = Timestamp.now(),
            meetingDate = timestamp,
            location = null,
            status = ServiceRequestStatus.PENDING)

    // Mock service request view model to return empty list
    val emptyFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
    doReturn(emptyFlow).`when`(serviceRequestViewModel).acceptedRequests

    // When
    val result = providerCalendarViewModel.checkServiceRequestConflict(serviceRequest)

    // Then
    assert(result.hasConflict)
    assert(result.reason.contains("outside your regular working hours"))
  }

  @Test
  fun testCheckServiceRequestWithOffTimeConflict() = runTest {
    // Given
    val exceptionDate = LocalDateTime.of(2024, 1, 1, 0, 0)
    val offTimeException =
        ScheduleException(exceptionDate, listOf(TimeSlot(10, 0, 11, 0)), ExceptionType.OFF_TIME)
    testProvider.schedule.exceptions.add(offTimeException)

    val meetingDate = LocalDateTime.of(2024, 1, 1, 10, 0)
    val timestamp = Timestamp(Date.from(meetingDate.atZone(ZoneId.systemDefault()).toInstant()))
    val serviceRequest =
        ServiceRequest(
            uid = "test_request",
            title = "Test Request",
            type = Services.PLUMBER,
            description = "",
            userId = "test_customer",
            providerId = testUserId,
            dueDate = Timestamp.now(),
            meetingDate = timestamp,
            location = null,
            status = ServiceRequestStatus.PENDING)

    // Mock service request view model to return empty list
    val emptyFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
    doReturn(emptyFlow).`when`(serviceRequestViewModel).acceptedRequests

    // When
    val result = providerCalendarViewModel.checkServiceRequestConflict(serviceRequest)

    // Then
    assert(result.hasConflict)
    assert(result.reason.contains("conflicts with your off-time schedule"))
  }

  @Test
  fun testCheckServiceRequestWithExistingRequestConflict() = runTest {
    // Given
    val existingMeetingDate = LocalDateTime.of(2024, 1, 1, 10, 0)
    val existingTimestamp =
        Timestamp(Date.from(existingMeetingDate.atZone(ZoneId.systemDefault()).toInstant()))
    val existingRequest =
        ServiceRequest(
            uid = "existing_request",
            title = "Existing Request",
            type = Services.PLUMBER,
            description = "",
            userId = "test_customer",
            providerId = testUserId,
            dueDate = Timestamp.now(),
            meetingDate = existingTimestamp,
            location = null,
            status = ServiceRequestStatus.ACCEPTED)

    // Mock service request view model to return the existing request
    val requestsFlow = MutableStateFlow(listOf(existingRequest))
    doReturn(requestsFlow).`when`(serviceRequestViewModel).acceptedRequests

    val newMeetingDate = LocalDateTime.of(2024, 1, 1, 10, 30) // Overlaps with existing request
    val newTimestamp =
        Timestamp(Date.from(newMeetingDate.atZone(ZoneId.systemDefault()).toInstant()))
    val newRequest =
        ServiceRequest(
            uid = "new_request",
            title = "New Request",
            type = Services.PLUMBER,
            description = "",
            userId = "test_customer",
            providerId = testUserId,
            dueDate = Timestamp.now(),
            meetingDate = newTimestamp,
            location = null,
            status = ServiceRequestStatus.PENDING)

    // When
    val result = providerCalendarViewModel.checkServiceRequestConflict(newRequest)

    // Then
    assert(result.hasConflict)
    assert(result.reason.contains("conflicts with another accepted service request"))
  }

  @Test
  fun testCheckServiceRequestNoMeetingDate() = runTest {
    // Given
    val serviceRequest =
        ServiceRequest(
            uid = "test_request",
            title = "Test Request",
            type = Services.PLUMBER,
            description = "",
            userId = "test_customer",
            providerId = testUserId,
            dueDate = Timestamp.now(),
            meetingDate = null, // No meeting date set
            location = null,
            status = ServiceRequestStatus.PENDING)

    // When
    val result = providerCalendarViewModel.checkServiceRequestConflict(serviceRequest)

    // Then
    assert(result.hasConflict)
    assert(result.reason == "Meeting date not set")
  }

  @Test
  fun testProviderNotLoaded() = runTest {
    // Given
    val testId = "test_user_id"
    val providerRepository = mock<ProviderRepository>()
    val authViewModel = mock<AuthViewModel>()
    val serviceRequestViewModel = mock<ServiceRequestViewModel>()

    // Setup auth view model mock
    val userFlow = MutableStateFlow(User(testId, "test@test.com", "provider"))
    doReturn(userFlow).`when`(authViewModel).user

    // Setup provider repository to return null provider
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(Provider?) -> Unit>(1)
          onSuccess(null)
          null
        }
        .`when`(providerRepository)
        .getProvider(eq(testId), any(), any())

    // Setup service request view model mock
    val acceptedRequestsFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
    doReturn(acceptedRequestsFlow).`when`(serviceRequestViewModel).acceptedRequests

    // Create view model with mocked dependencies
    val calendarViewModel =
        ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)

    // When
    val result =
        calendarViewModel.checkServiceRequestConflict(
            ServiceRequest(
                uid = "test_request",
                title = "Test Request",
                type = Services.PLUMBER,
                description = "",
                userId = "test_customer",
                providerId = testId,
                dueDate = Timestamp.now(),
                meetingDate = Timestamp.now(),
                location = null,
                status = ServiceRequestStatus.PENDING))

    // Then
    assert(result.hasConflict)
    assert(result.reason == "Provider data not loaded")
  }

  @Test
  fun testLoadProviderData() = runTest {
    // Given
    val providerRepository = mock<ProviderRepository>()
    val authViewModel = mock<AuthViewModel>()
    val serviceRequestViewModel = mock<ServiceRequestViewModel>()

    // Setup auth view model mock
    val userFlow = MutableStateFlow(User(testUserId, "test@test.com", "provider"))
    doReturn(userFlow).`when`(authViewModel).user

    // Create view model with mocked dependencies
    val calendarViewModel =
        ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)

    // When - loadProvider() is called automatically in init

    // Then
    verify(providerRepository).getProvider(eq(testUserId), any(), any())
  }
}
