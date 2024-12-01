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

class ProviderCalendarViewModel(
    private val providerRepository: ProviderRepository,
    private val authViewModel: AuthViewModel,
    private val serviceRequestViewModel: ServiceRequestViewModel
) : ViewModel() {

  private lateinit var currentProvider: Provider

  init {
    loadProvider()
    serviceRequestViewModel.getServiceRequests()
  }

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

  fun isAvailable(dateTime: LocalDateTime): Boolean {
    return currentProvider.schedule.isAvailable(dateTime)
  }

  /** Sets regular working hours for a specific day */
  fun setRegularHours(dayOfWeek: String, timeSlots: List<TimeSlot>, onComplete: (Boolean) -> Unit) {
    try {
      currentProvider.schedule.setRegularHours(dayOfWeek, timeSlots)
      updateProviderSchedule(onComplete)
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to set regular hours", e)
      onComplete(false)
    }
  }

  /** Clears regular working hours for a specific day */
  fun clearRegularHours(dayOfWeek: String, onComplete: (Boolean) -> Unit) {
    try {
      currentProvider.schedule.clearRegularHours(dayOfWeek)
      updateProviderSchedule(onComplete)
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to clear regular hours", e)
      onComplete(false)
    }
  }

  /** Adds an off-time exception */
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

  /** Adds an extra work time exception */
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

  /** Updates an existing exception */
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

  /** Gets all exceptions within a date range */
  fun getExceptions(
      startDate: LocalDateTime,
      endDate: LocalDateTime,
      type: ExceptionType? = null
  ): List<ScheduleException> {
    return currentProvider.schedule.getExceptions(startDate, endDate, type)
  }

  /** Builds a user-friendly feedback message for schedule updates */
  private fun buildFeedbackMessage(result: ExceptionUpdateResult): String {
    return if (result.mergedWith.isEmpty()) {
      "Schedule updated successfully"
    } else {
      val date = result.exception.date.toLocalDate()
      val type =
          when (result.exception.type) {
            ExceptionType.OFF_TIME -> "off-time"
            ExceptionType.EXTRA_TIME -> "extra work time"
          }
      "Merged with existing $type exception(s) on $date"
    }
  }

  /** Updates the provider's schedule in the repository */
  private fun updateProviderSchedule(onComplete: (Boolean) -> Unit) {
    providerRepository.updateProvider(
        currentProvider,
        onSuccess = { onComplete(true) },
        onFailure = { e ->
          Log.e("ProviderCalendarViewModel", "Failed to update provider schedule", e)
          onComplete(false)
        })
  }

  private fun validateTimeSlots(timeSlots: List<TimeSlot>): List<TimeSlot> {
    return timeSlots.filter { it.start < it.end }
  }

  private fun mergeExceptions(
      existingExceptions: MutableList<ScheduleException>,
      newException: ScheduleException
  ) {
    val matchingIndex =
        existingExceptions.indexOfFirst {
          it.date.toLocalDate() == newException.date.toLocalDate() && it.type == newException.type
        }

    if (matchingIndex != -1) {
      // Get all time slots
      val allSlots =
          (existingExceptions[matchingIndex].timeSlots + newException.timeSlots).sortedBy {
            it.start
          }

      // Merge overlapping slots
      val mergedSlots = mutableListOf<TimeSlot>()
      if (allSlots.isNotEmpty()) {
        var currentSlot = allSlots[0]
        for (i in 1 until allSlots.size) {
          val nextSlot = allSlots[i]
          if (currentSlot.overlaps(nextSlot)) {
            currentSlot = currentSlot.merge(nextSlot)
          } else {
            mergedSlots.add(currentSlot)
            currentSlot = nextSlot
          }
        }
        mergedSlots.add(currentSlot)
      }

      // Create new exception with merged slots
      existingExceptions[matchingIndex] =
          ScheduleException(
              existingExceptions[matchingIndex].date,
              mergedSlots,
              existingExceptions[matchingIndex].type)
    } else {
      existingExceptions.add(newException)
    }
  }

  fun addException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      type: ExceptionType,
      onComplete: (Boolean, String) -> Unit
  ) {
    val validTimeSlots = validateTimeSlots(timeSlots)
    try {
      val newException = ScheduleException(date, validTimeSlots, type)
      mergeExceptions(currentProvider.schedule.exceptions, newException)
      updateProviderSchedule { success ->
        val feedback = if (success) "Exception added successfully" else "Failed to update schedule"
        onComplete(success, feedback)
      }
    } catch (e: Exception) {
      Log.e("ProviderCalendarViewModel", "Failed to add exception", e)
      onComplete(false, e.message ?: "Failed to add exception")
    }
  }

  companion object {
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
