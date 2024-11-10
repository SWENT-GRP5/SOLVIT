package com.android.solvit.ui.requests

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.android.solvit.shared.ui.navigation.Route

// Composable function representing the top bar with a menu, slogan, and notifications icon
@Composable
fun RequestsTopBar() {
  val context = LocalContext.current
  Row(
      modifier = Modifier.testTag("RequestsTopBar").fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    // Menu button
    IconButton(
        modifier = Modifier.testTag("MenuOption"),
        onClick = { Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show() }) {
          Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Option")
        }

    // Display the app's slogan
    Row(modifier = Modifier.testTag("SloganIcon"), verticalAlignment = Alignment.CenterVertically) {
      Text(
          text = "Solv",
          style =
              TextStyle(
                  fontSize = 15.sp,
                  fontWeight = FontWeight(700),
                  color = Color(0xFF333333),
              ))
      Text(
          text = "it",
          style =
              TextStyle(
                  fontSize = 15.sp,
                  fontWeight = FontWeight(700),
                  color = Color(0xFF3D823B),
              ))
    }

    // Notifications button
    IconButton(
        onClick = { Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show() }) {
          Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
        }
  }
}

// Composable function representing the search bar with a text input field
@Composable
fun SearchBar() {
  var userResearch by remember { mutableStateOf("") }
  val context = LocalContext.current
  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    BasicTextField(
        value = "",
        onValueChange = {
          Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
        },
        singleLine = true,
        textStyle = TextStyle(color = Color.Gray, fontSize = 16.sp),
        modifier =
            Modifier.shadow(
                    elevation = 20.dp,
                    spotColor = Color(0x0F000000),
                    ambientColor = Color(0x0F000000))
                .border(
                    width = 1.dp,
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(size = 8.dp))
                .width(265.dp)
                .height(32.99994.dp)
                .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 8.dp))
                .testTag("SearchBar")) {
          // Placeholder text in the search bar
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.width(14.dp).height(15.dp),
                painter = painterResource(id = R.drawable.search_icon),
                contentDescription = "image description",
                contentScale = ContentScale.Crop)

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Search requests",
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.daibanna)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFAFAFAF),
                    ))
          }
        }
  }
}

// Composable function that displays the title screen text and an accompanying image
@Composable
fun TitleScreen() {
  Box(
      modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("TitleScreen"),
  ) {
    Text(
        text = "Find Your Seeker",
        fontSize = 30.sp,
        fontFamily = FontFamily(Font(R.font.ruwudu)),
        fontWeight = FontWeight(400),
        color = Color(0xFF00C853),
        textAlign = TextAlign.Center,
    )

    Image(
        modifier =
            Modifier.align(
                    Alignment
                        .BottomEnd) // Aligns the image to the bottom end (right under "Seeker")
                .offset(
                    x = (-170).dp,
                    y = 0.dp) // Adjust the X and Y offsets to place the image properly
                .width(50.dp) // Adjust the width to match "Seeker"
                .height(5.dp), // Adjust the height of the image
        painter = painterResource(id = R.drawable.title_icon),
        contentDescription = "image description",
        contentScale = ContentScale.Fit)
  }
}

