package com.android.solvit.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.model.map.Location
import com.android.solvit.model.provider.Language
import com.android.solvit.model.provider.ListProviderViewModel
import com.android.solvit.model.provider.Provider
import com.android.solvit.model.provider.ProviderRepository
import com.android.solvit.model.provider.Services
import com.android.solvit.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class ListProviderScreenTest {
  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  private val provider1 =
      Provider(
          "1",
          "Hassan",
          Services.TUTOR,
          "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F37a07762-d4ec-45ae-8c18-e74777d8a53b.jpg?alt=media&token=534578d5-9dad-404f-b129-9a3052331bc8",
          Location(0.0, 0.0, "EPFL"),
          "Serious tutor giving courses in MATHS",
          true,
          5.0,
          25.0,
          Timestamp.now(),
          listOf(Language.FRENCH, Language.ENGLISH, Language.ARABIC, Language.SPANISH))

  private val provider2 =
      Provider(
          "1",
          "Hassan",
          Services.TUTOR,
          "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F37a07762-d4ec-45ae-8c18-e74777d8a53b.jpg?alt=media&token=534578d5-9dad-404f-b129-9a3052331bc8",
          Location(0.0, 0.0, "EPFL"),
          "Serious tutor giving courses in MATHS",
          true,
          5.0,
          25.0,
          Timestamp.now(),
          listOf(Language.SPANISH))

  @Before
  fun setUp() {
    providerRepository = mock(ProviderRepository::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)
    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
    composeTestRule.setContent { SelectProviderScreen(listProviderViewModel, navigationActions) }
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

    composeTestRule.waitUntil(
        timeoutMillis = 20000L,
        condition = {
          composeTestRule.onNodeWithTag("popularProviders").isDisplayed() &&
              composeTestRule.onNodeWithTag("popularProviders").isDisplayed() &&
              composeTestRule.onAllNodesWithTag("Rating").fetchSemanticsNodes().isNotEmpty()
        })
    composeTestRule.onNodeWithTag("popularProviders").assertIsDisplayed()
    composeTestRule.onNodeWithTag("providersList").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("Rating")[0].assertIsDisplayed()
  }

  @Test
  fun filterProviderCallsFilterScreen() {

    composeTestRule.onNodeWithTag("filterOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterOption").performClick()
    composeTestRule.onNodeWithTag("filterSheet").assertIsDisplayed()
  }

  @Test
  fun filterAction() {

    composeTestRule.onNodeWithTag("filterOption").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterOption").performClick()
    composeTestRule.onNodeWithTag("filterSheet").assertIsDisplayed()
    composeTestRule.waitUntil(
        timeoutMillis = 10000L,
        condition = {
          composeTestRule.onAllNodesWithTag("filterAct").fetchSemanticsNodes().isNotEmpty() &&
              composeTestRule.onAllNodesWithTag("filterRating").fetchSemanticsNodes().isNotEmpty()
        })
    composeTestRule.onAllNodesWithTag("filterAct")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    composeTestRule.onAllNodesWithTag("filterAct")[0].performClick()
    /*var selectedFields = mutableListOf<String>()
    var iconsPressed = mutableListOf(false, false)
    var iconsColor = mutableListOf(Color.White, Color.White)
    val idx = 0
    val elem = "ENGLISH"
    val filterAction: (Provider) -> Boolean = { provider -> provider.languages.contains(Language.ENGLISH) }
    val defaultFilterAction: (Provider) -> Boolean = { true }

    filterStringFields(
        selectedFields,
        iconsPressed,
        iconsColor,
        { a -> selectedFields = a.toMutableList() },
        { b -> iconsPressed = b.toMutableList() },
        { c -> iconsColor = c.toMutableList() },
        idx,
        elem,
        listProviderViewModel,
        filterAction,
        defaultFilterAction
    )

    assert(selectedFields.contains("ENGLISH"))
    assertTrue(iconsPressed[0])*/
    // verify(providerRepository).filterProviders(any())
    composeTestRule.onNodeWithTag("minPrice").isDisplayed()
    composeTestRule.onNodeWithTag("maxPrice").isDisplayed()
    composeTestRule.onNodeWithTag("minPrice").performTextInput("20")
    composeTestRule.onNodeWithTag("maxPrice").performTextInput("30")

    `when`(providerRepository.filterProviders(any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Provider>) -> Unit>(0)
      onSuccess(listOf(provider1)) // Simulate success
    }
    composeTestRule.onNodeWithTag("applyFilterButton").performClick()
    composeTestRule.waitUntil(
        timeoutMillis = 10000L,
        condition = {
          composeTestRule.onAllNodesWithTag("popularProviders").fetchSemanticsNodes().isNotEmpty()
        })
    assert(composeTestRule.onAllNodesWithTag("popularProviders").fetchSemanticsNodes().size == 1)

    // verify(providerRepository).filterProviders(any())
  }
}
