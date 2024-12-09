package com.android.solvit.shared.model.review

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepositoryFirestore(private val db: FirebaseFirestore) : ReviewRepository {
  private val collectionPath = "reviews"
  private val auth = FirebaseAuth.getInstance()

  override fun getNewUid(): String = db.collection(collectionPath).document().id

  override fun init(onSuccess: () -> Unit) {
    // Add an authentication state listener to call onSuccess when the user is authenticated
    auth.addAuthStateListener { it.currentUser?.let { onSuccess() } }
  }

  override fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    // Add a new review document to Firestore
    performFirestoreOperation(
        db.collection(collectionPath).document(review.uid).set(review), onSuccess, onFailure)
  }

  override fun getReviews(onSuccess: (List<Review>) -> Unit, onFailure: (Exception) -> Unit) {
    // Retrieve all reviews from Firestore
    db.collection(collectionPath).get().addOnCompleteListener { result ->
      if (result.isSuccessful) {
        val reviews = result.result?.mapNotNull { documentToReview(it) } ?: emptyList()
        onSuccess(reviews)
      } else {
        onFailure(result.exception ?: Exception("Unknown error"))
      }
    }
  }

  override fun getReviewsByServiceRequest(
      serviceRequestId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Query reviews by service request ID
    queryReviews("serviceRequestId", serviceRequestId, onSuccess, onFailure)
  }

  override fun getReviewsByProvider(
      providerId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Query reviews by provider ID
    queryReviews("providerId", providerId, onSuccess, onFailure)
  }

  override fun getReviewsByUser(
      userId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Query reviews by user ID
    queryReviews("authorId", userId, onSuccess, onFailure)
  }

  override suspend fun getAverageRating(serviceRequestId: String): Double {
    return calculateAverageRating("serviceRequestId", serviceRequestId)
  }

  override suspend fun getAverageRatingByProvider(providerId: String): Double {
    return calculateAverageRating("providerId", providerId)
  }

  override suspend fun getAverageRatingByUser(userId: String): Double {
    return calculateAverageRating("authorId", userId)
  }

  override fun getReview(
      reviewId: String,
      onSuccess: (Review) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Retrieve a specific review document from Firestore
    db.collection(collectionPath).document(reviewId).get().addOnCompleteListener { result ->
      if (result.isSuccessful) {
        val review = result.result?.let { documentToReview(it) }
        if (review != null) onSuccess(review) else onFailure(Exception("Review not found"))
      } else {
        onFailure(result.exception ?: Exception("Unknown error"))
      }
    }
  }

  override fun updateReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    // Update an existing review document in Firestore
    performFirestoreOperation(
        db.collection(collectionPath).document(review.uid).set(review), onSuccess, onFailure)
  }

  override fun deleteReview(
      reviewId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Delete a specific review document from Firestore
    performFirestoreOperation(
        db.collection(collectionPath).document(reviewId).delete(), onSuccess, onFailure)
  }

  fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Perform a Firestore operation and handle success or failure
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) onSuccess()
      else
          result.exception?.let {
            Log.e("ReviewRepository", "Error in Firestore operation", it)
            onFailure(it)
          }
    }
  }

  fun queryReviews(
      field: String,
      value: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Query reviews based on a specific field and value
    db.collection(collectionPath).whereEqualTo(field, value).get().addOnCompleteListener { result ->
      if (result.isSuccessful) {
        val reviews = result.result?.mapNotNull { documentToReview(it) } ?: emptyList()
        onSuccess(reviews)
      } else {
        onFailure(result.exception ?: Exception("Unknown error"))
      }
    }
  }

  suspend fun calculateAverageRating(
      field: String,
      value: String,
  ): Double {
    // Calculate the average rating based on a specific field and value
    var averageRating = 5.0
    Log.e("Collection", "${field} : $value")
    return try {
      val result = db.collection(collectionPath).whereEqualTo(field, value).get().await()
      Log.e("averageRating Successfull", "$averageRating")
      val reviews = result.mapNotNull { documentToReview(it) } ?: emptyList()
      Log.e("Collection", "${reviews}")
      averageRating = reviews.map { it.rating }.average()
      Log.e("AverageRating", "${averageRating}")

      averageRating
    } catch (e: Exception) {
      Log.e("Failed to get Average Rating", "$e")
      averageRating
    }
  }

  fun documentToReview(document: DocumentSnapshot): Review? {
    return try {
      Review(
          uid = document.id,
          authorId = document.getString("authorId") ?: "",
          serviceRequestId = document.getString("serviceRequestId") ?: "",
          providerId = document.getString("providerId") ?: "",
          rating = document.getLong("rating")?.toInt() ?: 0,
          comment = document.getString("comment") ?: "")
    } catch (e: Exception) {
      Log.e("ReviewRepositoryFirestore", "Error converting document to a Review", e)
      null
    }
  }
}
