package com.android.solvit.shared.ui

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.map.SeekerMapScreen
import com.android.solvit.seeker.ui.profile.SeekerRegistrationScreen
import com.android.solvit.seeker.ui.provider.SelectProviderScreen
import com.android.solvit.seeker.ui.request.CreateRequestScreen
import com.android.solvit.seeker.ui.request.EditRequestScreen
import com.android.solvit.seeker.ui.request.RequestItemRow
import com.android.solvit.seeker.ui.request.RequestsOverviewScreen
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestType
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.authentication.OpeningScreen
import com.android.solvit.shared.ui.authentication.SignInScreen
import com.android.solvit.shared.ui.authentication.SignUpChooseProfile
import com.android.solvit.shared.ui.authentication.SignUpScreen
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

@RunWith(AndroidJUnit4::class)
class EndToEndTest1 {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions
  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var userRepository: UserRepository
  private lateinit var seekerViewModel: SeekerProfileViewModel
  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  private val request =
      ServiceRequest(
          "uid",
          "title",
          ServiceRequestType.CLEANING,
          "description",
          "assigneeName",
          Timestamp(GregorianCalendar(2024, 0, 1).time),
          Location(37.7749, -122.4194, "San Francisco"),
          "imageUrl",
          ServiceRequestStatus.PENDING)

  private val provider1 =
      Provider(
          "1",
          "Hassan",
          Services.TUTOR,
          "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F37a07762-d4ec-45ae-8c18-e74777d8a53b.jpg?alt=media&token=534578d5-9dad-404f-b129-9a3052331bc8",
          "",
          "",
          Location(0.0, 0.0, "EPFL"),
          "Serious tutor giving courses in MATHS",
          true,
          5.0,
          25.0,
          Timestamp.now(),
          listOf(Language.FRENCH, Language.ENGLISH, Language.ARABIC, Language.SPANISH))

