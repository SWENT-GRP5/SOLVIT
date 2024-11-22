package com.android.solvit.shared.model.provider

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp

data class Provider(
    val uid: String = "",
    val name: String = "",
    var service: Services = Services.TUTOR,
    val imageUrl: String = "",
    var companyName: String = "",
    var phone: String = "",
    var location: Location = Location(0.0, 0.0, ""),
    val description: String = "",
    val popular: Boolean = false,
    val rating: Double = 0.0,
    val price: Double = 0.0,
    val deliveryTime: Timestamp = Timestamp.now(),
    var languages: List<Language> = emptyList()
) {}
