package com.android.solvit

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.NotificationsRepository
import com.android.solvit.shared.model.NotificationsRepositoryFirestore
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.AiSolverViewModel
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
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestRepositoryFirebase
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.ReviewRepository
import com.android.solvit.shared.model.review.ReviewRepositoryFirestore
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.TopLevelDestinations
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
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

class EndToEndMessage {
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
  private lateinit var calendarViewModel: ProviderCalendarViewModel
  private lateinit var aiSolverViewModel: AiSolverViewModel
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
  private val provider =
      Provider(
          "1",
          "Paul",
          Services.PLUMBER,
          "",
          "Plumber & Co",
          "1234567890",
          Location(0.0, 0.0, "EPFL"),
          "Very good provider",
          true,
          5.0,
          100.0,
          Timestamp.now(),
          emptyList())
  private val request =
      ServiceRequest(
          uid = "1",
          title = "Test Request",
          description = "Test Description",
          userId = "1",
          providerId = "1",
          dueDate = Timestamp.now(),
          meetingDate = Timestamp.now(),
          location = Location(name = "EPFL", latitude = 0.0, longitude = 0.0),
          imageUrl = null,
          packageId = "1",
          agreedPrice = 200.15,
          type = Services.PLUMBER,
          status = ServiceRequestStatus.PENDING)

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
    chatRepository = ChatRepositoryFirestore(database, storage, firestore)
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
    calendarViewModel = ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)
    aiSolverViewModel = AiSolverViewModel()

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
                  notificationsViewModel,
                  aiSolverViewModel,
                  packageProposalViewModel)
          "provider" ->
              ProviderUI(
                  authViewModel,
                  listProviderViewModel,
                  serviceRequestViewModel,
                  seekerProfileViewModel,
                  chatViewModel,
                  notificationsViewModel,
                  locationViewModel,
                  packageProposalViewModel,
                  chatAssistantViewModel,
                  calendarViewModel)
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

    listProviderViewModel.addProvider(provider, null)
    serviceRequestViewModel.saveServiceRequest(
        request.copy(userId = authViewModel.user.value!!.uid))

    // Navigate to the requests overview screen
    composeTestRule.onNodeWithTag(TopLevelDestinations.REQUESTS_OVERVIEW.textId).performClick()
    composeTestRule.waitUntil {
      composeTestRule.onNodeWithTag("requestsOverviewScreen").isDisplayed()
    }

    composeTestRule.onNodeWithTag("requestListItem").performClick()

    // Assert the requests to be displayed
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("service_booking_screen").isDisplayed()
    }
    composeTestRule.onNodeWithTag("service_booking_screen").assertIsDisplayed()

    // Initialize the chat with the provider
    composeTestRule.onNodeWithTag("chat_button").performScrollTo()
    composeTestRule.onNodeWithTag("chat_button").performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("ChatScreen").isDisplayed()
    }

    // Send a message to the provider
    composeTestRule.onNodeWithTag("enterText").performTextInput("Hello, how are you?")
    composeTestRule.onNodeWithTag("sendMessageButton").performClick()

    // Assert the message to be displayed
    // composeTestRule.onNodeWithText("Hello, how are you?").performScrollTo()
    // composeTestRule.onNodeWithText("Hello, how are you?").isDisplayed()

    // Clean up
    chatViewModel.clearConversation(false)
    listProviderViewModel.deleteProvider("1")
    serviceRequestViewModel.deleteServiceRequestById("1")
  }
}
