package com.android.solvit.provider.ui.request

import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.map.GetDirectionsBubble
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.Orange
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable function that displays the top bar of the requests feed screen.
 */
@Preview
@Composable
fun RequestsTopBar() {
  val context = LocalContext.current
  Row(
      modifier =
          Modifier.fillMaxWidth().background(colorScheme.background).testTag("RequestsTopBar"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            modifier = Modifier.testTag("MenuOption"),
            onClick = {
              Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
            }) {
              Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Option")
            }

        Row(
            modifier = Modifier.testTag("SloganIcon"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = "Solv",
              style =
                  TextStyle(
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      color = colorScheme.onSurface))
          Text(
              text = "It",
              style =
                  TextStyle(
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold,
                      color = colorScheme.secondary))
        }

        IconButton(
            onClick = {
              Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
            }) {
              Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
            }
      }
}

/**
 * Composable function that displays the search bar of the requests feed screen.
 *
 * @param searchQuery The search query state
 */
@Composable
fun SearchBar(searchQuery: MutableState<String>) {
  OutlinedTextField(
      value = searchQuery.value,
      onValueChange = { searchQuery.value = it },
      singleLine = true,
      placeholder = {
        Text(
            "Search requests",
            style = TextStyle(color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold))
      },
      leadingIcon = {
        Icon(
            painter = painterResource(id = R.drawable.search_icon),
            contentDescription = "Search icon",
            tint = Orange,
            modifier = Modifier.size(20.dp))
      },
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .height(56.dp)
              .background(colorScheme.background)
              .border(3.dp, Orange, RoundedCornerShape(12.dp))
              .testTag("SearchBar"),
      textStyle = TextStyle(color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold),
      shape = RoundedCornerShape(12.dp))
}

/**
 * Composable function that displays the list of service requests.
 *
 * @param requests The list of service requests
 */
@Composable
fun ListRequests(requests: List<ServiceRequest>) {
  LazyColumn(
      modifier =
          Modifier.fillMaxSize()
              .padding(start = 16.dp, end = 16.dp)
              .background(colorScheme.background),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(requests) { request -> ServiceRequestItem(request) }
      }
}

/**
 * Composable function that displays a service request item.
 *
 * @param request The service request
 */
@Composable
fun ServiceRequestItem(request: ServiceRequest) {
  // State to hold the selected location
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val context = LocalContext.current
  // Placeholder onClick action
  val onClick = { Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_SHORT).show() }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .border(1.dp, colorScheme.onSurfaceVariant, RoundedCornerShape(12.dp))
              .background(colorScheme.primary, RoundedCornerShape(12.dp))
              .padding(16.dp)
              .testTag("ServiceRequest")) {

        // Row for profile picture and request details
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Image(
              painter = painterResource(id = R.drawable.default_pdp),
              contentDescription = "Profile Picture",
              modifier = Modifier.size(50.dp).clip(CircleShape))
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimary)
            Text(
                text = Services.format(request.type),
                fontSize = 14.sp,
                color = colorScheme.onPrimary)
          }
          IconButton(onClick = { onClick() }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = colorScheme.onPrimary)
          }
        }

        // Row for deadline information
        Row {
          Text(
              text = "Deadline: ",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = colorScheme.onPrimary)
          Text(
              text =
                  SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                      .format(request.dueDate.toDate()),
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = colorScheme.error)
        }

        // Display location if available
        request.location?.let {
          Text(
              text = if (it.name.length > 50) "${it.name.take(50)}..." else it.name,
              fontSize = 12.sp,
              color = colorScheme.onPrimary,
              modifier = Modifier.clickable { selectedLocation = it })
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display request description
        Text(
            text = request.description,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 18.sp,
            color = colorScheme.onPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        // Display image if available, otherwise show placeholder text
        val imageUrl = request.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
          AsyncImage(
              model = imageUrl,
              placeholder = painterResource(id = R.drawable.loading),
              error = painterResource(id = R.drawable.error),
              contentDescription = "Service Image",
              modifier =
                  Modifier.fillMaxWidth()
                      .height(160.dp)
                      .border(1.dp, colorScheme.onPrimary, RoundedCornerShape(12.dp))
                      .clip(RoundedCornerShape(12.dp)),
              contentScale = ContentScale.Crop)
        } else {
          Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(160.dp)
                      .border(1.dp, colorScheme.onPrimary, RoundedCornerShape(12.dp))
                      .clip(RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center) {
                Text(text = "No Image Provided", fontSize = 16.sp, color = colorScheme.onPrimary)
              }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row for interaction buttons
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          InteractionBar("Comment", R.drawable.comment_icon, onClick)
          InteractionBar("Share", R.drawable.share_icon, onClick)
          InteractionBar("Reply", R.drawable.reply_icon, onClick)
        }

        // Display directions bubble if location is selected
        selectedLocation?.let { GetDirectionsBubble(location = it) { selectedLocation = null } }
      }
}

/**
 * Composable function that displays an interaction bar.
 *
 * @param text The text to display
 * @param icon The icon to display
 * @param onClick The onClick action
 */
@Composable
fun InteractionBar(text: String, icon: Int, onClick: () -> Unit = {}) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = text, fontSize = 14.sp, color = colorScheme.onPrimary)
    IconButton(onClick = { onClick() }) {
      Image(
          painter = painterResource(id = icon),
          contentDescription = text,
          colorFilter = ColorFilter.tint(colorScheme.onPrimary),
          modifier = Modifier.size(21.dp))
    }
  }
}

