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
    composeTestRule.onNodeWithTag("searchBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("servicesGrid").assertIsDisplayed()
  }

  @Test
  fun allServicesDisplayed() {
    for (service in SERVICES_LIST.take(6)) {
      composeTestRule.onNodeWithTag(service.service.toString() + "Item").assertIsDisplayed()
    }
  }

  @Test
  fun addRequestButtonNavigatesToRequestScreen() {
    /*TODO*/
  }

  @Test
  fun clickServiceItemNavigatesToProvidersScreen() {
    val service = SERVICES_LIST[0]
    composeTestRule.onNodeWithTag(service.service.toString() + "Item").performClick()
    /*TODO*/
  }
}
