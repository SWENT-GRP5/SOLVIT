package com.android.solvit.provider.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.ExceptionType
import com.android.solvit.shared.model.provider.ExceptionUpdateResult
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.provider.ScheduleException
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepositoryFirebase
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class CalendarView {
  DAY,
  WEEK,
  MONTH
}

/**
 * ViewModel responsible for managing provider calendar operations and schedule management. Handles
 * regular working hours, exceptions (off-time and extra work time), and schedule updates.
 *
 * @property providerRepository Repository for provider data operations
 * @property authViewModel Authentication view model for user verification
 * @property serviceRequestViewModel View model for service request management
 */
class ProviderCalendarViewModel(
    private val providerRepository: ProviderRepository,
    private val authViewModel: AuthViewModel,
    private val serviceRequestViewModel: ServiceRequestViewModel
) : ViewModel() {

  private val _currentViewDate = MutableStateFlow(LocalDate.now())
  val currentViewDate: StateFlow<LocalDate> = _currentViewDate.asStateFlow()

  private val _selectedDate = MutableStateFlow(LocalDate.now())
  val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

  private val _calendarView = MutableStateFlow(CalendarView.WEEK)
  val calendarView: StateFlow<CalendarView> = _calendarView.asStateFlow()

  private val _timeSlots = MutableStateFlow<Map<LocalDate, List<ServiceRequest>>>(emptyMap())
  val timeSlots: StateFlow<Map<LocalDate, List<ServiceRequest>>> = _timeSlots.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _currentProvider = MutableStateFlow(Provider())
  val currentProvider: StateFlow<Provider> = _currentProvider.asStateFlow()

  /** Initializes the view model by loading the current provider and service requests. */
  init {
    // Initialize service requests
    serviceRequestViewModel.getServiceRequests()

    // Observe changes in service requests, user, calendar view, and current view date
    viewModelScope.launch {
      combine(
              serviceRequestViewModel.requests,
              authViewModel.user,
              _calendarView,
              _currentViewDate) { requests, user, view, date ->
                Pair(requests, user)
              }
          .collect { (requests, user) ->
            _isLoading.value = true
            try {
              val userId = user!!.uid // User is guaranteed to be non-null

              providerRepository.getProvider(
                  userId,
                  onSuccess = { provider ->
                    if (provider != null) {
                      _currentProvider.value = provider
                    } else {
                      Log.e("ProviderCalendarViewModel", "Failed to find provider")
                    }
                  },
                  onFailure = { e ->
                    Log.e("ProviderCalendarViewModel", "Failed to load provider", e)
                  })

              val startDate =
                  when (_calendarView.value) {
                    CalendarView.MONTH -> _currentViewDate.value.withDayOfMonth(1)
                    CalendarView.WEEK ->
                        _currentViewDate.value.with(
                            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    CalendarView.DAY -> _currentViewDate.value
                  }
              val endDate =
                  when (_calendarView.value) {
                    CalendarView.MONTH -> startDate.plusMonths(1).minusDays(1)
                    CalendarView.WEEK -> startDate.plusWeeks(1).minusDays(1)
                    CalendarView.DAY -> startDate
                  }

              // Filter and group requests
              val filteredRequests =
                  requests
                      .filter { it.providerId == userId }
                      .filter { request ->
                        val requestDate =
                            request.meetingDate
                                ?.toInstant()
                                ?.atZone(ZoneId.systemDefault())
                                ?.toLocalDate() ?: return@filter false
                        !requestDate.isBefore(startDate) && !requestDate.isAfter(endDate)
                      }
              // Group by date and sort by time
              val groupedRequests =
                  filteredRequests
                      .groupBy {
                        it.meetingDate!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                      }
                      .mapValues { (_, requests) ->
                        requests.sortedBy { it.meetingDate!!.toInstant() }
                      }

              _timeSlots.value = groupedRequests
            } catch (e: Exception) {
              Log.e("ProviderCalendarViewModel", "Error processing service requests", e)
            } finally {
              _isLoading.value = false
            }
          }
    }
  }

  fun onDateSelected(date: LocalDate) {
    _selectedDate.value = date
    _currentViewDate.value = date
  }

  fun onViewDateChanged(date: LocalDate) {
    _currentViewDate.value = date
  }

  fun onCalendarViewChanged(view: CalendarView) {
    _calendarView.value = view
  }

  fun onServiceRequestClick(request: ServiceRequest) {
    serviceRequestViewModel.selectRequest(request)
  }

  /**
   * Checks if the provider is available at a specific date and time.
   *
   * @param dateTime Date and time to check availability
   * @return True if available, false otherwise
   */
  fun isAvailable(dateTime: LocalDateTime): Boolean {
    return currentProvider.value.schedule.isAvailable(dateTime)
  }

  /**
   * Checks if the provider is available for a one-hour appointment starting at the given time.
   *
   * @param startTime Start time of the proposed appointment
   * @return True if available for the full hour, false otherwise
   */
  fun isAvailableForOneHour(startTime: LocalDateTime): Boolean {
    return currentProvider.value.schedule.isAvailableForOneHour(startTime)
  }

  /**
   * Sets regular working hours for a specific day of the week. Validates and updates the time
   * slots, then persists changes to the repository.
   *
   * @param dayOfWeek The day to set hours for (e.g., "MONDAY")
   * @param timeSlots List of time slots representing working hours
   * @param onComplete Callback with success status
   */
  fun setRegularHours(dayOfWeek: String, timeSlots: List<TimeSlot>, onComplete: (Boolean) -> Unit) {
    try {
      currentProvider.value.schedule.setRegularHours(dayOfWeek, timeSlots)
      updateProviderSchedule(onComplete)
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to set regular hours", e)
      onComplete(false)
    }
  }

  /**
   * Clears all regular working hours for a specific day.
   *
   * @param dayOfWeek The day to clear hours for
   * @param onComplete Callback with success status
   */
  fun clearRegularHours(dayOfWeek: String, onComplete: (Boolean) -> Unit) {
    try {
      currentProvider.value.schedule.clearRegularHours(dayOfWeek)
      updateProviderSchedule(onComplete)
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to clear regular hours", e)
      onComplete(false)
    }
  }

  /**
   * Adds an off-time exception to the schedule. Off-time exceptions represent periods when the
   * provider is unavailable during regular hours.
   *
   * @param date The date for the exception
   * @param timeSlots List of time slots representing off-time periods
   * @param onComplete Callback with success status and feedback message
   */
  fun addOffTimeException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      onComplete: (Boolean, String) -> Unit
  ) {
    try {
      val result =
          currentProvider.value.schedule.addException(date, timeSlots, ExceptionType.OFF_TIME)
      val feedback = buildFeedbackMessage(result)
      updateProviderSchedule { success ->
        onComplete(success, if (success) feedback else "Failed to update schedule")
      }
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to add off-time exception", e)
      onComplete(false, e.message ?: "Failed to add off-time exception")
    }
  }

  /**
   * Adds an extra work time exception to the schedule. Extra time exceptions represent periods when
   * the provider is available outside regular hours.
   *
   * @param date The date for the exception
   * @param timeSlots List of time slots representing extra work periods
   * @param onComplete Callback with success status and feedback message
   */
  fun addExtraTimeException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      onComplete: (Boolean, String) -> Unit
  ) {
    try {
      val result =
          currentProvider.value.schedule.addException(date, timeSlots, ExceptionType.EXTRA_TIME)
      val feedback = buildFeedbackMessage(result)
      updateProviderSchedule { success ->
        onComplete(success, if (success) feedback else "Failed to update schedule")
      }
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to add extra time exception", e)
      onComplete(false, e.message ?: "Failed to add extra time exception")
    }
  }

  /**
   * Updates an existing schedule exception. Can modify both time slots and exception type.
   *
   * @param date The date of the exception to update
   * @param timeSlots New list of time slots
   * @param type New exception type
   * @param onComplete Callback with success status and feedback message
   */
  fun updateException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      type: ExceptionType,
      onComplete: (Boolean, String) -> Unit
  ) {
    try {
      val result = currentProvider.value.schedule.updateException(date, timeSlots, type)
      val feedback = buildFeedbackMessage(result)
      updateProviderSchedule { success ->
        onComplete(success, if (success) feedback else "Failed to update schedule")
      }
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to update exception", e)
      onComplete(false, e.message ?: "Failed to update exception")
    }
  }

  /**
   * Retrieves schedule exceptions within a specified date range. Optionally filters by exception
   * type.
   *
   * @param startDate Start of the date range
   * @param endDate End of the date range
   * @param type Optional filter for exception type
   * @return List of schedule exceptions within the range
   */
  fun getExceptions(
      startDate: LocalDateTime,
      endDate: LocalDateTime,
      type: ExceptionType? = null
  ): List<ScheduleException> {
    return currentProvider.value.schedule.getExceptions(startDate, endDate, type)
  }

  /**
   * Generates a user-friendly feedback message for schedule updates. Includes information about
   * merged exceptions if applicable.
   *
   * @param result Result of the exception update operation
   * @return Formatted feedback message
   */
  private fun buildFeedbackMessage(result: ExceptionUpdateResult): String {
    return when {
      result.isUpdate -> "Exception updated successfully"
      result.mergedWith.isEmpty() -> "Exception added successfully"
      else -> "Exception added and merged"
    }
  }

  /**
   * Updates the provider's schedule in the repository. Handles success and failure cases with
   * appropriate logging.
   *
   * @param onComplete Callback with success status
   */
  private fun updateProviderSchedule(onComplete: (Boolean) -> Unit) {
    providerRepository.updateProvider(
        currentProvider.value,
        onSuccess = { onComplete(true) },
        onFailure = { e ->
          Log.e("ProviderCalendarViewModel", "Failed to update provider schedule", e)
          onComplete(false)
        })
  }

  /**
   * Adds a new schedule exception with validation and merging. Provides detailed feedback on the
   * operation result.
   *
   * @param date The date for the new exception
   * @param timeSlots List of time slots
   * @param type Type of exception (OFF_TIME or EXTRA_TIME)
   * @param onComplete Callback with success status and feedback message
   */
  fun addException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      type: ExceptionType,
      onComplete: (Boolean, String) -> Unit
  ) {
    try {
      if (timeSlots.isEmpty()) {
        onComplete(false, "No time slots provided")
        return
      }

      val result = currentProvider.value.schedule.addException(date, timeSlots, type)
      updateProviderSchedule { success ->
        val feedback = if (success) buildFeedbackMessage(result) else "Failed to update schedule"
        onComplete(success, feedback)
      }
    } catch (e: IllegalArgumentException) {
      Log.e("ProviderCalendarViewModel", "Invalid time slots", e)
      onComplete(false, "Invalid time slots: ${e.message}")
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to add exception", e)
      onComplete(false, e.message ?: "Failed to add exception")
    }
  }

  /**
   * Checks if a service request conflicts with the provider's schedule. A conflict occurs when:
   * 1. The request is outside regular working hours and no EXTRA_TIME exception exists
   * 2. The request overlaps with an OFF_TIME exception
   * 3. The request overlaps with another accepted service request
   *
   * @param serviceRequest The service request to check
   * @return ConflictResult containing whether there's a conflict and the reason
   */
  fun checkServiceRequestConflict(serviceRequest: ServiceRequest): ConflictResult {
    val meetingTimestamp =
        serviceRequest.meetingDate ?: return ConflictResult(true, "Meeting date not set")
    val meetingDate =
        LocalDateTime.ofInstant(meetingTimestamp.toDate().toInstant(), ZoneId.systemDefault())

    // Create a TimeSlot for the 1-hour service duration
    val timeSlot =
        TimeSlot(
            meetingDate.hour,
            meetingDate.minute,
            meetingDate.plusHours(1).hour,
            meetingDate.plusHours(1).minute)

    val dayOfWeek = meetingDate.dayOfWeek.name

    println("Checking conflict for date: $meetingDate ($dayOfWeek)")
    println("Time slot: $timeSlot")

    // Check if it's during regular hours
    val regularHours = currentProvider.value.schedule.regularHours[dayOfWeek] ?: emptyList()
    println("Regular hours for $dayOfWeek: $regularHours")

    val duringRegularHours =
        regularHours.any { slot -> timeSlot.start >= slot.start && timeSlot.end <= slot.end }
    println("During regular hours: $duringRegularHours")

    // Check exceptions
    val exceptions =
        currentProvider.value.schedule.exceptions.filter {
          it.date.toLocalDate() == meetingDate.toLocalDate()
        }
    println("Relevant exceptions: $exceptions")

    val hasOffTimeConflict =
        exceptions
            .filter { it.type == ExceptionType.OFF_TIME }
            .any { exception -> exception.timeSlots.any { it.overlaps(timeSlot) } }
    println("Has off-time conflict: $hasOffTimeConflict")

    // If outside regular hours, check for EXTRA_TIME exception
    val hasExtraTimeException =
        if (!duringRegularHours) {
          exceptions
              .filter { it.type == ExceptionType.EXTRA_TIME }
              .any { exception ->
                exception.timeSlots.any { slot ->
                  timeSlot.start >= slot.start && timeSlot.end <= slot.end
                }
              }
        } else true // During regular hours, so no need for EXTRA_TIME
    println("Has extra time exception: $hasExtraTimeException")

    // Check other service requests
    val acceptedRequests = serviceRequestViewModel.acceptedRequests.value
    println("Accepted requests: $acceptedRequests")

    val hasServiceRequestConflict =
        acceptedRequests
            .filter { request ->
              request.meetingDate?.let { timestamp ->
                LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault())
                    .toLocalDate() == meetingDate.toLocalDate()
              } ?: false
            }
            .mapNotNull { request ->
              request.meetingDate?.let { timestamp ->
                val date =
                    LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault())
                TimeSlot(date.hour, date.minute, date.plusHours(1).hour, date.plusHours(1).minute)
              }
            }
            .any { it.overlaps(timeSlot) }
    println("Has service request conflict: $hasServiceRequestConflict")

    return when {
      hasOffTimeConflict ->
          ConflictResult(true, "This time slot conflicts with your off-time schedule")
      hasServiceRequestConflict ->
          ConflictResult(true, "This time slot conflicts with another accepted service request")
      !duringRegularHours && !hasExtraTimeException ->
          ConflictResult(true, "This time slot is outside your regular working hours")
      else -> ConflictResult(false, "No conflicts")
    }
  }

  /**
   * Result of checking a service request for conflicts
   *
   * @param hasConflict Whether there is a conflict
   * @param reason Description of the conflict or confirmation message
   */
  data class ConflictResult(val hasConflict: Boolean, val reason: String)

  companion object Factory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      // Creates dependencies inline during ViewModel instantiation
      val providerRepository =
          ProviderRepositoryFirestore(Firebase.firestore, FirebaseStorage.getInstance())
      val authViewModel = AuthViewModel(AuthRepository(Firebase.auth, Firebase.firestore))
      val serviceRequestViewModel =
          ServiceRequestViewModel(
              ServiceRequestRepositoryFirebase(Firebase.firestore, Firebase.storage))

      return ProviderCalendarViewModel(providerRepository, authViewModel, serviceRequestViewModel)
          as T
    }
  }
}
