package com.android.solvit.ui.requests

import android.icu.util.GregorianCalendar
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.model.map.Location
import com.android.solvit.model.map.LocationViewModel
import com.android.solvit.model.requests.ServiceRequest
import com.android.solvit.model.requests.ServiceRequestStatus
import com.android.solvit.model.requests.ServiceRequestType
import com.android.solvit.model.requests.ServiceRequestViewModel
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRequestScreen(
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {
  // TODO: suppress when the request selection is implemented
  requestViewModel.selectRequest(
      ServiceRequest(
          title = "Bathtub leak",
          description = "I hit my bath too hard and now it's leaking",
          assigneeName = "assignee",
          dueDate = Timestamp(Calendar.getInstance().time),
          location =
              Location(
                  48.8588897,
                  2.3200410217200766,
                  "Paris, Île-de-France, France métropolitaine, France"),
          status = ServiceRequestStatus.PENDING,
          uid = "gIoUWJGkTgLHgA7qts59",
          type = ServiceRequestType.PLUMBING,
          imageUrl =
              "https://firebasestorage.googleapis.com/v0/b/solvit-14cc1.appspot.com/o/serviceRequestImages%2F588d3bd9-bcb7-47bc-9911-61fae59eaece.jpg?alt=media&token=5f747f33-9732-4b90-9b34-55e28732ebc3"))

  val request = requestViewModel.selectedRequest.value ?: return
  var title by remember { mutableStateOf(request.title) }
  var description by remember { mutableStateOf(request.description) }
  var dueDate by remember {
    mutableStateOf(
        request.dueDate.let {
          val calendar = GregorianCalendar()
          calendar.time = request.dueDate.toDate()
          return@let "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                    calendar.get(
                        Calendar.YEAR
                    )
                }"
        })
  }

  var selectedLocation by remember { mutableStateOf(request.location) }
  val locationQuery by locationViewModel.query.collectAsState()

  // State for dropdown visibility
  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  val context = LocalContext.current

  var showDropdownType by remember { mutableStateOf(false) }
  var typeQuery by remember { mutableStateOf(request.type.name) }
  val filteredServiceTypes =
      ServiceRequestType.entries.filter { it.name.contains(typeQuery, ignoreCase = true) }
  var selectedServiceType by remember { mutableStateOf(request.type) }

  // Image Picker
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri: Uri? -> selectedImageUri = uri })

  // Load the stored image when editing a request
  val imageUrl = request.imageUrl

  Scaffold(
      modifier = Modifier.testTag("editRequestScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Edit your request", modifier = Modifier.testTag("editRequestTitle")) },
            navigationIcon = {
              IconButton(
                  modifier = Modifier.testTag("goBackButton"),
                  onClick = { /*navigationActions.goBack()*/}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Title Input
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  label = { Text("Title") },
                  modifier = Modifier.fillMaxWidth().testTag("inputRequestTitle"),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          unfocusedContainerColor = Color.Transparent,
                      ))

              Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value =
                        typeQuery
                            .replace("_", " ")
                            .lowercase(Locale.getDefault())
                            .replaceFirstChar { it.uppercase(Locale.getDefault()) },
                    onValueChange = {
                      typeQuery = it
                      showDropdownType = true
                    },
                    label = { Text("Service Type") },
                    modifier =
                        Modifier.fillMaxWidth().testTag("inputServiceType").onFocusChanged {
                            focusState ->
                          // Close dropdown if focus is lost
                          if (!focusState.isFocused) showDropdownType = false
                        },
                    singleLine = true)

                DropdownMenu(
                    expanded = showDropdownType,
                    onDismissRequest = { showDropdownType = false },
                    properties = PopupProperties(focusable = false),
                    modifier =
                        Modifier.fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(8.dp))
                            .padding(start = 8.dp, end = 8.dp)
                            .testTag("serviceTypeMenu")) {
                      // Show filtered service types based on the search query
                      filteredServiceTypes.forEach { serviceType ->
                        DropdownMenuItem(
                            modifier = Modifier.testTag("serviceTypeResult"),
                            text = {
                              Text(
                                  serviceType.name
                                      .replace("_", " ")
                                      .lowercase(Locale.getDefault())
                                      .replaceFirstChar { it.uppercase(Locale.getDefault()) })
                            },
                            onClick = {
                              selectedServiceType = serviceType
                              typeQuery = serviceType.name
                              showDropdownType = false
                            })
                        HorizontalDivider() // Separate items with a divider
                      }

                      // If no results
                      if (filteredServiceTypes.isEmpty()) {
                        DropdownMenuItem(
                            modifier = Modifier.testTag("serviceTypeResult"),
                            text = {
                              Text(
                                  ServiceRequestType.OTHER.name
                                      .lowercase(Locale.getDefault())
                                      .replaceFirstChar { it.uppercase(Locale.getDefault()) })
                            },
                            onClick = {
                              selectedServiceType = ServiceRequestType.OTHER
                              typeQuery = ServiceRequestType.OTHER.name
                              showDropdownType = false
                            })
                        HorizontalDivider() // Separate items with a divider
                      }
                    }
              }

              // Description Input
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  modifier =
                      Modifier.fillMaxWidth().height(150.dp).testTag("inputRequestDescription"),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          unfocusedContainerColor = Color.Transparent,
                      ))

              Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = {
                      locationViewModel.setQuery(it)
                      showDropdown = true // Show dropdown when user starts typing
                    },
                    placeholder = { request.location?.let { Text(it.name) } },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth().testTag("inputRequestAddress"))

                // Dropdown to show location suggestions
                DropdownMenu(
                    expanded = showDropdown && locationSuggestions.isNotEmpty(),
                    onDismissRequest = { showDropdown = false },
                    properties = PopupProperties(focusable = false),
                    modifier =
                        Modifier.fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(8.dp))
                            .padding(start = 8.dp, end = 8.dp)) {
                      locationSuggestions.filterNotNull().forEach { location ->
                        DropdownMenuItem(
                            modifier =
                                Modifier.padding(start = 8.dp, end = 8.dp)
                                    .testTag("locationResult"),
                            text = {
                              Text(
                                  text =
                                      location.name.take(50) +
                                          if (location.name.length > 50) "..."
                                          else "", // Limit name length and add ellipsis
                                  maxLines = 1 // Ensure name doesn't overflow
                                  )
                            },
                            onClick = {
                              locationViewModel.setQuery(location.name)
                              selectedLocation = location // Store the selected location object
                              showDropdown = false // Close dropdown on selection
                            })
                        HorizontalDivider() // Separate items with a divider
                      }
                    }
              }

              // Due Date Input
              OutlinedTextField(
                  value = dueDate,
                  onValueChange = { dueDate = it },
                  label = { Text("What is the deadline for this request?") },
                  modifier = Modifier.fillMaxWidth().testTag("inputRequestDate"),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          unfocusedContainerColor = Color.Transparent,
                      ))

              // Image Picker and Display
              Button(
                  onClick = { imagePickerLauncher.launch("image/*") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(150.dp)
                          .border(
                              1.dp,
                              Color.Gray,
                              shape = RoundedCornerShape(8.dp)) // Added grey border
                          .background(Color.Transparent)
                          .testTag("imagePickerButton"),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                  content = {
                    AsyncImage(
                        model = selectedImageUri ?: imageUrl, // Show selected image or URL image
                        contentDescription = "Service Request Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                  })

              // Save Button
              Button(
                  onClick = {
                    val calendar = GregorianCalendar()
                    val parts = dueDate.split("/")
                    if (parts.size == 3) {
                      try {
                        calendar.set(
                            parts[2].toInt(),
                            parts[1].toInt() - 1, // Months are 0-based
                            parts[0].toInt(),
                            0,
                            0,
                            0)

                        val serviceRequest =
                            ServiceRequest(
                                title = title,
                                description = description,
                                assigneeName = request.assigneeName,
                                dueDate = Timestamp(calendar.time),
                                location = selectedLocation,
                                status = request.status,
                                uid = request.uid,
                                type = selectedServiceType,
                                imageUrl = selectedImageUri.toString())

                        requestViewModel.saveServiceRequestWithImage(
                            serviceRequest, selectedImageUri!!)

                        // TODO: navigate back
                        // navigationActions.back()
                        return@Button
                      } catch (_: NumberFormatException) {}
                    }

                    Toast.makeText(
                            context, "Invalid format, date must be DD/MM/YYYY.", Toast.LENGTH_SHORT)
                        .show()
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(start = 80.dp, end = 80.dp)
                          .height(40.dp)
                          .testTag("requestSubmit"),
                  shape = RoundedCornerShape(25.dp), // Fully rounded button
                  enabled = title.isNotBlank() && description.isNotBlank() && dueDate.isNotBlank(),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor =
                              Color(0xFFCA97FC), // Light violet background when enabled
                          disabledContainerColor =
                              Color(0xFFDECBFC), // Light violet color when disabled
                          contentColor = Color.Black, // White text color when enabled
                          disabledContentColor = Color.Gray // Gray text color when disabled
                          )) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()) {
                          Icon(
                              imageVector = Icons.Default.Done,
                              contentDescription = null,
                              modifier = Modifier.size(24.dp))
                          Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                          Text(
                              "Save Edits",
                          )
                        }
                  }

              Button(
                  modifier = Modifier.testTag("deleteRequestButton").fillMaxWidth().height(40.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                  onClick = {
                    try {
                      requestViewModel.deleteServiceRequestById(request.uid)
                      // navigationActions.navigateTo(Screen.OVERVIEW)
                    } catch (e: Exception) {
                      Log.e("EditToDoScreen", "Error deleting task", e)
                    }
                  }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                          Icon(
                              imageVector = Icons.Default.Delete,
                              contentDescription = "Delete",
                              tint = Color.Red,
                              modifier = Modifier.padding(end = 8.dp))
                          Text("Delete", color = Color.Red)
                        }
                  }
            }
      })
  /*bottomBar = {
      BottomNavigationMenu(
          onTabSelect = { destination -> navigationActions.navigateTo(destination) },
          tabList = LIST_TOP_LEVEL_DESTINATION,
          selectedItem = Screen.EDIT_REQUEST)
  })*/
}
