package com.android.solvit.provider.ui.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.provider.ui.profile.ProviderRegistrationScreen
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
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
class ProviderRegistrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var providerRepository: ProviderRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  @Before
  fun setUp() {

    providerRepository = mock(ProviderRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)

    // Mock the current route to be the add todo screen
    `when`(navigationActions.currentRoute()).thenReturn(Route.PROVIDER_REGISTRATION)
    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }
  }

  @Test
  fun testAllComponents() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
      )
    }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("signUpProviderTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fullNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestAddress").assertIsDisplayed()
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Now check for the complete registration button
    // composeTestRule.onNodeWithTag("completeRegistrationButton").assertIsDisplayed().assertTextEquals("Complete registration")

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
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("123 Main St")

    // Try to submit the form
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Verify that the step does not progress and userRepository was not called
    verify(providerRepository, never()).addProvider(any(), any(), any(), any())
  }

  @Test
  fun testCompleteProviderRegistrationButtonDisabledWhenFieldsAreIncomplete() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    // Initially, the button should be disabled when fields are empty
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()
    composeTestRule.onNodeWithTag("savePreferencesButton").isNotDisplayed()

    // Fill out only some of the fields
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")

    // Button should still be disabled as not all fields are filled
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()
    composeTestRule.onNodeWithTag("savePreferencesButton").isNotDisplayed()

    // Complete the rest of the fields
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Company")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("123 Main St")

    // Not enable because the location is not selected
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()
    composeTestRule.onNodeWithTag("savePreferencesButton").isNotDisplayed()

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()

    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()
    composeTestRule.onNodeWithTag("savePreferencesButton").isDisplayed()

    // Initially, the button should be disabled when fields are empty
    composeTestRule.onNodeWithTag("savePreferencesButton").performClick()
    composeTestRule.onNodeWithTag("enterPackagesButton").isNotDisplayed()

    // Fill out only some of the fields
    composeTestRule.onNodeWithTag("servicesDropDown").performClick()
    composeTestRule.onAllNodesWithTag("servicesDropdownMenu")[0].performClick()

    // Button should still be disabled as not all fields are filled
    composeTestRule.onNodeWithTag("descriptionInputProviderRegistration").performTextInput("ABC")
    composeTestRule
        .onNodeWithTag("startingPriceInputProviderRegistration")
        .performTextInput("1234567")

    // Complete the rest of the fields
    composeTestRule.onNodeWithTag("languageDropdown").performClick()
    composeTestRule.onAllNodesWithTag("languageDropdownMenu")[0].performClick()

    composeTestRule.onNodeWithTag("savePreferencesButton").performClick()
    composeTestRule.onNodeWithTag("enterPackagesButton").isDisplayed()
  }

  @Test
  fun testStepperMovesToStep2() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }
    // Initially, step 1 should be incomplete and visible
    composeTestRule.onNodeWithTag("stepCircle-1-incomplete").assertExists()

    // Fill out the form and move to step 2
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456789")
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Company")
    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()

    // Click the complete registration button (moves to step 2)
    composeTestRule.onNodeWithTag("completeRegistrationButton").performClick()

    // Check that the first step circle is marked as completed (green and checkmark)
    composeTestRule.onNodeWithTag("stepCircle-1-completed").assertExists()

    // Check that the second step circle is visible (indicating the user is on step 2)
    composeTestRule.onNodeWithTag("stepCircle-2-incomplete").assertExists()
  }

  @Test
  fun locMenuExpandsWithInput() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel,
      )
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[2].assertIsDisplayed()
  }

  @Test
  fun testEnterDetailedProviderInfo() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel,
      )
    }
  }

  @Test
  fun locSelectionFromDropdown() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[2].performClick()
    assert(locationViewModel.locationSuggestions.value == locations)
    assert(locationViewModel.query.value == "New York")
  }

  @Test
  fun providerRegistrationTest_errorShowInFullNameField() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    composeTestRule.onNodeWithTag("fullNameErrorProviderRegistration").isNotDisplayed()
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("Jo")
    composeTestRule.onNodeWithTag("fullNameErrorProviderRegistration").isDisplayed()
    composeTestRule.onNodeWithTag("fullNameInput").performTextClearance()
    composeTestRule.onNodeWithTag("fullNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("fullNameErrorProviderRegistration").isNotDisplayed()
  }

  @Test
  fun providerRegistrationTest_errorShowInPhoneField() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    composeTestRule.onNodeWithTag("phoneNumberErrorProviderRegistration").isNotDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("12345")
    composeTestRule.onNodeWithTag("phoneNumberErrorProviderRegistration").isDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextClearance()
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("phoneNumberErrorProviderRegistration").isDisplayed()
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextClearance()
    composeTestRule.onNodeWithTag("phoneNumberInput").performTextInput("123456")
    composeTestRule.onNodeWithTag("phoneNumberErrorProviderRegistration").isNotDisplayed()
  }

  @Test
  fun providerRegistrationTest_errorShowInCompanyNameField() {
    composeTestRule.setContent {
      ProviderRegistrationScreen(
          viewModel = listProviderViewModel,
          navigationActions = navigationActions,
          locationViewModel = locationViewModel)
    }

    composeTestRule.onNodeWithTag("companyNameErrorProviderRegistration").isNotDisplayed()
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Jo")
    composeTestRule.onNodeWithTag("companyNameErrorProviderRegistration").isDisplayed()
    composeTestRule.onNodeWithTag("companyNameInput").performTextClearance()
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("John Doe")
    composeTestRule.onNodeWithTag("companyNameErrorProviderRegistration").isNotDisplayed()
  }
}
