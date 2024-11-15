package com.android.solvit.shared.model.request

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale
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
    repository.init {
      getServiceRequests()
      getPendingRequests()
      getAcceptedRequests()
      getScheduledRequests()
      getCompletedRequests()
      getArchivedRequests()
    }
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

  private fun getPendingRequests() {
    repository.getPendingServiceRequests(
        onSuccess = { _pendingRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  private fun getAcceptedRequests() {
    repository.getAcceptedServiceRequests(
        onSuccess = { _acceptedRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  private fun getScheduledRequests() {
    repository.getScheduledServiceRequests(
        onSuccess = { _scheduledRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  private fun getCompletedRequests() {
    repository.getCompletedServiceRequests(
        onSuccess = { _completedRequests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  private fun getArchivedRequests() {
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

  fun unConfirmRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.PENDING))
  }

  fun confirmRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.ACCEPTED))
  }

  fun scheduleRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.SCHEDULED))
  }

  fun completeRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.COMPLETED))
  }

  fun archiveRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.ARCHIVED))
  }

  fun getTodayScheduledRequests(): List<ServiceRequest> {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val today = dateFormat.format(LocalDate.now())
    return _scheduledRequests.value
        .filter {
          val date = dateFormat.format(it.meetingDate?.toDate() ?: it.dueDate.toDate())

          date == today
        }
        .sortedBy {
          val time = timeFormat.format(it.meetingDate?.toDate() ?: it.dueDate.toDate())
          time
        }
  }
}
