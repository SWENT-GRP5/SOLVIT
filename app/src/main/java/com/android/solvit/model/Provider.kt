package com.android.solvit.model

import com.google.firebase.Timestamp

data class Provider(
    val uid: String,
    val name: String,
    val service: Services,
    val strongPoints: List<String>,
    val rating: Double,
    val price: Double,
    val deliveryTime: Timestamp,
    val languages: List<Language>
) {}
