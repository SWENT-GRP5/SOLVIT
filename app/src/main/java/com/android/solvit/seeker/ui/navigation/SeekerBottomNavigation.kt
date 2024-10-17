package com.android.solvit.seeker.ui.navigation

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
//noinspection UsingMaterialAndMaterial3Libraries

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.TopLevelDestination
import com.android.solvit.shared.ui.navigation.TopLevelDestinations

@Composable
fun SeekerBottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Transparent)
            .testTag("bottomNavigationMenu"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.Transparent)
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.7f, 0f)
                cubicTo(
                    size.width * 0.72f, 0f,
                    size.width * 0.75f, 0f,
                    size.width * 0.77f, 10.dp.toPx()
                )
                quadraticBezierTo(
                    size.width * 0.86f, 60.dp.toPx(),
                    size.width * 0.96f, 10.dp.toPx()
                )

                cubicTo(
                    size.width * 0.96f, 10.dp.toPx(),
                    size.width * 0.98f, 0f,
                    size.width * 1f, 0f
                )
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = path,
                color = Color(0xFFD8D8D8),
                style = Fill
            )

        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 80.dp)
                .height(60.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filteredTabList = tabList.filter { it.route != Route.CREATE_REQUEST }
            filteredTabList.forEachIndexed { index, tab ->
                BottomNavigationItem(
                    icon = { Icon(tab.icon, contentDescription = null, tint = Color.White) },
                    selected = tab.route == selectedItem,
                    onClick = { onTabSelect(tab) },
                    modifier = Modifier.testTag(tab.textId)
                )

            }
        }

        FloatingActionButton(
            onClick = { onTabSelect(TopLevelDestinations.CREATE_REQUEST) },
            modifier = Modifier
                .size(70.dp)
                .offset(y = (-25).dp)
                .offset(x = (142).dp)
                .align(Alignment.TopCenter)
                .testTag(TopLevelDestinations.CREATE_REQUEST.toString()),
            shape = CircleShape,
            containerColor = Color(0xFF0099FF)
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSeekerBottomNavigationMenu() {
    // Use the actual list of top-level destinations
    val tabList = LIST_TOP_LEVEL_DESTINATION

    // Preview with "Home" as the selected item
    SeekerBottomNavigationMenu(
        onTabSelect = {}, // No action needed for preview
        tabList = tabList,
        selectedItem = Route.SERVICES // Default to SERVICES (Home)
    )
}
