package com.android.solvit.seeker.ui.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.ReviewRepository
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class CreateReviewScreenTest {

  private lateinit var reviewRepository: ReviewRepository
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var listProviderRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  private val request =
      ServiceRequest(
          uid = "1",
          title = "title",
          type = Services.PLUMBER,
          description = "description",
          userId = "0",
          providerId = "1",
          dueDate = Timestamp(0, 0),
          meetingDate = Timestamp(0, 0),
          location = Location(0.0, 0.0, "address"),
          imageUrl = null,
          packageId = null,
          agreedPrice = 10.0,
          status = ServiceRequestStatus.COMPLETED,
      )

  private val invalidRequest =
      ServiceRequest(
          uid = "1",
          title = "title",
          type = Services.PLUMBER,
          description = "description",
          userId = "0",
          providerId = null,
          dueDate = Timestamp(0, 0),
          meetingDate = Timestamp(0, 0),
          location = Location(0.0, 0.0, "address"),
          imageUrl = null,
          packageId = null,
          agreedPrice = 10.0,
          status = ServiceRequestStatus.COMPLETED,
      )

  private val provider = Provider(uid = "1")

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    reviewRepository = mock(ReviewRepository::class.java)
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    listProviderRepository = mock(ProviderRepository::class.java)
    navController = mock(NavController::class.java)
    navigationActions = mock(NavigationActions::class.java)

    reviewViewModel = ReviewViewModel(reviewRepository)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    listProviderViewModel = ListProviderViewModel(listProviderRepository)

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ServiceRequest>) -> Unit>(0)
      onSuccess(listOf(request, invalidRequest))
    }
    `when`(listProviderRepository.getProviders(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Provider>) -> Unit>(1)
      onSuccess(listOf(provider))
    }

    `when`(reviewRepository.getNewUid()).thenReturn("0")
    composeTestRule.setContent {
      CreateReviewScreen(
          reviewViewModel, serviceRequestViewModel, listProviderViewModel, navigationActions)
    }
  }

  @Test
  fun displayAllComponents() {
    serviceRequestViewModel.selectRequest(request)
    composeTestRule.onNodeWithTag("reviewTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestPrice").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestProviderAndLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mapCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ratingBar").assertIsDisplayed()
    for (i in 1..5) {
      composeTestRule.onNodeWithTag("reviewStar$i").assertIsDisplayed()
    }
    composeTestRule.onNodeWithTag("reviewComment").assertIsDisplayed()
    composeTestRule.onNodeWithTag("submitReviewButton").assertIsDisplayed()
  }

  @Test
  fun submitValidReview() = runTest {
    `when`(reviewRepository.getAverageRatingByProvider(any())).thenReturn(4.0)
    serviceRequestViewModel.selectRequest(request)
    composeTestRule.onNodeWithTag("reviewStar5").performClick()
    composeTestRule.onNodeWithTag("reviewComment").performTextInput("comment")
    composeTestRule.onNodeWithTag("submitReviewButton").performClick()
    verify(reviewRepository).addReview(any(), any(), any())
  }

  @Test
  fun submitInvalidReview() {
    serviceRequestViewModel.selectRequest(invalidRequest)
    composeTestRule.onNodeWithTag("reviewStar5").performClick()
    composeTestRule.onNodeWithTag("reviewComment").performTextInput("comment")
    composeTestRule.onNodeWithTag("submitReviewButton").performClick()
    verify(reviewRepository, never()).addReview(any(), any(), any())
  }
}
