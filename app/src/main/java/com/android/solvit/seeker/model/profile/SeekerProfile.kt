package com.android.solvit.seeker.model.profile

import com.android.solvit.shared.model.map.Location

/**
 * Data class representing the profile of a Seeker in the system.
 *
 * @property uid Unique identifier for the Seeker, typically the user ID.
 * @property name First name of the Seeker.
 * @property lastname Last name of the Seeker.
 * @property imageUrl URL of the Seeker's profile image.
 * @property username Username chosen by the Seeker.
 * @property email Email address of the Seeker.
 * @property phone Phone number of the Seeker.
 * @property address Current location of the Seeker, represented by a [Location] object.
 * @property cachedLocations List of locations previously saved or cached by the Seeker.
 * @property preferences List of service preferences selected by the Seeker.
 */
data class SeekerProfile(
    val uid: String,
    val name: String = "",
    val lastname: String = "",
    val imageUrl: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: Location = Location(0.0, 0.0, ""),
    val cachedLocations: List<Location> = emptyList(),
    val preferences: List<String> = emptyList()
)
