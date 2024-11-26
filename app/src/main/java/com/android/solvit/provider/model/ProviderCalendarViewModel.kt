package com.android.solvit.provider.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.provider.Schedule
import com.android.solvit.shared.model.provider.ScheduleException
import com.android.solvit.shared.model.provider.TimeSlot
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlinx.coroutines.flow.map

class ProviderCalendarViewModel(
    private val providerRepository: ProviderRepository,
    private val authViewModel: AuthViewModel,
    private val serviceRequestViewModel: ServiceRequestViewModel
) : ViewModel() {

  private lateinit var currentProvider: Provider

  init {
    loadProvider()
    loadServiceRequests()
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

  private fun loadServiceRequests() {
    serviceRequestViewModel.getRequests()
  }

  fun getServiceRequests() =
      serviceRequestViewModel.requests.map { requests ->
        requests.filter { request ->
          try {
            request.meetingDate != null
          } catch (e: Exception) {
            Log.e("ProviderCalendarViewModel", "Error processing service request", e)
            false
          }
        }
      }

  fun setRegularHours(
      day: DayOfWeek,
      timeSlots: List<TimeSlot>,
      onSuccess: () -> Unit = {},
      onError: (String) -> Unit = {}
  ) {
    val newSchedule = currentProvider.schedule.setRegularHours(day, timeSlots)
    updateProviderSchedule(newSchedule, onSuccess, onError)
  }

  fun addException(
      date: LocalDateTime,
      timeSlots: List<TimeSlot>,
      onSuccess: () -> Unit = {},
      onError: (String) -> Unit = {}
  ) {
    val exception = ScheduleException(date, timeSlots)
    val newSchedule = currentProvider.schedule.addException(exception)
    updateProviderSchedule(newSchedule, onSuccess, onError)
  }

  fun removeException(
      date: LocalDateTime,
      onSuccess: () -> Unit = {},
      onError: (String) -> Unit = {}
  ) {
    val newSchedule = currentProvider.schedule.removeException(date)
    updateProviderSchedule(newSchedule, onSuccess, onError)
  }

  fun isAvailable(dateTime: LocalDateTime): Boolean {
    return currentProvider.schedule.isAvailable(dateTime)
  }

  fun getAvailableSlots(date: LocalDateTime): List<TimeSlot> {
    return currentProvider.schedule.getAvailableSlots(date)
  }

  private fun updateProviderSchedule(
      newSchedule: Schedule,
      onSuccess: () -> Unit,
      onError: (String) -> Unit
  ) {
    val updatedProvider = currentProvider.copy(schedule = newSchedule)
    providerRepository.updateProvider(
        updatedProvider,
        onSuccess = {
          currentProvider = updatedProvider
          onSuccess()
        },
        onFailure = { e -> onError("Failed to update schedule: ${e.message}") })
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
