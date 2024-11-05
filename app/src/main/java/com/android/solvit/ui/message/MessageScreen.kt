// FICHIER A SUPPRIMER QUAND LES MESSAGES SERONT IMPLEMENTES

package com.android.solvit.ui.message

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.android.solvit.shared.ui.navigation.NavigationActions

@Composable
fun MessageScreen(navigationActions: NavigationActions) {

  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  Box(modifier = Modifier.fillMaxSize()) {
    Text(
        text = "The message screen is under construction",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.align(Alignment.Center))
    /*SeekerBottomNavigationMenu(
    onTabSelect = { navigationActions.navigateTo(it.route) },
    tabList = LIST_TOP_LEVEL_DESTINATION,
    selectedItem = "Message Screen")*/
  }
}
