package com.android.solvit.seeker.ui.service

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.model.service.SearchServicesViewModel
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_CUSTOMMER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.LightBlue
import com.android.solvit.shared.ui.theme.LightOrange
import com.android.solvit.shared.ui.theme.LightRed
import com.android.solvit.shared.ui.theme.Purple80

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    navigationActions: NavigationActions,
    listProviderViewModel: ListProviderViewModel
) {

  val searchViewModel = SearchServicesViewModel()

  Scaffold(
      modifier = Modifier.testTag("servicesScreen"),
      bottomBar = {
        SeekerBottomNavigationMenu(
            { navigationActions.navigateTo(it.route) },
            LIST_TOP_LEVEL_DESTINATION_CUSTOMMER,
            navigationActions.currentRoute())
      }) {
        Column(modifier = Modifier.fillMaxSize()) {
          TopSection(searchViewModel, listProviderViewModel, navigationActions)
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { ShortcutsSection(navigationActions) }
            item { CategoriesSection(searchViewModel, listProviderViewModel, navigationActions) }
            item { PerformersSection(listProviderViewModel) }
            item { Spacer(Modifier.size(80.dp)) }
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(
    searchViewModel: SearchServicesViewModel,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  val searchText by searchViewModel.searchText.collectAsState()
  val searchResults by searchViewModel.servicesList.collectAsState()
  val isSearching by searchViewModel.isSearching.collectAsState()

  Column(
      modifier = Modifier.fillMaxWidth().background(Purple80, shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(70.dp)) {
              Image(
                  painterResource(id = R.drawable.empty_profile_img),
                  contentDescription = "profile picture",
                  Modifier.size(40.dp).clip(CircleShape).clickable { navigationActions.navigateTo(Route.PROFILE) }
              )
              Column(
                  modifier = Modifier.width(135.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                      Text("Current location", fontSize = 14.sp)
                      IconButton(
                          onClick = { /*TODO*/ },
                          modifier = Modifier.size(16.dp)
                      ) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                      }
                  }
                Text("Dubai, USA", fontSize = 15.sp)
              }
              IconButton(
                    onClick = { /*TODO*/ }
              ) {
                  Icon(imageVector = Icons.Default.Menu, contentDescription = null)
              }
            }
        DockedSearchBar(
            query = searchText,
            onQueryChange = searchViewModel::onSearchTextChange,
            onSearch = searchViewModel::onSearchTextChange,
            active = isSearching,
            onActiveChange = { searchViewModel.onToggleSearch() },
            modifier = Modifier.testTag("searchBar"),
            placeholder = { Text("Find services near you") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }) {
              LazyColumn(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults.size) { index ->
                      Text(
                          searchResults[index]
                              .service
                              .toString()
                              .replace("_", " ")
                              .lowercase()
                              .replaceFirstChar { it.uppercase() },
                          modifier =
                              Modifier.clickable {
                                listProviderViewModel.selectService(searchResults[index].service)
                                navigationActions.navigateTo(Route.PROVIDERS)
                              })
                    }
                  }
            }
        Spacer(Modifier.height(8.dp))
      }
}

@Composable
fun ShortcutsSection(navigationActions: NavigationActions) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(LightOrange, shape = RoundedCornerShape(16.dp))
                    .clickable { navigationActions.navigateTo(Route.PROVIDERS) }) {
              Row(
                  modifier = Modifier.padding(16.dp).fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Service\nProviders", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Image(
                        painterResource(id = R.drawable.providers_ovw_image),
                        contentDescription = "service providers",
                        Modifier.size(32.dp).clip(CircleShape))
                  }
            }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Box(
                  modifier =
                      Modifier.fillMaxWidth(.5f)
                          .background(LightBlue, shape = RoundedCornerShape(16.dp))
                          .clickable { navigationActions.navigateTo(Route.REQUESTS_OVERVIEW) }) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          Image(
                              painterResource(id = R.drawable.orders_ovw_image),
                              contentDescription = "All Orders",
                              Modifier.size(32.dp).clip(CircleShape).align(Alignment.End))
                          Text("All Orders", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                  }
              Spacer(Modifier.size(16.dp))
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(LightRed, shape = RoundedCornerShape(16.dp))
                          .clickable { navigationActions.navigateTo(Route.MAP) }) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          Image(
                              painterResource(id = R.drawable.map_ovw_image),
                              contentDescription = "providers map",
                              Modifier.size(32.dp).clip(CircleShape).align(Alignment.End))
                          Text("Providers Map", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                  }
            }
      }
}

@Composable
fun CategoriesSection(
    searchServicesViewModel: SearchServicesViewModel,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  val searchResults by searchServicesViewModel.servicesList.collectAsState()
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text("Top Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          items(searchResults.size) { index ->
            ServiceItem(
                searchResults[index],
                onClick = {
                  listProviderViewModel.selectService(searchResults[index].service)
                  navigationActions.navigateTo(Route.PROVIDERS)
                })
          }
        }
  }
}

@Composable
fun PerformersSection(listProviderViewModel: ListProviderViewModel) {
  val providers by listProviderViewModel.providersList.collectAsState()
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text("Top Performers", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    LazyRow(
        Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          items(providers.size) { index ->
            ProviderItem(
                providers[index],
                onClick = {
                  /*TODO*/
                })
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
              text =
                  service.service.toString().replace("_", " ").lowercase().replaceFirstChar {
                    it.uppercase()
                  },
              modifier = Modifier.padding(20.dp).align(Alignment.BottomStart),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Start)
        }
      }
}

@Composable
fun ProviderItem(provider: Provider, onClick: () -> Unit) {
  OutlinedCard(modifier = Modifier.aspectRatio(.7f).clickable { onClick() }) {
    Box {
      AsyncImage(
          model = provider.imageUrl,
          placeholder = painterResource(id = R.drawable.empty_profile_img),
          error = painterResource(id = R.drawable.empty_profile_img),
          contentDescription = null,
          contentScale = androidx.compose.ui.layout.ContentScale.Crop,
      )
      Text(
          text = provider.name,
          modifier = Modifier.padding(16.dp).align(Alignment.BottomStart),
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Start)
    }
  }
}
