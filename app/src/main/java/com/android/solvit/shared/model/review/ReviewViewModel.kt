package com.android.solvit.shared.model.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReviewViewModel(private val repository: ReviewRepository) : ViewModel() {
  private val _reviews = MutableStateFlow<List<Review>>(emptyList())
  val reviews: StateFlow<List<Review>> = _reviews

  private val _selectedReview = MutableStateFlow<Review?>(null)
  val selectedReview: StateFlow<Review?> = _selectedReview

  init {
    repository.init { getReviews() }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReviewViewModel(ReviewRepositoryFirestore(Firebase.firestore)) as T
          }
        }
  }

  fun getNewUid(): String {
    return repository.getNewUid()
  }

  fun getReviews() {
    repository.getReviews(
        onSuccess = { _reviews.value = it },
        onFailure = { exception -> Log.e("ReviewViewModel", "Error fetching Reviews", exception) })
  }

  fun addReview(review: Review) {
    repository.addReview(
        review,
        onSuccess = { getReviews() },
        onFailure = { exception -> Log.e("ReviewViewModel", "Error adding Review", exception) })
  }

  fun deleteReview(review: Review) {
    repository.deleteReview(
        review.uid,
        onSuccess = { getReviews() },
        onFailure = { exception -> Log.e("ReviewViewModel", "Error deleting Review", exception) })
  }

  fun getReviewsByServiceRequest(serviceRequestId: String) {
    repository.getReviewsByServiceRequest(
        serviceRequestId,
        onSuccess = { _reviews.value = it },
        onFailure = { exception -> Log.e("ReviewViewModel", "Error fetching Reviews", exception) })
  }

  fun getReviewsByProvider(providerId: String) {
    repository.getReviewsByProvider(
        providerId,
        onSuccess = { _reviews.value = it },
        onFailure = { exception -> Log.e("ReviewViewModel", "Error fetching Reviews", exception) })
  }

  fun getReviewsByUser(userId: String) {
    repository.getReviewsByUser(
        userId,
        onSuccess = { _reviews.value = it },
        onFailure = { exception -> Log.e("ReviewViewModel", "Error fetching Reviews", exception) })
  }

  fun getAverageRating(serviceRequestId: String): Double {
    return repository.getAverageRating(serviceRequestId)
  }

  fun getAverageRatingByProvider(providerId: String): Double {
    return repository.getAverageRatingByProvider(providerId)
  }

  fun getAverageRatingByUser(userId: String): Double {
    return repository.getAverageRatingByUser(userId)
  }

  fun selectReview(review: Review) {
    _selectedReview.value = review
  }
}
