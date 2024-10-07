package com.android.solvit.ui.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.model.services.SearchServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen() {

  val searchViewModel = SearchServicesViewModel()
  val searchText by searchViewModel.searchText.collectAsState()
  val searchResults by searchViewModel.servicesList.collectAsState()

  Scaffold(
      modifier = Modifier.padding(16.dp).testTag("servicesScreen"),
      floatingActionButton = {
        FloatingActionButton(
            modifier = Modifier.testTag("addRequestButton"), onClick = { /*TODO*/}) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Add Service Request")
            }
      }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              SearchBar(
                  query = searchText,
                  onQueryChange = searchViewModel::onSearchTextChange,
                  onSearch = searchViewModel::onSearchTextChange,
                  active = false,
                  onActiveChange = { searchViewModel.onToggleSearch() },
                  modifier = Modifier.testTag("searchBar"),
                  placeholder = { Text("Search Services") },
                  leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                  },
                  colors = SearchBarDefaults.colors(containerColor = Color.White)) {}
              LazyVerticalGrid(
                  columns = GridCells.Fixed(2),
                  modifier = Modifier.padding(padding).testTag("servicesGrid"),
                  verticalArrangement = Arrangement.spacedBy(16.dp),
                  horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(searchResults.size) { index ->
                      ServiceItem(searchResults[index], onClick = { /*TODO*/})
                    }
                  }
            }
      }
}

@Composable
fun ServiceItem(service: ServicesListItem, onClick: () -> Unit) {
  OutlinedCard(
      modifier =
          Modifier.aspectRatio(1f).testTag(service.service.toString() + "Item").clickable {
            onClick()
          }) {
        Box {
          Image(
              painter = painterResource(id = service.image),
              contentDescription = null,
              modifier = Modifier.fillMaxSize().alpha(0.3f))
          Text(
              text = service.service.toString().replace("_", " "),
              modifier = Modifier.align(Alignment.Center),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center)
        }
      }
}
