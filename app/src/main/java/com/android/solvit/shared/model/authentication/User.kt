package com.android.solvit.shared.model.authentication

data class User(
    val uid: String,
    val role: String,
    val email: String = "",
    val profileData: Map<String, Any> = emptyMap()
)
