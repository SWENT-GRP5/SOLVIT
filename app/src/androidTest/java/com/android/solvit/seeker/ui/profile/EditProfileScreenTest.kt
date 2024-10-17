package com.android.solvit.seeker.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class EditProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: UserRepository
  private lateinit var navController: NavController
  private lateinit var authRepository: AuthRepository
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var authViewModel: AuthViewModel

  // Using a real MutableStateFlow to simulate the ViewModel state
  private val fakeProfileFlow =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "+1234567890",
          address = "Chemin des Triaudes")

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navController = mock(NavController::class.java)
    // authRepository =  mock(AuthRepository::class.java)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    navigationActions = NavigationActions(navController)
    // authViewModel = AuthViewModel(authRepository)
    // profileViewModel.userProfile = fakeProfileFlow
  }

  @Test
  fun displayAllEditProfileComponents() {
    // Set the EditProfileScreen content
    composeTestRule.setContent {
      EditSeekerProfileScreen(
          viewModel = seekerProfileViewModel, navigationActions = navigationActions)
    }

    // Assertions to verify all components are displayed
    composeTestRule.onNodeWithTag("editProfileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fullNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("usernameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("emailInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addressInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("saveButton").assertIsDisplayed()
  }

  /*
  @Test
  fun saveProfileData() {
      // Create a slot to capture the UserProfile argument
      val capturedUserProfile = slot<UserProfile>()

      // Set the EditProfileScreen content
      composeTestRule.setContent {
          EditProfileScreen(viewModel = profileViewModel, navigationActions = navigationActions)
      }

      // Simulate input in the "Full Name" text field
      composeTestRule.onNodeWithTag("fullNameInput").performTextInput("Jane Doe")

      // Simulate a click on the "Save" button
      composeTestRule.onNodeWithTag("saveButton").performClick()

      // Verify that the profile was updated with a valid UserProfile object
      verify {
          profileViewModel.updateUserProfile(
              match { userProfile ->
                  userProfile.name == "Jane DoeJohn Doe"
              }
          )
      }

      // Verify that navigation goes back
      verify { navigationActions.goBack() }
  }
  */
}
