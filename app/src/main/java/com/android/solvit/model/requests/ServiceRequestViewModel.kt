package com.android.solvit.model.requests

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class ServiceRequestViewModel(private val repository: ServiceRequestRepository) : ViewModel() {
  private val requests_ = MutableStateFlow<List<ServiceRequest>>(emptyList())
  val requests: StateFlow<List<ServiceRequest>> = requests_.asStateFlow()

  private val selectedRequest_ = MutableStateFlow<ServiceRequest?>(null)
  open val selectedRequest: StateFlow<ServiceRequest?> = selectedRequest_.asStateFlow()

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
    repository.getServiceRequests(onSuccess = { requests_.value = it }, onFailure = {})
  }

  fun saveServiceRequest(serviceRequest: ServiceRequest) {
    repository.saveServiceRequest(
        serviceRequest, onSuccess = { getServiceRequests() }, onFailure = {})
  }

  fun saveServiceRequestWithImage(serviceRequest: ServiceRequest, imageUri: Uri) {
    repository.saveServiceRequestWithImage(
        serviceRequest, imageUri, onSuccess = { getServiceRequests() }, onFailure = {})
  }

  fun deleteServiceRequestById(id: String) {
    repository.deleteServiceRequestById(id, onSuccess = { getServiceRequests() }, onFailure = {})
  }

  fun selectRequest(serviceRequest: ServiceRequest) {
    selectedRequest_.value = serviceRequest
  }
}
