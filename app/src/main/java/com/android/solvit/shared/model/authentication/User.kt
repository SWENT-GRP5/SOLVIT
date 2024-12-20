package com.android.solvit.shared.model.authentication

import com.android.solvit.shared.model.map.Location

/**
 * Data class representing a user in the application, including personal information, role, and
 * account details.
 *
 * @property uid The unique identifier for the user.
 * @property role The role assigned to the user (e.g., provider, client).
 * @property userName The user's display name (optional).
 * @property email The user's email address (optional).
 * @property locations A list of saved user locations (optional).
 * @property registrationCompleted Indicates whether the user's registration process is complete.
 */
data class User(
    val uid: String,
    val role: String,
    val userName: String = "",
    val email: String = "",
    val locations: List<Location> = emptyList(),
    val registrationCompleted: Boolean = true,
)
