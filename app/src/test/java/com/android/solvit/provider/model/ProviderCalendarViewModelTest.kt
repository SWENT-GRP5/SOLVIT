package com.android.solvit.provider.model

import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.*
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.DayOfWeek
import java.time.LocalDateTime
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
                      LocalDateTime.of(2024, 1, 1, 0, 0), // First Monday of 2024
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
  fun setUp() {
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
}
