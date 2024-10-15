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
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.provider.ProviderRegistrationScreen
import com.android.solvit.shared.model.provider.ProviderRepository
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
class ProviderRegistrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var providerRepository: ProviderRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listProviderViewModel: ListProviderViewModel

  @Before
  fun setUp() {

    providerRepository = mock(ProviderRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)

    // Mock the current route to be the add todo screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.PROVIDER_REGISTRATION_PROFILE)
  }

  @Test
  fun testAllComponents() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel, navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpProfessionalTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fullNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("locationInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("passwordInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmPasswordInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Now check for the complete registration button
     //composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsDisplayed().assertTextEquals("Complete registration")

  }

  @Test
  fun testProviderRegistrationFailsWithMismatchedPasswords() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel, navigationActions = navigationActions)
    }
    // Fill out the form fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Company")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("password123")
    composeTestRule
        .onNodeWithTag("confirmPasswordInput")
        .performTextInput("password124") // Passwords do not match

    // Try to submit the form
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Verify that the step does not progress and userRepository was not called
    verify(providerRepository, never()).addProvider(any(), any(), any())
  }

  @Test
  fun testCompleteProviderRegistrationButtonDisabledWhenFieldsAreIncomplete() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel, navigationActions = navigationActions)
    }
    // Initially, the button should be disabled when fields are empty
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Fill out only some of the fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")

    // Button should still be disabled as not all fields are filled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsNotEnabled()

    // Complete the rest of the fields
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Company")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("password123")
    composeTestRule.onNodeWithTag("confirmPasswordInput").performTextInput("password123")

    // Now the button should be enabled
    composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsEnabled()
  }

  @Test
  fun testStepperMovesToStep2() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel, navigationActions = navigationActions)
    }
    // Initially, step 1 should be incomplete and visible
    composeTestRule.onNodeWithTag("stepCircle-1-incomplete").assertExists()

    // Fill out the form and move to step 2
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Company")
    composeTestRule.onNodeWithTag("locationInput").performTextInput("123 Main St")
    composeTestRule.onNodeWithTag("passwordInput").performTextInput("password123")
    composeTestRule.onNodeWithTag("confirmPasswordInput").performTextInput("password123")

    // Click the complete registration button (moves to step 2)
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Check that the first step circle is marked as completed (green and checkmark)
    composeTestRule.onNodeWithTag("stepCircle-1-completed").assertExists()

    // Check that the second step circle is visible (indicating the user is on step 2)
    composeTestRule.onNodeWithTag("stepCircle-2-incomplete").assertExists()
  }
}
