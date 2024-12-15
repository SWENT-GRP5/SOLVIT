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
  const val AUTH = "Authentication"
  const val SEEKER_REGISTRATION = "Seeker_Registration"
  const val PROVIDER_REGISTRATION = "Provider_Registration"

  // Seeker UI
  const val SEEKER_OVERVIEW = "Seeker_Overview"
  const val PROVIDERS_LIST = "Providers_List"
  const val PROVIDER_INFO = "Provider_Info"
  const val REQUESTS_OVERVIEW = "Requests_Overview"
  const val CREATE_REQUEST = "Create_Request"
  const val EDIT_REQUEST = "Edit_Request"
  const val REVIEW = "Review"
  const val AI_SOLVER = "Ai_Solver"

  // Provider UI
  const val REQUESTS_FEED = "Requests_Feed"
  const val CALENDAR = "Calendar"
  const val JOBS = "Jobs"
  const val NOTIFICATIONS = "Notifications"

  // Shared UI
  const val PROFILE = "Profile"
  const val MAP = "Map"
  const val INBOX = "Messages"
  const val BOOKING_DETAILS = "Booking_Details"
}

object Screen {
  // Authentication & Registration
  const val OPENING = "Opening_Screen"
  const val SIGN_IN = "SignIn_Screen"
  const val SIGN_UP = "Sign_Up_Screen"
  const val CHOOSE_ROLE = "Choose_Role_Screen"
  const val FORGOT_PASSWORD = "Forgot_Password_Screen"

  // Seeker UI
  const val SEEKER_PROFILE = "Seeker_Profile_Screen"
  const val EDIT_SEEKER_PROFILE = "Edit_Seeker_Profile_Screen"
  const val EDIT_PREFERENCES = "Edit_Preferences_Screen"
  const val AI_SOLVER_WELCOME_SCREEN = "Ai_Get_Started_Screen"
  const val AI_SOLVER_CHAT_SCREEN = "AI_Solver_Chat_Screen"

  // Provider UI
  const val PROVIDER_PROFILE = "Provider_Profile_Screen"
  const val EDIT_PROVIDER_PROFILE = "Modify_Provider_Profile_Screen"

  // Shared UI
  const val INBOX = "Inbox_Screen"
  const val CHAT = "Chat_Room_Screen"
}

data class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val textId: String,
    val testTag: String = ""
)

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
