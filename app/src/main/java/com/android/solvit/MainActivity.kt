package com.android.solvit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
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
import com.android.solvit.seeker.ui.service.ServicesScreen
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.authentication.OpeningScreen
import com.android.solvit.shared.ui.authentication.SignInScreen
import com.android.solvit.shared.ui.authentication.SignUpChooseProfile
import com.android.solvit.shared.ui.authentication.SignUpScreen
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.SampleAppTheme
import com.android.solvit.ui.message.MessageScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Firebase.auth.signOut()
    setContent {
      SampleAppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          SolvItApp()
        }
      }
    }
  }
}

@Composable
fun SolvItApp() {
  val authViewModel = viewModel<AuthViewModel>(factory = AuthViewModel.Factory)
  val user = authViewModel.user.collectAsState()
  val listProviderViewModel =
      viewModel<ListProviderViewModel>(factory = ListProviderViewModel.Factory)
  val seekerProfileViewModel =
      viewModel<SeekerProfileViewModel>(factory = SeekerProfileViewModel.Factory)

  if (user.value == null) {
    SharedUI(authViewModel, listProviderViewModel, seekerProfileViewModel)
  } else {
    when (user.value!!.role) {
      "seeker" -> SeekerUI(authViewModel, listProviderViewModel, seekerProfileViewModel)
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
    composable(Screen.SEEKER_REGISTRATION_PROFILE) {
      SeekerRegistrationScreen(seekerProfileViewModel, navigationActions, authViewModel)
    }
  }
}

@Composable
fun SeekerUI(
    authViewModel: AuthViewModel,
    listProviderViewModel: ListProviderViewModel,
    seekerProfileViewModel: SeekerProfileViewModel
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(navController = navController, startDestination = Route.SERVICES) {
    composable(Route.SERVICES) { ServicesScreen(navigationActions, listProviderViewModel) }
    composable(Route.PROVIDERS) { SelectProviderScreen(listProviderViewModel, navigationActions) }
    composable(Route.MESSAGE) { MessageScreen(navigationActions) }
    composable(Route.CREATE_REQUEST) { CreateRequestScreen(navigationActions) }
    composable(Route.EDIT_REQUEST) { EditRequestScreen(navigationActions) }
    composable(Route.MAP) { SeekerMapScreen(listProviderViewModel, navigationActions) }
    composable(Route.ORDER) {
      EditRequestScreen(navigationActions)
    } // This line can be replace when the OrderScreen is implemented
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
  Text("Provider UI")
}
