package com.android.solvit.provider.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
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

  fun getAvailableSlots(date: LocalDateTime): List<TimeSlot> {
    return currentProvider.schedule.getAvailableSlots(date)
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
