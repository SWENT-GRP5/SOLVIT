package com.android.solvit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import com.android.solvit.seeker.ui.provider.SelectProviderScreen
import com.android.solvit.seeker.ui.request.CreateRequestScreen
import com.android.solvit.seeker.ui.request.EditRequestScreen
import com.android.solvit.seeker.ui.service.ServicesScreen
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.SampleAppTheme
import com.android.solvit.ui.authentication.SignInScreen
import com.android.solvit.ui.message.MessageScreen

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val listProviderViewModel =
      viewModel<ListProviderViewModel>(factory = ListProviderViewModel.Factory)
  val viewModel: SeekerProfileViewModel = viewModel(factory = SeekerProfileViewModel.Factory)

  NavHost(navController = navController, startDestination = Route.MESSAGE) {
    composable(Route.AUTH) { SignInScreen(navigationActions) }
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
      composable(Screen.PROFILE) { SeekerProfileScreen(viewModel = viewModel, navigationActions) }
      composable(Screen.EDIT_PROFILE) {
        EditSeekerProfileScreen(viewModel = viewModel, navigationActions)
      }
    }
  }
}
