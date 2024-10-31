package com.android.solvit.seeker.model.profile

import com.android.solvit.shared.model.map.Location

data class SeekerProfile(
    val uid: String,
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val cachedLocations: List<Location> = emptyList()
)
