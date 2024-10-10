package com.android.solvit.screen

import android.annotation.SuppressLint
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.solvit.ui.navigation.NavigationActions
import com.android.solvit.ui.navigation.Screen
import com.android.solvit.ui.screens.profile.ProfileScreen
import com.android.solvit.ui.screens.profile.ProfileViewModel
import com.android.solvit.ui.screens.profile.UserProfile
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var profileViewModel: ProfileViewModel

  // Use a real MutableStateFlow to simulate the ViewModel state
  private val fakeProfileFlow =
      MutableStateFlow(
          listOf(
              UserProfile(
                  uid = "12345",
                  name = "John Doe",
                  email = "john.doe@example.com",
                  phone = "+1234567890")))

  @Before
  fun setUp() {
    // Mock the navigation actions
    navigationActions = mockk(relaxed = true)

    // Instead of mocking, create a real instance of ProfileViewModel but with the real StateFlow
    profileViewModel =
        ProfileViewModel(
            mockk(relaxed = true)) // Relaxed repository mock, assuming it's used inside ViewModel

    // Set the userProfile StateFlow in the ViewModel for testing purposes
    profileViewModel.userProfile = fakeProfileFlow
  }

  @Test
  fun displayAllProfileScreenComponents() {
    // Set the ProfileScreen content
    composeTestRule.setContent {
      ProfileScreen(viewModel = profileViewModel, navigationActions = navigationActions)
    }

    // Assertions
    composeTestRule.onNodeWithTag("profileScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editProfileButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editProfileButton").assertTextEquals("Edit Profile")

    composeTestRule.onNodeWithTag("sectionHeadline").assertIsDisplayed()
    composeTestRule.onNodeWithTag("optionPopular").assertIsDisplayed()
    composeTestRule.onNodeWithTag("optionFavourite").assertIsDisplayed()
    composeTestRule.onNodeWithTag("optionBilling").assertIsDisplayed()
    composeTestRule.onNodeWithTag("optionLanguage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("optionDarkmode").assertIsDisplayed()
  }

  @SuppressLint("CheckResult")
  @Test
  fun navigateToEditProfile() {
    // Set the ProfileScreen content
    composeTestRule.setContent {
      ProfileScreen(viewModel = profileViewModel, navigationActions = navigationActions)
    }

    // Simulate the click on "Edit Profile" button and verify navigation
    composeTestRule.onNodeWithTag("editProfileButton").performClick()
    verify { navigationActions.navigateTo(Screen.EDIT_PROFILE) }
  }
}
