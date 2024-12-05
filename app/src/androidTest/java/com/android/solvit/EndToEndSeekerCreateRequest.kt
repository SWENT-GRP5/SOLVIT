package com.android.solvit

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.NotificationsRepository
import com.android.solvit.shared.model.NotificationsRepositoryFirestore
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import com.android.solvit.shared.model.chat.ChatRepository
import com.android.solvit.shared.model.chat.ChatRepositoryFirestore
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.packages.PackageProposalRepository
import com.android.solvit.shared.model.packages.PackageProposalRepositoryFirestore
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.packages.PackagesAssistantViewModel
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestRepositoryFirebase
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.ReviewRepository
import com.android.solvit.shared.model.review.ReviewRepositoryFirestore
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.ui.navigation.TopLevelDestinations
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.storage.storage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

class EndToEndSeekerCreateRequest {

  private lateinit var authViewModel: AuthViewModel
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var packageProposalViewModel: PackageProposalViewModel
  private lateinit var chatAssistantViewModel: ChatAssistantViewModel
  private lateinit var notificationsViewModel: NotificationsViewModel
  private lateinit var packagesAssistantViewModel: PackagesAssistantViewModel

  private lateinit var authRepository: AuthRepository
  private lateinit var seekerRepository: UserRepository
  private lateinit var providerRepository: ProviderRepository
  private lateinit var locationRepository: LocationRepository
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var reviewRepository: ReviewRepository
  private lateinit var packageProposalRepository: PackageProposalRepository
  private lateinit var chatRepository: ChatRepository
  private lateinit var notificationsRepository: NotificationsRepository

  private val email = "test@test.ch"
  private val password = "password"

  private val locations = listOf(Location(37.7749, -122.4194, "San Francisco"))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    val database = Firebase.database
    database.useEmulator("10.0.2.2", 9000)

    val auth = Firebase.auth
    auth.useEmulator("10.0.2.2", 9099)

    val firestore = Firebase.firestore
    firestore.useEmulator("10.0.2.2", 8080)

    firestore.firestoreSettings = firestoreSettings { isPersistenceEnabled = false }

    val storage = Firebase.storage
    storage.useEmulator("10.0.2.2", 9199)

    authRepository = AuthRepository(Firebase.auth, firestore)
    seekerRepository = UserRepositoryFirestore(firestore)
    providerRepository = ProviderRepositoryFirestore(firestore, storage)
    locationRepository = mock(LocationRepository::class.java)
    serviceRequestRepository = ServiceRequestRepositoryFirebase(firestore, storage)
    reviewRepository = ReviewRepositoryFirestore(firestore)
    packageProposalRepository = PackageProposalRepositoryFirestore(firestore)
    chatRepository = ChatRepositoryFirestore(database)
    notificationsRepository = NotificationsRepositoryFirestore(firestore)
    packagesAssistantViewModel = PackagesAssistantViewModel()

