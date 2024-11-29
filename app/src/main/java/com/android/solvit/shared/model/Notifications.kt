package com.android.solvit.shared.model

import com.google.firebase.Timestamp

data class Notification(
    val uid: String, // The unique identifier for the notification
    val providerId: String, // The ID of the provider for whom the notification is intended
    val title: String, // The title of the notification (short description)
    val message: String, // The detailed message for the notification
    val timestamp: Timestamp =
        Timestamp
            .now(), // The timestamp when the notification was created; defaults to current time
    val isRead: Boolean =
        false // Flag indicating if the notification has been read by the provider, defaults to
    // `false`
)
