package com.android.solvit.shared.model.review

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class ReviewViewModelTest {
  private lateinit var reviewRepository: ReviewRepository
  private lateinit var reviewViewModel: ReviewViewModel

  private var review =
      Review(
          uid = "1",
          authorId = "user1",
          serviceRequestId = "request1",
          providerId = "provider1",
          rating = 5,
          comment = "Great service!")

  private var review2 =
      Review(
          uid = "2",
          authorId = "user2",
          serviceRequestId = "request1",
          providerId = "provider2",
          rating = 4,
          comment = "Good service!")

  private var reviews = listOf(review, review2)

  @Before
  fun setUp() {
    reviewRepository = mock(ReviewRepository::class.java)
    reviewViewModel = ReviewViewModel(reviewRepository)
  }

  @Test
  fun getReviews_updatesReviewsStateFlow() = runTest {
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Review>) -> Unit>(0)
          onSuccess(reviews)
        }
        .`when`(reviewRepository)
        .getReviews(any(), any())

    reviewViewModel.getReviews()

    assertEquals(reviews, reviewViewModel.reviews.value)
  }

  @Test
  fun addReview_callsRepositoryAddReview() = runTest {
    reviewViewModel.addReview(review)

    verify(reviewRepository).addReview(any(), any(), any())
  }

  @Test
  fun getReviewsByProvider_updatesReviewsStateFlow() = runTest {
    val providerId = "provider1"
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Review>) -> Unit>(1)
          onSuccess(reviews)
        }
        .`when`(reviewRepository)
        .getReviewsByProvider(any(), any(), any())

    reviewViewModel.getReviewsByProvider(providerId)

    assertEquals(reviews, reviewViewModel.reviews.value)
  }

  @Test
  fun getAverageRating_returnsCorrectValue() {
    val serviceRequestId = "request1"
    val averageRating = 4.5
    `when`(reviewRepository.getAverageRating(serviceRequestId)).thenReturn(averageRating)

    val result = reviewViewModel.getAverageRating(serviceRequestId)

    assertEquals(averageRating, result, 0.0)
  }

  @Test
  fun selectReview_updatesSelectedReviewStateFlow() {
    reviewViewModel.selectReview(review)

    assertEquals(review, reviewViewModel.selectedReview.value)
  }
}
