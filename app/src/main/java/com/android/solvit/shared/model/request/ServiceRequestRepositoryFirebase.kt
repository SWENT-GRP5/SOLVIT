package com.android.solvit.shared.model.request

import android.net.Uri
import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

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
      uploadImageToStorage(
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
          title = document.getString("title") ?: return null,
          description = document.getString("description") ?: return null,
          assigneeName = document.getString("assigneeName") ?: return null,
          dueDate = document.getTimestamp("dueDate") ?: return null,
          location =
              Location(
                  latitude = document.getDouble("location.latitude") ?: return null,
                  longitude = document.getDouble("location.longitude") ?: return null,
                  name = document.getString("location.name") ?: return null),
          imageUrl = document.getString("imageUrl"),
          type = ServiceRequestType.valueOf(document.getString("type") ?: return null),
          status = ServiceRequestStatus.valueOf(document.getString("status") ?: return null))
    } catch (e: Exception) {
      Log.e("ServiceRequestRepositoryFirestore", "Error converting document to ServiceRequest", e)
      null
    }
  }

  // Handle image uploads to Firebase Storage
  fun uploadImageToStorage(
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val uniqueFileName = UUID.randomUUID().toString() + ".jpg"
    val imageRef: StorageReference = storage.reference.child(imageFolderPath + uniqueFileName)

    imageRef
        .putFile(imageUri)
        .addOnSuccessListener {
          imageRef.downloadUrl
              .addOnSuccessListener { downloadUrl -> onSuccess(downloadUrl.toString()) }
              .addOnFailureListener { exception -> onFailure(exception) }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }
}
