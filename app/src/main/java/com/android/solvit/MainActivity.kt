package com.android.solvit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.solvit.provider.ui.NotificationScreen
import com.android.solvit.provider.ui.calendar.ProviderCalendarScreen
import com.android.solvit.provider.ui.map.ProviderMapScreen
import com.android.solvit.provider.ui.profile.ModifyProviderInformationScreen
import com.android.solvit.provider.ui.profile.ProviderProfileScreen
import com.android.solvit.provider.ui.profile.ProviderRegistrationScreen
import com.android.solvit.provider.ui.request.ListRequestsFeedScreen
import com.android.solvit.provider.ui.request.RequestsDashboardScreen
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.map.SeekerMapScreen
import com.android.solvit.seeker.ui.profile.EditPreferences
import com.android.solvit.seeker.ui.profile.EditSeekerProfileScreen
import com.android.solvit.seeker.ui.profile.SeekerProfileScreen
import com.android.solvit.seeker.ui.profile.SeekerRegistrationScreen
import com.android.solvit.seeker.ui.provider.ProviderInfoScreen
import com.android.solvit.seeker.ui.provider.SelectProviderScreen
import com.android.solvit.seeker.ui.request.CreateRequestScreen
import com.android.solvit.seeker.ui.request.EditRequestScreen
import com.android.solvit.seeker.ui.request.RequestsOverviewScreen
import com.android.solvit.seeker.ui.review.CreateReviewScreen
import com.android.solvit.seeker.ui.service.ServicesScreen
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import com.android.solvit.shared.model.chat.ChatViewModel
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.packages.PackagesAssistantViewModel
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.ui.authentication.ForgotPassword
import com.android.solvit.shared.ui.authentication.OpeningScreen
import com.android.solvit.shared.ui.authentication.SignInScreen
import com.android.solvit.shared.ui.authentication.SignUpChooseProfile
import com.android.solvit.shared.ui.authentication.SignUpScreen
import com.android.solvit.shared.ui.booking.ServiceBookingScreen
import com.android.solvit.shared.ui.chat.AiSolverScreen
import com.android.solvit.shared.ui.chat.AiSolverWelcomeScreen
import com.android.solvit.shared.ui.chat.ChatScreen
import com.android.solvit.shared.ui.chat.MessageBox
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      SampleAppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) { SolvitApp() }
      }
    }
  }
}

@Composable
fun SolvitApp() {
  val authViewModel = viewModel<AuthViewModel>(factory = AuthViewModel.Factory)
  val user = authViewModel.user.collectAsState()
  val userRegistered = authViewModel.userRegistered.collectAsState()
  val listProviderViewModel =
      viewModel<ListProviderViewModel>(factory = ListProviderViewModel.Factory)
  val seekerProfileViewModel =
      viewModel<SeekerProfileViewModel>(factory = SeekerProfileViewModel.Factory)
  val serviceRequestViewModel =
      viewModel<ServiceRequestViewModel>(factory = ServiceRequestViewModel.Factory)
  val locationViewModel = viewModel<LocationViewModel>(factory = LocationViewModel.Factory)
  val reviewViewModel = viewModel<ReviewViewModel>(factory = ReviewViewModel.Factory)
  val packageProposalViewModel =
      viewModel<PackageProposalViewModel>(factory = PackageProposalViewModel.Factory)
  val chatViewModel = viewModel<ChatViewModel>(factory = ChatViewModel.Factory)
  val chatAssistantViewModel =
      viewModel<ChatAssistantViewModel>(factory = ChatAssistantViewModel.Factory)
  val notificationViewModel =
      viewModel<NotificationsViewModel>(factory = NotificationsViewModel.Factory)
  val packagesAssistantViewModel =
      viewModel<PackagesAssistantViewModel>(factory = PackagesAssistantViewModel.Factory)
  if (!userRegistered.value) {
    SharedUI(
        authViewModel,
        listProviderViewModel,
        seekerProfileViewModel,
        locationViewModel,
        packageProposalViewModel,
        packagesAssistantViewModel)
  } else {
    when (user.value!!.role) {
      "seeker" ->
          SeekerUI(
              authViewModel,
              listProviderViewModel,
              seekerProfileViewModel,
              serviceRequestViewModel,
              reviewViewModel,
              locationViewModel,
              chatViewModel,
              chatAssistantViewModel,
              notificationViewModel,
              packageProposalViewModel)
      "provider" ->
          ProviderUI(
              authViewModel,
              listProviderViewModel,
              serviceRequestViewModel,
              seekerProfileViewModel,
              chatViewModel,
              notificationViewModel,
              locationViewModel,
              packageProposalViewModel,
              chatAssistantViewModel)
    }
  }
}

