package com.android.solvit.shared.model

import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest

/** Interface defining methods for managing notifications in the application. */
interface NotificationsRepository {
  /**
   * Generates a new unique identifier for a notification.
   *
   * @return A unique ID string.
   */
  fun getNewUid(): String

  /**
   * Initializes the notifications repository.
   *
   * @param onSuccess Callback function executed upon successful initialization.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves notifications for a specific provider.
   *
   * @param providerId The unique identifier of the provider.
   * @param onSuccess Callback function with a list of notifications if successful.
   * @param onFailure Callback function with an exception if retrieval fails.
   */
  fun getNotification(
      providerId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Sends notifications to providers based on a service request.
   *
   * @param serviceRequest The service request that triggers the notification.
   * @param providers A list of providers to notify.
   * @param onSuccess Callback function executed upon successful notification sending.
   * @param onFailure Callback function with an exception if sending fails.
   */
  fun sendNotification(
      serviceRequest: ServiceRequest,
      providers: List<Provider>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates the read status of a specific notification.
   *
   * @param notificationId The unique ID of the notification to update.
   * @param isRead A Boolean value indicating whether the notification has been read.
   */
  fun updateNotificationReadStatus(notificationId: String, isRead: Boolean)
}
