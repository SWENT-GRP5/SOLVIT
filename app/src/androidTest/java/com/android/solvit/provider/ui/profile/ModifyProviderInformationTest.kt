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
import com.android.solvit.seeker.model.provider.ListProviderViewModel
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
import org.mockito.Mockito.mock

class ProfessionalProfileScreenTest {

  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel

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
    listProviderViewModel = ListProviderViewModel(providerRepository)

    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)
  }

  @Test
  fun modifyProviderInformationScreen_topBarIsDisplay() {

    composeTestRule.setContent {
      ModifyProviderInformationScreen(
          listProviderViewModel = listProviderViewModel,
          locationViewModel = locationViewModel,
          navigationActions = navigationActions)
    }
    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_contentIsDisplay() {

    composeTestRule.setContent { ModifyInput(provider, navigationActions = navigationActions) }
    composeTestRule.onNodeWithTag("newProviderCompanyNameInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newServiceInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newLocationInputField").assertIsDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_errorIsDisplayInTheCompanyNameContent() {

    composeTestRule.setContent { ModifyInput(provider, navigationActions = navigationActions) }
    composeTestRule.onNodeWithTag("newProviderCompanyNameInputField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providerNameErrorMessage").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("newProviderCompanyNameInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newProviderCompanyNameInputField").performTextInput("a")
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextInput("+1234567")
    composeTestRule.onNodeWithTag("providerNameErrorMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newProviderCompanyNameInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newProviderCompanyNameInputField").performTextInput("Kevin")
    composeTestRule.onNodeWithTag("providerNameErrorMessage").assertIsNotDisplayed()
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
    composeTestRule.onNodeWithTag("newProviderCompanyNameInputField").performTextInput("Kevin")
    composeTestRule.onNodeWithTag("newPhoneNumberErrorMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newPhoneNumberInputField").performTextInput("+1234567")
    composeTestRule.onNodeWithTag("newPhoneNumberErrorMessage").assertIsNotDisplayed()
  }

  @Test
  fun modifyProviderInformationScreen_errorIsDisplayInTheLocationContent() {

    composeTestRule.onNodeWithTag("newLocationInputField").performTextClearance()
    composeTestRule.onNodeWithTag("newLocationInputField").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }
    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()
  }
}
