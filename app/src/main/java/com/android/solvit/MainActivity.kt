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
import com.android.solvit.provider.model.ProviderCalendarViewModel
import com.android.solvit.provider.model.profile.ProviderViewModel
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
import com.android.solvit.shared.model.chat.AiSolverViewModel
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
  val calendarViewModel = viewModel {
    ProviderCalendarViewModel(authViewModel, serviceRequestViewModel)
  }
  val aiSolverViewModel = viewModel<AiSolverViewModel>(factory = AiSolverViewModel.Factory)
  val notificationViewModel =
      viewModel<NotificationsViewModel>(factory = NotificationsViewModel.Factory)
  val providerViewModel = viewModel<ProviderViewModel>(factory = ProviderViewModel.Factory)

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
              aiSolverViewModel,
              packageProposalViewModel)
      "provider" ->
          ProviderUI(
              authViewModel,
              providerViewModel,
              listProviderViewModel,
              serviceRequestViewModel,
              seekerProfileViewModel,
              chatViewModel,
              notificationViewModel,
              locationViewModel,
              packageProposalViewModel,
              chatAssistantViewModel,
              calendarViewModel)
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
  val user by authViewModel.user.collectAsState()

  val startDestination =
      when {
        user == null -> Route.AUTH
        user!!.role == "seeker" -> Route.SEEKER_REGISTRATION
        user!!.role == "provider" -> Route.PROVIDER_REGISTRATION
        else -> Route.AUTH
      }

  NavHost(navController = navController, startDestination = startDestination) {
    // Authentication
    navigation(startDestination = Screen.OPENING, route = Route.AUTH) {
      composable(Screen.OPENING) { OpeningScreen(navigationActions) }
      composable(Screen.SIGN_IN) { SignInScreen(navigationActions, authViewModel) }
      composable(Screen.SIGN_UP) { SignUpScreen(navigationActions, authViewModel) }
      composable(Screen.CHOOSE_ROLE) { SignUpChooseProfile(navigationActions, authViewModel) }
      composable(Screen.FORGOT_PASSWORD) { ForgotPassword(navigationActions) }
    }

    // Registration
    composable(Route.PROVIDER_REGISTRATION) {
      ProviderRegistrationScreen(
          listProviderViewModel,
          navigationActions,
          locationViewModel,
          authViewModel,
          packageProposalViewModel,
          packagesAssistantViewModel)
    }
    composable(Route.SEEKER_REGISTRATION) {
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
    aiSolverViewModel: AiSolverViewModel,
    packageProposalViewModel: PackageProposalViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val user by authViewModel.user.collectAsState()

  NavHost(navController = navController, startDestination = Route.SEEKER_OVERVIEW) {
    // Overview
    composable(Route.SEEKER_OVERVIEW) { ServicesScreen(navigationActions, seekerProfileViewModel, listProviderViewModel) }

    // Providers
    composable(Route.PROVIDERS_LIST) {
      SelectProviderScreen(
          seekerProfileViewModel,
          listProviderViewModel,
          user!!.uid,
          navigationActions,
          locationViewModel)
    }
    composable(Route.PROVIDER_INFO) {
      ProviderInfoScreen(
          navigationActions,
          listProviderViewModel,
          reviewViewModel,
          serviceRequestViewModel,
          authViewModel)
    }

    // Service Requests
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
    composable(Route.CREATE_REQUEST) {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationViewModel,
          listProviderViewModel)
    }
    composable(Route.EDIT_REQUEST) {
      EditRequestScreen(
          navigationActions, serviceRequestViewModel, locationViewModel, authViewModel)
    }
    composable(Route.REVIEW) {
      CreateReviewScreen(
          reviewViewModel, serviceRequestViewModel, listProviderViewModel, navigationActions)
    }

    // Map
    composable(Route.MAP) { SeekerMapScreen(listProviderViewModel, navigationActions) }

    // Profile
    navigation(startDestination = Screen.SEEKER_PROFILE, route = Route.PROFILE) {
      composable(Screen.SEEKER_PROFILE) {
        SeekerProfileScreen(seekerProfileViewModel, navigationActions, authViewModel)
      }
      composable(Screen.EDIT_SEEKER_PROFILE) {
        EditSeekerProfileScreen(seekerProfileViewModel, navigationActions, authViewModel)
      }
      composable(Screen.EDIT_PREFERENCES) {
        EditPreferences(user!!.uid, seekerProfileViewModel, navigationActions)
      }
    }

    // Chat
    navigation(startDestination = Screen.INBOX, route = Route.INBOX) {
      composable(Screen.INBOX) {
        MessageBox(
            chatViewModel,
            navigationActions,
            authViewModel,
            listProviderViewModel,
            seekerProfileViewModel)
      }
      composable(Screen.CHAT) {
        ChatScreen(
            navigationActions,
            chatViewModel,
            authViewModel,
            chatAssistantViewModel,
            serviceRequestViewModel)
      }
    }

    // AI solver
    navigation(startDestination = Screen.AI_SOLVER_WELCOME_SCREEN, Route.AI_SOLVER) {
      composable(Screen.AI_SOLVER_WELCOME_SCREEN) {
        AiSolverWelcomeScreen(navigationActions, chatViewModel, authViewModel)
      }
      composable(Screen.AI_SOLVER_CHAT_SCREEN) {
        AiSolverScreen(navigationActions, authViewModel, chatViewModel, aiSolverViewModel)
      }
    }
  }
}

