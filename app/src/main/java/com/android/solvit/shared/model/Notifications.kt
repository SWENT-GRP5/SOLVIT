package com.android.solvit.shared.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String,
    val providerId: String,
    val title: String,
    val message: String,
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)