package com.android.solvit.seeker.ui.request

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A composable that displays a text field for inputting a title for the request.
 *
 * @param title The current title value to be displayed in the text field.
 * @param onTitleChange A callback function to update the title when the value changes.
 */
@Composable
fun TitleInput(title: String, onTitleChange: (String) -> Unit) {
  OutlinedTextField(
      value = title,
      onValueChange = onTitleChange,
      label = { Text("Title", style = Typography.bodyLarge) },
      placeholder = { Text("Name your Request", style = Typography.bodyLarge) },
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().testTag("inputRequestTitle"),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = colorScheme.secondary,
              unfocusedBorderColor = colorScheme.surfaceVariant))
}

/**
 * A composable that displays a text field for inputting a description for the request.
 *
 * @param description The current description value to be displayed in the text field.
 * @param onDescriptionChange A callback function to update the description when the value changes.
 */
@Composable
fun DescriptionInput(description: String, onDescriptionChange: (String) -> Unit) {
  OutlinedTextField(
      value = description,
      onValueChange = onDescriptionChange,
      label = { Text("Description", style = Typography.bodyLarge) },
      placeholder = { Text("Describe your request", style = Typography.bodyLarge) },
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().height(150.dp).testTag("inputRequestDescription"),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = colorScheme.secondary,
              unfocusedBorderColor = colorScheme.surfaceVariant))
}

/**
 * A composable that displays a dropdown menu for selecting a service type.
 *
 * @param typeQuery The current query to filter the service types.
 * @param onTypeQueryChange A callback function to update the type query when the value changes.
 * @param showDropdownType A boolean flag to show or hide the dropdown menu.
 * @param onShowDropdownTypeChange A callback function to toggle the visibility of the dropdown.
 * @param filteredServiceTypes A list of filtered service types to display in the dropdown.
 * @param onServiceTypeSelected A callback function to handle the selection of a service type.
 * @param readOnly A flag to make the field read-only (default is false).
 */
@Composable
fun ServiceTypeDropdown(
    typeQuery: String,
    onTypeQueryChange: (String) -> Unit,
    showDropdownType: Boolean,
    onShowDropdownTypeChange: (Boolean) -> Unit,
    filteredServiceTypes: List<Services>,
    onServiceTypeSelected: (Services) -> Unit,
    readOnly: Boolean = false
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value =
            typeQuery.replace("_", " ").lowercase(Locale.getDefault()).replaceFirstChar {
              it.uppercase(Locale.getDefault())
            },
        onValueChange = {
          onTypeQueryChange(it)
          onShowDropdownTypeChange(true)
        },
        readOnly = readOnly,
        label = { Text("Service Type", style = Typography.bodyLarge) },
        placeholder = { Text("Select a Service Type", style = Typography.bodyLarge) },
        shape = RoundedCornerShape(12.dp),
        modifier =
            Modifier.fillMaxWidth().testTag("inputServiceType").onFocusChanged { focusState ->
              if (!focusState.isFocused) onShowDropdownTypeChange(false)
            },
        singleLine = true,
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = colorScheme.secondary,
                unfocusedBorderColor = colorScheme.surfaceVariant))

    DropdownMenu(
        expanded = showDropdownType,
        onDismissRequest = { onShowDropdownTypeChange(false) },
        properties = PopupProperties(focusable = false),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(max = 200.dp)
                .background(colorScheme.background)
                .border(1.dp, colorScheme.onSurfaceVariant, shape = RoundedCornerShape(8.dp))
                .padding(start = 8.dp, end = 8.dp)
                .testTag("serviceTypeMenu")) {
          filteredServiceTypes.forEach { serviceType ->
            DropdownMenuItem(
                modifier = Modifier.testTag("serviceTypeResult"),
                text = {
                  Text(
                      serviceType.name
                          .replace("_", " ")
                          .lowercase(Locale.getDefault())
                          .replaceFirstChar { it.uppercase(Locale.getDefault()) },
                      style = Typography.bodyLarge)
                },
                onClick = {
                  onServiceTypeSelected(serviceType)
                  onShowDropdownTypeChange(false)
                })
            HorizontalDivider()
          }

          if (filteredServiceTypes.isEmpty()) {
            DropdownMenuItem(
                modifier = Modifier.testTag("serviceTypeResult"),
                text = {
                  Text(
                      Services.OTHER.name.lowercase(Locale.getDefault()).replaceFirstChar {
                        it.uppercase(Locale.getDefault())
                      },
                      style = Typography.bodyLarge)
                },
                onClick = {
                  onServiceTypeSelected(Services.OTHER)
                  onShowDropdownTypeChange(false)
                })
            HorizontalDivider()
          }
        }
  }
}

