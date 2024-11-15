package com.android.solvit.shared.model.request

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class ServiceRequestViewModel(private val repository: ServiceRequestRepository) : ViewModel() {
  private val _requests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val requests: StateFlow<List<ServiceRequest>> = _requests

  private val _selectedRequest = MutableStateFlow<ServiceRequest?>(null)
  val selectedRequest: StateFlow<ServiceRequest?> = _selectedRequest

  private val _pendingRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val pendingRequests: StateFlow<List<ServiceRequest>> = _pendingRequests

  private val _acceptedRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val acceptedRequests: StateFlow<List<ServiceRequest>> = _acceptedRequests

  private val _scheduledRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val scheduledRequests: StateFlow<List<ServiceRequest>> = _scheduledRequests

  private val _completedRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val completedRequests: StateFlow<List<ServiceRequest>> = _completedRequests

  private val _archivedRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val archivedRequests: StateFlow<List<ServiceRequest>> = _archivedRequests

  init {
    repository.init { getServiceRequests() }
  }

  // Create factory
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ServiceRequestViewModel(
                ServiceRequestRepositoryFirebase(Firebase.firestore, Firebase.storage))
                as T
          }
        }
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun getServiceRequests() {
    repository.getServiceRequests(
        onSuccess = { _requests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  fun getPendingRequests() {
    repository.getPendingServiceRequests(
        onSuccess = { _pendingRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  fun getAcceptedRequests() {
    repository.getAcceptedServiceRequests(
        onSuccess = { _acceptedRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  fun getScheduledRequests() {
    repository.getScheduledServiceRequests(
        onSuccess = { _scheduledRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  fun getCompletedRequests() {
    repository.getCompletedServiceRequests(
        onSuccess = { _completedRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  fun getArchivedRequests() {
    repository.getArchivedServiceRequests(
        onSuccess = { _archivedRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  fun saveServiceRequest(serviceRequest: ServiceRequest) {
    repository.saveServiceRequest(
        serviceRequest,
        onSuccess = { getServiceRequests() },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error saving ServiceRequest", exception)
        })
  }

  fun saveServiceRequestWithImage(serviceRequest: ServiceRequest, imageUri: Uri) {
    repository.saveServiceRequestWithImage(
        serviceRequest,
        imageUri,
        onSuccess = { getServiceRequests() },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error saving ServiceRequest", exception)
        })
  }

  fun deleteServiceRequestById(id: String) {
    repository.deleteServiceRequestById(
        id,
        onSuccess = { getServiceRequests() },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error deleting ServiceRequest", exception)
        })
  }

  fun selectRequest(serviceRequest: ServiceRequest) {
    _selectedRequest.value = serviceRequest
  }
}
