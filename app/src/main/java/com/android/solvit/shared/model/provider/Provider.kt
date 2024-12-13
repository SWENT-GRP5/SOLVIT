package com.android.solvit.shared.model.provider

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services

data class Provider(
    val uid: String = "",
    val name: String = "",
    val service: Services = Services.OTHER,
    val imageUrl: String = "",
    val companyName: String = "",
    val phone: String = "",
    val location: Location = Location(0.0, 0.0, ""),
    val description: String = "",
    val popular: Boolean = false,
    val rating: Double = 1.0,
    val price: Double = 0.0,
    val nbrOfJobs: Double = 0.0,
    val languages: List<Language> = emptyList(),
    val schedule: Schedule = Schedule(),
)
