package com.android.solvit.shared.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

object Route {
  const val AUTH = "Auth"
  const val SERVICES = "Overview"
  const val REQUESTS_FEED = "Requests Feed"
  const val CREATE_REQUEST = "Create request"
  const val EDIT_REQUEST = "Edit request"
  const val MESSAGE = "Message"
  const val PROFILE = "Profile"
  const val ORDER = "Order"
  const val PROVIDERS = "Providers"
  const val MAP = "Map"
  const val MAP_OF_SEEKERS = "Seekers Map"
  const val CALENDAR = "Calendar"
}

object Screen {
  const val PROFILE = "Profile Screen"
  const val EDIT_PROFILE = "EditProfile Screen"
  const val SIGN_IN = "Sign In"
  const val SIGN_UP = "Sign Up"
  const val SIGN_UP_CHOOSE_ROLE = "Choose Role Screen"
  const val SEEKER_REGISTRATION_PROFILE = "Seeker registration"
  const val PROVIDER_REGISTRATION_PROFILE = "Provider registration"
  const val CALENDAR = "Calendar"
}

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {

  val SERVICES =
      TopLevelDestination(
          route = Route.SERVICES, icon = Icons.Outlined.Home, textId = "Custommer Home")
  val REQUEST_FEED =
      TopLevelDestination(
          route = Route.REQUESTS_FEED, icon = Icons.Outlined.Home, textId = "Professional Home")
  val MAP_OF_SEEKERS =
      TopLevelDestination(
          route = Route.MAP_OF_SEEKERS, icon = Icons.Outlined.LocationOn, textId = "Map of Seekers")
  val CALENDAR =
      TopLevelDestination(
          route = Route.CALENDAR, icon = Icons.Outlined.DateRange, textId = "Professional Calendar")

  val MESSAGE =
      TopLevelDestination(
          route = Route.MESSAGE, icon = Icons.Outlined.MailOutline, textId = "Message")
  val EDIT_REQUEST =
      TopLevelDestination(
          route = Route.EDIT_REQUEST, icon = Icons.Outlined.Edit, textId = "Edit Request")
  val MAP_OF_PROVIDERS =
      TopLevelDestination(
          route = Route.MAP, icon = Icons.Outlined.LocationOn, textId = "Providers Map")
  val CREATE_REQUEST =
      TopLevelDestination(
          route = Route.CREATE_REQUEST, icon = Icons.Outlined.Add, textId = "Request")
  val ORDER =
      TopLevelDestination(route = Route.ORDER, icon = Icons.Outlined.Menu, textId = "Settings")
  val PROFILE =
      TopLevelDestination(
          route = Route.PROFILE, icon = Icons.Outlined.AccountCircle, textId = "Profile")
}

val LIST_TOP_LEVEL_DESTINATION_CUSTOMMER =
    listOf(
        TopLevelDestinations.SERVICES,
        TopLevelDestinations.MAP_OF_PROVIDERS,
        TopLevelDestinations.EDIT_REQUEST,
        TopLevelDestinations.PROFILE)

val LIST_TOP_LEVEL_DESTINATION_PROVIDER =
    listOf(
        TopLevelDestinations.REQUEST_FEED,
        TopLevelDestinations.MAP_OF_SEEKERS,
        TopLevelDestinations.CALENDAR)

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
      popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
