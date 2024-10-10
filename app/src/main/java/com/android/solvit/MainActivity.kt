package com.android.solvit


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.android.solvit.repository.FirebaseRepository
import com.android.solvit.repository.FirebaseRepositoryImp
import com.android.solvit.ui.navigation.NavigationActions
import com.android.solvit.ui.navigation.Route
import com.android.solvit.ui.navigation.Screen
import com.android.solvit.ui.screens.profile.EditProfileScreen
import com.android.solvit.ui.screens.profile.ProfileScreen
import com.android.solvit.ui.screens.profile.ProfileViewModel


class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {

        val navController = rememberNavController()
        val navigationActions = NavigationActions(navController)
        val viewModel: ProfileViewModel = viewModel(
            factory = ProfileViewModel.Factory
        )

        NavHost(navController = navController, startDestination = Route.PROFILE) {

            navigation(
                startDestination = Screen.PROFILE,
                route = Route.PROFILE,
            ) {
            composable(Screen.PROFILE) {
                ProfileScreen(viewModel = viewModel, navigationActions)
            }
            composable(Screen.EDIT_PROFILE) {
                EditProfileScreen(viewModel = viewModel,navigationActions)
                EditProfileScreen(viewModel = viewModel,navigationActions)
            }
        }
    }
  }
    }


@Composable
fun SolvitApp() {
  Text(text = "Hello, World!")
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