/**
 * A composable that displays a dropdown menu for selecting a location address.
 *
 * @param locationQuery The current query to filter the location suggestions.
 * @param onLocationQueryChange A callback function to update the location query when the value
 *   changes.
 * @param showDropdownLocation A boolean flag to show or hide the dropdown menu.
 * @param onShowDropdownLocationChange A callback function to toggle the visibility of the dropdown.
 * @param locationSuggestions A list of location suggestions to display in the dropdown.
 * @param userLocations A list of previously used locations.
 * @param onLocationSelected A callback function to handle the selection of a location.
 * @param requestLocation The current location request.
 * @param backgroundColor The background color for the dropdown (default is the theme's background
 *   color).
 * @param debounceDelay The debounce delay for API calls (default is 1001 milliseconds).
 * @param isValueOk A flag indicating whether the location input is valid.
 * @param errorMessage A custom error message to display when the location input is invalid.
 * @param testTag The test tag for the dropdown menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    locationQuery: String,
    onLocationQueryChange: (String) -> Unit, // Triggers the API call, needs to be debounced
    showDropdownLocation: Boolean,
    onShowDropdownLocationChange: (Boolean) -> Unit,
    locationSuggestions: List<Location>,
    userLocations: List<Location>,
    onLocationSelected: (Location) -> Unit,
    requestLocation: Location?,
    backgroundColor: Color = colorScheme.background,
    debounceDelay: Long = 1001L, // debounce delay longer than 1 second,
    isValueOk: Boolean = false,
    errorMessage: String = "Invalid location", // Default error message
    testTag: String = "inputRequestAddress"
) {
  val coroutineScope = rememberCoroutineScope()
  var debounceJob by remember { mutableStateOf<Job?>(null) }

  // Local state to update the text field instantly without triggering the API call
  var localQuery by remember { mutableStateOf(locationQuery) }

  // State to manage whether the field has been "visited" (focused then unfocused)
  var hasBeenFocused by remember { mutableStateOf(false) }
  var hasLostFocusAfterTyping by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxWidth().testTag("addressDropdown")) {
    OutlinedTextField(
        value = localQuery,
        onValueChange = { query ->
          // Update the local query immediately
          localQuery = query

          // Cancel any previous debounce job
          debounceJob?.cancel()

          // Start a new coroutine for debouncing the API call
          debounceJob =
              coroutineScope.launch {
                delay(debounceDelay)
                onLocationQueryChange(query) // Call API after debounce
                onShowDropdownLocationChange(true)
              }

          // Reset focus-loss tracking when user starts typing
          if (query.isNotEmpty()) {
            hasLostFocusAfterTyping = false
          }
        },
        label = { Text("Address", style = Typography.bodyLarge) },
        placeholder = {
          requestLocation?.name?.let { Text(it, style = Typography.bodyLarge) }
              ?: Text("Enter your address", style = Typography.bodyLarge)
        },
        shape = RoundedCornerShape(12.dp),
        modifier =
            Modifier.fillMaxWidth().testTag(testTag).onFocusChanged { focusState ->
              // Mark field as "visited" once it loses focus after user types
              if (!focusState.isFocused && localQuery.isNotBlank()) {
                hasBeenFocused = true
                hasLostFocusAfterTyping = true
              }
            },
        singleLine = true,
        leadingIcon = {
          Icon(
              Icons.Default.Home,
              contentDescription = "Location Icon",
              tint = if (isValueOk) colorScheme.secondary else colorScheme.onSurfaceVariant)
        },
        colors =
            TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = colorScheme.onBackground,
                unfocusedTextColor =
                    if (locationQuery.isEmpty()) colorScheme.onSurfaceVariant
                    else if (!isValueOk) colorScheme.error else colorScheme.onBackground,
                focusedBorderColor = if (isValueOk) colorScheme.secondary else colorScheme.primary,
                unfocusedBorderColor =
                    when {
                      locationQuery.isEmpty() -> colorScheme.surfaceVariant
                      isValueOk -> colorScheme.secondary
                      else -> colorScheme.error
                    },
            ))

    // Dropdown menu for location suggestions
    DropdownMenu(
        expanded = showDropdownLocation,
        onDismissRequest = { onShowDropdownLocationChange(false) },
        properties = PopupProperties(focusable = false),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(max = 200.dp)
                .background(backgroundColor)
                .border(1.dp, colorScheme.onSurfaceVariant, shape = RoundedCornerShape(8.dp))
                .padding(start = 8.dp, end = 8.dp)) {
          if (userLocations.isNotEmpty()) {
            Text(
                text = "Previously used locations",
                modifier = Modifier.padding(8.dp),
                style = Typography.bodyLarge.copy(color = colorScheme.primary))
            userLocations.forEach { location ->
              DropdownMenuItem(
                  modifier = Modifier.padding(start = 8.dp, end = 8.dp).testTag("locationResult"),
                  text = {
                    Text(
                        text =
                            location.name.take(50) + if (location.name.length > 50) "..." else "",
                        maxLines = 1,
                        style = Typography.bodyLarge)
                  },
                  onClick = {
                    onLocationQueryChange(location.name)
                    localQuery = location.name
                    onLocationSelected(location)
                    onShowDropdownLocationChange(false)
                  })
              HorizontalDivider()
            }
          }
          if (locationSuggestions.isNotEmpty()) {
            Text(
                text = "Suggested locations",
                modifier = Modifier.padding(8.dp),
                style = Typography.bodyLarge.copy(color = colorScheme.primary))
            locationSuggestions.forEach { location ->
              DropdownMenuItem(
                  modifier = Modifier.padding(start = 8.dp, end = 8.dp).testTag("locationResult"),
                  text = {
                    Text(
                        text =
                            location.name.take(50) + if (location.name.length > 50) "..." else "",
                        maxLines = 1,
                        style = Typography.bodyLarge)
                  },
                  onClick = {
                    onLocationQueryChange(location.name)
                    localQuery = location.name
                    onLocationSelected(location)
                    onShowDropdownLocationChange(false)
                  })
              HorizontalDivider()
            }
          }
        }

    // Display the error message if the field has been visited, input is incorrect, and focus was
    // lost after typing
    if (!isValueOk && hasBeenFocused && hasLostFocusAfterTyping) {
      Text(
          text = errorMessage,
          modifier = Modifier.padding(start = 16.dp, top = 65.dp),
          style = Typography.bodyLarge.copy(fontSize = 15.sp, color = colorScheme.error))
    }
  }
}

/**
 * A composable that displays a date picker modal dialog to select a date.
 *
 * @param onDateSelected A callback function to handle the selected date.
 * @param onDismiss A callback function to dismiss the date picker dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
  val datePickerState = rememberDatePickerState()

  DatePickerDialog(
      onDismissRequest = onDismiss,
      modifier = Modifier.testTag("datePickerDialog"),
      confirmButton = {
        TextButton(
            onClick = {
              onDateSelected(datePickerState.selectedDateMillis)
              onDismiss()
            }) {
              Text("OK", style = Typography.bodyLarge)
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel", style = Typography.bodyLarge) }
      }) {
        DatePicker(state = datePickerState)
      }
}

/**
 * A composable that displays a date field with a trailing icon to open a date picker.
 *
 * @param modifier The modifier to apply to the text field.
 * @param dueDate The current due date to display in the text field.
 * @param onDateChange A callback function to handle the change in the selected date.
 */
