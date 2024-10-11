package com.android.solvit.seeker.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.TopLevelDestination
import com.android.solvit.shared.ui.navigation.TopLevelDestinations
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(Enclosed::class)
class BottomNavigationMenuTest {

  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navController = Mockito.mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
  }

  @RunWith(AndroidJUnit4::class)
  class MyComposeEspressoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMyComposableWithEspresso() {
      // Set the Compose content for testing
      composeTestRule.setContent {
          BottomNavigationMenu(
              onTabSelect = {}, tabList = LIST_TOP_LEVEL_DESTINATION, selectedItem = "home"
          )
      }

      val tab = TopLevelDestinations.SERVICES.textId

      // Verify the button is displayed
      composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
      composeTestRule.onNodeWithTag(tab).assertIsDisplayed()
    }

    @Test
    fun testBottomNavigationItemClick() {
      var selectedTab: TopLevelDestination? = null
      val tabList =
          listOf(
              TopLevelDestinations.SERVICES,
              TopLevelDestinations.CREATE_REQUEST,
              TopLevelDestinations.MESSAGE,
              TopLevelDestinations.PROFILE,
              TopLevelDestinations.ORDER
          )

      composeTestRule.setContent {
          BottomNavigationMenu(
              onTabSelect = { selectedTab = it },
              tabList = tabList,
              selectedItem = TopLevelDestinations.SERVICES.route
          )
      }

      // Simulate click on the "Request" tab
      composeTestRule.onNodeWithTag(TopLevelDestinations.SERVICES.textId).performClick()

      // Verify that the selected tab is "Request"
      assert(selectedTab == TopLevelDestinations.SERVICES)
    }
  }
}