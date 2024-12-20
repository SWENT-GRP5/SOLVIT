package com.android.solvit.shared.model.request

import android.net.Uri

/** Interface defining the contract for managing service requests in the application. */
interface ServiceRequestRepository {

  /**
   * Generates a new unique identifier for a service request.
   *
   * @return A unique string identifier.
   */
  fun getNewUid(): String

  /**
   * Initializes the repository.
   *
   * @param onSuccess Callback invoked when initialization succeeds.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Adds a real-time listener for changes in service requests.
   *
   * @param onSuccess Callback invoked with the updated list of service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun addListenerOnServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all service requests.
   *
   * @param onSuccess Callback invoked with the list of service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getServiceRequests(onSuccess: (List<ServiceRequest>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all pending service requests.
   *
   * @param onSuccess Callback invoked with the list of pending service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getPendingServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all accepted service requests.
   *
   * @param onSuccess Callback invoked with the list of accepted service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getAcceptedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all scheduled service requests.
   *
   * @param onSuccess Callback invoked with the list of scheduled service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getScheduledServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all completed service requests.
   *
   * @param onSuccess Callback invoked with the list of completed service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getCompletedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all canceled service requests.
   *
   * @param onSuccess Callback invoked with the list of canceled service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getCancelledServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all archived service requests.
   *
   * @param onSuccess Callback invoked with the list of archived service requests.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getArchivedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves a specific service request by its ID.
   *
   * @param id The unique identifier of the service request.
   * @param onSuccess Callback invoked with the service request details.
   * @param onFailure Callback invoked in case of an error.
   */
  fun getServiceRequestById(
      id: String,
      onSuccess: (ServiceRequest) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Saves a new service request to the repository.
   *
   * @param serviceRequest The service request to be saved.
   * @param onSuccess Callback invoked when saving succeeds.
   * @param onFailure Callback invoked in case of an error.
   */
  fun saveServiceRequest(
      serviceRequest: ServiceRequest,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a service request by its unique identifier.
   *
   * @param id The unique identifier of the service request.
   * @param onSuccess Callback invoked when deletion succeeds.
   * @param onFailure Callback invoked in case of an error.
   */
  fun deleteServiceRequestById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Saves a service request with an associated image.
   *
   * @param serviceRequest The service request to be saved.
   * @param imageUri The URI of the image to upload.
   * @param onSuccess Callback invoked when saving succeeds.
   * @param onFailure Callback invoked in case of an error.
   */
  fun saveServiceRequestWithImage(
      serviceRequest: ServiceRequest,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Uploads multiple images to cloud storage.
   *
   * @param imageUris A list of URIs of the images to upload.
   * @param onSuccess Callback invoked with the list of uploaded image URLs.
   * @param onFailure Callback invoked in case of an error.
   */
  fun uploadMultipleImagesToStorage(
      imageUris: List<Uri>,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
