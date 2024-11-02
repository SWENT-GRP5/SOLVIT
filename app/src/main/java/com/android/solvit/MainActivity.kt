package com.android.solvit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import com.android.solvit.provider.ui.calendar.ProviderCalendarScreen
import com.android.solvit.provider.ui.map.ProviderMapScreen
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.map.SeekerMapScreen
import com.android.solvit.seeker.ui.profile.EditSeekerProfileScreen
import com.android.solvit.seeker.ui.profile.SeekerProfileScreen
import com.android.solvit.seeker.ui.profile.SeekerRegistrationScreen
import com.android.solvit.seeker.ui.provider.ProviderRegistrationScreen
import com.android.solvit.seeker.ui.provider.SelectProviderScreen
import com.android.solvit.seeker.ui.request.CreateRequestScreen
import com.android.solvit.seeker.ui.request.EditRequestScreen
import com.android.solvit.seeker.ui.request.RequestsOverviewScreen
import com.android.solvit.seeker.ui.service.ServicesScreen
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.ui.authentication.ForgotPassword
import com.android.solvit.shared.ui.authentication.OpeningScreen
import com.android.solvit.shared.ui.authentication.SignInScreen
import com.android.solvit.shared.ui.authentication.SignUpChooseProfile
import com.android.solvit.shared.ui.authentication.SignUpScreen
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.SampleAppTheme
import com.android.solvit.ui.message.MessageScreen
import com.android.solvit.ui.requests.ListRequestsFeedScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Firebase.auth.signOut()
    setContent {
      SampleAppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          SolvitApp()
        }
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

  if (!userRegistered.value) {
    SharedUI(authViewModel, listProviderViewModel, seekerProfileViewModel)
  } else {
    when (user.value!!.role) {
      "seeker" ->
          SeekerUI(
              authViewModel, listProviderViewModel, seekerProfileViewModel, serviceRequestViewModel)
      "provider" -> ProviderUI(authViewModel, listProviderViewModel, seekerProfileViewModel)
    }
  }
}

@Composable
fun SharedUI(
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(navController = navController, startDestination = Route.AUTH) {
    composable(Route.AUTH) { OpeningScreen(navigationActions) }
    composable(Screen.SIGN_IN) { SignInScreen(navigationActions, authViewModel) }
    composable(Screen.SIGN_UP) { SignUpScreen(navigationActions, authViewModel) }
    composable(Screen.SIGN_UP_CHOOSE_ROLE) { SignUpChooseProfile(navigationActions, authViewModel) }
    composable(Screen.PROVIDER_REGISTRATION_PROFILE) {
      ProviderRegistrationScreen(listProviderViewModel, navigationActions, authViewModel)
    }
    composable(Screen.FORGOT_PASSWORD) { ForgotPassword(navigationActions) }
    composable(Screen.SEEKER_REGISTRATION_PROFILE) {
      SeekerRegistrationScreen(seekerProfileViewModel, navigationActions, authViewModel)
    }
  }
}

@Composable
fun SeekerUI(
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel,
    serviceRequestViewModel: ServiceRequestViewModel
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
        )
      }
    }
    composable(Route.MESSAGE) { MessageScreen(navigationActions) }
    composable(Route.CREATE_REQUEST) {
      user?.let { user ->
        CreateRequestScreen(
            navigationActions = navigationActions,
            requestViewModel = serviceRequestViewModel,
            userId = user.uid)
      }
    }
    composable(Route.REQUESTS_OVERVIEW) {
      user?.let { user ->
        RequestsOverviewScreen(navigationActions, serviceRequestViewModel, user.uid)
      }
    }
    composable(Route.EDIT_REQUEST) { EditRequestScreen(navigationActions, serviceRequestViewModel) }
    composable(Route.MAP) { SeekerMapScreen(listProviderViewModel, navigationActions) }
    navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
      composable(Screen.PROFILE) { SeekerProfileScreen(seekerProfileViewModel, navigationActions) }
      composable(Screen.EDIT_PROFILE) {
        EditSeekerProfileScreen(seekerProfileViewModel, navigationActions, authViewModel)
      }
    }
  }
}

@Composable
fun ProviderUI(
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(navController = navController, startDestination = Route.REQUESTS_FEED) {
    composable(Route.REQUESTS_FEED) {
      ListRequestsFeedScreen(navigationActions = navigationActions)
    }
    composable(Route.MAP_OF_SEEKERS) { ProviderMapScreen(navigationActions = navigationActions) }
    composable(Screen.CALENDAR) { ProviderCalendarScreen(navigationActions = navigationActions) }
  }
}
