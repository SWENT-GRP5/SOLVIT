package com.android.solvit.kaspresso.end2end

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.ProviderUI
import com.android.solvit.SeekerUI
import com.android.solvit.SharedUI
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
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
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters
import org.mockito.Mockito.mock

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class EndToEndSeekerCreateRequest {

  private lateinit var authViewModel: AuthViewModel
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var reviewViewModel: ReviewViewModel

  private lateinit var authRepository: AuthRepository
  private lateinit var seekerRepository: UserRepository
  private lateinit var providerRepository: ProviderRepository
  private lateinit var locationRepository: LocationRepository
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var reviewRepository: ReviewRepository

  private val email = "test@test.ch"
  private val password = "password"

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
    providerRepository = ProviderRepositoryFirestore(firestore)
    locationRepository = mock(LocationRepository::class.java)
    serviceRequestRepository = ServiceRequestRepositoryFirebase(firestore, storage)
    reviewRepository = ReviewRepositoryFirestore(firestore)

    authViewModel = AuthViewModel(authRepository)
    seekerProfileViewModel = SeekerProfileViewModel(seekerRepository)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    locationViewModel = LocationViewModel(locationRepository)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    reviewViewModel = ReviewViewModel(reviewRepository)

    authViewModel.setEmail(email)
    authViewModel.setPassword(password)
    authViewModel.setRole("seeker")
    authViewModel.registerWithEmailAndPassword(
        onSuccess = { authViewModel.logout {} }, onFailure = { assertEquals(true, false) })
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
  fun CreateServiceRequest() {
    composeTestRule.setContent {
      val user = authViewModel.user.collectAsState()
      val userRegistered = authViewModel.userRegistered.collectAsState()

      if (!userRegistered.value) {
        SharedUI(authViewModel, listProviderViewModel, seekerProfileViewModel, locationViewModel)
      } else {
        when (user.value!!.role) {
          "seeker" ->
              SeekerUI(
                  authViewModel,
                  listProviderViewModel,
                  seekerProfileViewModel,
                  serviceRequestViewModel,
                  reviewViewModel)
          "provider" -> ProviderUI(authViewModel, listProviderViewModel, seekerProfileViewModel)
        }
      }
    }

    composeTestRule.onNodeWithTag("ctaButtonPortrait").performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("loginImage").isDisplayed()
    }

    composeTestRule.onNodeWithTag("emailInput").performTextInput(email)
    composeTestRule.onNodeWithTag("password").performTextInput(password)
    composeTestRule.onNodeWithTag("signInButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("servicesScreen").isDisplayed()
    }

    composeTestRule.onNodeWithTag(TopLevelDestinations.CREATE_REQUEST.toString()).performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("requestScreen").isDisplayed()
    }

    composeTestRule.onNodeWithTag("inputRequestTitle").performTextInput("Test Request")
    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("PLUMBER")
    composeTestRule.onNodeWithTag("inputRequestDescription").performTextInput("Test Description")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("inputRequestAddress")
    composeTestRule.onNodeWithTag("inputRequestDate").performTextInput("25/12/2024")
    composeTestRule.onNodeWithTag("requestSubmit").performClick()
  }
}
