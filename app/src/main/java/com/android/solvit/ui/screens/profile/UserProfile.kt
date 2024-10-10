package com.android.solvit.ui.screens.profile

data class UserProfile(
    val uid: String,
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = ""
)
