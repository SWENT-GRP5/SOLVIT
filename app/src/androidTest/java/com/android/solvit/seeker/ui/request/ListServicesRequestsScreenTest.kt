package com.android.solvit.seeker.ui.request

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestType
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.ui.requests.ListRequestsFeedScreen
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class ListServicesRequestsScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  @get:Rule val composeTestRule = createComposeRule()

  val request =
      listOf(
          ServiceRequest(
              title = "Bathtub leak",
              description = "I hit my bath too hard and now it's leaking",
              assigneeName = "Nathan",
              dueDate = Timestamp(Calendar.getInstance().time),
              location =
                  Location(
                      48.8588897,
                      2.3200410217200766,
                      "Paris, Île-de-France, France métropolitaine, France"),
              status = ServiceRequestStatus.PENDING,
              uid = "gIoUWJGkTgLHgA7qts59",
              type = ServiceRequestType.PLUMBING,
              imageUrl =
                  "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F588d3bd9-bcb7-47bc-9911-61fae59eaece.jpg?alt=media&token=5f747f33-9732-4b90-9b34-55e28732ebc3"))

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    composeTestRule.setContent { ListRequestsFeedScreen(serviceRequestViewModel) }

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<ServiceRequest>) -> Unit>(0)
      onSuccess(request) // Simulate success
    }

    serviceRequestViewModel.getServiceRequests()
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.onNodeWithTag("ListRequestsScreen").isDisplayed()

    composeTestRule.onNodeWithTag("RequestsTopBar").isDisplayed()

    composeTestRule.onNodeWithTag("ScreenContent")
    composeTestRule.onNodeWithTag("MenuOption").isDisplayed()
    composeTestRule.onNodeWithTag("SloganIcon").isDisplayed()

    composeTestRule.onNodeWithTag("SearchBar").isDisplayed()

    composeTestRule.onNodeWithTag("TitleScreen").isDisplayed()

    composeTestRule.onNodeWithTag("requests").isDisplayed()
  }

  @Test
  fun testSearchBar() {
    composeTestRule.onNodeWithTag("SearchBar").performTextInput("French")
    // TODO complete the test when implement searching functionnality
  }
}
