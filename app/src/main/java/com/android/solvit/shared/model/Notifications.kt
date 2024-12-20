package com.android.solvit.shared.model

import com.android.solvit.shared.model.request.ServiceRequest
import com.google.firebase.Timestamp

data class Notification(
    // The unique identifier for the notification
    val uid: String,
    // The ID of the provider for whom the notification is intended
    val providerId: String,
    // The title of the notification (short description)
    val title: String,
    // The detailed message for the notification
    val message: String,
    // The timestamp when the notification was created; defaults to current time
    val timestamp: Timestamp = Timestamp.now(),
    // The related service request
    val serviceRequest: ServiceRequest,
    // Flag indicating if the notification has been read by the provider, defaults to `false`
    var isRead: Boolean = false
)