@Composable
fun ProviderUI(
    authViewModel: AuthViewModel,
    providerViewModel: ProviderViewModel,
    listProviderViewModel: ListProviderViewModel,
    serviceRequestViewModel: ServiceRequestViewModel,
    seekerProfileViewModel: SeekerProfileViewModel,
    chatViewModel: ChatViewModel,
    notificationViewModel: NotificationsViewModel,
    locationViewModel: LocationViewModel,
    packageViewModel: PackageProposalViewModel,
    chatAssistantViewModel: ChatAssistantViewModel,
    calendarViewModel: ProviderCalendarViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val user by authViewModel.user.collectAsState()

  NavHost(navController = navController, startDestination = Route.REQUESTS_FEED) {
    // Overview
    composable(Route.REQUESTS_FEED) {
      ListRequestsFeedScreen(
          serviceRequestViewModel,
          packageViewModel,
          navigationActions,
          notificationViewModel,
          authViewModel)
    }

    // Map
    composable(Route.MAP) {
      ProviderMapScreen(
          serviceRequestViewModel = serviceRequestViewModel, navigationActions = navigationActions)
    }

    // Calendar
    composable(Route.CALENDAR) {
      ProviderCalendarScreen(navigationActions = navigationActions, viewModel = calendarViewModel)
    }

    // Jobs & Bookings
    composable(Route.JOBS) {
      RequestsDashboardScreen(
          navigationActions = navigationActions,
          serviceRequestViewModel = serviceRequestViewModel,
          authViewModel = authViewModel)
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

    // Profile
    navigation(startDestination = Screen.PROVIDER_PROFILE, route = Route.PROFILE) {
      composable(Screen.PROVIDER_PROFILE) {
        ProviderProfileScreen(
            providerViewModel, authViewModel, serviceRequestViewModel, navigationActions)
      }
      composable(Screen.EDIT_PROVIDER_PROFILE) {
        ModifyProviderInformationScreen(
            providerViewModel, authViewModel, locationViewModel, navigationActions)
      }
    }

    // Chat
    navigation(startDestination = Screen.INBOX, route = Route.INBOX) {
      composable(Screen.INBOX) {
        MessageBox(
            chatViewModel,
            navigationActions,
            authViewModel,
            listProviderViewModel,
            seekerProfileViewModel)
      }
      composable(Screen.CHAT) {
        ChatScreen(
            navigationActions,
            chatViewModel,
            authViewModel,
            chatAssistantViewModel,
            serviceRequestViewModel)
      }
    }

    // Notifications
    composable(Route.NOTIFICATIONS) {
      NotificationScreen(notificationViewModel, user!!.uid, navigationActions)
    }
  }
}
