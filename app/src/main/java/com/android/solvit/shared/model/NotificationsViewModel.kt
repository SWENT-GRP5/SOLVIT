package com.android.solvit.shared.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationsViewModel(private val repository: NotificationsRepository) : ViewModel() {

  private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
  val notifications: StateFlow<List<Notification>> = _notifications

  private var providerId: String = ""

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsViewModel(NotificationsRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  fun init(providerId: String) {
    this.providerId = providerId
    getNotifications(providerId)
  }

  fun getNotifications(providerId: String) {
    repository.getNotification(
        providerId = providerId, onSuccess = { _notifications.value = it }, onFailure = {})
  }

  fun sendNotifications(
      serviceRequest: ServiceRequest,
      providers: List<Provider>,
  ) {

    repository.sendNotification(
        serviceRequest, providers, onSuccess = { getNotifications(providerId) }, onFailure = {})
  }
}
