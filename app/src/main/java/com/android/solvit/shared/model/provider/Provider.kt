package com.android.solvit.shared.model.provider

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp

data class Provider(
    val uid: String,
    val name: String,
    val service: Services,
    val imageUrl: String,
    val location: Location,
    val description: String,
    val popular: Boolean,
    val rating: Double,
    val price: Double,
    val deliveryTime: Timestamp,
    val languages: List<Language>
) {}
