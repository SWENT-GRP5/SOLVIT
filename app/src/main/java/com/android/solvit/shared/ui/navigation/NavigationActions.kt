package com.android.solvit.shared.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

object Route {
  // Authentication & Registration
  const val AUTH = "Auth"
  const val SEEKER_REGISTRATION = "Seeker registration"
  const val PROVIDER_REGISTRATION = "Provider registration"

  // Seeker UI
  const val SEEKER_OVERVIEW = "Overview"
  const val PROVIDERS_LIST = "Providers"
  const val PROVIDER_INFO = "Provider info"
  const val REQUESTS_OVERVIEW = "Requests Overview"
  const val CREATE_REQUEST = "Create request"
  const val EDIT_REQUEST = "Edit request"
  const val REVIEW = "Review"
  const val AI_SOLVER = "Ai Solver"

  // Provider UI
  const val REQUESTS_FEED = "Requests Feed"
  const val CALENDAR = "Calendar"
  const val JOBS = "Jobs"
  const val NOTIFICATIONS = "Notifications Screen"

  // Shared UI
  const val PROFILE = "Profile"
  const val MAP = "Map"
  const val INBOX = "Messages"
  const val BOOKING_DETAILS = "Booking Details"
}

object Screen {
  // Authentication & Registration
  const val OPENING = "Opening Screen"
  const val SIGN_IN = "Sign In Screen"
  const val SIGN_UP = "Sign Up Screen"
  const val CHOOSE_ROLE = "Choose Role Screen"
  const val FORGOT_PASSWORD = "Forgot Password Screen"

  // Seeker UI
  const val SEEKER_PROFILE = "Seeker Profile Screen"
  const val EDIT_SEEKER_PROFILE = "Edit Seeker Profile Screen"
  const val EDIT_PREFERENCES = "Edit Preferences"
  const val AI_SOLVER_WELCOME_SCREEN = "Ai Get Started Screen"
  const val AI_SOLVER_CHAT_SCREEN = "AI Solver Chat Screen"

  // Provider UI
  const val PROVIDER_PROFILE = "Provider Profile"
  const val PROVIDER_MODIFY_PROFILE = "Modify Provider Profile"

  // Shared UI
  const val INBOX = "Inbox Screen"
  const val CHAT = "Chat Room Screen"
}

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {

  ////////////////////////////////// Shared //////////////////////////////////
  val MAP = TopLevelDestination(route = Route.MAP, icon = Icons.Outlined.LocationOn, textId = "Map")
  val MESSAGES =
      TopLevelDestination(Route.INBOX, icon = Icons.Outlined.MailOutline, textId = "Inbox messages")
  val PROFILE =
      TopLevelDestination(
          route = Route.PROFILE, icon = Icons.Outlined.AccountCircle, textId = "Profile")

  ////////////////////////////////// SEEKER //////////////////////////////////
  val SEEKER_OVERVIEW =
      TopLevelDestination(
          route = Route.SEEKER_OVERVIEW, icon = Icons.Outlined.Home, textId = "Customer Home")
  val CREATE_REQUEST =
      TopLevelDestination(
          route = Route.CREATE_REQUEST, icon = Icons.Outlined.Add, textId = "Request")
  val REQUESTS_OVERVIEW =
      TopLevelDestination(
          route = Route.REQUESTS_OVERVIEW, icon = Icons.Outlined.Menu, textId = "Overview")

  ////////////////////////////////// PROVIDER //////////////////////////////////
  val REQUEST_FEED =
      TopLevelDestination(
          route = Route.REQUESTS_FEED, icon = Icons.Outlined.Home, textId = "Professional Home")
  val CALENDAR =
      TopLevelDestination(
          route = Route.CALENDAR, icon = Icons.Outlined.DateRange, textId = "Professional Calendar")
  val JOBS =
      TopLevelDestination(route = Route.JOBS, icon = Icons.Outlined.CheckCircle, textId = "My Jobs")
}

val LIST_TOP_LEVEL_DESTINATION_SEEKER =
    listOf(
        TopLevelDestinations.SEEKER_OVERVIEW,
        TopLevelDestinations.MAP,
        TopLevelDestinations.MESSAGES,
        TopLevelDestinations.REQUESTS_OVERVIEW,
        TopLevelDestinations.PROFILE)

val LIST_TOP_LEVEL_DESTINATION_PROVIDER =
    listOf(
        TopLevelDestinations.REQUEST_FEED,
        TopLevelDestinations.MAP,
        TopLevelDestinations.MESSAGES,
        TopLevelDestinations.CALENDAR,
        TopLevelDestinations.PROFILE)

open class NavigationActions(
    private val navController: NavController,
) {
  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top level destination to navigate to.
   *
   * Clear the back stack up to startDestination when navigating to a new destination.
   */
  open fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      popUpTo(navController.graph.startDestinationId) { inclusive = false }
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

  /**
   * Navigate to the specified screen and set the back stack to startDestination plus the specified
   * routes.
   *
   * @param screen The screen to navigate to
   * @param backStackRoutes The routes to set the back stack to
   */
  open fun navigateAndSetBackStack(screen: String, backStackRoutes: List<String>) {
    navController.popBackStack(navController.graph.startDestinationId, false)
    backStackRoutes.forEach { route ->
      navController.navigate(route) {
        launchSingleTop = true
        restoreState = true
      }
    }
    navController.navigate(screen) {
      launchSingleTop = true
      restoreState = true
    }
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
