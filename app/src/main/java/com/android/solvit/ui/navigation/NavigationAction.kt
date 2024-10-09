package com.android.solvit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

object Route {
  const val SERVICES = "Overview"
  const val REQUEST = "Map"
  const val MESSAGE = "Message"
  const val PROFILE = "Profile"
  const val ORDER = "Order"
}

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {

  val SERVICES =
      TopLevelDestination(route = Route.SERVICES, icon = Icons.Outlined.Home, textId = "Home")

  val MESSAGE =
      TopLevelDestination(
          route = Route.MESSAGE, icon = Icons.Outlined.MailOutline, textId = "Message")
  val REQUEST =
      TopLevelDestination(route = Route.REQUEST, icon = Icons.Outlined.Create, textId = "Request")
  val ORDER =
      TopLevelDestination(route = Route.ORDER, icon = Icons.Outlined.Menu, textId = "Settings")
  val PROFILE =
      TopLevelDestination(
          route = Route.PROFILE, icon = Icons.Outlined.AccountCircle, textId = "Profile")
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(
        TopLevelDestinations.SERVICES,
        TopLevelDestinations.MESSAGE,
        TopLevelDestinations.REQUEST,
        TopLevelDestinations.ORDER,
        TopLevelDestinations.PROFILE)

open class NavigationActions(
    private val navController: NavController,
) {
  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top level destination to navigate to.
   *
   * Clear the back stack when navigating to a new destination.
   */
  open fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      popUpTo(navController.graph.startDestinationId) {
        saveState = true
        inclusive = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