@Composable
fun SharedUI(
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel,
    locationViewModel: LocationViewModel,
    packageProposalViewModel: PackageProposalViewModel,
    packagesAssistantViewModel: PackagesAssistantViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(navController = navController, startDestination = Route.AUTH) {
    composable(Route.AUTH) { OpeningScreen(navigationActions) }
    composable(Screen.SIGN_IN) { SignInScreen(navigationActions, authViewModel) }
    composable(Screen.SIGN_UP) { SignUpScreen(navigationActions, authViewModel) }
    composable(Screen.SIGN_UP_CHOOSE_ROLE) { SignUpChooseProfile(navigationActions, authViewModel) }
    composable(Screen.PROVIDER_REGISTRATION_PROFILE) {
      ProviderRegistrationScreen(
          listProviderViewModel,
          navigationActions,
          locationViewModel,
          authViewModel,
          packageProposalViewModel,
          packagesAssistantViewModel)
    }
    composable(Screen.FORGOT_PASSWORD) { ForgotPassword(navigationActions) }
    composable(Screen.SEEKER_REGISTRATION_PROFILE) {
      SeekerRegistrationScreen(
          seekerProfileViewModel, navigationActions, locationViewModel, authViewModel)
    }
  }
}

@Composable
fun SeekerUI(
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel,
    serviceRequestViewModel: ServiceRequestViewModel,
    reviewViewModel: ReviewViewModel,
    locationViewModel: LocationViewModel,
    chatViewModel: ChatViewModel,
    chatAssistantViewModel: ChatAssistantViewModel,
    notificationViewModel: NotificationsViewModel,
    packageProposalViewModel: PackageProposalViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val user by authViewModel.user.collectAsState()

  NavHost(navController = navController, startDestination = Route.SERVICES) {
    composable(Route.SERVICES) { ServicesScreen(navigationActions, listProviderViewModel) }
    composable(Route.PROVIDERS) {
      user?.let { it1 ->
        SelectProviderScreen(
            listProviderViewModel = listProviderViewModel,
            navigationActions = navigationActions,
            userId = it1.uid,
            locationViewModel = locationViewModel)
      }
    }
    composable(Route.PROVIDER_PROFILE) {
      ProviderInfoScreen(
          navigationActions,
          listProviderViewModel,
          reviewViewModel,
          serviceRequestViewModel,
          authViewModel)
    }
    navigation(startDestination = Screen.AI_SOLVER_WELCOME_SCREEN, Route.AI_SOLVER) {
      composable(Screen.AI_SOLVER_WELCOME_SCREEN) { AiSolverWelcomeScreen(navigationActions) }
      composable(Screen.AI_SOLVER_CHAT_SCREEN) { AiSolverScreen(navigationActions) }
    }
    navigation(startDestination = Screen.INBOX, route = Route.INBOX) {
      composable(Screen.INBOX) {
        MessageBox(
            chatViewModel = chatViewModel,
            navigationActions = navigationActions,
            authViewModel = authViewModel,
            listProviderViewModel = listProviderViewModel,
            seekerProfileViewModel = seekerProfileViewModel)
      }
      composable(Screen.CHAT) {
        ChatScreen(
            navigationActions = navigationActions,
            chatViewModel = chatViewModel,
            authViewModel = authViewModel,
            chatAssistantViewModel = chatAssistantViewModel)
      }
    }

    composable(Route.CREATE_REQUEST) {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          authViewModel,
          notificationViewModel,
          listProviderViewModel,
          locationViewModel)
    }
    composable(Route.REQUESTS_OVERVIEW) {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel, authViewModel)
    }
    composable(Route.BOOKING_DETAILS) {
      ServiceBookingScreen(
          navigationActions,
          authViewModel,
          seekerProfileViewModel,
          listProviderViewModel,
          serviceRequestViewModel,
          packageProposalViewModel,
          chatViewModel)
    }
    composable(Route.EDIT_REQUEST) {
      EditRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }
    composable(Route.MAP) { SeekerMapScreen(listProviderViewModel, navigationActions) }
    navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
      composable(Screen.PROFILE) {
        SeekerProfileScreen(seekerProfileViewModel, navigationActions, authViewModel)
      }
      composable(Screen.EDIT_PROFILE) {
        EditSeekerProfileScreen(seekerProfileViewModel, navigationActions, authViewModel)
      }
      composable(Screen.EDIT_PREFERENCES) {
        EditPreferences(user!!.uid, seekerProfileViewModel, navigationActions)
      }
    }
    composable(Screen.REVIEW_SCREEN) {
      CreateReviewScreen(
          reviewViewModel, serviceRequestViewModel, listProviderViewModel, navigationActions)
    }
  }
}

