package com.android.solvit.seeker.model.profile

import com.android.solvit.shared.model.map.Location

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