@Composable
fun DatePickerFieldToModal(
    modifier: Modifier = Modifier,
    dueDate: String,
    onDateChange: (String) -> Unit
) {
  var selectedDate by remember { mutableStateOf<Long?>(null) }
  var showModal by remember { mutableStateOf(false) }

  OutlinedTextField(
      value = selectedDate?.let { convertMillisToDate(it) } ?: dueDate,
      onValueChange = {},
      label = { Text("Deadline", style = Typography.bodyLarge) },
      placeholder = { Text("DD/MM/YYYY", style = Typography.bodyLarge) },
      trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") },
      shape = RoundedCornerShape(12.dp),
      modifier =
          modifier.fillMaxWidth().testTag("inputRequestDate").pointerInput(selectedDate) {
            awaitEachGesture {
              awaitFirstDown(pass = PointerEventPass.Initial)
              val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
              if (upEvent != null) {
                showModal = true
              }
            }
          },
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = colorScheme.secondary,
              unfocusedBorderColor = colorScheme.surfaceVariant))

  if (showModal) {
    DatePickerModal(
        onDateSelected = {
          selectedDate = it
          it?.let { millis ->
            val formattedDate = convertMillisToDate(millis)
            onDateChange(formattedDate)
          }
        },
        onDismiss = { showModal = false })
  }
}

