package com.android.solvit

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.provider.ProviderRegistrationScreen
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.authentication.User
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.packages.PackageProposalRepository
import com.android.solvit.shared.model.packages.PackageProposalRepositoryFirestore
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.provider.ProviderRepositoryFirestore
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestRepositoryFirebase
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.ReviewRepository
import com.android.solvit.shared.model.review.ReviewRepositoryFirestore
import com.android.solvit.shared.model.review.ReviewViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.storage
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

class EndToEndTestCreateProfile {

  private lateinit var authViewModel: AuthViewModel
  private lateinit var authViewModel2: AuthViewModel
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var packageProposalViewModel: PackageProposalViewModel

  private lateinit var authRepository: AuthRepository
  private lateinit var authRepository2: AuthRep
  private lateinit var seekerRepository: UserRepository
  private lateinit var providerRepository: ProviderRepository
  private lateinit var locationRepository: LocationRepository
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var reviewRepository: ReviewRepository
  private lateinit var packageProposalRepositoryFirestore: PackageProposalRepository

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Initialize Firebase App
    val firestore = FirebaseFirestore.getInstance()
    firestore.useEmulator("10.0.2.2", 8080)
    FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)

    val database = FirebaseDatabase.getInstance()
    database.useEmulator("10.0.2.2", 9000)
    firestore.firestoreSettings =
        FirebaseFirestoreSettings.Builder().setPersistenceEnabled(false).build()

    val storage = Firebase.storage
    storage.useEmulator("10.0.2.2", 9199)

    authRepository = AuthRepository(Firebase.auth, firestore)
    seekerRepository = UserRepositoryFirestore(firestore)
    providerRepository = ProviderRepositoryFirestore(firestore, storage)
    locationRepository = mock(LocationRepository::class.java)
    serviceRequestRepository = ServiceRequestRepositoryFirebase(firestore, storage)
    reviewRepository = ReviewRepositoryFirestore(firestore)
    packageProposalRepositoryFirestore = PackageProposalRepositoryFirestore(firestore)

    authViewModel = AuthViewModel(authRepository)
    seekerProfileViewModel = SeekerProfileViewModel(seekerRepository)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    locationViewModel = LocationViewModel(locationRepository)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    reviewViewModel = ReviewViewModel(reviewRepository)
    packageProposalViewModel = PackageProposalViewModel(packageProposalRepositoryFirestore)

    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }
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
  fun CreateSeekerProfile() {
    composeTestRule.setContent {
      val userRegistered = authViewModel.userRegistered.collectAsState()
      val user = authViewModel.user.collectAsState()

      if (!userRegistered.value) {
        SharedUI(
            authViewModel,
            listProviderViewModel,
            seekerProfileViewModel,
            locationViewModel,
            packageProposalViewModel)
      } else {
        when (user.value!!.role) {
          "seeker" ->
              SeekerUI(
                  authViewModel,
                  listProviderViewModel,
                  seekerProfileViewModel,
                  serviceRequestViewModel,
                  reviewViewModel,
                  locationViewModel)
          "provider" -> ProviderUI(authViewModel, listProviderViewModel, seekerProfileViewModel)
        }
      }
    }

    composeTestRule.onNodeWithTag("ctaButtonPortrait").performClick()

    composeTestRule.onNodeWithTag("loginImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpLink").performClick()

    composeTestRule.onNodeWithTag("signUpIllustration").assertIsDisplayed()
    val email = "e2eTest20@test.com"
    val password = "password"

    composeTestRule.onNodeWithTag("emailInputField").performTextInput(email)
    composeTestRule.onNodeWithTag("passwordInputField").performTextInput(password)
    composeTestRule.onNodeWithTag("confirmPasswordInputField").performTextInput(password)
    composeTestRule.onNodeWithTag("signUpButton").performClick()
    assertEquals(email, authViewModel.email.value)
    assertEquals(password, authViewModel.password.value)

    composeTestRule.onNodeWithTag("roleIllustration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("seekerButton").performClick()
    assertEquals("seeker", authViewModel.role.value)

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("fullNameInput").isDisplayed()
    }
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("JohnDoe123")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("EPFL")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()

    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    composeTestRule.onNodeWithTag("savePreferencesButton").performClick()
    composeTestRule.onNodeWithTag("exploreServicesButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("servicesScreen").isDisplayed()
    }
    composeTestRule.onNodeWithTag("servicesScreenCurrentLocation").performClick()

    composeTestRule.onNodeWithTag("servicesScreenProfileImage").performClick()
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("ProfileTopBar").isDisplayed()
    }

    Firebase.auth.currentUser?.delete()
  }

  @Test
  fun setProviderInfoDetails() {

    val sizeBefore = listProviderViewModel.providersList.value.size
    authRepository2 = mock(AuthRep::class.java)

    // Mock the `init` method
    doAnswer { invocation ->
          val callback = invocation.getArgument<(User?) -> Unit>(0)
          callback(User(uid = "1234", role = "Provider")) // Pass the mocked user
        }
        .whenever(authRepository2)
        .init(any())

    authViewModel2 = AuthViewModel(authRepository2)

    composeTestRule.setContent {
      navHostController = rememberNavController()
      navigationActions = NavigationActions(navHostController)
      ProviderRegistrationScreen(
          navigationActions = navigationActions,
          viewModel = listProviderViewModel,
          locationViewModel = locationViewModel,
          authViewModel = authViewModel2,
          packageViewModel = packageProposalViewModel)
    }

    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Company")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("123 Main St")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()

    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    composeTestRule.onNodeWithTag("servicesDropDown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesDropDown").performClick()
    composeTestRule.onNodeWithTag("PLUMBER").performClick() // choose plumber as Service
    composeTestRule.onNodeWithTag("providerImageButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("descriptionInputProviderRegistration").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("descriptionInputProviderRegistration")
        .performTextInput("No need a description ! I'm the Best Plumber In Town")
    composeTestRule.onNodeWithTag("startingPriceInputProviderRegistration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("startingPriceInputProviderRegistration").performTextInput("25")
    composeTestRule.onNodeWithTag("languageDropdown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("languageDropdown").performClick()
    composeTestRule.onNodeWithTag("FRENCH").performClick() // Set French as language spoken

    composeTestRule.onNodeWithTag("savePreferencesButton").performScrollTo().performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onNodeWithTag("offerPackagesDropDown").isDisplayed()
    }

    composeTestRule.onNodeWithTag("offerPackagesDropDown").assertIsDisplayed()
    composeTestRule.onNodeWithTag("offerPackagesDropDown").performClick()
    composeTestRule.onNodeWithTag("Yes").performClick() // Choose to offer packages services
    composeTestRule.onNodeWithTag("package1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packageName1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packageName1").performTextInput("Basic")
    composeTestRule.onNodeWithTag("packagePrice1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packagePrice1").performTextInput("50")
    composeTestRule.onNodeWithTag("packageDetails1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packageDetails1").performTextInput("Basic plumber services")
    composeTestRule.onNodeWithTag("packageFeatures10").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packageFeatures10").performTextInput("a")
    composeTestRule.onNodeWithTag("packageFeatures11").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packageFeatures11").performTextInput("a")
    composeTestRule.onNodeWithTag("packageFeatures12").assertIsDisplayed()
    composeTestRule.onNodeWithTag("packageFeatures12").performTextInput("a")

    composeTestRule.onNodeWithTag("savePreferences2Button").performScrollTo().performClick()

    composeTestRule.onNodeWithTag("continueDashboardButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("continueDashboardButton").performClick()
  }
}
