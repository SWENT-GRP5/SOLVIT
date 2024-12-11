package com.android.solvit.shared.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

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

    navigationActions.navigateTo(TopLevelDestinations.SEEKER_OVERVIEW)
    verify(navHostController)
        .navigate(eq(Route.SEEKER_OVERVIEW), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(Route.REQUESTS_OVERVIEW)
    verify(navHostController).navigate(Route.REQUESTS_OVERVIEW)
  }

  @Test
  fun goBackCallsController1() {
    navigationActions.goBack()
    verify(navHostController).popBackStack()
  }

  @Test
  fun currentRouteWorksWithDestination1() {
    `when`(navHostController.currentDestination).thenReturn(navigationDestination)
    `when`(navigationDestination.route).thenReturn(Route.CREATE_REQUEST)

    assertThat(navigationActions.currentRoute(), CoreMatchers.`is`(Route.CREATE_REQUEST))
  }

  @Test
  fun navigateToCallsController() {

    navigationActions.navigateTo(TopLevelDestinations.SEEKER_OVERVIEW)
    verify(navHostController)
        .navigate(eq(Route.SEEKER_OVERVIEW), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.CREATE_REQUEST)
    verify(navHostController)
        .navigate(eq(Route.CREATE_REQUEST), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.MESSAGES)
    verify(navHostController).navigate(eq(Route.INBOX), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(TopLevelDestinations.PROFILE)
    verify(navHostController).navigate(eq(Route.PROFILE), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun goBackCallsController() {
    navigationActions.goBack()
    verify(navHostController).popBackStack()
  }

  @Test
  fun currentRouteWorksWithDestination() {
    val navDestination = Mockito.mock(NavDestination::class.java)
    `when`(navHostController.currentDestination).thenReturn(navDestination)
    `when`(navDestination.route).thenReturn(Route.CREATE_REQUEST)

    assertThat(navigationActions.currentRoute(), CoreMatchers.`is`(Route.CREATE_REQUEST))
  }

  @Test
  fun goBackToCallsController() {
    navigationActions.goBackTo(Route.CREATE_REQUEST)
    Mockito.verify(navHostController).popBackStack(Route.CREATE_REQUEST, false)
  }
}
