package com.android.solvit.shared.model.packages

import kotlinx.serialization.Serializable

/**
 * Data class representing a package proposal in the application. This class is used for managing
 * service packages proposed by providers.
 *
 * @property uid Unique identifier of the package proposal.
 * @property packageNumber Numeric identifier for sorting or categorization purposes.
 * @property providerId Unique identifier of the provider creating the package.
 * @property title Title of the package proposal, summarizing its main offering.
 * @property description Detailed explanation of the services provided in the package.
 * @property price Optional price for the package, indicating its cost.
 * @property bulletPoints List of key features or highlights describing the package.
 */
@Serializable
data class PackageProposal(
    val uid: String,
    val packageNumber: Double = 0.0,
    val providerId: String? = null,
    val title: String,
    val description: String,
    val price: Double? = null,
    val bulletPoints: List<String>
)
