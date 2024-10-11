package com.android.solvit.seeker.model.profile

data class UserProfile(
    val uid: String,
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = ""
)