@Composable
fun ProviderUI(
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    serviceRequestViewModel: ServiceRequestViewModel,
    seekerProfileViewModel: SeekerProfileViewModel,
    chatViewModel: ChatViewModel,
    notificationViewModel: NotificationsViewModel,
    locationViewModel: LocationViewModel,
    packageViewModel: PackageProposalViewModel,
    chatAssistantViewModel: ChatAssistantViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val user by authViewModel.user.collectAsState()

  NavHost(navController = navController, startDestination = Route.REQUESTS_FEED) {
    composable(Route.REQUESTS_FEED) {
      ListRequestsFeedScreen(
          serviceRequestViewModel = serviceRequestViewModel, navigationActions = navigationActions)
    }
    composable(Route.MAP_OF_SEEKERS) {
      ProviderMapScreen(
          serviceRequestViewModel = serviceRequestViewModel, navigationActions = navigationActions)
    }
    composable(Screen.CALENDAR) { ProviderCalendarScreen(navigationActions = navigationActions) }
    composable(Screen.MY_JOBS) {
      RequestsDashboardScreen(navigationActions, serviceRequestViewModel)
    }
    composable(Route.BOOKING_DETAILS) {
      ServiceBookingScreen(
          navigationActions,
          authViewModel,
          seekerProfileViewModel,
          listProviderViewModel,
          serviceRequestViewModel,
          packageViewModel,
          chatViewModel)
    }
    composable(Screen.PROVIDER_PROFILE) {
      ProviderProfileScreen(listProviderViewModel, authViewModel, navigationActions)
    }
    navigation(startDestination = Screen.INBOX, route = Route.INBOX) {
      composable(Screen.INBOX) {
        MessageBox(
            chatViewModel = chatViewModel,
            navigationActions = navigationActions,
            authViewModel = authViewModel,
            listProviderViewModel = listProviderViewModel,
            seekerProfileViewModel = seekerProfileViewModel)
      }
      composable(Screen.CHAT) {
        ChatScreen(
            navigationActions = navigationActions,
            chatViewModel = chatViewModel,
            authViewModel = authViewModel,
            chatAssistantViewModel = chatAssistantViewModel)
      }
    }

    composable(Screen.MY_JOBS) { RequestsDashboardScreen(navigationActions = navigationActions) }
    composable(Screen.PROVIDER_MODIFY_PROFILE) {
      ModifyProviderInformationScreen(
          listProviderViewModel, authViewModel, locationViewModel, navigationActions)
    }
    composable(Route.NOTIFICATIONS) {
      user?.let { it1 -> NotificationScreen(notificationViewModel, it1.uid, navigationActions) }
    }
  }
}