    authViewModel = AuthViewModel(authRepository)
    seekerProfileViewModel = SeekerProfileViewModel(seekerRepository)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    locationViewModel = LocationViewModel(locationRepository)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    reviewViewModel = ReviewViewModel(reviewRepository)
    packageProposalViewModel = PackageProposalViewModel(packageProposalRepository)
    chatViewModel = ChatViewModel(chatRepository)
    chatAssistantViewModel = ChatAssistantViewModel()
    notificationsViewModel = NotificationsViewModel(notificationsRepository)

    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }

    authViewModel.setEmail(email)
    authViewModel.setPassword(password)
    authViewModel.setRole("seeker")
    authViewModel.registerWithEmailAndPassword(
        onSuccess = { authViewModel.logout {} }, onFailure = {})
    serviceRequestRepository.getServiceRequests(
        onSuccess = { requests ->
          requests.forEach { serviceRequestViewModel.deleteServiceRequestById(it.uid) }
        },
        onFailure = {})
  }

  @After
  fun tearDown() {
    FirebaseApp.clearInstancesForTest()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    val firestore = FirebaseFirestore.getInstance()
    firestore.firestoreSettings =
        FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(
                true) // Set to true or false as needed for your production environment
            .build()

    // Reinitialize FirebaseAuth without the emulator
    FirebaseAuth.getInstance().signOut()
  }

  @Test
  fun createServiceRequest() {
    composeTestRule.setContent {
      val user = authViewModel.user.collectAsState()
      val userRegistered = authViewModel.userRegistered.collectAsState()

      if (!userRegistered.value) {
        SharedUI(
            authViewModel,
            listProviderViewModel,
            seekerProfileViewModel,
            locationViewModel,
            packageProposalViewModel,
            packagesAssistantViewModel)
      } else {
        when (user.value!!.role) {
          "seeker" ->
              SeekerUI(
                  authViewModel,
                  listProviderViewModel,
                  seekerProfileViewModel,
                  serviceRequestViewModel,
                  reviewViewModel,
                  locationViewModel,
                  chatViewModel,
                  chatAssistantViewModel,
                  notificationsViewModel)
          "provider" ->
              ProviderUI(
                  authViewModel,
                  listProviderViewModel,
                  serviceRequestViewModel,
                  seekerProfileViewModel,
                  chatViewModel,
                  notificationsViewModel,
                  locationViewModel,
                  chatAssistantViewModel)
        }
      }
    }

    // Login
    composeTestRule.onNodeWithTag("ctaButtonPortrait").performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("loginImage").isDisplayed()
    }

    composeTestRule.onNodeWithTag("emailInput").performTextInput(email)
    composeTestRule.onNodeWithTag("passwordInput").performTextInput(password)
    composeTestRule.onNodeWithTag("signInButton").performClick()

    // Wait for the services screen to be displayed
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("servicesScreen").isDisplayed()
    }

    authViewModel.user.value?.locations?.forEach { authViewModel.removeUserLocation(it, {}, {}) }

    // Navigate to the requests overview screen
    composeTestRule.onNodeWithTag(TopLevelDestinations.REQUESTS_OVERVIEW.textId).performClick()
    composeTestRule.waitUntil {
      composeTestRule.onNodeWithTag("requestsOverviewScreen").isDisplayed()
    }
    composeTestRule.onNodeWithTag("noServiceRequestsScreen").assertIsDisplayed()

    // Create a new request
    composeTestRule.onNodeWithTag(TopLevelDestinations.CREATE_REQUEST.toString()).performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("requestScreen").isDisplayed()
    }

    composeTestRule.onNodeWithTag("inputRequestTitle").performTextInput("Test Request")
    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("PLUMBER")
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("serviceTypeMenu").isDisplayed()
    }
    composeTestRule.onNodeWithTag("serviceTypeResult").performClick()
    composeTestRule.onNodeWithTag("inputRequestDescription").performTextInput("Test Description")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("test")
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      locationViewModel.locationSuggestions.value.isNotEmpty()
    }
    composeTestRule.onNodeWithTag("locationResult").performClick()
    composeTestRule.onNodeWithTag("inputRequestDate").performClick()
    composeTestRule.onNodeWithTag("datePickerDialog").performClick()
    composeTestRule.onNodeWithText("OK").performClick()

    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    // Assert the requests to be displayed
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("requestsList").isDisplayed()
    }

    // Delete the request
    composeTestRule.onNodeWithTag("requestListItem").performClick()
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("edit_button").isDisplayed()
    }
    composeTestRule.onNodeWithTag("edit_button").performClick()
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("requestScreen").isDisplayed()
    }
    composeTestRule.onNodeWithTag("deleteRequestButton").performClick()
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("requestsOverviewScreen").isDisplayed()
    }
    composeTestRule.onNodeWithTag("noServiceRequestsScreen").assertIsDisplayed()
  }
}
