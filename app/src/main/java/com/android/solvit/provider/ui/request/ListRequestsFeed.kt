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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.map.GetDirectionsBubble
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_PROVIDER
import com.android.solvit.shared.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.Locale

@Preview
@Composable
fun RequestsTopBar() {
  val context = LocalContext.current
  Row(
      modifier = Modifier.fillMaxWidth().background(Color.White).testTag("RequestsTopBar"),
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
                      fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333)))
          Text(
              text = "It",
              style =
                  TextStyle(
                      fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D823B)))
        }

        IconButton(
            onClick = {
              Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
            }) {
              Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
            }
      }
}

@Preview
@Composable
fun SearchBar() {
  val context = LocalContext.current
  Row(
      modifier = Modifier.fillMaxWidth().background(Color.White),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = "",
            onValueChange = {
              Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
            },
            singleLine = true,
            textStyle = TextStyle(color = Color.Gray, fontSize = 16.sp),
            modifier =
                Modifier.fillMaxWidth(0.9f)
                    .height(50.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("SearchBar")) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.size(15.dp),
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "Search icon",
                    contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Search requests",
                    style =
                        TextStyle(
                            color = Color(0xFFAFAFAF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold))
              }
            }
      }
}

@Composable
fun ListRequests(requests: List<ServiceRequest>) {
  LazyColumn(
      modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(requests) { request -> ServiceRequestItem(request) }
      }
}

@Composable
fun ServiceRequestItem(request: ServiceRequest) {
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val context = LocalContext.current
  val onClick = { Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show() }
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
              .background(Color.White, RoundedCornerShape(12.dp))
              .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Image(
              painter = painterResource(id = R.drawable.image_user),
              contentDescription = "Profile Picture",
              modifier = Modifier.size(50.dp).clip(CircleShape))
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = request.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text =
                    request.type.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                        .replace("_", " "),
                fontSize = 14.sp,
                color = Color.Gray,
            )
          }
          IconButton(onClick = { onClick() }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
          }
        }
        Row {
          Text(text = "Deadline:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          Text(
              text =
                  SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                      .format(request.dueDate.toDate()),
              fontSize = 15.sp)
        }
        request.location?.let {
          Text(
              text = if (it.name.length > 50) "${it.name.take(50)}..." else it.name,
              fontSize = 12.sp,
              color = Color.Gray,
              modifier = Modifier.clickable { selectedLocation = it })
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = request.description, fontSize = 14.sp, lineHeight = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (request.imageUrl != null) {
          AsyncImage(
              model = request.imageUrl,
              placeholder = painterResource(id = R.drawable.loading),
              error = painterResource(id = R.drawable.logosolvit_firstpage),
              contentDescription = "Service Image",
              modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
              contentScale = ContentScale.Crop)
        } else {
          Box(
              modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center) {
                Text(text = "No Image Provided", fontSize = 16.sp, color = Color.Gray)
              }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          InteractionBar("Comment", R.drawable.comment_icon, onClick)
          InteractionBar("Share", R.drawable.share_icon, onClick)
          InteractionBar("Reply", R.drawable.reply_icon, onClick)
        }
        selectedLocation?.let { GetDirectionsBubble(location = it) { selectedLocation = null } }
      }
}

@Composable
fun InteractionBar(text: String, icon: Int, onClick: () -> Unit = {}) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = text, fontSize = 14.sp, color = Color(0xFF585C60))
    IconButton(onClick = { onClick() }) {
      Image(
          painter = painterResource(id = icon),
          contentDescription = text,
          modifier = Modifier.size(21.dp))
    }
  }
}

@Composable
fun FilterBar(
    selectedService: String,
    selectedFilters: Set<String>,
    onSelectedService: (String) -> Unit,
    onFilterChange: (String, Boolean) -> Unit
) {
  val filters = listOf("Service", "Near To Me", "Due Time")

  LazyRow(
      modifier = Modifier.testTag("FilterBar"),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.Top) {
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

@Composable
fun ServiceChip(selectedService: String, onServiceSelected: (String) -> Unit) {
  var selectedText by remember { mutableStateOf(selectedService) }
  var showDropdown by remember { mutableStateOf(false) }

  val backgroundColor = if (selectedText != "Service") Color(0xFFFFFAF5) else Color(0xFFFFFFFF)
  val borderTextColor = if (selectedText != "Service") Color(0xFF00C853) else Color(0xFFAFAFAF)

  Box(
      modifier =
          Modifier.testTag("ServiceChip").chipModifier(backgroundColor, borderTextColor).clickable {
            showDropdown = !showDropdown
          },
      contentAlignment = Alignment.Center) {
        Text(
            text = selectedText,
            fontSize = 16.sp,
            lineHeight = 34.sp,
            fontFamily = FontFamily(Font(R.font.donegal_one)),
            fontWeight = FontWeight(400),
            color = borderTextColor)
      }

  DropdownMenu(
      expanded = showDropdown,
      onDismissRequest = { showDropdown = false },
      modifier =
          Modifier.border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
              .background(Color.White, RoundedCornerShape(12.dp))
              .testTag("ServiceDropdown")) {
        Services.entries.forEach { service ->
          val serviceName = service.name.replace("_", " ")
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

@Composable
fun FilterChip(label: String, isSelected: Boolean, onSelected: (Boolean) -> Unit) {
  val backgroundColor = if (isSelected) Color(0xFFFFFAF5) else Color(0xFFFFFFFF)
  val borderTextColor = if (isSelected) Color(0xFF00C853) else Color(0xFFAFAFAF)

  val context = LocalContext.current
  Box(
      modifier =
          Modifier.chipModifier(backgroundColor, borderTextColor).clickable {
            onSelected(!isSelected)
            if (label == "Near To Me") {
              Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
            }
          },
      contentAlignment = Alignment.Center) {
        Text(
            text = label,
            fontSize = 16.sp,
            lineHeight = 34.sp,
            fontFamily = FontFamily(Font(R.font.donegal_one)),
            fontWeight = FontWeight(400),
            color = borderTextColor)
      }
}

// Extension function for common modifier styling
private fun Modifier.chipModifier(backgroundColor: Color, borderTextColor: Color) =
    this.padding(8.dp)
        .border(1.dp, borderTextColor, shape = RoundedCornerShape(50))
        .background(backgroundColor, shape = RoundedCornerShape(50))
        .padding(12.dp, 6.dp)

@Composable
fun ListRequestsFeedScreen(
    serviceRequestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    navigationActions: NavigationActions
) {
  val requests by serviceRequestViewModel.requests.collectAsState()
  val selectedFilters = remember { mutableStateOf(setOf<String>()) }
  var selectedService by remember { mutableStateOf("Service") }

  Scaffold(
      topBar = { RequestsTopBar() },
      bottomBar = {
        SeekerBottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_PROVIDER,
            selectedItem = navigationActions.currentRoute())
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .testTag("ScreenContent"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              SearchBar()
              Spacer(modifier = Modifier.height(15.dp))
              FilterBar(
                  selectedService = selectedService,
                  selectedFilters = selectedFilters.value,
                  onSelectedService = { service -> selectedService = service },
                  onFilterChange = { filter, isSelected ->
                    selectedFilters.value =
                        if (isSelected) selectedFilters.value + filter
                        else selectedFilters.value - filter
                  })
              val filteredRequests =
                  requests.filter { request ->
                    // Apply filtering logic here
                    true
                  }
              ListRequests(filteredRequests)
            }
      }
}
