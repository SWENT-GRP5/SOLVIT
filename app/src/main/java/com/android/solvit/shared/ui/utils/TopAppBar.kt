package com.android.solvit.shared.ui.utils

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.android.solvit.shared.ui.theme.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBarInbox(
    titre: String = "",
    testTagTitle: String = "",
    leftButtonAction: () -> Unit = {},
    leftButtonForm: ImageVector? = null,
    testTagLeft: String = "",
    rightButton: () -> Unit = {},
    rightButtonForm: ImageVector? = null,
    testTagRight: String = "",
    testTagGeneral: String = ""
) {
  var canGoBack by remember { mutableStateOf(true) }
  val coroutineScope = rememberCoroutineScope()
  TopAppBar(
      modifier = Modifier.testTag(testTagGeneral),
      title = {
        Text(
            text = titre,
            style = Typography.titleLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.testTag(testTagTitle))
      },
      navigationIcon = {
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
            }) {
              if (leftButtonForm != null) {
                Icon(
                    leftButtonForm,
                    contentDescription = "Back",
                    modifier = Modifier.testTag(testTagLeft))
              }
            }
      },
      actions = {
        IconButton(onClick = { rightButton() }) {
          if (rightButtonForm != null) {
            Icon(
                rightButtonForm,
                contentDescription = "Back",
                modifier = Modifier.testTag(testTagRight))
          }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background))
}
