package com.android.solvit.seeker.ui.service

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.model.service.SearchServicesViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_SEEKER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.LightBlue
import com.android.solvit.shared.ui.theme.LightOrange
import com.android.solvit.shared.ui.theme.LightRed
import com.android.solvit.shared.ui.theme.OnPrimary
import com.android.solvit.shared.ui.theme.Typography

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SourceLockedOrientationActivity")
@Composable
fun ServicesScreen(
    navigationActions: NavigationActions,
    seekerProfileViewModel: SeekerProfileViewModel,
    listProviderViewModel: ListProviderViewModel,
    authViewModel: AuthViewModel
) {

  // Lock Orientation to Portrait
  val localContext = LocalContext.current
  DisposableEffect(Unit) {
    val activity = localContext as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  LaunchedEffect(Unit) { listProviderViewModel.clearSelectedService() }

  val searchViewModel = SearchServicesViewModel()

  Scaffold(
      modifier = Modifier.testTag("servicesScreen"),
      bottomBar = {
        BottomNavigationMenu(
            { navigationActions.navigateTo(it) },
            LIST_TOP_LEVEL_DESTINATION_SEEKER,
            Route.SEEKER_OVERVIEW)
      },
      topBar = {
        TopSection(
            searchViewModel,
            seekerProfileViewModel,
            listProviderViewModel,
            authViewModel = authViewModel,
            navigationActions = navigationActions)
      }) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
          LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { Spacer(Modifier.size(16.dp)) }
            item { DiscountSection(navigationActions, listProviderViewModel) }
            item { ShortcutsSection(navigationActions) }
            item { CategoriesSection(searchViewModel, listProviderViewModel, navigationActions) }
            item { PerformersSection(listProviderViewModel, navigationActions) }
            item { Spacer(Modifier.size(40.dp)) }
          }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSection(
    searchViewModel: SearchServicesViewModel,
    seekerProfileViewModel: SeekerProfileViewModel,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel
) {
  val searchText by searchViewModel.searchText.collectAsStateWithLifecycle()
  val searchResults by searchViewModel.servicesList.collectAsStateWithLifecycle()
  val isSearching by searchViewModel.isSearching.collectAsStateWithLifecycle()
  val userProfile by seekerProfileViewModel.seekerProfile.collectAsStateWithLifecycle()
  val user by authViewModel.user.collectAsState()
  user?.let { seekerProfileViewModel.getUserProfile(it.uid) }
  Log.e("TopSection", "$userProfile")

  Box(modifier = Modifier.fillMaxWidth().testTag("servicesScreenTopSection")) {
    // Background Image
    Image(
        painter = painterResource(id = R.drawable.top_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize())
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
      val context = LocalContext.current
      val toast = Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
      Row(
          modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp).height(45.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = userProfile.imageUrl.ifEmpty { R.drawable.ic_user },
                placeholder = painterResource(id = R.drawable.loading),
                error = painterResource(id = R.drawable.error),
                contentDescription = "profile picture",
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier.size(40.dp)
                        .clip(CircleShape)
                        .clickable { navigationActions.navigateTo(Route.PROFILE) }
                        .testTag("servicesScreenProfileImage"))

            Row(
                modifier = Modifier.testTag("SloganIcon"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                  text = "Solv",
                  style =
                      TextStyle(
                          fontSize = 25.sp,
                          fontWeight = FontWeight.Bold,
                          color = colorScheme.onBackground))
              Text(
                  text = "It",
                  style =
                      TextStyle(
                          fontSize = 25.sp,
                          fontWeight = FontWeight.Bold,
                          color = colorScheme.secondary))
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
          modifier = Modifier.offset(y = 20.dp).testTag("servicesScreenSearchBar"),
          placeholder = { Text("Find services near you") },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
          colors = SearchBarDefaults.colors(containerColor = OnPrimary)) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  items(searchResults.size) { index ->
                    Text(
                        Services.format(searchResults[index].service),
                        modifier =
                            Modifier.clickable {
                              listProviderViewModel.selectService(searchResults[index].service)
                              navigationActions.navigateTo(Route.PROVIDERS_LIST)
                            })
                  }
                }
          }
    }
  }
}

@Composable
fun DiscountSection(
    navigationActions: NavigationActions,
    listProviderViewModel: ListProviderViewModel,
) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .testTag("servicesScreenDiscount")
              .padding(16.dp)
              .height(200.dp)
              .clip(RoundedCornerShape(16.dp))
              .clickable {
                listProviderViewModel.selectService(Services.PLUMBER)
                navigationActions.navigateTo(Route.PROVIDERS_LIST)
              }) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.discount_bg),
            contentDescription = "Discount Announcement",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize())
      }
}

