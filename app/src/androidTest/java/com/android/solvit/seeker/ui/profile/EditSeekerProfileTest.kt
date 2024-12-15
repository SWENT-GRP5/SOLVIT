package com.android.solvit.seeker.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class EditSeekerProfileTest {
  private lateinit var userRepository: UserRepository
  private lateinit var navController: NavController
  private lateinit var authRepository: AuthRepository
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var authViewModel: AuthViewModel

  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  private val testSeekerProfile =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "1234567890",
          address = Location(0.0, 0.0, "Chemin des Triaudes"))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {

    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)
    userRepository = mock(UserRepository::class.java)
    navController = mock(NavController::class.java)
    authRepository = mock(AuthRepository::class.java)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    navigationActions = NavigationActions(navController)
    authViewModel = AuthViewModel(authRepository)

    composeTestRule.setContent {
      EditSeekerProfileScreen(
          seekerProfileViewModel,
          authViewModel = authViewModel,
          locationViewModel = locationViewModel,
          navigationActions = navigationActions)
    }
  }

  @Test
  fun displayAllcomponents() {

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profileAddress").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePhone").assertIsDisplayed()
  }

  @Test
  fun inputsHaveInitialValue() {

    `when`(userRepository.getUserProfile(eq("1234"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(SeekerProfile) -> Unit>(1)
      onSuccess(testSeekerProfile) // Simulate success
    }
    // Call the ViewModel method
    seekerProfileViewModel.getUserProfile("1234")

    // Assert that the ViewModel's SeekerProfile was updated with the correct data
    assertEquals(testSeekerProfile, seekerProfileViewModel.seekerProfile.value)
    Thread.sleep(10000)

    composeTestRule.onNodeWithTag("profileName").assertTextContains(testSeekerProfile.name)
    composeTestRule.onNodeWithTag("profileUsername").assertTextContains(testSeekerProfile.username)
    composeTestRule.onNodeWithTag("profilePhone").assertTextContains(testSeekerProfile.phone)
  }
}
