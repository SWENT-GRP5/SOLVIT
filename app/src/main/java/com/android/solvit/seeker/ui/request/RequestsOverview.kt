package com.android.solvit.seeker.ui.request

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.seeker.ui.service.SERVICES_LIST
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestStatus.Companion.getStatusColor
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_SEEKER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.LightBlue
import com.android.solvit.shared.ui.theme.LightOrange
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SourceLockedOrientationActivity")
@Composable
fun RequestsOverviewScreen(
    navigationActions: NavigationActions,
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
      requestViewModel.clearFilters()
    }
  }

  Scaffold(
      modifier = Modifier.testTag("requestsOverviewScreen"),
      bottomBar = {
        val currentRoute = navigationActions.currentRoute()
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION_SEEKER,
            selectedItem = currentRoute)
      }) {
        val user = authViewModel.user.collectAsState()
        val userId = user.value?.uid ?: "-1"
        val allRequests =
            requestViewModel.requests.collectAsState().value.filter { it.userId == userId }

        var selectedTab by remember { mutableIntStateOf(0) }
        val statusTabs = ServiceRequestStatus.entries.toTypedArray()

        val isSortSelected by requestViewModel.sortSelected.collectAsState()
        val selectedServices by requestViewModel.selectedServices.collectAsState()

        Column {
          TopOrdersSection()
          CategoriesFiltersSection(serviceRequestViewModel = requestViewModel)

          // Tabs for filtering by status
          ScrollableTabRow(
              selectedTabIndex = selectedTab,
              modifier = Modifier.fillMaxWidth().testTag("statusTabRow"),
              containerColor = colorScheme.background,
              contentColor = colorScheme.primary) {
                statusTabs.forEachIndexed { index, status ->
                  Tab(
                      selected = selectedTab == index,
                      onClick = { selectedTab = index },
                      text = {
                        Text(
                            text = ServiceRequestStatus.format(status),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = getStatusColor(status))
                      })
                }
              }

          val filteredRequests =
              if (selectedTab < statusTabs.size) {
                allRequests.filter { it.status == statusTabs[selectedTab] }
              } else {
                allRequests
              }

          val sortedRequests =
              if (selectedServices.isNotEmpty() && isSortSelected) {
                filteredRequests
                    .filter { selectedServices.contains(it.type) }
                    .sortedBy { it.dueDate }
              } else if (selectedServices.isNotEmpty()) {
                filteredRequests.filter { selectedServices.contains(it.type) }
              } else if (isSortSelected) {
                filteredRequests.sortedBy { it.dueDate }
              } else {
                filteredRequests
              }

          if (sortedRequests.isEmpty()) {
            NoRequestsText()
          } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("requestsList"),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  items(sortedRequests) { request ->
                    RequestItemRow(
                        request = request,
                        onClick = {
                          requestViewModel.selectRequest(request)
                          navigationActions.navigateTo(Route.BOOKING_DETAILS)
                        })
                  }
                }
          }
        }
      }
}

@Composable
fun TopOrdersSection() {
  TopAppBarInbox(
      title = "Orders",
      testTagGeneral = "topOrdersSection",
  )
}

@Composable
fun NoRequestsText() {
  Column(
      modifier = Modifier.fillMaxSize().testTag("noServiceRequestsScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
        text = "You have no active service request.\nCreate one.",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onSurfaceVariant)
  }
}

