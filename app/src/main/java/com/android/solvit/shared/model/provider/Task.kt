package com.android.solvit.shared.model.provider

data class Task(
    val id: String,
    val providerId: String, // Reference to the provider
    val description: String,
    val price: Double,
    val status: String // e.g., "completed", "pending"
)
