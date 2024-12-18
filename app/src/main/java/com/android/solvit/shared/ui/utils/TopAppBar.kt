package com.android.solvit.shared.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.solvit.R
import com.android.solvit.shared.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBarInbox(
    title: String = "",
    showAppIcon: Boolean = false,
    testTagTitle: String = "",
    leftButtonAction: () -> Unit = {},
    leftButtonForm: ImageVector? = null,
    testTagLeft: String = "",
    rightButtonAction: () -> Unit = {},
    rightButtonForm: ImageVector? = null,
    testTagRight: String = "",
    testTagGeneral: String = "",
    containerColor: Color = colorScheme.background
) {
  var canGoBack by remember { mutableStateOf(true) }
  val coroutineScope = rememberCoroutineScope()
  TopAppBar(
      modifier = Modifier.testTag(testTagGeneral),
      title = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          if (showAppIcon) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Icon",
                modifier = Modifier.size(50.dp).padding(end = 8.dp))
          }
          Text(
              text = title,
              style = Typography.titleLarge,
              textAlign = TextAlign.Start,
              modifier = Modifier.testTag(testTagTitle))
        }
      },
      navigationIcon = {
        if (leftButtonForm != null) {
          IconButton(
              onClick = {
                if (canGoBack) {
                  canGoBack = false
                  leftButtonAction()
                  coroutineScope.launch {
                    delay(500)
                    canGoBack = true
                  }
                }
              },
              modifier = Modifier.testTag(testTagLeft)) {
                Icon(leftButtonForm, contentDescription = "Back", tint = colorScheme.onSurface)
              }
        }
      },
      actions = {
        if (rightButtonForm != null) {
          IconButton(
              onClick = {
                if (canGoBack) {
                  canGoBack = false
                  rightButtonAction()
                  coroutineScope.launch {
                    delay(500)
                    canGoBack = true
                  }
                }
              },
              modifier = Modifier.testTag(testTagRight)) {
                Icon(rightButtonForm, contentDescription = "Back", tint = colorScheme.onSurface)
              }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor))
}
