package com.android.solvit.ui.navigation

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  BottomNavigation(
      modifier = Modifier.fillMaxWidth().testTag("bottomNavigationMenu"),
      backgroundColor = MaterialTheme.colorScheme.surface,
      content = {
        tabList.forEach { tab ->
          BottomNavigationItem(
              icon = {
                Icon(
                    tab.icon,
                    contentDescription = "Bottom bar navigation icons",
                    modifier = Modifier.size(35.dp))
              },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              modifier = Modifier.clip(RoundedCornerShape(50.dp)).testTag(tab.route))
        }
      },
  )
}
