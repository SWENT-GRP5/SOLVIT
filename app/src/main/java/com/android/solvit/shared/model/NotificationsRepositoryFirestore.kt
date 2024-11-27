package com.android.solvit.shared.model

import android.util.Log
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsRepositoryFirestore(private val db: FirebaseFirestore) :
  NotificationsRepository {

private val collectionPath = "notifications"

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

override fun getNotification(
    providerId: String,
    onSuccess: (List<Notification>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    Log.d("NotificationRepositoryFirestore", "getNotifications")

    db.collection(collectionPath)
        .whereEqualTo("providerId", providerId) // Filter by providerId
        .get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val notif =
                task.result?.mapNotNull { document -> documentToNotif(document) }
                    ?: emptyList()
            onSuccess(notif)
        } else {
            task.exception?.let { e ->
                Log.e("NotificationRepositoryFirestore", "Error getting documents", e)
                onFailure(e)
            }
        }
    }
 }
    private fun documentToNotif(document: DocumentSnapshot): Notification? {
        return try {
            val id = document.id
            val providerId = document.getString("providerId") ?: return null
            val title = document.getString("title") ?: return null
            val message = document.getString("message") ?: return null
            val timestamp = document.getTimestamp("timestamp") ?: return null
            val isRead = document.getBoolean("isRead") ?: false

            Notification(
                id = id,
                providerId = providerId,
                title = title,
                message = message,
                timestamp = timestamp,
                isRead = isRead
            )
        } catch (e: Exception) {
            Log.e("NotificationsRepositoryFirestore", "Error converting document to Notification", e)
            null
        }
    }


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

    override fun sendNotification(
        serviceRequest: ServiceRequest,
        providers: List<Provider>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Filter providers whose service matches the service request type
            val matchingProviders = providers.filter { provider ->
                provider.service == serviceRequest.type
            }
            val matchingProvidersSize = matchingProviders.size
            Log.d("sendNotification", "Found $matchingProvidersSize matching providers")

            // Create a notification for each matching provider
            matchingProviders.forEach { provider ->
                val notification = Notification(
                    id = getNewUid(),
                    providerId = provider.uid,
                    title = "New Service Request for ${serviceRequest.type}",
                    message = "A new service request for ${serviceRequest.title} has been posted. Check it out!",
                    timestamp = Timestamp.now(),
                    isRead = false
                )

                // Save the notification for the provider (can call save function here)
                performFirestoreOperation(
                db.collection(collectionPath).document(notification.id).set(notification), onSuccess, onFailure)

            }

            onSuccess() // Notify success after processing all providers
        } catch (exception: Exception) {
            onFailure(exception) // Notify failure in case of error
        }
    }

}
