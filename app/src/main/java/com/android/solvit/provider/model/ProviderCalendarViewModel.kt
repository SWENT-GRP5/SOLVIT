package com.android.solvit.provider.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.ExceptionType
import com.android.solvit.shared.model.provider.ExceptionUpdateResult
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.provider.ScheduleException
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

  private lateinit var currentProvider: Provider

  /** Initializes the view model by loading the current provider and service requests. */
  init {
    loadProvider()
    serviceRequestViewModel.getServiceRequests()
  }

  /**
   * Loads the current provider from the repository. Handles cases where the user is not logged in
   * or the provider is not found.
   */
  private fun loadProvider() {
    val currentUserId =
        authViewModel.user.value?.uid
            ?: run {
              Log.e("ProviderCalendarViewModel", "No user logged in")
              return
            }

    providerRepository.getProvider(
        currentUserId,
        onSuccess = { provider ->
          if (provider != null) {
            currentProvider = provider
          } else {
            Log.e("ProviderCalendarViewModel", "Failed to find provider")
          }
        },
        onFailure = { e -> Log.e("ProviderCalendarViewModel", "Failed to load provider", e) })
  }

  /**
   * Flow of service requests assigned to the current provider. Filters requests with a meeting date
   * and assigned to the current provider.
   */
  val serviceRequests: Flow<List<ServiceRequest>> =
      serviceRequestViewModel.requests.map { requests ->
        requests.filter { request ->
          try {
            // Filter requests that have a meeting date and are assigned to this provider
            request.meetingDate != null && request.providerId == currentProvider.uid
          } catch (e: Exception) {
            Log.e("ProviderCalendarViewModel", "Error processing service request", e)
            false
          }
        }
      }

  /**
   * Checks if the provider is available at a specific date and time.
   *
   * @param dateTime Date and time to check availability
   * @return True if available, false otherwise
   */
  fun isAvailable(dateTime: LocalDateTime): Boolean {
    return currentProvider.schedule.isAvailable(dateTime)
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
      currentProvider.schedule.setRegularHours(dayOfWeek, timeSlots)
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
      currentProvider.schedule.clearRegularHours(dayOfWeek)
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
      val result = currentProvider.schedule.addException(date, timeSlots, ExceptionType.OFF_TIME)
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
      val result = currentProvider.schedule.addException(date, timeSlots, ExceptionType.EXTRA_TIME)
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
      val result = currentProvider.schedule.updateException(date, timeSlots, type)
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
    return currentProvider.schedule.getExceptions(startDate, endDate, type)
  }

  /**
   * Generates a user-friendly feedback message for schedule updates. Includes information about
   * merged exceptions if applicable.
   *
   * @param result Result of the exception update operation
   * @return Formatted feedback message
   */
  private fun buildFeedbackMessage(result: ExceptionUpdateResult): String {
    return if (result.mergedWith.isEmpty()) {
      "Exception added successfully"
    } else {
      "Exception merged with existing ${result.exception.type.toString().lowercase().replace('_', ' ')} exception"
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
        currentProvider,
        onSuccess = { onComplete(true) },
        onFailure = { e ->
          Log.e("ProviderCalendarViewModel", "Failed to update provider schedule", e)
          onComplete(false)
        })
  }

  /**
   * Validates a list of time slots. Ensures that each slot's start time is before its end time.
   *
   * @param timeSlots List of time slots to validate
   * @return List of valid time slots
   */
  private fun validateTimeSlots(timeSlots: List<TimeSlot>): List<TimeSlot> {
    val validSlots = timeSlots.filter { it.start < it.end }
    if (validSlots.isEmpty() && timeSlots.isNotEmpty()) {
      throw IllegalArgumentException(
          "All time slots are invalid. End time must be after start time.")
    }
    return validSlots
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

      val result = currentProvider.schedule.addException(date, timeSlots, type)
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

  companion object {
    /** Factory for creating instances of ProviderCalendarViewModel. */
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProviderCalendarViewModel(
                ProviderRepositoryFirestore(Firebase.firestore, Firebase.storage),
                AuthViewModel.Factory.create(AuthViewModel::class.java),
                ServiceRequestViewModel.Factory.create(ServiceRequestViewModel::class.java))
                as T
          }
        }
  }
}
