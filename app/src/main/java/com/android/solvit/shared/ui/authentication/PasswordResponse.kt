package com.android.solvit.shared.ui.authentication

data class PasswordResponse(
    val passwords: List<String>,
    val response_date_time: String,
    val api_version: String,
    val api_last_update_date: String
)

