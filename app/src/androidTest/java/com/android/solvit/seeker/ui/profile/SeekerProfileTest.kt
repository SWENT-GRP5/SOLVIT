package com.android.solvit.seeker.ui.profile

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class SeekerProfileTest {
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

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
    userRepository = mock(UserRepository::class.java)
    navController = mock(NavController::class.java)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    navigationActions = NavigationActions(navController)

    `when`(userRepository.getUserProfile(eq("1234"), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(SeekerProfile) -> Unit>(1)
      onSuccess(testSeekerProfile) // Simulate success
    }
    // Call the ViewModel method
    seekerProfileViewModel.getUserProfile("1234")

    composeTestRule.setContent {
      SeekerProfileScreen(seekerProfileViewModel, navigationActions = navigationActions)
    }
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.onNodeWithTag("ProfileTopBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditProfileButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileInfoCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileName").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileEmail").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FirstGroupCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MyAccountOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("OrdersOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PrivacySettingsOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BillingOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PreferencesOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MoreOptionsText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SecondGroupCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HelpSupportOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AboutAppOption").performScrollTo().assertIsDisplayed()
    // composeTestRule.onNodeWithTag("BottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun profileOptionItemsAreClickable() {
    composeTestRule.onNodeWithTag("MyAccountOption").assertHasClickAction()
    composeTestRule.onNodeWithTag("OrdersOption").assertHasClickAction()
    composeTestRule.onNodeWithTag("PrivacySettingsOption").assertHasClickAction()
    composeTestRule.onNodeWithTag("BillingOption").assertHasClickAction()
    // composeTestRule.onNodeWithTag("PreferencesOption").performClick()
    composeTestRule.onNodeWithTag("HelpSupportOption").assertHasClickAction()
    composeTestRule.onNodeWithTag("AboutAppOption").assertHasClickAction()
  }
}
