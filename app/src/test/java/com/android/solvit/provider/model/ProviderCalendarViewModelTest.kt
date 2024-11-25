package com.android.solvit.provider.model

import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.*
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ProviderCalendarViewModelTest {
  private val testUserId = "test_user_id"
  private val testLocation = Location(37.7749, -122.4194, "San Francisco")
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
          schedule = Schedule())

  private lateinit var authRepository: AuthRepository
  private lateinit var providerRepository: ProviderRepository
  private lateinit var mockUser: User
  private lateinit var userFlow: MutableStateFlow<User?>
  private lateinit var requestsFlow: MutableStateFlow<List<ServiceRequest>>
  private lateinit var authViewModel: AuthViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var calendarViewModel: ProviderCalendarViewModel

  @Before
  fun setUp() {
    // Set up mocks
    authRepository = mock()
    providerRepository = mock()
    mockUser = mock()
    serviceRequestViewModel = mock()

    // Configure mock user
    whenever(mockUser.uid).thenReturn(testUserId)

    // Set up user flow before initializing AuthViewModel
    userFlow = MutableStateFlow(mockUser)
    whenever(authRepository.init(any())).then {
      val callback = it.getArgument<(User?) -> Unit>(0)
      callback(mockUser)
      Unit
    }

    // Configure auth view model with mock user
    authViewModel = AuthViewModel(authRepository)

    // Configure service request view model
    requestsFlow = MutableStateFlow(emptyList())
    whenever(serviceRequestViewModel.requests)
        .thenReturn(requestsFlow as StateFlow<List<ServiceRequest>>)

    // Configure provider repository mock
    whenever(providerRepository.getProvider(eq(testUserId), any(), any())).then {
      val onSuccess = it.getArgument<(Provider?) -> Unit>(1)
      onSuccess(testProvider)
      Unit
    }

    // Configure successful provider updates
    whenever(providerRepository.updateProvider(any(), any(), any())).then {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
      Unit
    }

    // Initialize view model with mocked dependencies
    calendarViewModel =
        ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)

    // Wait for provider to be loaded by triggering getProvider
    verify(providerRepository, timeout(1000)).getProvider(eq(testUserId), any(), any())
  }

  @Test
  fun testSetRegularHours() {
    // Test setting regular hours
    val timeSlots =
        listOf(
            TimeSlot(LocalTime.of(9, 0), LocalTime.of(12, 0)),
            TimeSlot(LocalTime.of(13, 0), LocalTime.of(17, 0)))

    var successCalled = false
    calendarViewModel.setRegularHours(
        DayOfWeek.MONDAY, timeSlots, onSuccess = { successCalled = true })

    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
    Assert.assertTrue("setRegularHours should call onSuccess", successCalled)

    // Verify the hours were set correctly
    val availableSlots =
        calendarViewModel.getAvailableSlots(
            LocalDateTime.of(2024, 1, 1, 0, 0) // A Monday
            )
    Assert.assertEquals(2, availableSlots.size)
    Assert.assertEquals(LocalTime.of(9, 0), availableSlots[0].start)
    Assert.assertEquals(LocalTime.of(12, 0), availableSlots[0].end)
    Assert.assertEquals(LocalTime.of(13, 0), availableSlots[1].start)
    Assert.assertEquals(LocalTime.of(17, 0), availableSlots[1].end)
  }

  @Test
  fun testAddException() {
    // First set regular hours
    val regularSlots = listOf(TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0)))

    var regularHoursSet = false
    calendarViewModel.setRegularHours(
        DayOfWeek.MONDAY, regularSlots, onSuccess = { regularHoursSet = true })
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
    Assert.assertTrue("Failed to set regular hours", regularHoursSet)

    // Add an exception
    val exceptionDate = LocalDateTime.of(2024, 1, 1, 0, 0) // A Monday
    val exceptionSlots = listOf(TimeSlot(LocalTime.of(10, 0), LocalTime.of(15, 0)))

    var exceptionAdded = false
    calendarViewModel.addException(
        exceptionDate, exceptionSlots, onSuccess = { exceptionAdded = true })

    verify(providerRepository, timeout(1000).times(2)).updateProvider(any(), any(), any())
    Assert.assertTrue("addException should call onSuccess", exceptionAdded)

    // Verify the exception was added
    val availableSlots = calendarViewModel.getAvailableSlots(exceptionDate)
    Assert.assertEquals(1, availableSlots.size)
    Assert.assertEquals(LocalTime.of(10, 0), availableSlots[0].start)
    Assert.assertEquals(LocalTime.of(15, 0), availableSlots[0].end)
  }

  @Test
  fun testRemoveException() {
    // First set regular hours and add an exception
    val regularSlots = listOf(TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0)))

    val exceptionDate = LocalDateTime.of(2024, 1, 1, 0, 0) // A Monday
    val exceptionSlots = listOf(TimeSlot(LocalTime.of(10, 0), LocalTime.of(15, 0)))

    var regularHoursSet = false
    calendarViewModel.setRegularHours(
        DayOfWeek.MONDAY, regularSlots, onSuccess = { regularHoursSet = true })
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
    Assert.assertTrue("Failed to set regular hours", regularHoursSet)

    var exceptionAdded = false
    calendarViewModel.addException(
        exceptionDate, exceptionSlots, onSuccess = { exceptionAdded = true })
    verify(providerRepository, timeout(1000).times(2)).updateProvider(any(), any(), any())
    Assert.assertTrue("addException should call onSuccess", exceptionAdded)

    // Remove the exception
    var successCalled = false
    calendarViewModel.removeException(exceptionDate, onSuccess = { successCalled = true })

    verify(providerRepository, timeout(1000).times(3)).updateProvider(any(), any(), any())
    Assert.assertTrue("removeException should call onSuccess", successCalled)

    // Verify the exception was removed and regular hours are back
    val availableSlots = calendarViewModel.getAvailableSlots(exceptionDate)
    Assert.assertEquals(1, availableSlots.size)
    Assert.assertEquals(LocalTime.of(9, 0), availableSlots[0].start)
    Assert.assertEquals(LocalTime.of(17, 0), availableSlots[0].end)
  }

  @Test
  fun testIsAvailable() {
    // Set up regular hours for Monday
    val regularSlots = listOf(TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0)))

    var regularHoursSet = false
    calendarViewModel.setRegularHours(
        DayOfWeek.MONDAY, regularSlots, onSuccess = { regularHoursSet = true })
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
    Assert.assertTrue("Failed to set regular hours", regularHoursSet)

    // Test availability for a Monday at 10 AM (should be available)
    val mondayAt10 = LocalDateTime.of(2024, 1, 1, 10, 0) // This is a Monday
    Assert.assertTrue(calendarViewModel.isAvailable(mondayAt10))

    // Test availability for a Monday at 18 PM (should not be available)
    val mondayAt18 = LocalDateTime.of(2024, 1, 1, 18, 0)
    Assert.assertFalse(calendarViewModel.isAvailable(mondayAt18))

    // Test availability for a Sunday (should not be available)
    val sundayAt10 = LocalDateTime.of(2024, 1, 7, 10, 0)
    Assert.assertFalse(calendarViewModel.isAvailable(sundayAt10))
  }

  @Test
  fun testGetAvailableSlots() {
    // Set up regular hours for Monday
    val regularSlots =
        listOf(
            TimeSlot(LocalTime.of(9, 0), LocalTime.of(12, 0)),
            TimeSlot(LocalTime.of(13, 0), LocalTime.of(17, 0)))

    var regularHoursSet = false
    calendarViewModel.setRegularHours(
        DayOfWeek.MONDAY, regularSlots, onSuccess = { regularHoursSet = true })
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
    Assert.assertTrue("Failed to set regular hours", regularHoursSet)

    // Test getting available slots for a Monday
    val monday = LocalDateTime.of(2024, 1, 1, 0, 0)
    val availableSlots = calendarViewModel.getAvailableSlots(monday)

    Assert.assertEquals(2, availableSlots.size)
    Assert.assertEquals(LocalTime.of(9, 0), availableSlots[0].start)
    Assert.assertEquals(LocalTime.of(12, 0), availableSlots[0].end)
    Assert.assertEquals(LocalTime.of(13, 0), availableSlots[1].start)
    Assert.assertEquals(LocalTime.of(17, 0), availableSlots[1].end)

    // Test getting available slots for a Sunday (should be empty)
    val sunday = LocalDateTime.of(2024, 1, 7, 0, 0)
    val sundaySlots = calendarViewModel.getAvailableSlots(sunday)
    Assert.assertTrue(sundaySlots.isEmpty())
  }

  @Test
  fun testSetRegularHoursWithDefaultCallbacks() {
    val timeSlots = listOf(TimeSlot(9, 0, 17, 0))
    calendarViewModel.setRegularHours(DayOfWeek.MONDAY, timeSlots)
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
  }

  @Test
  fun testAddExceptionWithDefaultCallbacks() {
    val timeSlots = listOf(TimeSlot(9, 0, 17, 0))
    val date = LocalDateTime.now()
    calendarViewModel.addException(date, timeSlots)
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
  }

  @Test
  fun testRemoveExceptionWithDefaultCallbacks() {
    val date = LocalDateTime.now()
    calendarViewModel.removeException(date)
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
  }

  @Test
  fun testGetServiceRequests() {
    // Verify that getServiceRequests is called and returns the flow
    verify(serviceRequestViewModel).requests

    // Test that the flow is accessible
    val requests = calendarViewModel.getServiceRequests()
    assertNotNull(requests)
  }

  @Test
  fun testLoadProviderNoUserLoggedIn() {
    // Create new auth view model with null user
    val nullAuthRepo = mock<AuthRepository>()
    val nullUser = mock<User>()
    whenever(nullUser.uid).thenReturn(null)
    whenever(nullAuthRepo.init(any())).then {
      val callback = it.getArgument<(User?) -> Unit>(0)
      callback(nullUser)
      Unit
    }
    val nullAuthViewModel = AuthViewModel(nullAuthRepo)

    // Create new provider repository for this test
    val testProviderRepo = mock<ProviderRepository>()

    // Create new view model with null user auth
    val newViewModel =
        ProviderCalendarViewModel(testProviderRepo, nullAuthViewModel, serviceRequestViewModel)

    // Verify getProvider is not called when no user is logged in
    verify(testProviderRepo, never()).getProvider(any(), any(), any())
  }

  @Test
  fun testLoadProviderFailure() {
    // Create new provider repository for this test
    val testProviderRepo = mock<ProviderRepository>()

    // Configure provider repository to fail
    whenever(testProviderRepo.getProvider(eq(testUserId), any(), any())).then {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(Exception("Test error"))
      Unit
    }

    // Create new view model to trigger load
    val newViewModel =
        ProviderCalendarViewModel(testProviderRepo, authViewModel, serviceRequestViewModel)

    // Verify getProvider was called with failure callback
    verify(testProviderRepo, timeout(1000)).getProvider(eq(testUserId), any(), any())
  }

  @Test
  fun testLoadProviderNullProvider() {
    // Create new provider repository for this test
    val testProviderRepo = mock<ProviderRepository>()

    // Configure provider repository to return null provider
    whenever(testProviderRepo.getProvider(eq(testUserId), any(), any())).then {
      val onSuccess = it.getArgument<(Provider?) -> Unit>(1)
      onSuccess(null)
      Unit
    }

    // Create new view model to trigger load
    val newViewModel =
        ProviderCalendarViewModel(testProviderRepo, authViewModel, serviceRequestViewModel)

    // Verify getProvider was called
    verify(testProviderRepo, timeout(1000)).getProvider(eq(testUserId), any(), any())
  }

  @Test
  fun testUpdateProviderScheduleFailure() {
    // Configure provider repository to fail on update
    whenever(providerRepository.updateProvider(any(), any(), any())).then {
      val onFailure = it.getArgument<(Exception) -> Unit>(2)
      onFailure(Exception("Test error"))
      Unit
    }

    var errorMessage: String? = null
    calendarViewModel.setRegularHours(
        DayOfWeek.MONDAY, listOf(TimeSlot(9, 0, 17, 0)), onError = { errorMessage = it })

    // Verify error callback was called with correct message
    verify(providerRepository, timeout(1000)).updateProvider(any(), any(), any())
    assertNotNull(errorMessage)
    assertTrue(errorMessage!!.contains("Failed to update schedule"))
  }
}
