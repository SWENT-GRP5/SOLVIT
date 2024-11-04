package com.android.solvit.shared.model.chat

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val imageUrl: String = "",
    val senderId: String = "",
)
