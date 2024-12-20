package com.android.solvit.shared.model.request

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.notifications.FcmTokenManager
import com.android.solvit.shared.notifications.NotificationManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for managing service requests. Handles CRUD operations, state management, and data
 * synchronization.
 *
 * @property repository Interface for accessing service request data.
 * @property notificationManager Manages notifications.
 * @property authViewModel Manages authentication state.
 * @property fcmTokenManager Handles FCM token updates.
 */
open class ServiceRequestViewModel(
    private val repository: ServiceRequestRepository,
    private val notificationManager: NotificationManager? = null,
    private val authViewModel: AuthViewModel? = null,
    private val fcmTokenManager: FcmTokenManager? = null
) : ViewModel() {

  // State management properties

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

  private val _selectedServices = MutableStateFlow<List<Services>>(emptyList())
  val selectedServices: StateFlow<List<Services>> = _selectedServices
  private val _sortSelected = MutableStateFlow(false)
  val sortSelected: StateFlow<Boolean> = _sortSelected

  init {
    repository.init { updateAllRequests() }
    repository.addListenerOnServiceRequests(
        onSuccess = { requests ->
          _requests.value = requests
          // Update all request categories when new data arrives
          _pendingRequests.value = requests.filter { it.status == ServiceRequestStatus.PENDING }
          _acceptedRequests.value = requests.filter { it.status == ServiceRequestStatus.ACCEPTED }
          _scheduledRequests.value = requests.filter { it.status == ServiceRequestStatus.SCHEDULED }
          _completedRequests.value = requests.filter { it.status == ServiceRequestStatus.COMPLETED }
          _cancelledRequests.value = requests.filter { it.status == ServiceRequestStatus.CANCELED }
          _archivedRequests.value = requests.filter { it.status == ServiceRequestStatus.ARCHIVED }
        },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error listening ServiceRequests", exception)
        })
    authViewModel?.user?.value?.uid?.let { userId ->
      fcmTokenManager?.let { updateFcmToken(userId) }
    }
  }

  /**
   * Updates the FCM token for push notifications.
   *
   * @param userId The unique user identifier.
   */
  private fun updateFcmToken(userId: String) {
    viewModelScope.launch {
      try {
        val token = FirebaseMessaging.getInstance().token.await()
        FcmTokenManager.getInstance().updateUserFcmToken(userId, token)
        Log.d("FCM_DEBUG", "FCM Token updated for user $userId in ViewModel")
      } catch (e: Exception) {
        Log.e("FCM_DEBUG", "Error updating FCM token in ViewModel: ${e.message}", e)
      }
    }
  }

  // ---------------- Factory -------------------
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ServiceRequestViewModel(
                ServiceRequestRepositoryFirebase(Firebase.firestore, Firebase.storage),
                NotificationManager.getInstance(),
                null, // AuthViewModel should be injected from the activity/fragment
                FcmTokenManager.getInstance())
                as T
          }
        }
  }

  /**
   * Retrieves a new unique identifier.
   *
   * @return The unique ID as a String.
   */
  fun getNewUid(): String {
    return repository.getNewUid()
  }

  /** Fetches all service requests from the repository. */
  fun getServiceRequests() {
    repository.getServiceRequests(
        onSuccess = { _requests.value = it },
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequests", exception)
        })
  }

  fun selectService(service: Services) {
    _selectedServices.value += service
  }

  fun unSelectService(service: Services) {
    _selectedServices.value -= service
  }

  fun sortSelected() {
    _sortSelected.value = !_sortSelected.value
  }

  fun clearFilters() {
    _selectedServices.value = emptyList()
    _sortSelected.value = false
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

  /**
   * Fetches service requests filtered by ID.
   *
   * @param id The unique service request ID.
   * @param onSuccess Callback invoked with the service request details.
   */
  fun getServiceRequestById(id: String, onSuccess: (ServiceRequest) -> Unit) {
    repository.getServiceRequestById(
        id,
        onSuccess = onSuccess,
        onFailure = { exception ->
          Log.e("ServiceRequestViewModel", "Error fetching ServiceRequest", exception)
        })
  }

  /**
   * Saves a service request to the repository.
   *
   * @param serviceRequest The service request object.
   */
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

  /**
   * Deletes a service request by its unique ID.
   *
   * @param id The service request ID to delete.
   */
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

  fun setSelectedRequest(request: ServiceRequest) {
    _selectedRequest.value = request
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

  /**
   * Confirms a service request, marking it as accepted.
   *
   * @param serviceRequest The service request to be confirmed.
   * @param providerName The name of the provider accepting the request.
   */
  fun confirmRequest(serviceRequest: ServiceRequest, providerName: String) {
    try {
      val updatedRequest = serviceRequest.copy(status = ServiceRequestStatus.ACCEPTED)
      repository.saveServiceRequest(
          updatedRequest,
          onSuccess = {
            // Update the selected request immediately
            _selectedRequest.value = updatedRequest

            // Update all request lists
            updateAllRequests()

            // Send notification
            viewModelScope.launch {
              notificationManager
                  ?.sendServiceRequestAcceptedNotification(
                      recipientUserId = serviceRequest.userId,
                      requestId = serviceRequest.uid,
                      providerName = providerName)
                  ?.onFailure { e ->
                    Log.e("FCM_DEBUG", "Failed to send acceptance notification", e)
                  }
            }
          },
          onFailure = { e -> Log.e("ServiceRequestViewModel", "Error confirming request", e) })
    } catch (e: Exception) {
      Log.e("ServiceRequestViewModel", "Error in confirmRequest", e)
    }
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

  /**
   * Fetches scheduled requests for the current day.
   *
   * @return A list of today's scheduled requests.
   */
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

  fun addListenerOnServiceRequests(onSuccess: (List<ServiceRequest>) -> Unit) {
    repository.addListenerOnServiceRequests(
        onSuccess = { onSuccess(requests.value) }, onFailure = {})
  }
}
