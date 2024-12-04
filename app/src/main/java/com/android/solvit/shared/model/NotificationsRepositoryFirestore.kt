package com.android.solvit.shared.model

import android.util.Log
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.service.Services
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * NotificationsRepositoryFirestore is a repository class responsible for managing notifications for
 * providers stored in Firestore. It implements the NotificationsRepository interface to handle
 * operations like fetching, sending, and processing notifications for providers.
 *
 * @param db An instance of FirebaseFirestore used to interact with Firestore.
 */
class NotificationsRepositoryFirestore(private val db: FirebaseFirestore) :
    NotificationsRepository {

  // Collection path for notifications in Firestore
  private val collectionPath = "notifications"

  /**
   * Generates a new unique ID for a notification document.
   *
   * @return A new unique document ID.
   */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Initializes the repository by adding an authentication state listener to Firebase Auth. When
   * the user is authenticated, the provided `onSuccess` callback is triggered.
   *
   * @param onSuccess Callback to execute when a user is authenticated.
   */
  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  /**
   * Retrieves notifications for a specific provider from Firestore.
   *
   * @param providerId The ID of the provider whose notifications are to be retrieved.
   * @param onSuccess Callback to execute with the list of notifications on success.
   * @param onFailure Callback to execute if there is an error retrieving the notifications.
   */
  override fun getNotification(
      providerId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d("NotificationRepositoryFirestore", "getNotifications")

    db.collection(collectionPath)
        .whereEqualTo("providerId", providerId) // Filter by providerId
        .get()
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            // Convert Firestore documents to Notification objects
            val notif =
                task.result?.mapNotNull { document -> documentToNotif(document) } ?: emptyList()
            onSuccess(notif)
          } else {
            task.exception?.let { e ->
              Log.e("NotificationRepositoryFirestore", "Error getting documents", e)
              onFailure(e)
            }
          }
        }
  }

  /**
   * Converts a Firestore document snapshot into a Notification object.
   *
   * @param document The Firestore document snapshot to convert.
   * @return A Notification object or null if the document could not be converted.
   */
  fun documentToNotif(document: DocumentSnapshot): Notification? {
    return try {
      val id = document.id
      val providerId = document.getString("providerId") ?: return null
      val title = document.getString("title") ?: return null
      val message = document.getString("message") ?: return null
      val timestamp = document.getTimestamp("timestamp") ?: return null
      val isRead = document.getBoolean("isRead") ?: false
      // Get ServiceRequest fields
      val serviceRequestMap = document.get("serviceRequest") as? Map<String, Any> ?: return null
      val locationMap = serviceRequestMap["location"] as? Map<String, Any>
      val location =
          if (locationMap != null) {
            Location(
                latitude = locationMap["latitude"] as? Double ?: 0.0,
                longitude = locationMap["longitude"] as? Double ?: 0.0,
                name = locationMap["name"] as? String ?: "")
          } else {
            null
          }
      val serviceRequest =
          ServiceRequest(
              uid = serviceRequestMap["uid"] as String,
              title = serviceRequestMap["title"] as String,
              type = Services.valueOf(serviceRequestMap["type"] as String),
              description = serviceRequestMap["description"] as String,
              userId = serviceRequestMap["userId"] as String,
              providerId = serviceRequestMap["providerId"] as? String,
              dueDate = serviceRequestMap["dueDate"] as Timestamp,
              meetingDate = serviceRequestMap["meetingDate"] as? Timestamp,
              location = location,
              imageUrl = serviceRequestMap["imageUrl"] as? String,
              packageId = serviceRequestMap["packageId"] as? String,
              agreedPrice = serviceRequestMap["agreedPrice"] as? Double,
              status = ServiceRequestStatus.valueOf(serviceRequestMap["status"] as String))

      Notification(
          uid = id,
          providerId = providerId,
          title = title,
          message = message,
          timestamp = timestamp,
          serviceRequest = serviceRequest,
          isRead = isRead)
    } catch (e: Exception) {
      Log.e("NotificationsRepositoryFirestore", "Error converting document to Notification", e)
      null
    }
  }

  /**
   * Executes a Firestore operation and handles success or failure.
   *
   * @param task The Firestore task to perform.
   * @param onSuccess Callback to execute on successful operation.
   * @param onFailure Callback to execute if the operation fails.
   */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("NotificationsRepositoryFirestore", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Sends notifications to providers whose services match the type of a service request.
   *
   * @param serviceRequest The service request for which notifications are to be sent.
   * @param providers The list of providers to check for matching services.
   * @param onSuccess Callback to execute after all notifications are processed successfully.
   * @param onFailure Callback to execute if an error occurs while sending notifications.
   */
  override fun sendNotification(
      serviceRequest: ServiceRequest,
      providers: List<Provider>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      // Filter providers whose services match the service request type
      val matchingProviders =
          providers.filter { provider -> provider.service == serviceRequest.type }
      val matchingProvidersSize = matchingProviders.size
      Log.d("sendNotification", "Found $matchingProvidersSize matching providers")

      // Create a notification for each matching provider
      matchingProviders.forEach { provider ->
        val notification =
            Notification(
                uid = getNewUid(),
                providerId = provider.uid,
                title = "New Service Request Available!",
                message =
                    "A new service request has been posted. Tap here to view the details and get started!",
                timestamp = Timestamp.now(),
                serviceRequest = serviceRequest,
                isRead = false)

        // Save the notification for the provider
        performFirestoreOperation(
            db.collection(collectionPath).document(notification.uid).set(notification),
            onSuccess,
            onFailure)
      }

      // Notify success after processing all providers
      onSuccess()
    } catch (exception: Exception) {
      // Notify failure in case of error
      onFailure(exception)
    }
  }

  /**
   * Updates the read status of a notification in the Firestore database.
   *
   * @param notificationId The unique identifier of the notification to be updated.
   * @param isRead The read status to set (true for read, false for unread).
   */
  override fun updateNotificationReadStatus(notificationId: String, isRead: Boolean) {
    db.collection(collectionPath)
        .document(notificationId)
        .update("isRead", isRead)
        .addOnSuccessListener { Log.d("NotificationsRepository", "Notification marked as read") }
        .addOnFailureListener { e ->
          Log.e("NotificationsRepository", "Error updating notification: $e")
        }
  }
}