// Displays a list of service requests using LazyColumn
@Composable
fun ListRequests(requests: List<ServiceRequest>) {
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  LazyColumn(
      modifier = Modifier.fillMaxSize().testTag("requests"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(requests) { request ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(8.dp)
                      .background(color = Color(0xFFFAFAFA))
                      .testTag("ServiceRequest")) {
                HorizontalDivider(
                    Modifier.border(width = 2.dp, color = Color(0xFFE0E0E0))
                        .padding(2.dp)
                        .fillMaxWidth()
                        .height(0.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {

                      // Profile picture
                      Image(
                          painter =
                              painterResource(
                                  id =
                                      R.drawable
                                          .image_user), // TODO Replace with actual image resource
                          contentDescription = "Profile Picture",
                          modifier = Modifier.size(50.dp).clip(CircleShape))

                      Spacer(modifier = Modifier.width(8.dp))
                      // Assignee information
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = request.userId,
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.roboto)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF000000),
                            letterSpacing = 1.sp,
                        )
                        request.location?.let {
                          Text(
                              text = it.name,
                              fontSize = 12.sp,
                              color = Color.Gray,
                              modifier = Modifier.clickable { selectedLocation = it })
                        }
                        // TODO date request was created

                      }
                      IconButton(onClick = { /*TODO report add ...*/}) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                      }
                    }

                Spacer(modifier = Modifier.height(8.dp))
                // Request description
                Text(
                    text = request.description,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 18.2.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF000000),
                        ))

                Spacer(modifier = Modifier.height(8.dp))
                // Display image related to the request
                AsyncImage(
                    modifier =
                        Modifier.width(237.dp)
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .align(Alignment.CenterHorizontally),
                    model = request.imageUrl,
                    placeholder = painterResource(id = R.drawable.loading),
                    error = painterResource(id = R.drawable.error),
                    contentDescription = "service image",
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    Modifier.border(width = 2.dp, color = Color(0xFFE0E0E0))
                        .padding(2.dp)
                        .fillMaxWidth()
                        .height(0.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // Interaction bar for comments, sharing, and replying
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                      InteractionBar("Comment", R.drawable.comment_icon)
                      InteractionBar("Share", R.drawable.share_icon)
                      InteractionBar("Reply", R.drawable.reply_icon)
                    }
                selectedLocation?.let {
                  GetDirectionsBubble(location = it) { selectedLocation = null }
                }
              }
        }
      }
}

// A composable for the interaction bar, showing options to comment, share, and reply
@Composable
fun InteractionBar(text: String, icon: Int) {
  val context = LocalContext.current
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontFamily = FontFamily(Font(R.font.roboto)),
        fontWeight = FontWeight(500),
        color = Color(0xFF585C60))

    IconButton(
        onClick = { /* Handle comment click EPIC 3*/
          Toast.makeText(context, "Not Yet Implemented !", Toast.LENGTH_LONG).show()
        }) {
          Image(
              modifier = Modifier.padding(0.dp).width(21.dp).height(18.90004.dp),
              painter = painterResource(id = icon),
              contentDescription = text,
              contentScale = ContentScale.Crop)
        }
  }
}

