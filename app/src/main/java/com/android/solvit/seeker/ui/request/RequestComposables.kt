package com.android.solvit.seeker.ui.request

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import kotlinx.coroutines.GlobalScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
fun TitleInput(title: String, onTitleChange: (String) -> Unit) {
  OutlinedTextField(
      value = title,
      onValueChange = onTitleChange,
      label = { Text("Title") },
      placeholder = { Text("Name your Request") },
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().testTag("inputRequestTitle"),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = colorScheme.secondary,
              unfocusedBorderColor = colorScheme.onSurfaceVariant))
}

@Composable
fun DescriptionInput(description: String, onDescriptionChange: (String) -> Unit) {
  OutlinedTextField(
      value = description,
      onValueChange = onDescriptionChange,
      label = { Text("Description") },
      placeholder = { Text("Describe your request") },
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().height(150.dp).testTag("inputRequestDescription"),
      colors =
          OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = Color.Transparent,
              focusedBorderColor = colorScheme.secondary,
              unfocusedBorderColor = colorScheme.onSurfaceVariant))
}

@Composable
fun ServiceTypeDropdown(
    typeQuery: String,
    onTypeQueryChange: (String) -> Unit,
    showDropdownType: Boolean,
    onShowDropdownTypeChange: (Boolean) -> Unit,
    filteredServiceTypes: List<Services>,
    onServiceTypeSelected: (Services) -> Unit
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
        label = { Text("Service Type") },
        placeholder = { Text("Select a Service Type") },
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
                unfocusedBorderColor = colorScheme.onSurfaceVariant))

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
                          .replaceFirstChar { it.uppercase(Locale.getDefault()) })
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
                      })
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    locationQuery: String,
    onLocationQueryChange: (String) -> Unit, // Triggers the API call, needs to be debounced
    showDropdownLocation: Boolean,
    onShowDropdownLocationChange: (Boolean) -> Unit,
    locationSuggestions: List<Location>,
    onLocationSelected: (Location) -> Unit,
    requestLocation: Location?,
    backgroundColor: Color = colorScheme.background,
    debounceDelay: Long = 1001L, // debounce delay longer than 1 second,
    isValueOk: Boolean = false,
    errorMessage: String = "Invalid location" // Default error message
) {
  val coroutineScope = rememberCoroutineScope()
  var debounceJob by remember { mutableStateOf<Job?>(null) }

  // Local state to update the text field instantly without triggering the API call
  var localQuery by remember { mutableStateOf(locationQuery) }

  // State to manage whether the field has been "visited" (focused then unfocused)
  var hasBeenFocused by remember { mutableStateOf(false) }
  var hasLostFocusAfterTyping by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxWidth()) {
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
        label = { Text("Address") },
        placeholder = { requestLocation?.name?.let { Text(it) } ?: Text("Enter your address") },
        shape = RoundedCornerShape(12.dp),
        modifier =
            Modifier.fillMaxWidth().testTag("inputRequestAddress").onFocusChanged { focusState ->
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
                      locationQuery.isEmpty() -> colorScheme.onSurfaceVariant
                      isValueOk -> colorScheme.secondary
                      else -> colorScheme.error
                    },
            ))

    // Dropdown menu for location suggestions
    DropdownMenu(
        expanded = showDropdownLocation && locationSuggestions.isNotEmpty(),
        onDismissRequest = { onShowDropdownLocationChange(false) },
        properties = PopupProperties(focusable = false),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(max = 200.dp)
                .background(backgroundColor)
                .border(1.dp, colorScheme.onSurfaceVariant, shape = RoundedCornerShape(8.dp))
                .padding(start = 8.dp, end = 8.dp)) {
          locationSuggestions.forEach { location ->
            DropdownMenuItem(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp).testTag("locationResult"),
                text = {
                  Text(
                      text = location.name.take(50) + if (location.name.length > 50) "..." else "",
                      maxLines = 1)
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

    // Display the error message if the field has been visited, input is incorrect, and focus was
    // lost after typing
    if (!isValueOk && hasBeenFocused && hasLostFocusAfterTyping) {
      Text(
          text = errorMessage,
          color = colorScheme.error,
          fontSize = 15.sp, // Error text size
          modifier = Modifier.padding(start = 16.dp, top = 65.dp))
    }
  }
}

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
              Text("OK")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) {
        DatePicker(state = datePickerState)
      }
}

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
      label = { Text("Deadline") },
      placeholder = { Text("DD/MM/YYYY") },
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
              unfocusedBorderColor = colorScheme.onSurfaceVariant))

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

fun convertMillisToDate(millis: Long): String {
  val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  return formatter.format(Date(millis))
}

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
              .border(1.dp, colorScheme.onSurfaceVariant, shape = RoundedCornerShape(12.dp))
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
                    tint = colorScheme.onSurfaceVariant, // Icon in grey
                    modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
                Text(
                    "Upload Image", color = colorScheme.onSurfaceVariant // Text in grey
                    )
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
          navigationActions.goBack()
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
              Text("Delete", color = colorScheme.error)
            }
      }
}


@Composable
fun AIPopup(
    onDismiss: () -> Unit,
    onAIResult: (title: String, serviceType: String, description: String, images: List<Uri>) -> Unit
) {
    val context = LocalContext.current
    val selectedImages = remember { mutableStateListOf<Uri>() }

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
        ) {
            AndroidView(factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true

                    // Add the JavascriptInterface
                    addJavascriptInterface(
                        AIWebInterface { jsonResult ->
                            // Parse JSON result
                            val results = JSONArray(jsonResult)
                            val firstResult = results.getJSONObject(0)

                            val title = firstResult.getString("title")
                            val serviceType = firstResult.getString("serviceType")
                            val description = firstResult.getString("description")

                            onAIResult(title, serviceType, description, selectedImages)
                            onDismiss()
                        },
                        "Android"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)

                            // Inject JavaScript to pass image URIs to WebView
                            val imageUrisJs = selectedImages.joinToString(",") { "\"${it.toString()}\"" }
                            val jsCode = """
                                const images = [$imageUrisJs];
                                analyzeImages(images);
                            """
                            evaluateJavascript(jsCode, null)
                        }
                    }

                    // Load the local HTML file containing TensorFlow.js logic
                    loadUrl("file:///android_asset/index.html")
                }
            }, modifier = Modifier.fillMaxSize())
        }
    }
}



fun processImagesWithAI(
    images: List<Uri>,
    onResult: (title: String, serviceType: String, description: String) -> Unit
) {
    // Simulate AI processing
    GlobalScope.launch {
        delay(3000) // Simulating AI processing delay
        onResult("Sample Title", "PLUMBER", "Detected plumbing issue")
    }
}

