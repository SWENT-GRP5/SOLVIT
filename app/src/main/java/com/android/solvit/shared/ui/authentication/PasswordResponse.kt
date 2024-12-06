package com.android.solvit.shared.ui.authentication

/**
 * A data class representing the response from the password generation API.
 *
 * @property passwords A list of generated passwords.
 * @property response_date_time The date and time when the response was generated.
 * @property api_version The current version of the API.
 * @property api_last_update_date The last update date of the API.
 *
 * This class encapsulates all the information returned by the API when requesting password
 * generation, including the generated passwords and metadata about the API.
 */
data class PasswordResponse(
    val passwords: List<String>,
    val response_date_time: String,
    val api_version: String,
    val api_last_update_date: String
)
