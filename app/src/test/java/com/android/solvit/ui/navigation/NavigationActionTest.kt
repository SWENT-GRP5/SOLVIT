package com.android.solvit.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.TopLevelDestinations
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class NavigationActionTest {

  private lateinit var navigationDestination: NavDestination
  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationDestination = mock(NavDestination::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
  }

  @Test
  fun navigateToCallsController1() {

    navigationActions.navigateTo(TopLevelDestinations.SERVICES)
    verify(navHostController).navigate(eq(Route.SERVICES), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(Route.ORDER)
    verify(navHostController).navigate(Route.ORDER)
  }

  @Test
  fun goBackCallsController1() {
    navigationActions.goBack()
    verify(navHostController).popBackStack()
  }

  @Test
  fun currentRouteWorksWithDestination() {
    `when`(navHostController.currentDestination).thenReturn(navigationDestination)
    `when`(navigationDestination.route).thenReturn(Route.ORDER)

    assertThat(navigationActions.currentRoute(), `is`(Route.ORDER))
  }

  @Test
  fun navigateToCallsController() {

    navigationActions.navigateTo(TopLevelDestinations.SERVICES)
    verify(navHostController).navigate(eq(Route.SERVICES), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.CREATE_REQUEST)
    verify(navHostController)
        .navigate(eq(Route.CREATE_REQUEST), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.MESSAGE)
    verify(navHostController).navigate(eq(Route.MESSAGE), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.PROFILE)
    verify(navHostController).navigate(eq(Route.PROFILE), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.ORDER)
    verify(navHostController).navigate(eq(Route.ORDER), any<NavOptionsBuilder.() -> Unit>())
  }
}
