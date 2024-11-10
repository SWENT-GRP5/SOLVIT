package com.android.solvit.seeker.ui.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepositoryFirestore
import com.android.solvit.seeker.ui.profile.SeekerRegistrationScreen
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SeekerRegistrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userRepository: UserRepositoryFirestore
  private lateinit var navigationActions: NavigationActions
  private lateinit var seekerViewModel: SeekerProfileViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  @Before
  fun setUp() {
    userRepository = mock(UserRepositoryFirestore::class.java)
    navigationActions = mock(NavigationActions::class.java)
    seekerViewModel = SeekerProfileViewModel(userRepository)
    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SEEKER_REGISTRATION_PROFILE)
    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }
  }

  @Test
  fun displayAllcomponents() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(
          viewModel = seekerViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    composeTestRule.onNodeWithTag("backButton").assertIsDisplayed()
    // Step 1: Registration form fields should be displayed
    composeTestRule.onNodeWithTag("signUpIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpCustomerTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fullNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestAddress").assertIsDisplayed()
    composeTestRule.onNodeWithTag("userNameInput").assertIsDisplayed()

    // Verify the complete registration button
    composeTestRule
        .onNodeWithTag("completeRegistrationButton")
        .assertIsDisplayed()
        .assertTextEquals("Complete registration")

    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()

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
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("123 Main St")

    // Try to submit the form
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Verify that the step does not progress and userRepository was not called
    verify(userRepository, never()).addUserProfile(any(), any(), any())
  }

  @Test
  fun testCompleteRegistrationButtonDisabledWhenFieldsAreIncomplete() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(
          viewModel = seekerViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    // Initially, the button should be disabled when fields are empty
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Fill out only some of the fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")

    // Button should still be disabled as not all fields are filled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Complete the rest of the fields
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("123 Main St")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()
    // Now the button should be enabled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsEnabled()
  }

  @Test
  fun testStepperMovesToStep2() {
    composeTestRule.setContent {
      SeekerRegistrationScreen(
          viewModel = seekerViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    // Initially, step 1 should be incomplete and visible
    composeTestRule.onNodeWithTag("stepCircle-1-incomplete").assertExists()

    // Fill out the form and move to step 2
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("userNameInput").performTextInput("password123")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("123 Main St")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()

    // Click the complete registration button (moves to step 2)
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Check that the first step circle is marked as completed (green and checkmark)
    composeTestRule.onNodeWithTag("stepCircle-1-completed").assertExists()

    // Check that the second step circle is visible (indicating the user is on step 2)
    composeTestRule.onNodeWithTag("stepCircle-2-incomplete").assertExists()
  }
}
