package com.android.solvit.shared.model.request

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

  private val _cancelledRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val cancelledRequests: StateFlow<List<ServiceRequest>> = _cancelledRequests

  private val _archivedRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val archivedRequests: StateFlow<List<ServiceRequest>> = _archivedRequests

  private val _selectedProviderId = MutableStateFlow<String?>(null)
  val selectedProviderId: StateFlow<String?> = _selectedProviderId

  private val _selectedProviderService = MutableStateFlow<Services?>(null)
  val selectedProviderService: StateFlow<Services?> = _selectedProviderService

  init {
    repository.init { updateAllRequests() }
    repository.addListenerOnServiceRequests(
        onSuccess = { _requests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error listening ServiceRequests", exception)
        })
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

  fun getScheduledRequests() {
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

  private fun getCancelledRequests() {
    repository.getCancelledServiceRequests(
        onSuccess = { _cancelledRequests.value = it },
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

  private fun updateAllRequests() {
    getServiceRequests()
    getPendingRequests()
    getAcceptedRequests()
    getScheduledRequests()
    getCompletedRequests()
    getCancelledRequests()
    getArchivedRequests()
  }

  fun getServiceRequestById(id: String, onSuccess: (ServiceRequest) -> Unit) {
    repository.getServiceRequestById(
        id,
        onSuccess = onSuccess,
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequest", exception)
        })
  }

  fun saveServiceRequest(serviceRequest: ServiceRequest) {
    repository.saveServiceRequest(
        serviceRequest,
        onSuccess = {
          updateAllRequests()
          if (serviceRequest.uid == _selectedRequest.value?.uid) {
            _selectedRequest.value = serviceRequest
          }
        },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error saving ServiceRequest", exception)
        })
  }

  fun saveServiceRequestWithImage(
      serviceRequest: ServiceRequest,
      imageUri: Uri,
      onSuccess: () -> Unit = {}
  ) {
    repository.saveServiceRequestWithImage(
        serviceRequest,
        imageUri,
        onSuccess = {
          updateAllRequests()
          if (serviceRequest.uid == _selectedRequest.value?.uid) {
            _selectedRequest.value = serviceRequest
          }
          onSuccess()
        },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error saving ServiceRequest", exception)
        })
  }

  fun uploadMultipleImages(
      imageUris: List<Uri>,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.uploadMultipleImagesToStorage(
        imageUris = imageUris,
        onSuccess = { urls ->
          onSuccess(urls) // Pass URLs back to the UI
        },
        onFailure = { exception ->
          onFailure(exception) // Pass exception back to the UI
        })
  }

  fun deleteServiceRequestById(id: String) {
    repository.deleteServiceRequestById(
        id,
        onSuccess = {
          updateAllRequests()
          if (_selectedRequest.value?.uid == id) {
            _selectedRequest.value = null
          }
        },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error deleting ServiceRequest", exception)
        })
  }

  fun selectRequest(serviceRequest: ServiceRequest) {
    _selectedRequest.value = serviceRequest
  }

  fun selectProvider(providerId: String, type: Services) {
    _selectedProviderId.value = providerId
    _selectedProviderService.value = type
  }

  fun unSelectProvider() {
    _selectedProviderId.value = null
    _selectedProviderService.value = null
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

  fun cancelRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.CANCELED))
  }

  fun archiveRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.ARCHIVED))
  }

  fun getTodayScheduledRequests(): List<ServiceRequest> {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val today = LocalDate.now().format(dateFormatter)
    return _scheduledRequests.value
        .filter {
          val date =
              it.meetingDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                  ?: it.dueDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
          date.format(dateFormatter) == today
        }
        .sortedBy {
          val time =
              it.meetingDate?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalTime()
                  ?: it.dueDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
          time.format(timeFormatter)
        }
  }
}
