package com.android.solvit.shared.model.review

/**
 * Data class representing a review given by a user for a service provider.
 *
 * @property uid Unique identifier for the review.
 * @property authorId ID of the user who authored the review.
 * @property serviceRequestId ID of the related service request.
 * @property providerId ID of the provider receiving the review.
 * @property rating Numeric rating given by the user, typically from 1 to 5.
 * @property comment Textual feedback or comment provided by the user.
 */
data class Review(
    val uid: String,
    val authorId: String,
    val serviceRequestId: String,
    val providerId: String,
    val rating: Int,
    val comment: String
)