@Composable
fun CategoriesFiltersSection(serviceRequestViewModel: ServiceRequestViewModel) {
  var showFilters by remember { mutableStateOf(false) }
  var showSort by remember { mutableStateOf(false) }
  Column {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("filterRequestsBar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
          Box(
              modifier =
                  Modifier.weight(1f)
                      .background(LightBlue, shape = RoundedCornerShape(16.dp))
                      .clickable { showFilters = !showFilters }
                      .testTag("categoriesSettings")) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                      Image(
                          painter = painterResource(id = R.drawable.filter_square),
                          contentDescription = "categories filter",
                          modifier = Modifier.size(24.dp).weight(0.3f).padding(horizontal = 4.dp),
                          colorFilter = ColorFilter.tint(colorScheme.onPrimary))
                      Text(
                          text = "Services",
                          fontWeight = FontWeight.Bold,
                          color = colorScheme.onPrimary,
                          modifier = Modifier.weight(0.8f),
                          maxLines = 1,
                          softWrap = false)
                    }
              }

          Box(
              modifier =
                  Modifier.weight(1f)
                      .background(LightOrange, shape = RoundedCornerShape(16.dp))
                      .clickable { showSort = !showSort }
                      .testTag("categoriesSort")) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                      Image(
                          painter = painterResource(id = R.drawable.filter_circle),
                          contentDescription = "categories sort",
                          modifier = Modifier.size(24.dp).weight(0.3f).padding(horizontal = 4.dp),
                          colorFilter = ColorFilter.tint(colorScheme.onPrimary))
                      Text(
                          text = "Sort",
                          fontWeight = FontWeight.Bold,
                          color = colorScheme.onPrimary,
                          modifier = Modifier.weight(0.8f),
                          maxLines = 1,
                          softWrap = false)
                    }
              }
        }

    if (showFilters) {
      CategoriesFilter(serviceRequestViewModel)
    }
    if (showSort) {
      CategoriesSort(serviceRequestViewModel)
    }
  }
}

@Composable
fun CategoriesFilter(serviceRequestViewModel: ServiceRequestViewModel) {
  val selectedServices by serviceRequestViewModel.selectedServices.collectAsState()
  LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.padding(16.dp).testTag("categoriesFilter")) {
        for (service in SERVICES_LIST) {
          val isSelected = selectedServices.contains(service.service)
          item {
            FilterItem(Services.format(service.service), isSelected) {
              if (isSelected) {
                serviceRequestViewModel.unSelectService(service.service)
              } else {
                serviceRequestViewModel.selectService(service.service)
              }
            }
          }
        }
      }
}

@Composable
fun CategoriesSort(serviceRequestViewModel: ServiceRequestViewModel) {
  val isSortSelected by serviceRequestViewModel.sortSelected.collectAsState()
  LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.padding(16.dp).testTag("categoriesSortFilter")) {
        item {
          FilterItem("Sort by date", isSortSelected) { serviceRequestViewModel.sortSelected() }
        }
      }
}

@Composable
fun FilterItem(text: String, isSelected: Boolean, filter: () -> Unit) {
  var isFilterSelected by remember { mutableStateOf(isSelected) }
  val borderColor = if (isFilterSelected) colorScheme.primary else colorScheme.onSurfaceVariant
  Box(
      modifier =
          Modifier.padding(8.dp)
              .border(1.dp, borderColor, RoundedCornerShape(8.dp))
              .clickable {
                isFilterSelected = !isFilterSelected
                filter()
              }
              .testTag("$text FilterItem"),
      contentAlignment = Alignment.Center) {
        Text(text = text, color = borderColor)
      }
}

@Composable
fun RequestItemRow(request: ServiceRequest, onClick: () -> Unit) {
  // Use a Box to wrap the entire row and the bottom bar
  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  val date = dateFormat.format(request.dueDate.toDate())
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .clickable { onClick() }
              .border(1.dp, colorScheme.onSurfaceVariant, RoundedCornerShape(16.dp))
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag("requestListItem")) {
        Column {
          Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val image = request.imageUrl
            AsyncImage(
                model = if (!image.isNullOrEmpty()) image else R.drawable.no_photo,
                placeholder = painterResource(id = R.drawable.loading),
                error = painterResource(id = R.drawable.error),
                contentDescription = "service request image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
            Column {
              Text(text = request.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
              Text(
                  text = Services.format(request.type),
                  fontSize = 14.sp,
              )
            }
          }
          Spacer(modifier = Modifier.size(12.dp))
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = ServiceRequestStatus.format(request.status),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(request.status))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text(text = "Until:", fontSize = 14.sp)
                  Text(text = date, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
              }
        }
      }
}
