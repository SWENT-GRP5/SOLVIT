package com.android.solvit.shared.model.provider

import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services

/**
 * Data class representing a service provider in the application.
 *
 * @property uid Unique identifier for the provider.
 * @property name Name of the provider.
 * @property service The type of service the provider offers.
 * @property imageUrl URL of the provider's profile image.
 * @property companyName Name of the provider's company, if applicable.
 * @property phone Contact phone number of the provider.
 * @property location Geographic location of the provider's base.
 * @property description Detailed description of the provider's services.
 * @property popular Indicates whether the provider is popular.
 * @property rating Average rating of the provider based on user feedback.
 * @property price Base price for the provider's services.
 * @property nbrOfJobs Number of jobs completed by the provider.
 * @property languages List of languages the provider can communicate in.
 * @property schedule Provider's availability and schedule details.
 */
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
