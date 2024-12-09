package com.android.solvit.seeker.ui.service

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class ServicesScreenTest {
  private lateinit var repository: ProviderRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var navController: NavController
  private lateinit var listProviderViewModel: ListProviderViewModel

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    repository = Mockito.mock(ProviderRepository::class.java)
    navController = Mockito.mock(NavController::class.java)
    navigationActions = Mockito.mock(NavigationActions::class.java)
    listProviderViewModel = ListProviderViewModel(repository)
    `when`(navigationActions.currentRoute()).thenReturn(Route.SERVICES)
    composeTestRule.setContent { ServicesScreen(navigationActions, listProviderViewModel) }
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenTopSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenShortcuts").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCategories").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenPerformers").assertIsDisplayed()
  }

  @Test
  fun topSectionHasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreenTopSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCurrentLocation").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenLocationButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenSearchBar").assertIsDisplayed()
  }

  @Test
  fun profileImageNavigatesToProfileScreen() {
    composeTestRule.onNodeWithTag("servicesScreenProfileImage").performClick()
    verify(navigationActions).navigateTo(Route.SEEKER_PROFILE)
  }

  @Test
  fun locationButtonNavigatesToLocationScreen() {
    composeTestRule.onNodeWithTag("servicesScreenLocationButton").performClick()
    /*TODO*/
  }

  @Test
  fun shortcutsSectionHasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreenShortcuts").assertIsDisplayed()
    composeTestRule.onNodeWithTag("solveItWithAi").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenOrdersShortcut").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenMapShortcut").assertIsDisplayed()
  }

  @Test
  fun providersShortcutsNavigateToProvidersScreen() {
    composeTestRule.onNodeWithTag("solveItWithAi").performClick()
    verify(navigationActions).navigateTo(Route.AI_SOLVER)
  }

  @Test
  fun ordersShortcutsNavigateToOrdersScreen() {
    composeTestRule.onNodeWithTag("servicesScreenOrdersShortcut").performClick()
    verify(navigationActions).navigateTo(Route.REQUESTS_OVERVIEW)
  }

  @Test
  fun mapShortcutsNavigateToMapScreen() {
    composeTestRule.onNodeWithTag("servicesScreenMapShortcut").performClick()
    verify(navigationActions).navigateTo(Route.MAP)
  }

  @Test
  fun categoriesSectionHasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreenCategories").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCategoriesTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenCategoriesList").assertIsDisplayed()
  }

  @Test
  fun servicesItemsAreDisplayed() {
    for (service in SERVICES_LIST.take(3)) {
      composeTestRule.onNodeWithTag(service.service.toString() + "Item").assertIsDisplayed()
    }
  }

  @Test
  fun clickServiceItemNavigatesToProvidersScreen() {
    val service = SERVICES_LIST[0]
    composeTestRule.onNodeWithTag(service.service.toString() + "Item").performClick()
    assert(service.service == listProviderViewModel.selectedService.value)
    verify(navigationActions).navigateTo(Route.PROVIDERS)
  }

  @Test
  fun performersSectionHasRequiredComponents() {
    composeTestRule.onNodeWithTag("servicesScreenPerformers").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenPerformersTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesScreenPerformersList").assertIsDisplayed()
  }
}
