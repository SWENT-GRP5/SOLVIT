package com.android.solvit.seeker.ui.navigation

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_SEEKER
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.TopLevelDestination
import com.android.solvit.shared.ui.navigation.TopLevelDestinations

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {

  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  // Get the background color from the theme
  val backgroundColor = colorScheme.background

  BoxWithConstraints(
      modifier =
          Modifier.fillMaxWidth()
              .height(60.dp)
              .background(Color.Transparent)
              .testTag("bottomNavigationMenu"),
      contentAlignment = Alignment.BottomCenter) {
        val width = maxWidth
        val height = maxHeight
        Canvas(modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.Transparent)) {
          val path =
              Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.7f, 0f)
                cubicTo(
                    size.width * 0.72f,
                    0f,
                    size.width * 0.75f,
                    0f,
                    size.width * 0.77f,
                    10.dp.toPx())
                quadraticTo(size.width * 0.86f, 60.dp.toPx(), size.width * 0.96f, 10.dp.toPx())

                cubicTo(
                    size.width * 0.96f, 10.dp.toPx(), size.width * 0.98f, 0f, size.width * 1f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
              }

          drawPath(path = path, color = backgroundColor, style = Fill)
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 80.dp).height(60.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              val filteredTabList = tabList.filter { it.route != Route.CREATE_REQUEST }
              filteredTabList.forEachIndexed { _, tab ->
                NavigationBarItem(
                    icon = {
                      Icon(
                          tab.icon,
                          contentDescription = null,
                          tint =
                              if (tab.route == selectedItem) colorScheme.onBackground
                              else colorScheme.onSurfaceVariant)
                    },
                    selected = tab.route == selectedItem,
                    onClick = { onTabSelect(tab) },
                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = colorScheme.primary,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            indicatorColor = colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag(tab.textId))
              }
            }

        FloatingActionButton(
            onClick = {
              onTabSelect(
                  if (tabList == LIST_TOP_LEVEL_DESTINATION_SEEKER)
                      TopLevelDestinations.CREATE_REQUEST
                  else TopLevelDestinations.MY_JOBS)
            },
            modifier =
                Modifier.size(height * 0.85f)
                    .offset(y = (-25).dp)
                    .offset(x = width * 0.5f - 56.dp)
                    .align(Alignment.TopCenter)
                    .testTag(TopLevelDestinations.CREATE_REQUEST.toString()),
            shape = CircleShape,
            containerColor = colorScheme.primary) {
              Icon(
                  if (tabList == LIST_TOP_LEVEL_DESTINATION_SEEKER) Icons.Outlined.Add
                  else Icons.Outlined.CheckCircle,
                  contentDescription =
                      if (tabList == LIST_TOP_LEVEL_DESTINATION_SEEKER) "Add" else "Myjobs",
                  tint = colorScheme.onPrimary,
                  modifier =
                      Modifier.size(30.dp)
                          .testTag(
                              if (tabList == LIST_TOP_LEVEL_DESTINATION_SEEKER) "AddTestTag"
                              else "MyJobsTestTag"))
            }
      }
}

@Preview(showBackground = true)
@Composable
fun PreviewSeekerBottomNavigationMenu() {
  // Use the actual list of top-level destinations
  val tabList = LIST_TOP_LEVEL_DESTINATION_SEEKER

  // Preview with "Home" as the selected item
  BottomNavigationMenu(
      onTabSelect = {}, // No action needed for preview
      tabList = tabList,
      selectedItem = Route.SERVICES // Default to SERVICES (Home)
      )
}
