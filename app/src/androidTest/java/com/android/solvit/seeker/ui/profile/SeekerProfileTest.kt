package com.android.solvit.seeker.ui.profile

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class SeekerProfileTest {
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel
  private lateinit var userRepository: UserRepository
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navController = mock(NavController::class.java)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    navigationActions = NavigationActions(navController)

    composeTestRule.setContent { SeekerProfileScreen(seekerProfileViewModel, navigationActions) }
  }

  @Test
  fun allComponentsAreDisplayed() {
    composeTestRule.onNodeWithTag("ProfileTopBar").isDisplayed()
    composeTestRule.onNodeWithTag("ProfileContent").isDisplayed()
    composeTestRule.onNodeWithTag("EditProfileButton").isDisplayed()
  }
}
