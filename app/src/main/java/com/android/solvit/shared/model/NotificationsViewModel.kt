package com.android.solvit.shared.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * NotificationsViewModel is responsible for managing the state and business logic related to
 * notifications. It interacts with the NotificationsRepository to fetch and send notifications and
 * provides an observable state to the UI layer via StateFlow.
 *
 * @param repository The repository used to handle notification-related operations.
 */
class NotificationsViewModel(private val repository: NotificationsRepository) : ViewModel() {

  // Backing property for notifications stored in a MutableStateFlow
  private val _notifications = MutableStateFlow<List<Notification>>(emptyList())

  /**
   * Public observable property for notifications exposed as a StateFlow. The UI can observe this to
   * get updates on the list of notifications.
   */
  val notifications: StateFlow<List<Notification>> = _notifications

  // The ID of the provider for whom the notifications are fetched
  private var providerId: String = ""

  /**
   * Companion object containing the ViewModel factory. The factory creates an instance of
   * NotificationsViewModel with a NotificationsRepositoryFirestore.
   */
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsViewModel(NotificationsRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  /**
   * Initializes the ViewModel by setting the providerId and fetching notifications for that
   * provider.
   *
   * @param providerId The ID of the provider whose notifications are being managed.
   */
  fun init(providerId: String) {
    this.providerId = providerId
    getNotifications(providerId)
  }

  /**
   * Fetches notifications for the given provider and updates the _notifications state.
   *
   * @param providerId The ID of the provider whose notifications are being fetched.
   */
  fun getNotifications(providerId: String) {
    repository.getNotification(
        providerId = providerId,
        onSuccess = { _notifications.value = it }, // Updates _notifications on success
        onFailure = { exception ->
          Log.e("NotificationsViewModel", "Failed to fetch notifications: ${exception.message}")
        })
  }

  /**
   * Sends notifications to a list of providers based on the given service request. Once the
   * notifications are sent, it refreshes the list of notifications for the current provider.
   *
   * @param serviceRequest The service request for which notifications are being sent.
   * @param providers The list of providers who might receive notifications.
   */
  fun sendNotifications(serviceRequest: ServiceRequest, providers: List<Provider>) {
    repository.sendNotification(
        serviceRequest,
        providers,
        onSuccess = { getNotifications(providerId) }, // Refreshes notifications after sending
        onFailure = { exception ->
          Log.e("NotificationsViewModel", "Failed to send notifications: ${exception.message}")
        })
  }

  /**
   * Marks a notification as read by updating the `isRead` field in Firestore.
   *
   * @param notification The notification to be marked as read.
   */
  fun markNotificationAsRead(notification: Notification) {
    repository.updateNotificationReadStatus(notification.uid, true)
  }
}
