package com.android.solvit.shared.model.review

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ReviewRepositoryFirestoreTest {
  @Mock private lateinit var mockAuth: FirebaseAuth
  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  private lateinit var reviewRepositoryFirestore: ReviewRepositoryFirestore

  private var review1 =
      Review(
          uid = "1",
          authorId = "user1",
          serviceRequestId = "request1",
          providerId = "provider1",
          rating = 3,
          comment = "Great service!")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    reviewRepositoryFirestore = ReviewRepositoryFirestore(mockFirestore)

    `when`(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  @Test
  fun init_doesNotCallOnSuccessWhenUserIsNotAuthenticated() {
    `when`(mockAuth.currentUser).thenReturn(null)

    var onSuccessCalled = false
    reviewRepositoryFirestore.init { onSuccessCalled = true }

    assertFalse(onSuccessCalled)
  }

  @Test
  fun getNewUid_returnsUniqueId() {
    `when`(mockDocumentReference.id).thenReturn("uniqueId")
    val uid = reviewRepositoryFirestore.getNewUid()
    assertNotNull(uid)
    assertTrue(uid.isNotEmpty())
  }

  @Test
  fun addReview_successful() {
    `when`(mockDocumentReference.set(review1)).thenReturn(mockSuccessfulTask())

    reviewRepositoryFirestore.addReview(review1, { /* onSuccess */}, { fail("Should not fail") })
  }

  @Test
  fun updateReview_failure() {
    `when`(mockDocumentReference.set(review1)).thenReturn(mockFailureTask("Firestore error"))

    reviewRepositoryFirestore.updateReview(
        review1, { fail("Should not succeed") }, { /* onFailure */})
  }

  @Test
  fun updateReview_successful() {
    `when`(mockDocumentReference.set(review1)).thenReturn(mockSuccessfulTask())

    reviewRepositoryFirestore.updateReview(review1, { /* onSuccess */}, { fail("Should not fail") })
  }

  @Test
  fun addReview_failure() {
    `when`(mockDocumentReference.set(review1)).thenReturn(mockFailureTask("Firestore error"))

    reviewRepositoryFirestore.addReview(review1, { fail("Should not succeed") }, { /* onFailure */})
  }

  @Test
  fun getReviews_successful() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    mockDocumentSnapshotWithReviewData()

    reviewRepositoryFirestore.getReviews(
        { reviews -> assertReviewListContainsExpectedReview(reviews, "1") },
        { fail("Should not fail") })
  }

  @Test
  fun getReviews_failure() {
    `when`(mockCollectionReference.get()).thenReturn(mockQuerySnapshotFailureTask())

    var onFailureCalled = false

    reviewRepositoryFirestore.getReviews(
        { fail("Should not succeed") },
        { exception ->
          assertNotNull(exception)
          assertEquals("java.lang.Exception: Firestore error", exception.message)
          onFailureCalled = true
        })

    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(onFailureCalled)
  }

  @Test
  fun getReviews_emptyResult_returnsEmptyList() {
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())

    reviewRepositoryFirestore.getReviews(
        { reviews ->
          assertNotNull(reviews)
          assertTrue(reviews.isEmpty())
        },
        { fail("Should not fail") })
  }

  @Test
  fun getReview_successful() {
    `when`(mockDocumentReference.get()).thenReturn(taskForDocumentSnapshot())
    mockDocumentSnapshotWithReviewData()

    reviewRepositoryFirestore.getReview(
        "1",
        { review ->
          assertNotNull(review)
          assertEquals("1", review.uid)
          assertEquals("user1", review.authorId)
          assertEquals("request1", review.serviceRequestId)
          assertEquals("provider1", review.providerId)
          assertEquals(5, review.rating)
          assertEquals("Great service!", review.comment)
        },
        { fail("Should not fail") })
  }

  @Test
  fun getReview_failure() {
    `when`(mockDocumentReference.get()).thenReturn(taskForDocumentSnapshot())

    var onFailureCalled = false
    reviewRepositoryFirestore.getReview(
        "1",
        { fail("Should not succeed") },
        { exception ->
          assertNotNull(exception)
          assertEquals("Review not found", exception.message)
          onFailureCalled = true
        })

    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(onFailureCalled)
  }

  @Test
  fun getReview_notFound() {
    `when`(mockDocumentReference.get()).thenReturn(taskForDocumentSnapshot())
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())

    reviewRepositoryFirestore.getReview(
        "1",
        { fail("Should not succeed") },
        { exception ->
          assertNotNull(exception)
          assertEquals("Review not found", exception.message)
        })
  }

  @Test
  fun deleteReview_successful() {
    `when`(mockDocumentReference.delete()).thenReturn(mockSuccessfulTask())

    reviewRepositoryFirestore.deleteReview("1", { /* onSuccess */}, { fail("Should not fail") })
  }

  @Test
  fun deleteReview_failure() {
    `when`(mockDocumentReference.delete()).thenReturn(mockFailureTask("Firestore error"))

    reviewRepositoryFirestore.deleteReview("1", { fail("Should not succeed") }, { /* onFailure */})
  }

  @Test
  fun documentToReview_validDocument_returnsReview() {
    mockDocumentSnapshotWithReviewData()
    val review = reviewRepositoryFirestore.documentToReview(mockDocumentSnapshot)

    assertNotNull(review)
    assertEquals("1", review?.uid)
    assertEquals("user1", review?.authorId)
    assertEquals("request1", review?.serviceRequestId)
    assertEquals("provider1", review?.providerId)
    assertEquals(5, review?.rating)
    assertEquals("Great service!", review?.comment)
  }

  @Test
  fun documentToReview_missingFields_returnsReviewWithDefaults() {
    `when`(mockDocumentSnapshot.id).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("authorId")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("serviceRequestId")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("providerId")).thenReturn(null)
    `when`(mockDocumentSnapshot.getLong("rating")).thenReturn(null)
    `when`(mockDocumentSnapshot.getString("comment")).thenReturn(null)

    val review = reviewRepositoryFirestore.documentToReview(mockDocumentSnapshot)

    assertNotNull(review)
    assertEquals("1", review?.uid)
    assertEquals("", review?.authorId)
    assertEquals("", review?.serviceRequestId)
    assertEquals("", review?.providerId)
    assertEquals(0, review?.rating)
    assertEquals("", review?.comment)
  }

  @Test
  fun queryReviews_successful() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)
    `when`(mockCollectionReference.whereEqualTo("field", "value"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    mockDocumentSnapshotWithReviewData()

    reviewRepositoryFirestore.queryReviews(
        "field",
        "value",
        { reviews -> assertReviewListContainsExpectedReview(reviews, "1") },
        { fail("Should not fail") })
  }

  @Test
  fun queryReviews_failure() {
    `when`(mockCollectionReference.whereEqualTo("field", "value"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(mockQuerySnapshotFailureTask())

    var onFailureCalled = false
    reviewRepositoryFirestore.queryReviews(
        "field",
        "value",
        { fail("Should not succeed") },
        { exception ->
          assertNotNull(exception)
          assertEquals("java.lang.Exception: Firestore error", exception.message)
          onFailureCalled = true
        })

    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(onFailureCalled)
  }

  @Test
  fun queryReviews_emptyResult_returnsEmptyList() {
    `when`(mockCollectionReference.whereEqualTo("field", "value"))
        .thenReturn(mockCollectionReference)
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())

    reviewRepositoryFirestore.queryReviews(
        "field",
        "value",
        { reviews ->
          assertNotNull(reviews)
          assertTrue(reviews.isEmpty())
        },
        { fail("Should not fail") })
  }

  @Test
  fun getReviewsByServiceRequest_successful() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)
    `when`(mockCollectionReference.whereEqualTo("serviceRequestId", "request1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    mockDocumentSnapshotWithReviewData()

    reviewRepositoryFirestore.getReviewsByServiceRequest(
        "request1",
        { reviews -> assertReviewListContainsExpectedReview(reviews, "1") },
        { fail("Should not fail") })
  }

  @Test
  fun getReviewsByServiceRequest_failure() {
    `when`(mockCollectionReference.whereEqualTo("serviceRequestId", "request1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(mockQuerySnapshotFailureTask())

    reviewRepositoryFirestore.getReviewsByServiceRequest(
        "request1",
        { fail("Should not succeed") },
        { exception ->
          assertNotNull(exception)
          assertEquals("Firestore error", exception.message)
        })
  }

  @Test
  fun getReviewsByServiceRequest_emptyResult_returnsEmptyList() {
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
    `when`(mockCollectionReference.whereEqualTo("serviceRequestId", "request1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())

    reviewRepositoryFirestore.getReviewsByServiceRequest(
        "request1",
        { reviews ->
          assertNotNull(reviews)
          assertTrue(reviews.isEmpty())
        },
        { fail("Should not fail") })
  }

  @Test
  fun getReviewsByProvider_successful() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)
    `when`(mockCollectionReference.whereEqualTo("providerId", "provider1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    mockDocumentSnapshotWithReviewData()

    reviewRepositoryFirestore.getReviewsByProvider(
        "provider1",
        { reviews -> assertReviewListContainsExpectedReview(reviews, "1") },
        { fail("Should not fail") })
  }

  @Test
  fun getReviewsByProvider_failure() {
    `when`(mockCollectionReference.whereEqualTo("providerId", "provider1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(mockQuerySnapshotFailureTask())

    reviewRepositoryFirestore.getReviewsByProvider(
        "provider1",
        { fail("Should not succeed") },
        { exception ->
          assertNotNull(exception)
          assertEquals("Firestore error", exception.message)
        })
  }

  @Test
  fun getReviewsByProvider_emptyResult_returnsEmptyList() {
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
    `when`(mockCollectionReference.whereEqualTo("providerId", "provider1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())

    reviewRepositoryFirestore.getReviewsByProvider(
        "provider1",
        { reviews ->
          assertNotNull(reviews)
          assertTrue(reviews.isEmpty())
        },
        { fail("Should not fail") })
  }

  @Test
  fun getReviewsByUser_successful() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)
    `when`(mockCollectionReference.whereEqualTo("authorId", "user1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    mockDocumentSnapshotWithReviewData()

    reviewRepositoryFirestore.getReviewsByUser(
        "user1",
        { reviews -> assertReviewListContainsExpectedReview(reviews, "1") },
        { fail("Should not fail") })
  }

  @Test
  fun getReviewsByUser_failure() {
    `when`(mockCollectionReference.whereEqualTo("authorId", "user1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(mockQuerySnapshotFailureTask())

    reviewRepositoryFirestore.getReviewsByUser(
        "user1",
        { fail("Should not succeed") },
        { exception ->
          assertNotNull(exception)
          assertEquals("Firestore error", exception.message)
        })
  }

  @Test
  fun getReviewsByUser_emptyResult_returnsEmptyList() {
    `when`(mockQuerySnapshot.documents).thenReturn(emptyList())
    `when`(mockCollectionReference.whereEqualTo("authorId", "user1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())

    reviewRepositoryFirestore.getReviewsByUser(
        "user1",
        { reviews ->
          assertNotNull(reviews)
          assertTrue(reviews.isEmpty())
        },
        { fail("Should not fail") })
  }

  @Test
  fun calculateAverageRating_multipleReviews_returnsCorrectAverage() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot, mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)
    `when`(mockCollectionReference.whereEqualTo("field", "value"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    `when`(mockDocumentSnapshot.getLong("rating")).thenReturn(4L, 6L)

    val averageRating = reviewRepositoryFirestore.calculateAverageRating("field", "value")
    assertEquals(5.0, averageRating, 0.0)
  }

  @Test
  fun calculateAverageRating_successfulResult_executesAndReturnsCorrectAverage() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot, mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)

    `when`(mockCollectionReference.whereEqualTo("field", "value"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    `when`(mockDocumentSnapshot.getLong("rating")).thenReturn(4L, 6L)

    val averageRating = reviewRepositoryFirestore.calculateAverageRating("field", "value")

    assertEquals(5.0, averageRating, 0.0)
  }

  @Test
  fun getAverageRating_returnsCorrectResult() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot, mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)

    `when`(mockCollectionReference.whereEqualTo("serviceRequestId", "1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    `when`(mockDocumentSnapshot.getLong("rating")).thenReturn(4L, 6L)

    val averageRating = reviewRepositoryFirestore.getAverageRating("1")

    assertEquals(5.0, averageRating, 0.0)
  }

  @Test
  fun getAverageRatingByProvider_returnsCorrectResult() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot, mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)

    `when`(mockCollectionReference.whereEqualTo("providerId", "1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    `when`(mockDocumentSnapshot.getLong("rating")).thenReturn(4L, 6L)

    val averageRating = reviewRepositoryFirestore.getAverageRatingByProvider("1")

    assertEquals(5.0, averageRating, 0.0)
  }

  @Test
  fun getAverageRatingByUser_returnsCorrectResult() {
    val mockDocumentSnapshotList = listOf(mockDocumentSnapshot, mockDocumentSnapshot)
    `when`(mockQuerySnapshot.documents).thenReturn(mockDocumentSnapshotList)

    `when`(mockCollectionReference.whereEqualTo("authorId", "1"))
        .thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.get()).thenReturn(taskForQuerySnapshot())
    `when`(mockDocumentSnapshot.getLong("rating")).thenReturn(4L, 6L)

    val averageRating = reviewRepositoryFirestore.getAverageRatingByUser("1")

    assertEquals(5.0, averageRating, 0.0)
  }

  private fun mockDocumentSnapshotWithReviewData() {
    `when`(mockDocumentSnapshot.id).thenReturn("1")
    `when`(mockDocumentSnapshot.getString("authorId")).thenReturn("user1")
    `when`(mockDocumentSnapshot.getString("serviceRequestId")).thenReturn("request1")
    `when`(mockDocumentSnapshot.getString("providerId")).thenReturn("provider1")
    `when`(mockDocumentSnapshot.getLong("rating")).thenReturn(5L)
    `when`(mockDocumentSnapshot.getString("comment")).thenReturn("Great service!")
  }

  private fun mockSuccessfulTask(): Task<Void> = Tasks.forResult(null)

  private fun mockFailureTask(exceptionMessage: String): Task<Void> =
      Tasks.forException(Exception(exceptionMessage))

  private fun mockQuerySnapshotFailureTask(): Task<QuerySnapshot> =
      Tasks.forException(Exception(Exception("Firestore error")))

  private fun taskForQuerySnapshot(): Task<QuerySnapshot> = Tasks.forResult(mockQuerySnapshot)

  private fun taskForDocumentSnapshot(): Task<DocumentSnapshot> =
      Tasks.forResult(mockDocumentSnapshot)

  private fun assertReviewListContainsExpectedReview(
      reviews: List<Review>,
      expectedReviewId: String
  ) {
    assertNotNull(reviews)
    assertEquals(1, reviews.size)
    assertEquals(expectedReviewId, reviews[0].uid)
  }
}
