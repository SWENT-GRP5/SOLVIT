package com.android.solvit.shared.model.request

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.solvit.MainActivity
import com.android.solvit.R
import com.android.solvit.shared.notifications.FcmTokenManager
import com.android.solvit.shared.notifications.NotificationManager
import com.android.solvit.shared.notifications.NotificationService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
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

  private val notificationManager = NotificationManager.getInstance()

  init {
    viewModelScope.launch {
      try {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
          if (task.isSuccessful) {
            val token = task.result
            Log.d("FCM_DEBUG", "FCM Token in ViewModel: $token")
            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
              viewModelScope.launch {
                try {
                  FcmTokenManager.getInstance().updateUserFcmToken(userId, token)
                  Log.d("FCM_DEBUG", "FCM Token updated for user $userId in ViewModel")
                } catch (e: Exception) {
                  Log.e("FCM_DEBUG", "Error updating FCM token in ViewModel: ${e.message}", e)
                }
              }
            }
                ?: Log.w(
                    "FCM_DEBUG", "No user logged in when trying to update FCM token in ViewModel")
          } else {
            Log.e("FCM_DEBUG", "Failed to get FCM token in ViewModel", task.exception)
          }
        }
      } catch (e: Exception) {
        Log.e("FCM_DEBUG", "Error getting FCM token in ViewModel: ${e.message}", e)
      }
    }
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

  fun saveServiceRequestWithImage(serviceRequest: ServiceRequest, imageUri: Uri) {
    repository.saveServiceRequestWithImage(
        serviceRequest,
        imageUri,
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

  fun unConfirmRequest(serviceRequest: ServiceRequest) {
    saveServiceRequest(serviceRequest.copy(status = ServiceRequestStatus.PENDING))
  }

  suspend fun acceptServiceRequest(requestId: String) {
    try {
      repository.getServiceRequests(
          { requests ->
            val request = requests.find { it.uid == requestId }
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (request == null || currentUser == null) {
              Log.e("ServiceRequestViewModel", "Invalid request or user")
              return@getServiceRequests
            }

            // Update request status
            val updatedRequest =
                request.copy(status = ServiceRequestStatus.ACCEPTED, providerId = currentUser.uid)

            repository.saveServiceRequest(
                updatedRequest,
                onSuccess = {
                  // Send notification to the seeker
                  viewModelScope.launch {
                    notificationManager
                        .sendServiceRequestAcceptedNotification(
                            recipientUserId = request.userId,
                            requestId = requestId,
                            providerName = currentUser.displayName ?: "A provider")
                        .onFailure { e ->
                          Log.e("FCM_DEBUG", "Failed to send acceptance notification", e)
                        }
                  }
                  updateAllRequests()
                },
                onFailure = { e ->
                  Log.e("ServiceRequestViewModel", "Error saving updated request", e)
                })
          },
          { e -> Log.e("ServiceRequestViewModel", "Error getting service request", e) })
    } catch (e: Exception) {
      Log.e("ServiceRequestViewModel", "Error accepting service request", e)
    }
  }

  suspend fun updateServiceRequestStatus(requestId: String, status: ServiceRequestStatus) {
    try {
      repository.getServiceRequests(
          { requests ->
            val request = requests.find { it.uid == requestId }
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (request == null || currentUser == null) {
              Log.e("ServiceRequestViewModel", "Invalid request or user")
              return@getServiceRequests
            }

            // Update request status
            val updatedRequest = request.copy(status = status)

            repository.saveServiceRequest(
                updatedRequest,
                onSuccess = {
                  // Send notification to the appropriate user
                  viewModelScope.launch {
                    val recipientId =
                        if (currentUser.uid == request.userId) {
                          request.providerId
                        } else {
                          request.userId
                        }

                    if (recipientId != null) {
                      notificationManager
                          .sendServiceRequestUpdateNotification(
                              recipientUserId = recipientId,
                              requestId = requestId,
                              status = status.name,
                              message = "Service request status updated to: ${status.name}")
                          .onFailure { e ->
                            Log.e("FCM_DEBUG", "Failed to send status update notification", e)
                          }
                    }
                  }
                  updateAllRequests()
                },
                onFailure = { e ->
                  Log.e("ServiceRequestViewModel", "Error saving updated request", e)
                })
          },
          { e -> Log.e("ServiceRequestViewModel", "Error getting service request", e) })
    } catch (e: Exception) {
      Log.e("ServiceRequestViewModel", "Error updating service request status", e)
    }
  }

  fun confirmRequest(serviceRequest: ServiceRequest, providerName: String) {
    try {
      val updatedRequest = serviceRequest.copy(status = ServiceRequestStatus.ACCEPTED)
      repository.saveServiceRequest(
          updatedRequest,
          onSuccess = {
            viewModelScope.launch {
              notificationManager
                  .sendServiceRequestAcceptedNotification(
                      recipientUserId = serviceRequest.userId,
                      requestId = serviceRequest.uid,
                      providerName = providerName)
                  .onFailure { e ->
                    Log.e("FCM_DEBUG", "Failed to send acceptance notification", e)
                  }
            }
            updateAllRequests()
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

  private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager

        // Check if channel exists
        val existingChannel =
            notificationManager.getNotificationChannel(NotificationService.CHANNEL_ID)
        if (existingChannel != null) {
          Log.d("FCM_DEBUG", "Test: Channel already exists")
          return
        }

        val name = context.getString(R.string.app_name)
        val descriptionText = "Service request notifications"
        val importance = android.app.NotificationManager.IMPORTANCE_HIGH
        val channel =
            android.app
                .NotificationChannel(NotificationService.CHANNEL_ID, name, importance)
                .apply {
                  description = descriptionText
                  enableLights(true)
                  lightColor = Color.RED
                  enableVibration(true)
                  setShowBadge(true)
                }

        notificationManager.createNotificationChannel(channel)
        Log.d("FCM_DEBUG", "Test: Created notification channel")
      } catch (e: Exception) {
        Log.e("FCM_DEBUG", "Test: Error creating channel: ${e.message}", e)
      }
    }
  }

  fun testNotification(context: Context) {
    viewModelScope.launch {
      try {
        // Get current user ID as the seeker (for testing)
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
          Log.e("FCM_DEBUG", "No user logged in")
          return@launch
        }

        // Test both local and FCM notifications
        // 1. Local notification
        createNotificationChannel(context)
        val systemNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager

        val intent =
            Intent(context, MainActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification =
            NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Test Local Notification")
                .setContentText("This is a test local notification")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

        // Check notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
              PackageManager.PERMISSION_GRANTED) {
            Log.e("FCM_DEBUG", "Test: Notification permission not granted")
            return@launch
          }
        }

        Log.d("FCM_DEBUG", "Test: Sending local notification")
        systemNotificationManager.notify(100, notification)
        Log.d("FCM_DEBUG", "Test: Local notification sent")

        // 2. FCM notification
        Log.d("FCM_DEBUG", "Test: Sending FCM notification")
        this@ServiceRequestViewModel.notificationManager
            .sendServiceRequestAcceptedNotification(
                recipientUserId = currentUser.uid,
                requestId = "test_request_id",
                providerName = "Test Provider")
            .onFailure { e -> Log.e("FCM_DEBUG", "Test: Error sending FCM notification", e) }
        Log.d("FCM_DEBUG", "Test: FCM notification sent to user: ${currentUser.uid}")
      } catch (e: Exception) {
        Log.e("FCM_DEBUG", "Test: Error sending notification: ${e.message}", e)
        e.printStackTrace()
      }
    }
  }

  fun testNotification(seekerId: String) {
    viewModelScope.launch {
      try {
        notificationManager
            .sendServiceRequestAcceptedNotification(
                recipientUserId = seekerId,
                requestId = "test_request_id",
                providerName = "Test Provider")
            .onFailure { e -> Log.e("FCM_DEBUG", "Error sending test notification", e) }
        Log.d("FCM_DEBUG", "Test notification sent successfully")
      } catch (e: Exception) {
        Log.e("FCM_DEBUG", "Error sending test notification", e)
      }
    }
  }

  fun runFullNotificationTest() {
    viewModelScope.launch {
      try {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
          Log.e("FCM_DEBUG", "Test: No user logged in")
          return@launch
        }

        Log.d("FCM_DEBUG", "Test: Sending FCM notification")
        notificationManager
            .sendServiceRequestAcceptedNotification(
                recipientUserId = currentUser.uid,
                requestId = "test_request_id",
                providerName = "Test Provider")
            .onFailure { e -> Log.e("FCM_DEBUG", "Test: Error sending FCM notification", e) }
        Log.d("FCM_DEBUG", "Test: FCM notification sent to user: ${currentUser.uid}")
      } catch (e: Exception) {
        Log.e("FCM_DEBUG", "Test: Error running notification test", e)
      }
    }
  }
}
