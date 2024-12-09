package com.android.solvit.seeker.ui.service

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.model.service.SearchServicesViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_SEEKER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.LightBlue
import com.android.solvit.shared.ui.theme.LightOrange
import com.android.solvit.shared.ui.theme.LightRed

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ServicesScreen(
    navigationActions: NavigationActions,
    listProviderViewModel: ListProviderViewModel
) {
  // Lock Orientation to Portrait
  val localContext = LocalContext.current
  DisposableEffect(Unit) {
    val activity = localContext as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  val searchViewModel = SearchServicesViewModel()

  Scaffold(
      modifier = Modifier.testTag("servicesScreen"),
      bottomBar = {
        BottomNavigationMenu(
            { navigationActions.navigateTo(it.route) },
            LIST_TOP_LEVEL_DESTINATION_SEEKER,
            Route.SERVICES)
      }) {
        Column(modifier = Modifier.fillMaxSize()) {
          TopSection(searchViewModel, listProviderViewModel, navigationActions)
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { ShortcutsSection(navigationActions, listProviderViewModel) }
            item { CategoriesSection(searchViewModel, listProviderViewModel, navigationActions) }
            item { PerformersSection(listProviderViewModel, navigationActions) }
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
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  colorScheme.primary,
                  shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
              .testTag("servicesScreenTopSection"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        val context = LocalContext.current
        val toast = Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(70.dp)) {
              Image(
                  painterResource(id = R.drawable.default_pdp),
                  contentDescription = "profile picture",
                  Modifier.size(40.dp)
                      .clip(CircleShape)
                      .clickable { navigationActions.navigateTo(Route.SEEKER_PROFILE) }
                      .testTag("servicesScreenProfileImage"))
              Column(
                  modifier = Modifier.width(135.dp).testTag("servicesScreenCurrentLocation"),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text("Current location", fontSize = 14.sp)
                  IconButton(
                      onClick = { toast.show() },
                      modifier = Modifier.size(16.dp).testTag("servicesScreenLocationButton")) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                      }
                }
                Text("Lausanne, VD", fontSize = 15.sp)
              }
              IconButton(
                  onClick = { toast.show() }, modifier = Modifier.testTag("servicesScreenMenu")) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                  }
            }
        DockedSearchBar(
            query = searchText,
            onQueryChange = searchViewModel::onSearchTextChange,
            onSearch = searchViewModel::onSearchTextChange,
            active = isSearching,
            onActiveChange = { searchViewModel.onToggleSearch() },
            modifier = Modifier.testTag("servicesScreenSearchBar"),
            placeholder = { Text("Find services near you") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }) {
              LazyColumn(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults.size) { index ->
                      Text(
                          Services.format(searchResults[index].service),
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
fun ShortcutsSection(
    navigationActions: NavigationActions,
    listProviderViewModel: ListProviderViewModel
) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("servicesScreenShortcuts"),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(LightOrange, shape = RoundedCornerShape(16.dp))
                    .clickable { navigationActions.navigateTo(Route.AI_SOLVER) }
                    .testTag("solveItWithAi")) {
              Row(
                  modifier = Modifier.padding(16.dp).fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Solve It with AI",
                        color = colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)
                    Image(
                        painter = painterResource(id = R.drawable.ai_flat_design),
                        contentDescription = "ai_logo",
                        modifier = Modifier.size(50.dp),
                    )
                  }
            }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              Box(
                  modifier =
                      Modifier.weight(1f)
                          .background(LightBlue, shape = RoundedCornerShape(16.dp))
                          .clickable { navigationActions.navigateTo(Route.REQUESTS_OVERVIEW) }
                          .testTag("servicesScreenOrdersShortcut")) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          Image(
                              painter = painterResource(id = R.drawable.orders_ovw_image),
                              contentDescription = "All Orders",
                              modifier =
                                  Modifier.size(32.dp).clip(CircleShape).align(Alignment.End))
                          Text(
                              "All Orders",
                              color = colorScheme.onPrimary,
                              fontSize = 20.sp,
                              fontWeight = FontWeight.Bold)
                        }
                  }
              Box(
                  modifier =
                      Modifier.weight(1f)
                          .background(LightRed, shape = RoundedCornerShape(16.dp))
                          .clickable { navigationActions.navigateTo(Route.MAP) }
                          .testTag("servicesScreenMapShortcut")) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                          Image(
                              painter = painterResource(id = R.drawable.map_ovw_image),
                              contentDescription = "providers map",
                              modifier =
                                  Modifier.size(32.dp).clip(CircleShape).align(Alignment.End))
                          Text(
                              "Providers Map",
                              color = colorScheme.onPrimary,
                              fontSize = 20.sp,
                              fontWeight = FontWeight.Bold)
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
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("servicesScreenCategories"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
        "Top Categories",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag("servicesScreenCategoriesTitle"))
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(150.dp).testTag("servicesScreenCategoriesList"),
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
fun PerformersSection(
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  val providers by listProviderViewModel.providersList.collectAsState()
  val topProviders = providers.sortedByDescending { it.rating }
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("servicesScreenPerformers"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
        "Top Performers",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag("servicesScreenPerformersTitle"))
    LazyRow(
        Modifier.fillMaxWidth().height(150.dp).testTag("servicesScreenPerformersList"),
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          items(providers.size) { index ->
            ProviderItem(
                topProviders[index],
                onClick = {
                  listProviderViewModel.selectProvider(topProviders[index])
                  navigationActions.navigateTo(Route.PROVIDER_PROFILE)
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
              text = Services.format(service.service),
              modifier = Modifier.padding(20.dp).align(Alignment.BottomStart),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Start)
        }
      }
}

@Composable
fun ProviderItem(provider: Provider, onClick: () -> Unit) {
  OutlinedCard(
      modifier =
          Modifier.aspectRatio(.7f).clickable { onClick() }.testTag(provider.name + "Item")) {
        Box {
          AsyncImage(
              model = provider.imageUrl,
              placeholder = painterResource(id = R.drawable.empty_profile_img),
              error = painterResource(id = R.drawable.empty_profile_img),
              contentDescription = null,
              contentScale = androidx.compose.ui.layout.ContentScale.Crop,
              alpha = 0.3f)
          Column(
              modifier = Modifier.fillMaxSize().padding(16.dp),
              verticalArrangement = Arrangement.SpaceBetween,
              horizontalAlignment = Alignment.Start) {
                Text(text = Services.format(provider.service), fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.align(Alignment.End)) {
                  Text(text = provider.rating.toString(), fontWeight = FontWeight.Bold)
                  Icon(
                      imageVector = Icons.Default.Star,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp))
                }
                Text(text = provider.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
              }
        }
      }
}
