package com.android.solvit.provider.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.provider.model.profile.ProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

class ProfessionalProfileScreenTest {

  private lateinit var providerRepository: ProviderRepository
  private lateinit var providerViewModel: ProviderViewModel

  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  private val provider =
      Provider(
          uid = "user123",
          name = "John Doe",
          companyName = "Company",
          phone = "1234567890",
          location = Location(0.0, 0.0, "Chemin des Triaudes"),
          description = "Description",
          rating = 4.5,
          price = 50.0,
          languages = listOf(Language.ENGLISH, Language.FRENCH))

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    providerRepository = mock(ProviderRepository::class.java)
    providerViewModel = ProviderViewModel(providerRepository)

    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)

    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }
  }

  @Test
  fun modifyProviderInformationScreen_topBarIsDisplay() {

    composeTestRule.setContent {
      ModifyProviderInformationScreen(
          providerViewModel = providerViewModel,
          locationViewModel = locationViewModel,
          navigationActions = navigationActions)
    }
    composeTestRule.onNodeWithTag("titleModifyProvider").assertIsDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_contentIsDisplay() {

    composeTestRule.setContent { ModifyInput(provider, navigationActions = navigationActions) }
    composeTestRule.onNodeWithTag("newNameInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newCompanyNameInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newServiceInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newLocationInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newDescriptionInputField").assertIsDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_errorIsDisplayInTheNameContent() {

    composeTestRule.setContent { ModifyInput(provider, navigationActions = navigationActions) }
    composeTestRule.onNodeWithTag("newNameInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nameErrorMessage").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("newNameInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newNameInputField").performTextInput("a")
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextInput("+1234567")
    composeTestRule.onNodeWithTag("nameErrorMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newNameInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newNameInputField").performTextInput("Kevin")
    composeTestRule.onNodeWithTag("nameErrorMessage").assertIsNotDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_errorIsDisplayInTheCompanyNameContent() {

    composeTestRule.setContent { ModifyInput(provider, navigationActions = navigationActions) }
    composeTestRule.onNodeWithTag("newCompanyNameInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyNameErrorMessage").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("newCompanyNameInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newCompanyNameInputField").performTextInput("a")
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextInput("+1234567")
    composeTestRule.onNodeWithTag("companyNameErrorMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newCompanyNameInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newCompanyNameInputField").performTextInput("Kevin")
    composeTestRule.onNodeWithTag("companyNameErrorMessage").assertIsNotDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_errorIsDisplayInThePhoneNumberContent() {

    composeTestRule.setContent {
      ModifyInput(provider, locationViewModel, navigationActions = navigationActions)
    }
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newPhoneNumberErrorMessage").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextInput("a")
    composeTestRule.onNodeWithTag("newCompanyNameInputField").performTextInput("Kevin")
    composeTestRule.onNodeWithTag("newPhoneNumberErrorMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextInput("+1234567")
    composeTestRule.onNodeWithTag("newPhoneNumberErrorMessage").assertIsNotDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_errorIsDisplayInTheDescriptionContent() {

    composeTestRule.setContent {
      ModifyInput(provider, locationViewModel, navigationActions = navigationActions)
    }
    composeTestRule.onNodeWithTag("newDescriptionInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newDescriptionErrorMessage").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("newDescriptionInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newDescriptionErrorMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newDescriptionInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newDescriptionInputField").performTextInput("Bonjour")
    composeTestRule.onNodeWithTag("newDescriptionErrorMessage").assertIsNotDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_errorIsDisplayInTheLocationContent() {
    composeTestRule.setContent {
      ModifyInput(provider, locationViewModel, navigationActions = navigationActions)
    }
    composeTestRule.onNodeWithTag("newLocationInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newLocationInputField").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()
  }
}
