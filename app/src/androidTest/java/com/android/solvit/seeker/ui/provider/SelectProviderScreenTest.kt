package com.android.solvit.seeker.ui.provider

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.profile.UserRepository
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

class SelectProviderScreenTest {
  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel

  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel

  private lateinit var userRepository: UserRepository
  private lateinit var seekerProfileViewModel: SeekerProfileViewModel

  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  private val provider1 =
      Provider(
          "1",
          "Hassan",
          Services.TUTOR,
          "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F37a07762-d4ec-45ae-8c18-e74777d8a53b.jpg?alt=media&token=534578d5-9dad-404f-b129-9a3052331bc8",
          "",
          "",
          Location(0.0, 0.0, "EPFL"),
          "Serious tutor giving courses in MATHS",
          true,
          5.0,
          25.0,
          languages = listOf(Language.FRENCH, Language.ENGLISH, Language.ARABIC, Language.SPANISH))

  private val provider2 =
      Provider(
          "1",
          "Hassan",
          Services.TUTOR,
          "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F37a07762-d4ec-45ae-8c18-e74777d8a53b.jpg?alt=media&token=534578d5-9dad-404f-b129-9a3052331bc8",
          "",
          "",
          Location(0.0, 0.0, "EPFL"),
          "Serious tutor giving courses in MATHS",
          true,
          5.0,
          25.0,
          languages = listOf(Language.SPANISH))

  @Before
  fun setUp() {
    providerRepository = Mockito.mock(ProviderRepository::class.java)
    userRepository = Mockito.mock(UserRepository::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    seekerProfileViewModel = SeekerProfileViewModel(userRepository)
    navController = Mockito.mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
    locationRepository = Mockito.mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)

    composeTestRule.setContent {
      SelectProviderScreen(
          seekerProfileViewModel = seekerProfileViewModel,
          locationViewModel = locationViewModel,
          // authViewModel=authViewModel,
          listProviderViewModel = listProviderViewModel,
          navigationActions = navigationActions,
          userId = "1234")
    }
    `when`(userRepository.getCachedLocation(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Location>) -> Unit>(1)
      onSuccess(listOf(Location(0.0, 0.0, "EPFL")))
    }
    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(listOf(Location(0.0, 0.0, "EPFL")))
        }
  }

  @Test
  fun hasRequiredElements() {
    `when`(providerRepository.getProviders(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Provider>) -> Unit>(1)
      onSuccess(listOf(provider1, provider2)) // Simulate success
    }

    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterBar").assertIsDisplayed()
    listProviderViewModel.selectService(Services.PLUMBER)
    listProviderViewModel.getProviders()

    composeTestRule.onNodeWithTag("popularProviders").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providersList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterByLocation").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("filterIcon").fetchSemanticsNodes().isNotEmpty()
  }

  @Test
  fun filterProviderCallsFilterScreen() {
    // We assert here that there is three filter options displayed in the screen (Top Rates, Top
    // Prices, Highest Activity)
    assertEquals(3, composeTestRule.onAllNodesWithTag("filterOption").fetchSemanticsNodes().size)
    composeTestRule.onAllNodesWithTag("filterOption")[0].performClick()
    composeTestRule.onNodeWithTag("filterIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterIcon").performClick()
  }

  @Test
  fun filterByLocationOpenBottomSheet() {
    composeTestRule.onNodeWithTag("filterByLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterByLocation").performClick()
    composeTestRule.onNodeWithTag("SearchLocBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterByLocationSheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cachedLocations").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SearchLocBar").performTextInput("EPFL")
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag("suggestedLocations").isDisplayed()
    }
    composeTestRule.onNodeWithTag("suggestedLocations").assertIsDisplayed()
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("suggestedLocation").fetchSemanticsNodes().isNotEmpty()
    }
    composeTestRule.onAllNodesWithTag("suggestedLocation")[0].performClick()
    assert(composeTestRule.onAllNodesWithTag("popularProviders").fetchSemanticsNodes().size == 1)
  }

  @Test
  fun filterAction() {

    composeTestRule.onNodeWithTag("filterIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterIcon").performClick()
    composeTestRule.onNodeWithTag("filterSheet").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("filterAct")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onNodeWithTag("minPrice").isDisplayed()
    composeTestRule.onNodeWithTag("maxPrice").isDisplayed()
    composeTestRule.onNodeWithTag("minPrice").performTextInput("20")
    composeTestRule.onNodeWithTag("maxPrice").performTextInput("30")

    composeTestRule.onNodeWithTag("applyFilterButton").performClick()
    composeTestRule.waitUntil(
        timeoutMillis = 10000L,
        condition = {
          composeTestRule.onAllNodesWithTag("popularProviders").fetchSemanticsNodes().isNotEmpty()
        })
    assert(composeTestRule.onAllNodesWithTag("popularProviders").fetchSemanticsNodes().size == 1)
    composeTestRule.onAllNodesWithTag("filterIcon")[0].performClick()

    // verify(providerRepository).filterProviders(any())
  }
}
