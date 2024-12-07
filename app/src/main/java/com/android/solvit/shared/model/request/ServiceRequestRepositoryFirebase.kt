package com.android.solvit.shared.model.request

import android.net.Uri
import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.model.utils.uploadImageToStorage
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ServiceRequestRepositoryFirebase(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ServiceRequestRepository {

  private val collectionPath = "serviceRequests"
  private val imageFolderPath = "serviceRequestImages/"

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun addListenerOnServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).addSnapshotListener { value, error ->
      if (error != null) {
        onFailure(error)
        return@addSnapshotListener
      }
      if (value != null) {
        val serviceRequests = value.mapNotNull { documentToServiceRequest(it) }
        onSuccess(serviceRequests)
      }
    }
  }

  // Fetch service requests
  override fun getServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { result ->
      if (result.isSuccessful) {
        val serviceRequests =
            result.result?.mapNotNull { documentToServiceRequest(it) } ?: emptyList()
        onSuccess(serviceRequests)
      } else {
        val exception = result.exception ?: Exception("Unknown error")
        onFailure(exception)
      }
    }
  }

  override fun getPendingServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getServiceRequestsByStatus(ServiceRequestStatus.PENDING, onSuccess, onFailure)
  }

  override fun getAcceptedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getServiceRequestsByStatus(ServiceRequestStatus.ACCEPTED, onSuccess, onFailure)
  }

  override fun getScheduledServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getServiceRequestsByStatus(ServiceRequestStatus.SCHEDULED, onSuccess, onFailure)
  }

  override fun getCompletedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getServiceRequestsByStatus(ServiceRequestStatus.COMPLETED, onSuccess, onFailure)
  }

  override fun getCancelledServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getServiceRequestsByStatus(ServiceRequestStatus.CANCELED, onSuccess, onFailure)
  }

  override fun getArchivedServiceRequests(
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getServiceRequestsByStatus(ServiceRequestStatus.ARCHIVED, onSuccess, onFailure)
  }

  override fun getServiceRequestById(
      id: String,
      onSuccess: (ServiceRequest) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(id).get().addOnCompleteListener { result ->
      if (result.isSuccessful) {
        val serviceRequest = documentToServiceRequest(result.result!!)
        if (serviceRequest != null) {
          onSuccess(serviceRequest)
        } else {
          onFailure(Exception("Service request not found"))
        }
      } else {
        val exception = result.exception ?: Exception("Unknown error")
        onFailure(exception)
      }
    }
  }

  override fun saveServiceRequest(
      serviceRequest: ServiceRequest,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val document = db.collection(collectionPath).document(serviceRequest.uid)
    performFirestoreOperation(document.set(serviceRequest), onSuccess, onFailure)
  }

  // Save service request with image handling
  override fun saveServiceRequestWithImage(
      serviceRequest: ServiceRequest,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (imageUri != null) {
      saveServiceRequest(serviceRequest, onSuccess, onFailure)
      uploadImageToStorage(
          storage,
          imageFolderPath,
          imageUri,
          { imageUrl ->
            // Set image URL in the service request
            val requestWithImage = serviceRequest.copy(imageUrl = imageUrl)
            saveServiceRequest(requestWithImage, onSuccess, onFailure)
          },
          onFailure)
    } else {
      saveServiceRequest(serviceRequest, onSuccess, onFailure)
    }
  }

  override fun uploadMultipleImagesToStorage(
      imageUris: List<Uri>,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val imageUrls = mutableListOf<String>()
    val totalCount = imageUris.size
    var successCount = 0
    var failureCount = 0

    if (imageUris.isEmpty()) {
      Log.w("ImageUpload", "No images to upload.")
      onSuccess(emptyList())
      return
    }

    Log.i("ImageUpload", "Starting upload for $totalCount images.")

    for (uri in imageUris) {
      uploadImageToStorage(
          storage = storage,
          path = imageFolderPath,
          imageUri = uri,
          onSuccess = { url ->
            synchronized(imageUrls) {
              imageUrls.add(url)
              successCount++
            }
            Log.i(
                "ImageUpload",
                "Image uploaded successfully: $uri. Total success: $successCount/$totalCount")
            if (successCount + failureCount == totalCount) {
              Log.i(
                  "ImageUpload",
                  "All uploads completed. Success: $successCount, Failures: $failureCount")
              onSuccess(imageUrls)
            }
          },
          onFailure = { exception ->
            synchronized(this) { failureCount++ }
            Log.e(
                "ImageUpload",
                "Failed to upload image: $uri. Exception: ${exception.message}. Total failures: $failureCount/$totalCount",
                exception)
            if (successCount + failureCount == totalCount) {
              Log.w(
                  "ImageUpload",
                  "Uploads completed with errors. Success: $successCount, Failures: $failureCount")
              onFailure(Exception("Failed to upload $failureCount out of $totalCount images."))
            }
          })
    }
  }

  // Delete service request
  override fun deleteServiceRequestById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val document = db.collection(collectionPath).document(id)
    performFirestoreOperation(document.delete(), onSuccess, onFailure)
  }

  // Perform Firestore operation
  fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("ServiceRequestRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  fun documentToServiceRequest(document: DocumentSnapshot): ServiceRequest? {
    return try {
      ServiceRequest(
          uid = document.id,
          title = document.getString("title") ?: "",
          description = document.getString("description") ?: "",
          userId = document.getString("userId") ?: "",
          providerId = document.getString("providerId"),
          dueDate = document.getTimestamp("dueDate") ?: Timestamp.now(),
          meetingDate = document.getTimestamp("meetingDate"),
          location =
              Location(
                  latitude = document.getDouble("location.latitude") ?: 0.0,
                  longitude = document.getDouble("location.longitude") ?: 0.0,
                  name = document.getString("location.name") ?: ""),
          imageUrl = document.getString("imageUrl"),
          packageId = document.getString("packageId"),
          agreedPrice = document.getDouble("agreedPrice"),
          type = Services.valueOf(document.getString("type") ?: Services.OTHER.name),
          status =
              ServiceRequestStatus.valueOf(
                  document.getString("status") ?: ServiceRequestStatus.PENDING.name))
    } catch (e: Exception) {
      Log.e("ServiceRequestRepositoryFirestore", "Error converting document to ServiceRequest", e)
      null
    }
  }

  private fun getServiceRequestsByStatus(
      status: ServiceRequestStatus,
      onSuccess: (List<ServiceRequest>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).whereEqualTo("status", status.name).get().addOnCompleteListener {
        result ->
      if (result.isSuccessful) {
        val serviceRequests =
            result.result?.mapNotNull { documentToServiceRequest(it) } ?: emptyList()
        onSuccess(serviceRequests)
      } else {
        val exception = result.exception ?: Exception("Unknown error")
        onFailure(exception)
      }
    }
  }
}
