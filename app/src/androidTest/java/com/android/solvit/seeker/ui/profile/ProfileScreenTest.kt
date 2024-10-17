package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ProfileScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var seekerProfileViewModel: SeekerProfileViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var navController: NavController
    private lateinit var navigationActions: NavigationActions


    // Use a real MutableStateFlow to simulate the ViewModel state
    private val fakeProfileFlow =
                SeekerProfile(
                    uid = "12345",
                    name = "John Doe",
                    username = "johndoe",
                    email = "john.doe@example.com",
                    phone = "+1234567890",
                    address = "Chemin des Triaudes"
                )

    @Before
    fun setUp() {

        userRepository = mock(UserRepository::class.java)
        navController = mock(NavController::class.java)
        // authRepository =  mock(AuthRepository::class.java)
        seekerProfileViewModel = SeekerProfileViewModel(userRepository)
        navigationActions = NavigationActions(navController)
    }

    @Test
    fun displayAllProfileScreenComponents() {
        // Set the ProfileScreen content
        composeTestRule.setContent {
            SeekerProfileScreen(viewModel = seekerProfileViewModel, navigationActions = navigationActions)
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

    /*
    @SuppressLint("CheckResult")
    @Test
    fun navigateToEditProfile() {
        // Set the ProfileScreen content
        composeTestRule.setContent {
            SeekerProfileScreen(viewModel = seekerProfileViewModel, navigationActions = navigationActions)
        }

        // Simulate the click on "Edit Profile" button and verify navigation
        composeTestRule.onNodeWithTag("editProfileButton").performClick()
        verify { navigationActions.navigateTo(Screen.EDIT_PROFILE) }
    }
    */
}