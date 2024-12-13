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
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.*
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
open class ProviderCalendarViewModel(
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
              if (user == null) {
                _timeSlots.value = emptyMap()
                _currentProvider.value = Provider()
                return@collect
              }

              // Only fetch service requests when we have a valid user
              serviceRequestViewModel.getServiceRequests()

              // Set up real-time listener for provider updates
              providerRepository.addListenerOnProviders(
                  onSuccess = { providers ->
                    val currentProvider = providers.find { it.uid == user.uid }
                    if (currentProvider != null) {
                      _currentProvider.value = currentProvider

                      // Only process requests after provider is loaded
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
                              .filter { it.providerId == user.uid }
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
                                it.meetingDate!!
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                              }
                              .mapValues { (_, requests) ->
                                requests.sortedBy { it.meetingDate!!.toInstant() }
                              }

                      _timeSlots.value = groupedRequests
                    } else {
                      Log.e("ProviderCalendarViewModel", "Failed to find provider")
                      _timeSlots.value = emptyMap()
                    }
                  },
                  onFailure = { e ->
                    Log.e("ProviderCalendarViewModel", "Failed to load provider", e)
                    _timeSlots.value = emptyMap()
                  })
            } catch (e: Exception) {
              Log.e("ProviderCalendarViewModel", "Error processing service requests", e)
              _timeSlots.value = emptyMap()
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
    viewModelScope.launch {
      try {
        val currentProvider = _currentProvider.value
        if (currentProvider.uid.isEmpty()) {
          onComplete(false)
          return@launch
        }

        // Update provider schedule
        val updatedProvider =
            currentProvider.copy(
                schedule =
                    currentProvider.schedule.copy(
                        regularHours =
                            currentProvider.schedule.regularHours.toMutableMap().apply {
                              put(dayOfWeek, timeSlots.toMutableList())
                            }))

        // Save to repository
        providerRepository.updateProvider(
            provider = updatedProvider,
            onSuccess = {
              _currentProvider.value = updatedProvider
              onComplete(true)
            },
            onFailure = { e ->
              Log.e("ProviderCalendarViewModel", "Error setting regular hours", e)
              onComplete(false)
            })
      } catch (e: IllegalArgumentException) {
        Log.e("ProviderCalendarViewModel", "Invalid time range", e)
        onComplete(false)
      } catch (e: Exception) {
        Log.e("ProviderCalendarViewModel", "Error setting regular hours", e)
        onComplete(false)
      }
    }
  }

  /**
   * Clears all regular working hours for a specific day.
   *
   * @param dayOfWeek The day to clear hours for
   * @param onComplete Callback with success status
   */
  fun clearRegularHours(dayOfWeek: String, onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      try {
        val currentProvider = _currentProvider.value
        if (currentProvider.uid.isEmpty()) {
          onComplete(false)
          return@launch
        }

        // Update provider schedule
        val updatedProvider =
            currentProvider.copy(
                schedule =
                    currentProvider.schedule.copy(
                        regularHours =
                            currentProvider.schedule.regularHours.toMutableMap().apply {
                              remove(dayOfWeek)
                            }))

        // Save to repository
        providerRepository.updateProvider(
            provider = updatedProvider,
            onSuccess = {
              _currentProvider.value = updatedProvider
              onComplete(true)
            },
            onFailure = { e ->
              Log.e("ProviderCalendarViewModel", "Error clearing regular hours", e)
              onComplete(false)
            })
      } catch (e: Exception) {
        Log.e("ProviderCalendarViewModel", "Error clearing regular hours", e)
        onComplete(false)
      }
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
   * Deletes an exception from the schedule.
   *
   * @param date The date of the exception to delete
   * @param onComplete Callback with success status and feedback message
   */
  fun deleteException(date: LocalDateTime, onComplete: (Boolean, String) -> Unit) {
    try {
      val exceptions = currentProvider.value.schedule.exceptions
      exceptions
          .find { it.date.toLocalDate() == date.toLocalDate() }
          ?.let { exception ->
            exceptions.remove(exception)
            updateProviderSchedule { success ->
              onComplete(
                  success,
                  if (success) "Exception deleted successfully" else "Failed to delete exception")
            }
          } ?: run { onComplete(false, "Exception not found") }
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to delete exception", e)
      onComplete(false, e.message ?: "Failed to delete exception")
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
      result.isUpdate -> "Schedule exception updated successfully"
      result.mergedWith.isEmpty() -> "Schedule exception added successfully"
      else -> {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val mergedDates =
            result.mergedWith.joinToString(", ") { exception -> formatter.format(exception.date) }
        "Schedule exception added and merged with existing exceptions on: $mergedDates"
      }
    }
  }

  /**
   * Updates the provider's schedule in the repository. Handles success and failure cases with
   * appropriate logging.
   *
   * @param onComplete Callback with success status
   */
  private fun updateProviderSchedule(onComplete: (Boolean) -> Unit) {
    viewModelScope.launch {
      try {
        // Create a new provider instance with a deep copy of the schedule
        val updatedProvider =
            currentProvider.value.copy(
                schedule =
                    currentProvider.value.schedule.copy(
                        regularHours =
                            currentProvider.value.schedule.regularHours
                                .mapValues { it.value.toMutableList() }
                                .toMutableMap(),
                        exceptions = currentProvider.value.schedule.exceptions.toMutableList()))

        // Update the state immediately to reflect changes in UI
        _currentProvider.value = updatedProvider

        // Then update Firestore
        providerRepository.updateProvider(
            provider = updatedProvider,
            onSuccess = { onComplete(true) },
            onFailure = { e ->
              // Revert the state if Firestore update fails
              _currentProvider.value = currentProvider.value
              Log.e("ProviderCalendarViewModel", "Failed to update provider schedule", e)
              onComplete(false)
            })
      } catch (e: Exception) {
        Log.e("ProviderCalendarViewModel", "Failed to update provider schedule", e)
        onComplete(false)
      }
    }
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

    val schedule = currentProvider.value.schedule

    // Check for off-time conflicts first
    val startOfDay = meetingDate.toLocalDate().atStartOfDay()
    val endOfDay = startOfDay.plusDays(1).minusNanos(1)
    val offTimeExceptions = schedule.getExceptions(startOfDay, endOfDay, ExceptionType.OFF_TIME)
    println("Checking off-time conflicts:")
    println("- Meeting date: $meetingDate")
    println("- Off-time exceptions: $offTimeExceptions")

    val requestSlot = TimeSlot(meetingDate.toLocalTime(), meetingDate.plusHours(1).toLocalTime())
    println("- Request slot: $requestSlot")

    val hasOffTimeConflict =
        offTimeExceptions.any { exception ->
          exception.timeSlots.any { slot ->
            val offTimeSlot =
                TimeSlot(
                    LocalTime.of(slot.startHour, slot.startMinute),
                    LocalTime.of(slot.endHour, slot.endMinute))
            println("  - Checking against off-time slot: $offTimeSlot")
            // Check if the request slot overlaps with the off-time slot
            val overlaps =
                !(requestSlot.end <= offTimeSlot.start || requestSlot.start >= offTimeSlot.end)
            println("  - Overlaps: $overlaps")
            overlaps
          }
        }
    if (hasOffTimeConflict) {
      println("Found off-time conflict")
      return ConflictResult(true, "This time slot conflicts with your off-time schedule")
    }

    // Check if it's outside regular hours
    val regularHours = schedule.regularHours[meetingDate.dayOfWeek.name] ?: emptyList()
    println("\nChecking regular hours:")
    println("- Day of week: ${meetingDate.dayOfWeek}")
    println("- Regular hours: $regularHours")
    println("- Request slot: $requestSlot")
    val requestSlot2 = TimeSlot(meetingDate.toLocalTime(), meetingDate.plusHours(1).toLocalTime())
    val duringRegularHours =
        regularHours.any { slot ->
          val slotStart = LocalTime.of(slot.startHour, slot.startMinute)
          val slotEnd = LocalTime.of(slot.endHour, slot.endMinute)
          requestSlot2.start >= slotStart && requestSlot2.end <= slotEnd
        }

    if (!duringRegularHours) {
      // Check for extra time exceptions
      val extraTimeExceptions =
          schedule.getExceptions(meetingDate, meetingDate, ExceptionType.EXTRA_TIME)
      val hasExtraTime =
          extraTimeExceptions.any { exception ->
            exception.timeSlots.any { slot ->
              val slotStart = LocalTime.of(slot.startHour, slot.startMinute)
              val slotEnd = LocalTime.of(slot.endHour, slot.endMinute)
              requestSlot2.start >= slotStart && requestSlot2.end <= slotEnd
            }
          }
      if (!hasExtraTime) {
        return ConflictResult(true, "This time slot is outside your regular working hours")
      }
    }

    // Check for conflicts with other service requests
    val acceptedRequests = serviceRequestViewModel.acceptedRequests.value
    val hasServiceRequestConflict =
        acceptedRequests
            .filter { request ->
              request.uid != serviceRequest.uid && // Don't conflict with self
                  request.meetingDate?.let { timestamp ->
                    LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault())
                        .toLocalDate() == meetingDate.toLocalDate()
                  } ?: false
            }
            .any { request ->
              request.meetingDate?.let { timestamp ->
                val otherDate =
                    LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.systemDefault())
                val requestSlot3 =
                    TimeSlot(meetingDate.toLocalTime(), meetingDate.plusHours(1).toLocalTime())
                val otherSlot =
                    TimeSlot(otherDate.toLocalTime(), otherDate.plusHours(1).toLocalTime())
                requestSlot3.overlaps(otherSlot)
              } ?: false
            }

    return if (hasServiceRequestConflict) {
      ConflictResult(true, "This time slot conflicts with another accepted service request")
    } else {
      ConflictResult(false, "No conflicts")
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
