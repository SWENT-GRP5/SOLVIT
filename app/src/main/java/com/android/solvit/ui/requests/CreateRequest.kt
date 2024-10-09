package com.android.solvit.ui.requests

import android.graphics.Bitmap
import android.icu.util.GregorianCalendar
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.model.map.Location
import com.android.solvit.model.map.LocationViewModel
import com.android.solvit.model.requests.ServiceRequest
import com.android.solvit.model.requests.ServiceRequestStatus
import com.android.solvit.model.requests.ServiceRequestType
import com.android.solvit.model.requests.ServiceRequestViewModel
import com.android.solvit.model.requests.loadBitmapFromUri
import com.google.firebase.Timestamp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    requestViewModel: ServiceRequestViewModel =
        viewModel(factory = ServiceRequestViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dueDate by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  val context = LocalContext.current
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val locationQuery by locationViewModel.query.collectAsState()

  var showDropdownLocation by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

  var showDropdownType by remember { mutableStateOf(false) }
  var typeQuery by remember { mutableStateOf("") }
  val filteredServiceTypes =
      ServiceRequestType.entries.filter { it.name.contains(typeQuery, ignoreCase = true) }
  var selectedServiceType by remember { mutableStateOf(ServiceRequestType.OTHER) }

  // Image Picker
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri: Uri? ->
            selectedImageUri = uri
            uri?.let { selectedImageBitmap = loadBitmapFromUri(context, it) }
          })

  Scaffold(
      modifier = Modifier.testTag("createRequestScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Create a new request", Modifier.testTag("createRequestTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { /*navigationActions.goBack()*/},
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Title Input
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  label = { Text("Title") },
                  placeholder = { Text("Name your Request") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .border(1.dp, Color.Transparent)
                          .testTag("inputRequestTitle"),
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
                    placeholder = { Text("Select a Service Type") },
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
                        HorizontalDivider()
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
                        HorizontalDivider()
                      }
                    }
              }

              // Description Input
              OutlinedTextField(
                  value = description,
                  onValueChange = { description = it },
                  label = { Text("Description") },
                  placeholder = { Text("Describe your request") },
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
                      showDropdownLocation = true // Show dropdown when user starts typing
                    },
                    label = { Text("Address") },
                    placeholder = { Text("What is your address?") },
                    modifier = Modifier.fillMaxWidth().testTag("inputRequestAddress"),
                    singleLine = true)

                DropdownMenu(
                    expanded = showDropdownLocation && locationSuggestions.isNotEmpty(),
                    onDismissRequest = { showDropdownLocation = false },
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
                                          else "", // Limit name length
                                  maxLines = 1 // Ensure name doesn't overflow
                                  )
                            },
                            onClick = {
                              locationViewModel.setQuery(location.name)
                              selectedLocation = location
                              showDropdownLocation = false // Close dropdown on selection
                            })
                        HorizontalDivider()
                      }
                    }
              }

              // Due Date Input
              OutlinedTextField(
                  value = dueDate,
                  onValueChange = { dueDate = it },
                  label = { Text("What is the deadline for this request?") },
                  placeholder = { Text("--/--/----") },
                  modifier = Modifier.fillMaxWidth().testTag("inputRequestDate"),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          unfocusedContainerColor = Color.Transparent,
                      ))

              // Display the selected image (if any)
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
                    if (selectedImageBitmap != null) {
                      Image(
                          bitmap = selectedImageBitmap!!.asImageBitmap(),
                          contentDescription = "Selected Image",
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize())
                    } else {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.Center,
                          modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Gray, // Icon in grey
                                modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                            Text(
                                "Upload Image", color = Color.Gray // Text in grey
                                )
                          }
                    }
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
                                // TODO: retrieve assignee from user
                                assigneeName = "assignee",
                                dueDate = Timestamp(calendar.time),
                                location = selectedLocation,
                                status = ServiceRequestStatus.PENDING,
                                uid = requestViewModel.getNewUid(),
                                type = selectedServiceType,
                                imageUrl = null)

                        requestViewModel.saveServiceRequestWithImage(
                            serviceRequest, selectedImageUri!!)

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
                          containerColor = Color(0xFFCA97FC), // Violet background when enabled
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
                              "Submit Request",
                          )
                        }
                  }
            }
      })
  /*bottomBar = {
      BottomNavigationMenu(
          onTabSelect = { destination -> navigationActions.navigateTo(destination) },
          tabList = LIST_TOP_LEVEL_DESTINATION,
          selectedItem = Screen.CREATE_REQUEST)
  })*/
}