  private val testProviders =
      listOf(
          Provider(
              uid = "1",
              name = "Test Provider 1",
              service = Services.WRITER,
              imageUrl = "https://example",
              location = Location(0.0, 0.0, "Test Location 1"),
              rating = 4.5,
              price = 10.0,
              description = "Test Description 1",
              languages = listOf(Language.ARABIC, Language.ENGLISH),
              deliveryTime = Timestamp(0, 0),
              popular = true),
          Provider(
              uid = "2",
              name = "Test Provider 2",
              service = Services.WRITER,
              imageUrl = "https://example",
              location = Location(10.0, 10.0, "Test Location 2"),
              rating = 4.5,
              price = 10.0,
              description = "Test Description 2",
              languages = listOf(Language.ARABIC, Language.ENGLISH),
              deliveryTime = Timestamp(0, 0),
              popular = true))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
    mockNavigationActions = mock(NavigationActions::class.java)
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)
    userRepository = mock(UserRepositoryFirestore::class.java)
    seekerViewModel = SeekerProfileViewModel(userRepository)
    providerRepository = mock(ProviderRepository::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)
  }

  @Test
  fun openingScreen_navigatesToSignInScreen() {
    composeTestRule.setContent { OpeningScreen(mockNavigationActions) }

    // Perform click on "Tap to Continue" and verify navigation to Sign In screen
    composeTestRule.onNodeWithTag("ctaButton").performClick()

    // Verify that the navigation action was triggered
    verify(mockNavigationActions).navigateTo(Screen.SIGN_IN)
  }

  @Test
  fun signUpScreen_PerformClick() {
    composeTestRule.setContent { SignUpScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.onNodeWithTag("passwordInput").performClick()
    composeTestRule.onNodeWithTag("confirmPasswordInput").performClick()
    composeTestRule.onNodeWithTag("signUpButton").performClick()
    composeTestRule.onNodeWithTag("logInLink").performClick()
  }

  @Test
  fun signUpScreen_emailInput() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
  }

  @Test
  fun signUpChooseProfile_performClick() {
    composeTestRule.setContent { SignUpChooseProfile(mockNavigationActions) }

    composeTestRule.onNodeWithTag("backButton").performClick()
    composeTestRule.onNodeWithTag("customerButton").performClick()
    composeTestRule.onNodeWithTag("professionalButton").performClick()
    composeTestRule.onNodeWithTag("learnMoreLink").performClick()

    org.mockito.kotlin.verify(mockNavigationActions).goBack()
  }

  @Test
  fun signInScreen_emailAndPasswordInput() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    composeTestRule.onNodeWithTag("emailInput").performTextInput("test@example.com")
    composeTestRule.onNodeWithTag("password").performTextInput("password123")
  }

  @Test
  fun signInScreen_performClick() {
    composeTestRule.setContent { SignInScreen(mockNavigationActions) }

    // Test that the checkbox is clickable and can toggle between states
    composeTestRule.onNodeWithTag("rememberMeCheckbox").performClick()
    composeTestRule.onNodeWithTag("forgotPasswordLink").performClick()
    composeTestRule.onNodeWithTag("signInButton").performClick()
    composeTestRule.onNodeWithTag("googleSignInButton").performClick()
  }

  @Test
  fun seekerRegistrationScreen_FailsWithMismatchedPasswords() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(viewModel = seekerViewModel, navigationActions = navigationActions)
    }

    // Fill out the form fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")

    // Try to submit the form
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Verify that the step does not progress and userRepository was not called
    org.mockito.kotlin.verify(userRepository, never()).addUserProfile(any(), any(), any())
  }

  @Test
  fun seekerRegistrationScreen_CompleteRegistrationButtonDisabledWhenFieldsAreIncomplete() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(viewModel = seekerViewModel, navigationActions = navigationActions)
    }

    // Initially, the button should be disabled when fields are empty
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Fill out only some of the fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")

    // Button should still be disabled as not all fields are filled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Complete the rest of the fields
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")

    // Now the button should be enabled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsEnabled()
  }

  @Test
  fun seekerRegistrationScreen_StepperMovesToStep2() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(viewModel = seekerViewModel, navigationActions = navigationActions)
    }

    // Initially, step 1 should be incomplete and visible
    composeTestRule.onNodeWithTag("stepCircle-1-incomplete").assertExists()

    // Fill out the form and move to step 2
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")

    // Click the complete registration button (moves to step 2)
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Check that the first step circle is marked as completed (green and checkmark)
    composeTestRule.onNodeWithTag("stepCircle-1-completed").assertExists()

    // Check that the second step circle is visible (indicating the user is on step 2)
    composeTestRule.onNodeWithTag("stepCircle-2-incomplete").assertExists()
  }

  @Test
  fun createRequest_locationMenuExpandsWithInput() {
    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }

    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[2].assertIsDisplayed()
  }

  @Test
  fun createRequest_locationSelectionFromDropdown() {
    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }

    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()
    assert(locationViewModel.locationSuggestions.value == locations)
    assert(locationViewModel.query.value == "San Francisco")
  }

  @Test
  fun serviceTypeDropdown_showsFilteredResults() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertExists()
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Plumbing"))
  }

  @Test
  fun serviceTypeDropdown_closesOnSelection() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeResult").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun serviceTypeDropdown_showsNoResultsMessage() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("NonExistentType")
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Other"))
  }

  @Test
  fun serviceTypeDropdown_closesOnFocusLost() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("inputRequestTitle").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun editRequest_deleteButton_triggersDeleteAction() {
    serviceRequestViewModel.selectRequest(request)

    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("deleteRequestButton").performClick()

    verify(serviceRequestRepository).deleteServiceRequestById(any(), any(), any())
  }

  @Test
  fun editRequest_deleteButton_logsErrorOnFailure() {
    serviceRequestViewModel.selectRequest(request)

    composeTestRule.setContent {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    `when`(serviceRequestRepository.deleteServiceRequestById(any(), any(), any())).thenAnswer {
        invocation ->
      val onError = invocation.getArgument<(String) -> Unit>(2)
      onError("Error")
    }

    composeTestRule.onNodeWithTag("deleteRequestButton").performClick()

    verify(serviceRequestRepository).deleteServiceRequestById(any(), any(), any())
  }

  @Test
  fun requestOverview_clickOnRequestNavigatesToEditScreen() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Route.REQUESTS_OVERVIEW)

    composeTestRule.setContent {
      RequestsOverviewScreen(mockNavigationActions, serviceRequestViewModel)
    }

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).then {
      it.getArgument<(List<ServiceRequest>) -> Unit>(0)(listOf(request))
    }
    serviceRequestViewModel.getServiceRequests()

    composeTestRule.onNodeWithText("title").performClick()
    verify(mockNavigationActions).navigateTo(Route.EDIT_REQUEST)
  }

  @Test
  fun requestOverview_todoItemDisplaysCorrectData() {
    composeTestRule.setContent { RequestItemRow(request = request, onClick = {}) }

    composeTestRule.onNodeWithText("title").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cleaning").assertIsDisplayed()

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val expectedDate = dateFormat.format(request.dueDate.toDate())
    composeTestRule.onNodeWithText("Deadline: $expectedDate").assertIsDisplayed()
  }

  @Test
  fun requestOverview_todoItemRowClickCallsOnClick() {
    val onClickMock = mock<() -> Unit>()

    composeTestRule.setContent { RequestItemRow(request = request, onClick = onClickMock) }

    composeTestRule.onNodeWithTag("requestListItem").performClick()
    verify(onClickMock).invoke()
  }

  @Test
  fun listProvider_filterProviderCallsFilterScreen() {
    composeTestRule.setContent { SelectProviderScreen(listProviderViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("filterOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterOption").performClick()
    composeTestRule.onNodeWithTag("filterSheet").assertIsDisplayed()
  }

  @Test
  fun listProvider_filterAction() {
    composeTestRule.setContent { SelectProviderScreen(listProviderViewModel, navigationActions) }

    composeTestRule.onNodeWithTag("filterOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterOption").performClick()
    composeTestRule.onNodeWithTag("filterSheet").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("filterAct")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onNodeWithTag("minPrice").isDisplayed()
    composeTestRule.onNodeWithTag("maxPrice").isDisplayed()
    composeTestRule.onNodeWithTag("minPrice").performTextInput("20")
    composeTestRule.onNodeWithTag("maxPrice").performTextInput("30")

    `when`(providerRepository.filterProviders(any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Provider>) -> Unit>(0)
      onSuccess(listOf(provider1)) // Simulate success
    }
    composeTestRule.onNodeWithTag("applyFilterButton").performClick()
    composeTestRule.waitUntil(
        timeoutMillis = 10000L,
        condition = {
          composeTestRule.onAllNodesWithTag("popularProviders").fetchSemanticsNodes().isNotEmpty()
        })
    assert(composeTestRule.onAllNodesWithTag("popularProviders").fetchSemanticsNodes().size == 1)
  }

  @Test
  fun seekerMap_displaysProvidersOnMap() {
    `when`(providerRepository.getProviders(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Provider>) -> Unit>(0)
      onSuccess(testProviders)
    }

    composeTestRule.setContent { SeekerMapScreen(listProviderViewModel, navigationActions, false) }

    listProviderViewModel.providersList.value.forEach { provider ->
      composeTestRule.onNodeWithTag("providerMarker-${provider.uid}").assertIsDisplayed()
      composeTestRule.onNodeWithText(provider.name).assertIsDisplayed()
      composeTestRule.onNodeWithText(provider.description).assertIsDisplayed()
    }
  }
}
