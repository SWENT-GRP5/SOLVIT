package com.android.solvit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class EndToEndTestCreateProfile {

  private lateinit var authViewModel: AuthViewModel
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  private lateinit var authRepository: AuthRep
  private lateinit var userRepository: UserRepository
  private lateinit var providerRepository: ProviderRepository
  private lateinit var locationRepository: LocationRepository

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

    locationRepository = mock(LocationRepository::class.java)
    authRepository = AuthRepository(Firebase.auth, firestore)
    authViewModel = AuthViewModel(authRepository)

    userRepository = mock(UserRepository::class.java)
    providerRepository = mock(ProviderRepository::class.java)

    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    locationViewModel = LocationViewModel(locationRepository)
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
      navHostController = rememberNavController()
      navigationActions = NavigationActions(navHostController)

      SharedUI(
          authViewModel = authViewModel,
          listProviderViewModel = listProviderViewModel,
          seekerProfileViewModel = seekerProfileViewModel,
          locationViewModel = locationViewModel,
          navController = navHostController,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("ctaButton").performClick()

    assertEquals(Screen.SIGN_IN, navHostController.currentDestination?.route)
    composeTestRule.onNodeWithTag("signUpLink").performClick()

    assertEquals(Screen.SIGN_UP, navHostController.currentDestination?.route)
    val email = "atest0@test.com"
    val password = "password"
    composeTestRule.onNodeWithTag("emailInputField").performTextInput(email)
    composeTestRule.onNodeWithTag("passwordInput").performTextInput(password)
    composeTestRule.onNodeWithTag("confirmPasswordInput").performTextInput(password)
    composeTestRule.onNodeWithTag("signUpButton").performClick()
    assertEquals(email, authViewModel.email.value)
    assertEquals(password, authViewModel.password.value)

    assertEquals(Screen.SIGN_UP_CHOOSE_ROLE, navHostController.currentDestination?.route)
    composeTestRule.onNodeWithTag("customerButton").performClick()
    assertEquals("seeker", authViewModel.role.value)

    assertEquals(Screen.SEEKER_REGISTRATION_PROFILE, navHostController.currentDestination?.route)
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("JohnDoe123")
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()
    composeTestRule.onNodeWithTag("savePreferencesButton").performClick()
  }
}