@Composable
fun ShortcutsSection(
    navigationActions: NavigationActions,
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
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        fontWeight = FontWeight.Bold)
                    Icon(
                        painter = painterResource(id = R.drawable.ai_solver),
                        contentDescription = "ai_logo",
                        modifier = Modifier.size(50.dp),
                        tint = Color.Unspecified)
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
                              painter = painterResource(id = R.drawable.ic_orders),
                              contentDescription = "All Orders",
                              modifier =
                                  Modifier.size(40.dp)
                                      .clip(RoundedCornerShape(8.dp))
                                      .align(Alignment.End))
                          Text(
                              "All Orders",
                              color = colorScheme.onPrimary,
                              fontSize = 20.sp,
                              style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                              painter = painterResource(id = R.drawable.ic_map),
                              contentDescription = "providers map",
                              modifier =
                                  Modifier.size(40.dp).clip(CircleShape).align(Alignment.End))
                          Text(
                              "Providers Map",
                              color = colorScheme.onPrimary,
                              fontSize = 20.sp,
                              style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
  val searchResults by searchServicesViewModel.servicesList.collectAsStateWithLifecycle()
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("servicesScreenCategories"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
        "Top Categories",
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.testTag("servicesScreenCategoriesTitle"))
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(150.dp).testTag("servicesScreenCategoriesList"),
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          items(searchResults.size) { index ->
            ServiceItem(
                searchResults[index],
                workerCount =
                    listProviderViewModel.countProvidersByService(searchResults[index].service),
                onClick = {
                  listProviderViewModel.selectService(searchResults[index].service)
                  navigationActions.navigateTo(Route.PROVIDERS_LIST)
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
  val providers by listProviderViewModel.providersList.collectAsStateWithLifecycle()
  val topProviders = providers.sortedByDescending { it.rating }
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("performersSection"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
        "Top Performers",
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.testTag("servicesScreenPerformersTitle"))
    LazyRow(
        modifier = Modifier.fillMaxWidth().testTag("servicesScreenPerformersList"),
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          items(topProviders.size) { index ->
            ProviderItem(
                topProviders[index],
                onClick = {
                  listProviderViewModel.selectProvider(topProviders[index])
                  navigationActions.navigateTo(Route.PROVIDER_INFO)
                })
          }
        }
  }
}

@Composable
fun ServiceItem(service: ServicesListItem, workerCount: Int, onClick: () -> Unit) {
  OutlinedCard(
      modifier =
          Modifier.width(140.dp)
              .height(160.dp)
              .testTag(service.service.toString() + "Item")
              .clickable(onClick = onClick),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(2.dp, service.color),
      colors = CardDefaults.outlinedCardColors(containerColor = colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start) {
              // Service icon at the top
              Icon(
                  painter = painterResource(id = service.icon),
                  contentDescription = null,
                  modifier = Modifier.size(48.dp).testTag(service.service.toString() + "Icon"),
                  tint = Color.Unspecified)

              // Service name in bold
              Text(
                  text = Services.format(service.service),
                  modifier = Modifier.testTag(service.service.toString() + "Name"),
                  style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = colorScheme.onBackground,
                  textAlign = TextAlign.Center)

              // Worker count below the service name
              Text(
                  text = "+ $workerCount workers",
                  modifier = Modifier.testTag(service.service.toString() + "WorkerCount"),
                  style = Typography.bodySmall,
                  color = colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center)
            }
      }
}

@Composable
fun ProviderItem(provider: Provider, showIcon: Boolean = true, onClick: () -> Unit) {
  OutlinedCard(
      modifier =
          Modifier.width(180.dp).height(220.dp).testTag(provider.name + "Item").clickable {
            onClick()
          },
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.outlinedCardElevation()) {
        Box {
          AsyncImage(
              model = provider.imageUrl,
              placeholder = painterResource(id = Services.getProfileImage(provider.service)),
              error = painterResource(id = Services.getProfileImage(provider.service)),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.testTag(Services.getProfileImage(provider.service).toString() + "Image")
                      .fillMaxSize()
                      .clip(RoundedCornerShape(16.dp)))
          if (showIcon) {
            Box(
                modifier =
                    Modifier.padding(8.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OnPrimary) // Choose a color that fits your theme
                        .align(Alignment.TopStart),
                contentAlignment = Alignment.Center) {
                  Icon(
                      painter = painterResource(id = Services.getIcon(provider.service)),
                      contentDescription = null,
                      tint = Color.Unspecified,
                      modifier =
                          Modifier.size(30.dp)
                              .testTag(Services.getIcon(provider.service).toString() + "Icon"))
                }
          }
          // Gradient overlay at the bottom to create the tinted area behind texts
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(100.dp)
                      .align(Alignment.BottomCenter)
                      .background(
                          brush =
                              Brush.verticalGradient(
                                  colors =
                                      listOf(
                                          Color.Transparent, Services.getColor(provider.service)))))

          Column(
              modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      text = provider.rating.toString(),
                      modifier = Modifier.testTag(provider.rating.toString() + "Rating"),
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 16.sp,
                      color = Color.White)
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                      imageVector = Icons.Default.Star,
                      contentDescription = null,
                      tint = Color.Yellow,
                      modifier = Modifier.size(16.dp))
                }
                Text(
                    text = provider.name,
                    modifier = Modifier.testTag(provider.name + "Name"),
                    color = Color.White,
                    style =
                        Typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold, fontSize = 20.sp))
                Text(
                    text = Services.format(provider.service),
                    modifier = Modifier.testTag(provider.service.toString() + "Service"),
                    color = Color.White,
                    style =
                        Typography.bodyLarge.copy(
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp))
              }
        }
      }
}
