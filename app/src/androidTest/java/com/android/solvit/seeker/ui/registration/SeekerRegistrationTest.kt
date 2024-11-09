package com.android.solvit.seeker.ui.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.ui.profile.SeekerRegistrationScreen
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SeekerRegistrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var navigationActions: NavigationActions
  private lateinit var seekerViewModel: SeekerProfileViewModel

  @Before
  fun setUp() {
    userRepository = mock(UserRepositoryFirestore::class.java)
    navigationActions = mock(NavigationActions::class.java)
    seekerViewModel = SeekerProfileViewModel(userRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SEEKER_REGISTRATION_PROFILE)
  }

  @Test
  fun displayAllcomponents() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(viewModel = seekerViewModel, navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    // Step 1: Registration form fields should be displayed
    composeTestRule.onNodeWithTag("signUpIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpCustomerTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fullNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("locationInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userNameInput").assertIsDisplayed()

    // Verify the complete registration button
    composeTestRule
        .onNodeWithTag("completeRegistrationButton")
        .assertIsDisplayed()
        .assertTextEquals("Complete registration")

    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")

    // Click the complete registration button to move to the next step
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Verify Step 2: Preferences step components
    composeTestRule.onNodeWithTag("preferencesTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("preferencesIllustration").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("savePreferencesButton")
        .assertIsDisplayed()
        .assertTextEquals("Save Preferences")
    composeTestRule.onNodeWithTag("footerText").assertIsDisplayed()

    // Perform click on save preferences
    composeTestRule.onNodeWithTag("savePreferencesButton").performClick()

    // Verify Step 3: Completion step components
    composeTestRule.onNodeWithTag("confirmationTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("celebrationIllustration").assertIsDisplayed()
    composeTestRule.onNodeWithTag("successMessageText").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("exploreServicesButton")
        .assertIsDisplayed()
        .assertTextEquals("Continue to Explore Services")
  }

  @Test
  fun testRegistrationFailsWithMismatchedPasswords() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(viewModel = seekerViewModel, navigationActions = navigationActions)
    }

    // Fill out the form fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")

    // Try to submit the form
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Verify that the step does not progress and userRepository was not called
    verify(userRepository, never()).addUserProfile(any(), any(), any())
  }

  @Test
  fun testCompleteRegistrationButtonDisabledWhenFieldsAreIncomplete() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(viewModel = seekerViewModel, navigationActions = navigationActions)
    }

    // Initially, the button should be disabled when fields are empty
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Fill out only some of the fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")

    // Button should still be disabled as not all fields are filled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Complete the rest of the fields
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")

    // Now the button should be enabled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsEnabled()
  }

  @Test
  fun testStepperMovesToStep2() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(viewModel = seekerViewModel, navigationActions = navigationActions)
    }

    // Initially, step 1 should be incomplete and visible
    composeTestRule.onNodeWithTag("stepCircle-1-incomplete").assertExists()

    // Fill out the form and move to step 2
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")

    // Click the complete registration button (moves to step 2)
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Check that the first step circle is marked as completed (green and checkmark)
    composeTestRule.onNodeWithTag("stepCircle-1-completed").assertExists()

    // Check that the second step circle is visible (indicating the user is on step 2)
    composeTestRule.onNodeWithTag("stepCircle-2-incomplete").assertExists()
  }
}
