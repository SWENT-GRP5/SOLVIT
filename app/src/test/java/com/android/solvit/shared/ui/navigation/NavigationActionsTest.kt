package com.android.solvit.shared.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class NavigationActionsTest {

  private lateinit var navigationDestination: NavDestination
  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationDestination = Mockito.mock(NavDestination::class.java)
    navHostController = Mockito.mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
  }

  @Test
  fun navigateToCallsController1() {
    // Test top level destination navigation
    navigationActions.navigateTo(TopLevelDestinations.SERVICES)
    Mockito.verify(navHostController)
        .navigate(eq(Route.SERVICES), any<NavOptionsBuilder.() -> Unit>())

    // Test regular screen navigation
    navigationActions.navigateTo(Route.REQUESTS_OVERVIEW)
    Mockito.verify(navHostController)
        .navigate(
            eq(Route.REQUESTS_OVERVIEW),
            any<NavOptionsBuilder.() -> Unit>() // Changed to verify with options
            )
  }

  @Test
  fun goBackCallsController1() {
    navigationActions.goBack()
    Mockito.verify(navHostController).popBackStack()
  }

  @Test
  fun currentRouteWorksWithDestination1() {
    Mockito.`when`(navHostController.currentDestination).thenReturn(navigationDestination)
    Mockito.`when`(navigationDestination.route).thenReturn(Route.CREATE_REQUEST)

    MatcherAssert.assertThat(
        navigationActions.currentRoute(), CoreMatchers.`is`(Route.CREATE_REQUEST))
  }

  @Test
  fun navigateToCallsController() {

    navigationActions.navigateTo(TopLevelDestinations.SERVICES)
    Mockito.verify(navHostController)
        .navigate(eq(Route.SERVICES), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.CREATE_REQUEST)
    Mockito.verify(navHostController)
        .navigate(eq(Route.CREATE_REQUEST), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.MESSAGE)
    Mockito.verify(navHostController)
        .navigate(eq(Route.MESSAGE), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.PROFILE)
    Mockito.verify(navHostController)
        .navigate(eq(Route.PROFILE), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun goBackCallsController() {
    navigationActions.goBack()
    Mockito.verify(navHostController).popBackStack()
  }

  @Test
  fun currentRouteWorksWithDestination() {
    val navDestination = Mockito.mock(NavDestination::class.java)
    Mockito.`when`(navHostController.currentDestination).thenReturn(navDestination)
    Mockito.`when`(navDestination.route).thenReturn(Route.CREATE_REQUEST)

    MatcherAssert.assertThat(
        navigationActions.currentRoute(), CoreMatchers.`is`(Route.CREATE_REQUEST))
  }
}
