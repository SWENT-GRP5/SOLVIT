package com.android.solvit.shared.ui.booking

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.packages.PackageProposalRepository
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.GregorianCalendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class BookingScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var providerRepository: ProviderRepository
  private lateinit var providerViewModel: ListProviderViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions
  private lateinit var packageProposalRepository: PackageProposalRepository
  private lateinit var packageProposalViewModel: PackageProposalViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val serviceRequest =
      ServiceRequest(
          uid = "1",
          title = "Test Request",
          description = "Test Description",
          userId = "1",
          providerId = "1",
          dueDate = Timestamp(GregorianCalendar(2021, 1, 1, 12, 30).time),
          meetingDate = Timestamp(GregorianCalendar(2021, 1, 1, 12, 30).time),
          location = Location(name = "EPFL", latitude = 0.0, longitude = 0.0),
          imageUrl = null,
          packageId = "1",
          agreedPrice = 200.15,
          type = Services.PLUMBER,
          status = ServiceRequestStatus.PENDING)

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    providerRepository = mock(ProviderRepository::class.java)
    providerViewModel = ListProviderViewModel(providerRepository)
    packageProposalRepository = mock(PackageProposalRepository::class.java)
    packageProposalViewModel = PackageProposalViewModel(packageProposalRepository)
    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    serviceRequestViewModel.selectRequest(serviceRequest)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      ServiceBookingScreen(
          requestViewModel = serviceRequestViewModel,
          providerViewModel = providerViewModel,
          packageViewModel = packageProposalViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("booking_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("price_appointment_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("appointment_date").assertIsDisplayed()
    composeTestRule.onNodeWithTag("address_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("google_map_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_button").assertIsDisplayed()
  }
}
