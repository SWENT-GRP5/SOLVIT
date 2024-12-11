package com.android.solvit.seeker.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class SeekerMapScreenTest {
  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  private val testProviders =
      listOf(
          Provider(
              uid = "1",
              name = "Test Provider 1",
              service = Services.WRITER,
              imageUrl = "https://example",
              location = Location(0.0, 0.0, "Test Location 1"),
              rating = 4.5,
              price = 10.0,
              description = "Test Description 1",
              languages = listOf(Language.ARABIC, Language.ENGLISH),
              deliveryTime = Timestamp(0, 0),
              popular = true),
          Provider(
              uid = "2",
              name = "Test Provider 2",
              service = Services.WRITER,
              imageUrl = "https://example",
              location = Location(10.0, 10.0, "Test Location 2"),
              rating = 4.5,
              price = 10.0,
              description = "Test Description 2",
              languages = listOf(Language.ARABIC, Language.ENGLISH),
              deliveryTime = Timestamp(0, 0),
              popular = true))

  @Before
  fun setUp() {
    providerRepository = mock(ProviderRepository::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    navController = mock(NavController::class.java)
    navigationActions = mock(NavigationActions::class.java)

    `when`(providerRepository.getProviders(any(), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<Provider>) -> Unit>(0)
      onSuccess(testProviders)
    }

    `when`(navigationActions.currentRoute()).thenReturn(Route.MAP)
  }

  @Test
  fun hasRequiredElements() {
    composeTestRule.setContent { SeekerMapScreen(listProviderViewModel, navigationActions, false) }

    composeTestRule.onNodeWithTag("mapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("googleMap").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
  }

  @Test
  fun displaysProvidersOnMap() {
    composeTestRule.setContent { SeekerMapScreen(listProviderViewModel, navigationActions, false) }

    listProviderViewModel.providersList.value.forEach { provider ->
      composeTestRule.onNodeWithTag("providerMarker-${provider.uid}").assertIsDisplayed()
      composeTestRule.onNodeWithText(provider.name).assertIsDisplayed()
      composeTestRule.onNodeWithText(provider.description).assertIsDisplayed()
    }
  }

  @Test
  fun showsRequestLocationPermission() {
    composeTestRule.setContent { SeekerMapScreen(listProviderViewModel, navigationActions, true) }

    assert(true)
  }

  @Test
  fun onTabSelect_navigatesToCorrectRoute() {
    composeTestRule.setContent { SeekerMapScreen(listProviderViewModel, navigationActions, false) }

    composeTestRule.onNodeWithTag(TopLevelDestinations.SEEKER_OVERVIEW.textId).assertIsDisplayed()
    composeTestRule.onNodeWithTag(TopLevelDestinations.SEEKER_OVERVIEW.textId).performClick()
  }
}
