package com.android.solvit.shared.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.solvit.shared.ui.navigation.NavigationActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A composable function that displays a "Go Back" button with a debounce mechanism to prevent
 * multiple rapid clicks.
 *
 * @param navigationActions A set of navigation actions to handle screen transitions.
 */
@Composable
fun GoBackButton(navigationActions: NavigationActions) {
  var canGoBack by remember { mutableStateOf(true) }
  val coroutineScope = rememberCoroutineScope()
  IconButton(
      onClick = {
        if (canGoBack) {
          canGoBack = false
          navigationActions.goBack()
          coroutineScope.launch {
            delay(500)
            canGoBack = true
          }
        }
      },
      modifier = Modifier.testTag("goBackButton"),
      enabled = canGoBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "goBackButton")
      }
}
