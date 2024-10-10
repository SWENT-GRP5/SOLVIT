package com.android.solvit.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import com.android.solvit.ui.navigation.NavigationActions
import com.android.solvit.ui.screens.profile.EditProfileScreen
import com.android.solvit.ui.screens.profile.ProfileViewModel
import com.android.solvit.ui.screens.profile.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class EditProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var profileViewModel: ProfileViewModel

    // Using a real MutableStateFlow to simulate the ViewModel state
    private val fakeProfileFlow = MutableStateFlow(
        listOf(UserProfile(uid = "12345", name = "John Doe", email = "john.doe@example.com", phone = "+1234567890"))
    )

    @Before
    fun setUp() {
        // Mock the navigation actions
        navigationActions = mockk(relaxed = true)

        // Instead of mocking, create a real instance of ProfileViewModel but with the real StateFlow
        profileViewModel = ProfileViewModel(mockk(relaxed = true)) // Relaxed repository mock, assuming it's used inside ViewModel

        // Set the userProfile StateFlow in the ViewModel for testing purposes
        profileViewModel.userProfile = fakeProfileFlow
    }

    @Test
    fun displayAllEditProfileComponents() {
        // Set the EditProfileScreen content
        composeTestRule.setContent {
            EditProfileScreen(viewModel = profileViewModel, navigationActions = navigationActions)
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
