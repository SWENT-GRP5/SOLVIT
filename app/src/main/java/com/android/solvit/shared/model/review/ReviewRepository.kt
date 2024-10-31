package com.android.solvit.shared.model.review

interface ReviewRepository {
  /**
   * Generates a new unique identifier.
   *
   * @return A new unique identifier as a String.
   */
  fun getNewUid(): String

  /**
   * Initializes the repository.
   *
   * @param onSuccess A callback function to be executed upon successful initialization.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Adds a new review to the repository.
   *
   * @param review The review to be added.
   * @param onSuccess A callback function to be executed upon successful addition.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves a specific review by its ID.
   *
   * @param reviewId The ID of the review.
   * @param onSuccess A callback function to be executed upon successful retrieval, with the review.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun getReview(reviewId: String, onSuccess: (Review) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates an existing review in the repository.
   *
   * @param review The review to be updated.
   * @param onSuccess A callback function to be executed upon successful update.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun updateReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a specific review by its ID.
   *
   * @param reviewId The ID of the review to be deleted.
   * @param onSuccess A callback function to be executed upon successful deletion.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun deleteReview(reviewId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves a list of all reviews.
   *
   * @param onSuccess A callback function to be executed upon successful retrieval, with a list of
   *   reviews.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun getReviews(onSuccess: (List<Review>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves a list of reviews for a specific service request.
   *
   * @param serviceRequestId The ID of the service request.
   * @param onSuccess A callback function to be executed upon successful retrieval, with a list of
   *   reviews.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun getReviewsByServiceRequest(
      serviceRequestId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves a list of reviews for a specific provider.
   *
   * @param providerId The ID of the provider.
   * @param onSuccess A callback function to be executed upon successful retrieval, with a list of
   *   reviews.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun getReviewsByProvider(
      providerId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves a list of reviews for a specific user.
   *
   * @param userId The ID of the user.
   * @param onSuccess A callback function to be executed upon successful retrieval, with a list of
   *   reviews.
   * @param onFailure A callback function to be executed upon failure, with an Exception.
   */
  fun getReviewsByUser(
      userId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves the average rating for a specific service request.
   *
   * @param serviceRequestId The ID of the service request.
   * @return The average rating as a Double.
   */
  fun getAverageRating(serviceRequestId: String): Double

  /**
   * Retrieves the average rating for a specific provider.
   *
   * @param providerId The ID of the provider.
   * @return The average rating as a Double.
   */
  fun getAverageRatingByProvider(providerId: String): Double

  /**
   * Retrieves the average rating for a specific user.
   *
   * @param userId The ID of the user.
   * @return The average rating as a Double.
   */
  fun getAverageRatingByUser(userId: String): Double
}