/**
 * A helper function to convert a date in milliseconds to a formatted string (dd/MM/yyyy).
 *
 * @param millis The date in milliseconds.
 * @return The formatted date string.
 */
fun convertMillisToDate(millis: Long): String {
  val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return formatter.format(Date(millis))
}

/**
 * A composable that displays a button to select an image, and shows a preview of the selected
 * image.
 *
 * @param selectedImageUri The URI of the currently selected image.
 * @param imageUrl The URL of the image to display if no image is selected.
 * @param onImageSelected A callback function to handle the image selection.
 */
@Composable
fun ImagePicker(
    selectedImageUri: Uri?,
    imageUrl: String?, // URL of the image to display if no image is selected
    onImageSelected: (Uri?) -> Unit
) {
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri: Uri? -> onImageSelected(uri) })

  // Using Box instead of Button for full control over image filling
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(150.dp)
              .border(1.dp, colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Transparent)
              .clickable { imagePickerLauncher.launch("image/*") }
              .testTag("imagePickerButton")) {
        if (selectedImageUri == null && imageUrl == null) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                Text(
                    "Upload Image",
                    style = Typography.bodyLarge.copy(color = colorScheme.onSurfaceVariant))
              }
        } else {
          AsyncImage(
              model =
                  selectedImageUri?.toString()
                      ?: imageUrl, // Show selected image URI or fallback URL
              contentDescription = "Service Request Image",
              contentScale = ContentScale.Crop, // Crop to fill the space
              modifier = Modifier.fillMaxSize() // Fill the entire box
              )
        }
      }
}

/**
 * A composable button that triggers the deletion of a service request when clicked. It also
 * navigates back to the requests overview screen after the deletion is successful.
 *
 * @param request The service request to be deleted, containing its details such as `uid`.
 * @param requestViewModel The view model responsible for handling service request actions, such as
 *   deleting a request.
 * @param navigationActions The navigation actions used to navigate between screens. The
 *   `navigateAndSetBackStack` method is used to navigate to the requests overview screen after
 *   deletion.
 */
@Composable
fun DeleteButton(
    request: ServiceRequest,
    requestViewModel: ServiceRequestViewModel,
    navigationActions: NavigationActions
) {
  Button(
      modifier = Modifier.testTag("deleteRequestButton").fillMaxWidth().height(40.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      onClick = {
        try {
          requestViewModel.deleteServiceRequestById(request.uid)
          navigationActions.navigateAndSetBackStack(Route.REQUESTS_OVERVIEW, listOf())
        } catch (e: Exception) {
          Log.e("EditRequestScreen", "Error deleting request", e)
        }
      }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = colorScheme.error,
                  modifier = Modifier.padding(end = 8.dp))
              Text("Delete", style = Typography.bodyLarge.copy(color = colorScheme.error))
            }
      }
}