/**
 * Composable function that displays the filter bar of the requests feed screen.
 *
 * @param selectedService The selected service
 * @param selectedFilters The selected filters
 * @param onSelectedService The onSelectedService action
 * @param onFilterChange The onFilterChange action
 */
@Composable
fun FilterBar(
    selectedService: String,
    selectedFilters: Set<String>,
    onSelectedService: (String) -> Unit,
    onFilterChange: (String, Boolean) -> Unit
) {
  val filters = listOf("Service", "Near Me", "Due Time")

  LazyRow(
      modifier = Modifier.background(colorScheme.background).testTag("FilterBar"),
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      verticalAlignment = Alignment.Top) {
      // Display the filter chips
        items(filters.size) { idx ->
          val filter = filters[idx]
          if (filter == "Service") {
            ServiceChip(
                selectedService = selectedService,
                onServiceSelected = { service ->
                  onSelectedService(service)
                  onFilterChange("Service", true)
                })
          } else {
            FilterChip(
                label = filter,
                isSelected = selectedFilters.contains(filter),
                onSelected = { isSelected -> onFilterChange(filter, isSelected) })
          }
        }
      }
}

/**
 * Composable function that displays a service chip.
 *
 * @param selectedService The selected service
 * @param onServiceSelected The onServiceSelected action
 */
@Composable
fun ServiceChip(selectedService: String, onServiceSelected: (String) -> Unit) {
  var selectedText by remember { mutableStateOf(selectedService) }
  var showDropdown by remember { mutableStateOf(false) }

    // Set the border text color based on the selected service
  val borderTextColor =
      if (selectedText != "Service") colorScheme.secondary else colorScheme.secondary

  Box(
      modifier =
          Modifier.testTag("ServiceChip")
              .chipModifier(colorScheme.background, borderTextColor)
              .clickable { showDropdown = !showDropdown },
      contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = selectedText,
                  fontSize = 16.sp,
                  lineHeight = 34.sp,
                  fontWeight = FontWeight(400),
                  color = borderTextColor)
              Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "More Options",
                  tint = borderTextColor,
              )
            }
      }

  DropdownMenu(
      expanded = showDropdown,
      onDismissRequest = { showDropdown = false },
      modifier =
          Modifier.border(1.dp, colorScheme.onSurface, RoundedCornerShape(12.dp))
              .background(colorScheme.background, RoundedCornerShape(12.dp))
              .testTag("ServiceDropdown")) {
      // Display the service options
        Services.entries.forEach { service ->
          val serviceName = Services.format(service)
          DropdownMenuItem(
              modifier = Modifier.testTag(serviceName),
              text = { Text(serviceName) },
              onClick = {
                selectedText = serviceName
                onServiceSelected(serviceName)
                showDropdown = false
              })
        }
      }
}

/**
 * Composable function that displays a filter chip.
 *
 * @param label The label to display
 * @param isSelected The isSelected state
 * @param onSelected The onSelected action
 */
@Composable
fun FilterChip(label: String, isSelected: Boolean, onSelected: (Boolean) -> Unit) {
  val borderTextColor = if (isSelected) colorScheme.secondary else colorScheme.secondary

  val context = LocalContext.current
  Box(
      modifier =
          Modifier.chipModifier(colorScheme.background, borderTextColor).clickable {
            onSelected(!isSelected)
            Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
          },
      contentAlignment = Alignment.Center) {
        Text(
            text = label,
            fontSize = 16.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight(400),
            color = borderTextColor)
      }
}

/**
 * Modifier function that applies the chip style to a composable.
 *
 * @param backgroundColor The background color
 * @param borderTextColor The border text color
 */
private fun Modifier.chipModifier(backgroundColor: Color, borderTextColor: Color) =
    this.padding(8.dp)
        .border(3.dp, borderTextColor, shape = RoundedCornerShape(12.dp))
        .background(backgroundColor, shape = RoundedCornerShape(12.dp))
        .padding(12.dp, 6.dp)

/**
 * Composable function that displays the list requests feed screen.
 *
 * @param serviceRequestViewModel The service request view model
 * @param navigationActions The navigation actions
 */
@Composable
fun ListRequestsFeedScreen(
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    navigationActions: NavigationActions
) {
  val requests by serviceRequestViewModel.requests.collectAsState()
  val selectedFilters = remember { mutableStateOf(setOf<String>()) }
  var selectedService by remember { mutableStateOf("Service") }
  val searchQuery = remember { mutableStateOf("") }

  Scaffold(
      topBar = { RequestsTopBar() },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(colorScheme.background)
                    .testTag("ScreenContent"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Spacer(modifier = Modifier.height(8.dp))
              SearchBar(searchQuery) // Pass the searchQuery state
              FilterBar(
                  selectedService = selectedService,
                  selectedFilters = selectedFilters.value,
                  onSelectedService = { service -> selectedService = service },
                  onFilterChange = { filter, isSelected ->
                    selectedFilters.value =
                        if (isSelected) selectedFilters.value + filter
                        else selectedFilters.value - filter
                  })

              // Filter requests based on the search query
              val filteredRequests =
                  requests.filter { request ->
                    // Check if the request matches the search query
                    val query = searchQuery.value.trim().lowercase()
                    val matchesQuery =
                        query.isEmpty() ||
                            request.title.lowercase().contains(query) ||
                            request.description.lowercase().contains(query)

                    // Check if the request matches the selected filters
                    val matchesFilters =
                        if (selectedService == "Service") true
                        else request.type.name == selectedService.replace(" ", "_").uppercase()

                    matchesQuery && matchesFilters
                  }

              ListRequests(filteredRequests)
            }
      }
}
