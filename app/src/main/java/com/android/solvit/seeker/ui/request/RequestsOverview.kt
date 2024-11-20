package com.android.solvit.seeker.ui.request

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  Scaffold(
      modifier = Modifier.testTag("requestsOverviewScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_SEEKER,
            selectedItem = Route.REQUESTS_OVERVIEW)
      }) {
        val user = authViewModel.user.collectAsState()
        val userId = user.value?.uid ?: "-1"
        val requests =
            requestViewModel.requests.collectAsState().value.filter { it.userId == userId }

        Column {
          TopOrdersSection(navigationActions)
          CategoriesFiltersSection()
          if (requests.isEmpty()) {
            NoRequestsText()
          } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("requestsList"),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                  items(requests) { request ->
                    RequestItemRow(
                        request = request,
                        onClick = {
                          requestViewModel.selectRequest(request)
                          navigationActions.navigateTo(Route.EDIT_REQUEST)
                        })
                  }
                }
          }
        }
      }
}

@Composable
fun TopOrdersSection(navigationActions: NavigationActions) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 32.dp)
              .testTag("topOrdersSection"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        val context = LocalContext.current
        Row {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = null,
              modifier = Modifier.clickable { navigationActions.goBack() }.testTag("arrowBack"))
          Spacer(modifier = Modifier.size(22.dp))
          Text(
              text = "Orders",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
          )
        }
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            modifier =
                Modifier.clickable {
                  Toast.makeText(context, "This feature is not yet implemented", Toast.LENGTH_SHORT)
                      .show()
                })
      }
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
fun CategoriesFiltersSection() {
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
      CategoriesFilter()
    }
    if (showSort) {
      CategoriesSort()
    }
  }
}

@Composable
fun CategoriesFilter() {
  val context = LocalContext.current
  LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.padding(16.dp).testTag("categoriesFilter")) {
        items(SERVICES_LIST.size) {
          FilterItem(Services.format(SERVICES_LIST[it].service)) {
            Toast.makeText(context, "This feature is not yet implemented", Toast.LENGTH_SHORT)
                .show()
          }
        }
      }
}

@Composable
fun CategoriesSort() {
  val context = LocalContext.current
  LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.padding(16.dp).testTag("categoriesSortFilter")) {
        item {
          FilterItem("Sort by date") {
            Toast.makeText(context, "This feature is not yet implemented", Toast.LENGTH_SHORT)
                .show()
          }
        }
        item {
          FilterItem("Sort by status") {
            Toast.makeText(context, "This feature is not yet implemented", Toast.LENGTH_SHORT)
                .show()
          }
        }
      }
}

@Composable
fun FilterItem(text: String, filter: () -> Unit) {
  var isFilterSelected by remember { mutableStateOf(false) }
  val borderColor = if (isFilterSelected) colorScheme.onBackground else colorScheme.onSurfaceVariant
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
        Text(text = text)
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
