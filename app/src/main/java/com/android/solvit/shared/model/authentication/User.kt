package com.android.solvit.shared.model.authentication

import com.android.solvit.shared.model.map.Location

data class User(
    val uid: String,
    val role: String,
    val email: String = "",
    val locations: List<Location> = emptyList()
)
