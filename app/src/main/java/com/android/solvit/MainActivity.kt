package com.android.solvit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation

import com.android.solvit.model.provider.ListProviderViewModel
import com.android.solvit.ui.navigation.NavigationActions
import com.android.solvit.ui.navigation.Route
import com.android.solvit.ui.navigation.Screen
import com.android.solvit.ui.overview.SelectProviderScreen
import com.android.solvit.ui.screens.profile.EditProfileScreen
import com.android.solvit.ui.screens.profile.ProfileScreen
import com.android.solvit.ui.screens.profile.ProfileViewModel
import com.android.solvit.ui.services.ServicesScreen
import com.android.solvit.ui.theme.SampleAppTheme

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
  val viewModel: ProfileViewModel = viewModel(
    factory = ProfileViewModel.Factory
  )

  NavHost(navController = navController, startDestination = Route.SERVICES) {
    composable(Route.SERVICES) { ServicesScreen(navigationActions, listProviderViewModel) }
    composable(Route.PROVIDERS){ SelectProviderScreen(listProviderViewModel,navigationActions)}
    composable(Route.MESSAGE) { Text("Not implemented yet") }
    composable(Route.REQUEST) { Text("Not implemented yet") }
    composable(Route.ORDER) { Text("Not implemented yet") }
    navigation(
      startDestination = Screen.PROFILE,
      route = Route.PROFILE,
    ) {
      composable(Screen.PROFILE) { ProfileScreen(viewModel = viewModel, navigationActions) }
      composable(Screen.EDIT_PROFILE) { EditProfileScreen(viewModel = viewModel, navigationActions) }
    }


}}

/*
@Composable
fun Greeting() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  NavHost(navController = navController, startDestination = Route.HOME) {
    composable(Route.HOME) { HomeScreen(navigationActions) }
    composable(Route.MESSAGE) { MessageScreen(navigationActions) }
    composable(Route.REQUEST) { RequestScreen(navigationActions) }
    composable(Route.ORDER) { OrderScreen(navigationActions) }
    composable(Route.PROFILE) { ProfileScreen(navigationActions) }
  }
}*/