// Composable for filter bar
@SuppressLint("SuspiciousIndentation")
@Composable
fun FilterBar(
    selectedService: String,
    selectedFilters: Set<String>,
    onSelectedService: (String) -> Unit,
    onFilterChange: (String, Boolean) -> Unit
) {

  // For the moment we chose to have 3 filters, it can ameliorated later
  // TODO near to me locate in function of user location
  val filters = listOf("Service", "Near To Me", "Due Time")

  // LazyRow containing filters
  LazyRow(
      modifier = Modifier.testTag("FilterBar"),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
      verticalAlignment = Alignment.Top,
  ) {
    items(filters.size) { idx ->
      val filter = filters[idx]
      // Calling special chip when it's Service to have a dropdown menu with different possible
      // services
      if (filter == "Service") {
        ServiceChip(
            selectedService,
            onServiceSelected = { u ->
              onSelectedService(u)
              onFilterChange("Service", true)
            })
        // Calling a lambda filter Chip
      } else {
        val isSelected = selectedFilters.contains(filter)
        FilterChip(
            filter,
            isSelected = isSelected,
            onSelected = { selected -> onFilterChange(filter, selected) })
      }
    }
  }
}
// Composable for service chip
@Composable
fun ServiceChip(
    selectedService: String,
    onServiceSelected: (String) -> Unit,
) {

  var selectedText by remember { mutableStateOf(selectedService) }
  var showDropdown by remember { mutableStateOf(false) }
  val backgroundColor = if (selectedText != "Service") Color(0xFFFFFAF5) else Color(0xFFFFFFFF)
  val borderTextColor = if (selectedText != "Service") Color(0xFF00C853) else Color(0xFFAFAFAF)
  // Box containing "Service" or a specific service if selected
  Box(
      modifier =
          Modifier.testTag("ServiceChip")
              .padding(8.dp)
              .border(1.dp, borderTextColor, shape = RoundedCornerShape(50))
              .background(backgroundColor, shape = RoundedCornerShape(50))
              .clickable { showDropdown = !showDropdown } // Toggle selection state
              .padding(12.dp, 6.dp), // Add some padding inside the chip
      contentAlignment = Alignment.Center) {
        Text(
            text = selectedText,
            fontSize = 16.sp,
            lineHeight = 34.sp,
            fontFamily = FontFamily(Font(R.font.donegal_one)),
            fontWeight = FontWeight(400),
            color = borderTextColor,
        )
      }

  // DropDown Menu to list different possible services
  if (showDropdown) {
    DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
      Services.entries.forEach { service ->
        val serviceName = service.name.replace("_", " ")
        DropdownMenuItem(
            modifier = Modifier.testTag(serviceName),
            text = { Text(serviceName) },
            onClick = {
              selectedText = serviceName
              onServiceSelected(serviceName)
              showDropdown = false
            },
        )
      }
    }
  }
}
// Filter Chip
@Composable
fun FilterChip(label: String, isSelected: Boolean, onSelected: (Boolean) -> Unit) {
  val backgroundColor = if (isSelected) Color(0xFFFFFAF5) else Color(0xFFFFFFFF)
  val borderTextColor = if (isSelected) Color(0xFF00C853) else Color(0xFFAFAFAF)

  val context = LocalContext.current
  Box(
      modifier =
          Modifier.padding(8.dp)
              .border(1.dp, borderTextColor, shape = RoundedCornerShape(50))
              .background(backgroundColor, shape = RoundedCornerShape(50))
              .clickable {
                onSelected(!isSelected)
                if (label == "Near To Me")
                    Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
              } // Toggle selection state
              .padding(12.dp, 6.dp), // Add some padding inside the chip
      contentAlignment = Alignment.Center) {
        Text(
            text = label,
            fontSize = 16.sp,
            lineHeight = 34.sp,
            fontFamily = FontFamily(Font(R.font.donegal_one)),
            fontWeight = FontWeight(400),
            color = borderTextColor,
        )
      }
}

// Main screen displaying the list of requests with the top bar, search bar, and title
@Composable
fun ListRequestsFeedScreen(
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    navigationActions: NavigationActions
) {
  val requests by serviceRequestViewModel.requests.collectAsState()
  val selectedFilters = remember { mutableStateOf(setOf<String>()) }
  var selectedService by remember { mutableStateOf("Service") }
  serviceRequestViewModel.getServiceRequests()

  Log.e("ListRequestsFeed", "${selectedFilters.value}")
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("ListRequestsScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = Route.REQUESTS_FEED)
      },
      topBar = { RequestsTopBar() },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(color = Color(0xFFF6F6F6))
                    .testTag("ScreenContent")) {
              SearchBar()
              Spacer(Modifier.height(15.dp))
              TitleScreen()
              FilterBar(
                  selectedService,
                  selectedFilters.value,
                  onSelectedService = { service -> selectedService = service },
                  onFilterChange = { filter, isSelected ->
                    // add or delete filters to selected filters  onClick
                    if (isSelected) {
                      selectedFilters.value += filter
                    } else {
                      selectedFilters.value -= filter
                    }
                  })
              // Filter requests
              var filteredRequest =
                  requests.filter { serviceRequest ->
                    var condition = true
                    if (selectedFilters.value.contains("Service")) {
                      condition =
                          condition && serviceRequest.type.toString().uppercase() == selectedService
                    }
                    if (selectedFilters.value.contains("Near To Me")) {
                      // TODO
                    }
                    condition
                  }
              // Sort requests on due time
              if (selectedFilters.value.contains("Due Time")) {
                filteredRequest = filteredRequest.sortedBy { it.dueDate.toDate().time }
              }
              ListRequests(filteredRequest)
            }
      })
}
