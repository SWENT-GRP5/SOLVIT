package com.android.solvit.shared.model

import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest

interface NotificationsRepository {
  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  fun getNotification(
      providerId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun sendNotification(
      serviceRequest: ServiceRequest,
      providers: List<Provider>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun updateNotificationReadStatus(notificationId: String, isRead: Boolean)
}
