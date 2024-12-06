package com.android.solvit.provider.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProviderCalendarViewModel(
    authViewModel: AuthViewModel,
    serviceRequestViewModel: ServiceRequestViewModel
) : ViewModel() {

  init {
    serviceRequestViewModel.getServiceRequests()
  }

  val serviceRequests: Flow<List<ServiceRequest>> =
      combine(authViewModel.user, serviceRequestViewModel.requests) { user, requests ->
        val userId = user?.uid
        if (userId == null) {
          emptyList()
        } else {
          requests.filter { request -> request.providerId == userId }
        }
      }

  companion object {
    val Factory: (AuthViewModel, ServiceRequestViewModel) -> ViewModelProvider.Factory =
        { auth, service ->
          object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
              if (modelClass.isAssignableFrom(ProviderCalendarViewModel::class.java)) {
                return ProviderCalendarViewModel(auth, service) as T
              }
              throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
          }
        }
  }
}
