package com.android.solvit.shared.ui.booking

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.packages.PackageProposalRepository
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.provider.Provider
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
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

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

  private lateinit var authRep: AuthRep
  private lateinit var authViewModel: AuthViewModel
  private lateinit var chatRepository: ChatRepository
  private lateinit var chatViewModel: ChatViewModel

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

  private val provider = Provider(uid = "1", name = "Test Provider", imageUrl = "", rating = 4.5)

  private val packageProposal =
      PackageProposal(
          uid = "1",
          title = "Basic Maintenance",
          description = "Ideal for minor repairs and maintenance tasks.",
          price = 49.99,
          bulletPoints =
              listOf("Fix leaky faucets", "Unclog drains", "Inspect plumbing for minor issues"))

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
    chatRepository = mock(ChatRepository::class.java)
    chatViewModel = ChatViewModel(chatRepository)
    authRep = mock(AuthRep::class.java)
    authViewModel = AuthViewModel(authRep)

    serviceRequestViewModel.selectRequest(serviceRequest)
    `when`(providerRepository.getProviders(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Provider>) -> Unit>(1)
      onSuccess(listOf(provider)) // Simulate success
    }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      ServiceBookingScreen(
          requestViewModel = serviceRequestViewModel,
          providerViewModel = providerViewModel,
          authViewModel = authViewModel,
          packageViewModel = packageProposalViewModel,
          navigationActions = navigationActions,
          chatViewModel = chatViewModel)
    }

    composeTestRule.onNodeWithTag("booking_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profile_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("price_appointment_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pending_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("address_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("google_map_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_button").assertIsDisplayed()
  }

  @Test
  fun chatWithProvider() {

    composeTestRule.setContent {
      ServiceBookingScreen(
          requestViewModel = serviceRequestViewModel,
          providerViewModel = providerViewModel,
          authViewModel = authViewModel,
          packageViewModel = packageProposalViewModel,
          navigationActions = navigationActions,
          chatViewModel = chatViewModel)
    }
    providerViewModel.selectService(Services.PLUMBER)
    providerViewModel.getProviders()

    composeTestRule.onNodeWithTag("booking_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("problem_description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("price_appointment_box").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pending_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("address_label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("google_map_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_discuss_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("edit_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_button").assertIsDisplayed()
  }

  @Test
  fun dateAndTimePickersFunctionsAsIntended() {
    composeTestRule.setContent {
      DateAndTimePickers(
          request = serviceRequest,
          requestViewModel = serviceRequestViewModel,
      )
    }

    composeTestRule.onNodeWithTag("date_time_picker_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_time_picker_button").performClick()
    composeTestRule.onNodeWithTag("date_picker").assertIsDisplayed()
    composeTestRule.onNodeWithTag("date_picker").performClick()
    composeTestRule.onNodeWithTag("action_button").performClick()
    composeTestRule.onNodeWithTag("time_picker").assertIsDisplayed()
    composeTestRule.onNodeWithTag("time_picker").performClick()
    composeTestRule.onNodeWithTag("action_button").performClick()
    composeTestRule.onNodeWithTag("date_time_picker_button").performClick()
    composeTestRule.onNodeWithTag("cancel_button").performClick()
    composeTestRule.onNodeWithTag("date_picker").assertDoesNotExist()
    composeTestRule.onNodeWithTag("date_time_picker_button").performClick()
    composeTestRule.onNodeWithTag("date_picker").assertIsDisplayed()
  }

  @Test
  fun providerCardDisplaysAndFunctionsCorrectly() {
    composeTestRule.setContent {
      ProviderCard(
          provider = provider,
          providerViewModel = providerViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("provider_card").assertIsDisplayed()
  }

  @Test
  fun packageCardDisplaysAndFunctionsCorrectly() {
    composeTestRule.setContent {
      PackageCard(
          packageProposal = packageProposal,
      )
    }

    composeTestRule.onNodeWithTag("package_content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("price").assertIsDisplayed()
    composeTestRule.onNodeWithTag("title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bullet_points").assertIsDisplayed()
  }
}
