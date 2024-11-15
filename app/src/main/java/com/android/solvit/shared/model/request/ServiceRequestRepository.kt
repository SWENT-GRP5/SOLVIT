package com.android.solvit.shared.model.request

import android.net.Uri

interface ServiceRequestRepository {

  fun getNewUid(): String

  // Initialize the repository
  fun init(onSuccess: () -> Unit)

  // Retrieve all service requests
  fun getServiceRequests(onSuccess: (List<ServiceRequest>) -> Unit, onFailure: (Exception) -> Unit)

  fun getPendingServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getAcceptedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getScheduledServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getCompletedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getArchivedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Add a new service request
  fun saveServiceRequest(
      serviceRequest: ServiceRequest,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  // Delete a service request by its ID
  fun deleteServiceRequestById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  // Add a service request with an image
  fun saveServiceRequestWithImage(
      serviceRequest: ServiceRequest,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
