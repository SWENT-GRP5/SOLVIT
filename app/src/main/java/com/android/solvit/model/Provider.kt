package com.android.solvit.model

import com.android.solvit.model.map.Location
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
