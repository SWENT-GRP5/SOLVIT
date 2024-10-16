package com.android.solvit.seeker.ui.profile

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.shared.model.authentication.AuthRepository
import com.android.solvit.shared.model.authentication.AuthViewModel
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

  private val testSeekerProfile =
      SeekerProfile(
          uid = "12345",
          name = "John Doe",
          username = "johndoe",
          email = "john.doe@example.com",
          phone = "+1234567890",
          address = "Chemin des Triaudes")

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    userRepository = mock(UserRepository::class.java)
    navController = mock(NavController::class.java)
    // authRepository =  mock(AuthRepository::class.java)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    navigationActions = NavigationActions(navController)
    // authViewModel = AuthViewModel(authRepository)
    composeTestRule.setContent {
      EditSeekerProfileScreen(seekerProfileViewModel, navigationActions)
    }
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
    composeTestRule.onNodeWithTag("profileEmail").assertTextContains(testSeekerProfile.email)
    composeTestRule.onNodeWithTag("profilePhone").assertTextContains(testSeekerProfile.phone)
    composeTestRule.onNodeWithTag("profileAddress").assertTextContains(testSeekerProfile.address)
  }
}
