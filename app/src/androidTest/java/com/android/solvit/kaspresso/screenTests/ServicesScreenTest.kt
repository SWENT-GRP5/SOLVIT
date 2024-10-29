package com.android.solvit.kaspresso.screenTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.kaspresso.screens.ServicesScreenObject
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.service.ServicesScreen
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class ServicesScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()) {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun servicesScreenTest() = run {
    step("Set up the Opening screen") {
      // Set up the Opening screen
      composeTestRule.setContent {
        val navigationActions = mock(NavigationActions::class.java)
        val providerRepository = mock(ProviderRepository::class.java)
        val listProviderViewModel = ListProviderViewModel(providerRepository)
        ServicesScreen(navigationActions, listProviderViewModel)
        `when`(navigationActions.currentRoute()).thenReturn(Route.SERVICES)
      }
    }

    step("Check the UI components") {
      // Check the UI components
      ComposeScreen.onComposeScreen<ServicesScreenObject>(composeTestRule) {
        searchBar.assertIsDisplayed()
        servicesGrid.assertIsDisplayed()
      }
    }

    step("Perform the click action") {
      // Perform the action
      ComposeScreen.onComposeScreen<ServicesScreenObject>(composeTestRule) {
        searchBar.performClick()
      }
    }
  }
}
