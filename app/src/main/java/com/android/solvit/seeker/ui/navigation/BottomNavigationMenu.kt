package com.android.solvit.seeker.ui.navigation

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.TopLevelDestination
import com.android.solvit.shared.ui.navigation.TopLevelDestinations

@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(80.dp) // Increase height for the central button
              .background(Color.Transparent)
              .testTag("bottomNavigationMenu"),
      contentAlignment = Alignment.BottomCenter) {
        Canvas(
            modifier =
                Modifier.fillMaxWidth()
                    .height(60.dp) // Height of the navigation bar
                    .background(Color.Transparent)) {
              val path =
                  Path().apply {
                    // Start on the left
                    moveTo(0f, 0f)

                    // Straight line until about 32% of the width (instead of 35%)
                    lineTo(size.width * 0.3f, 0f)

                    // Smooth transition to the curve (no sharp right angle)
                    cubicTo(
                        size.width * 0.3f,
                        0f, // First control point slightly moved to the left
                        size.width * 0.35f,
                        0f, // Curve down slightly shifted to the left
                        size.width * 0.38f,
                        20.dp.toPx() // Start descending toward the main curve, also shifted
                        )

                    // Main downward curve
                    quadraticBezierTo(
                        size.width * 0.5f,
                        70.dp.toPx(), // Control point for a deep curve, more to the left
                        size.width * 0.6f,
                        30.dp.toPx() // End of the main curve, also slightly shifted
                        )

                    // Smooth transition to go back up the curve
                    cubicTo(
                        size.width * 0.62f,
                        20.dp.toPx(), // Small upward round, slightly more to the left
                        size.width * 0.65f,
                        0f, // Back to the straight line
                        size.width * 0.7f,
                        0f // Finish with a smooth transition
                        )

                    // Straight line to finish
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                  }

              // Draw the path
              drawPath(
                  path = path,
                  color = Color.Red, // Red background for the bar
                  style = Fill)
            }

        // Red container for the other icons
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp) // Red strip for the icons
                    .clip(RoundedCornerShape(30.dp)), // Rounded edges of the strip
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              val filteredTabList = tabList.filter { it.route != Route.CREATE_REQUEST }
              filteredTabList.forEachIndexed { index, tab ->
                if (index == tabList.size / 2) {
                  Spacer(modifier = Modifier.weight(1.7f)) // Leave space for the central button
                }
                BottomNavigationItem(
                    icon = { Icon(tab.icon, contentDescription = null, tint = Color.White) },
                    selected = tab.route == selectedItem,
                    onClick = { onTabSelect(tab) },
                    modifier = Modifier.testTag(tab.textId))
              }
            }

        // Central "+" button above the red strip
        FloatingActionButton(
            onClick = { onTabSelect(TopLevelDestinations.CREATE_REQUEST) },
            modifier =
                Modifier.size(70.dp)
                    .offset(y = (-10).dp) // Larger size for the central button
                    .align(Alignment.TopCenter)
                    .testTag(TopLevelDestinations.CREATE_REQUEST.toString()), // Centered position
            shape = CircleShape,
            containerColor = Color.Red // Central button color (red like in the image)
            ) {
              Icon(
                  Icons.Outlined.Add,
                  contentDescription = "Add",
                  tint = Color.White,
                  modifier = Modifier.size(30.dp)) // Red icon
        }
      }
}
