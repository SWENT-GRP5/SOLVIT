package com.android.solvit.shared.model.chat

data class ChatMessage(
    val id: String = "",
    val message: String = "",
    val timestamp: Long? = System.currentTimeMillis(),
    val imageUrl: String = "",
    val senderId: String = "",
)
