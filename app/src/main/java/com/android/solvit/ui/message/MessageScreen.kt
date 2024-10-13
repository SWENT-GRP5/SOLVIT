package com.android.solvit.ui.message

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.solvit.shared.ui.navigation.NavigationActions

@Composable
fun MessageScreen(navigationActions: NavigationActions) {
  Box(modifier = Modifier.fillMaxSize()) {
    Text(
        text = "The message screen is under construction",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.align(Alignment.Center))
    SeekerBottomNavigationMenu(
        onTabSelect = { navigationActions.navigateTo(it.route) },
        tabList = LIST_TOP_LEVEL_DESTINATION,
        selectedItem = "Message Screen")
  }
}
