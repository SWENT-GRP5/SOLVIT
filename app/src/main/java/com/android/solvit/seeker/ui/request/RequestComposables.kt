package com.android.solvit.seeker.ui.request

import analyzeImagesWithOpenAI
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                      locationQuery.isEmpty() -> colorScheme.onSurfaceVariant
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
                color = colorScheme.primary)
            userLocations.forEach { location ->
              DropdownMenuItem(
                  modifier = Modifier.padding(start = 8.dp, end = 8.dp).testTag("locationResult"),
                  text = {
                    Text(
                        text =
                            location.name.take(50) + if (location.name.length > 50) "..." else "",
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
          if (locationSuggestions.isNotEmpty()) {
            Text(
                text = "Suggested locations",
                modifier = Modifier.padding(8.dp),
                color = colorScheme.primary)
            locationSuggestions.forEach { location ->
              DropdownMenuItem(
                  modifier = Modifier.padding(start = 8.dp, end = 8.dp).testTag("locationResult"),
                  text = {
                    Text(
                        text =
                            location.name.take(50) + if (location.name.length > 50) "..." else "",
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
fun AIAssistantDialog(onCancel: () -> Unit, onUploadPictures: () -> Unit) {
  Dialog(onDismissRequest = { onCancel() }) {
    Box(
        modifier =
            Modifier.fillMaxWidth(0.9f) // Adjust width as 90% of the screen
                .wrapContentHeight()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp) // Padding around the dialog content
        ) {
          Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)) {
                      // Icon
                      Box(
                          modifier =
                              Modifier.size(64.dp)
                                  .background(
                                      Color(0xFF2196F3), shape = CircleShape), // Blue Circle
                          contentAlignment = Alignment.Center) {
                            Icon(
                                painter =
                                    painterResource(
                                        id =
                                            R.drawable.ic_ai_assistant), // Replace with proper icon
                                contentDescription = "AI Assistant",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp))
                          }

                      // Title
                      Box(
                          modifier =
                              Modifier.offset(x = -14.dp)
                                  .fillMaxWidth()
                                  .background(
                                      Color(0xFF2196F3),
                                      shape =
                                          RoundedCornerShape(
                                              topStart = 0.dp,
                                              topEnd = 18.dp,
                                              bottomStart = 0.dp,
                                              bottomEnd = 18.dp))
                                  .padding(horizontal = 8.dp, vertical = 10.dp)) {
                            Text(
                                text = "Your AI-Powered Assistant",
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold),
                                color = Color.White,
                                textAlign = TextAlign.Center)
                          }
                    }

                // Dialog Content
                Text(
                    text =
                        "Would you like to use the AI assistant to create your request by uploading pictures of your issue?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()) {
                      Button(
                          onClick = { onCancel() },
                          colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                          modifier = Modifier.padding(end = 8.dp) // Space between buttons
                          ) {
                            Text("Cancel")
                          }

                      Button(
                          onClick = { onUploadPictures() },
                          colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) {
                            Text("Upload Pictures")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                          }
                    }
              }
        }
  }
}

@Composable
fun ImagePickerStep(
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onStartAnalyzing: () -> Unit
) {
  val imagePickerLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null) {
          onImagesSelected(selectedImages + uris)
        }
      }

  Column(modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState())) {
    StepHeader(R.drawable.circle_one_icon, title = "Upload Images")

    Spacer(modifier = Modifier.height(16.dp)) // Spacing below the header

    if (selectedImages.isEmpty()) {
      Text(
          text = "No Images added",
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth())
    } else {
      LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(selectedImages) { uri ->
              Box(
                  modifier =
                      Modifier.size(80.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .background(Color.Gray)
                          .padding(4.dp)) {
                    AsyncImage(
                        model = uri, contentDescription = null, modifier = Modifier.fillMaxSize())
                    Box(
                        Modifier.align(Alignment.TopEnd)
                            .background(Color.White, shape = CircleShape)
                            .clickable { onRemoveImage(uri) }
                            .padding(4.dp)) {
                          Icon(
                              Icons.Default.Delete,
                              contentDescription = "Remove Image",
                              tint = Color.Red)
                        }
                  }
            }
          }
    }

    Spacer(modifier = Modifier.height(16.dp)) // Spacing before the buttons

    // Buttons Row
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
      Button(
          onClick = { imagePickerLauncher.launch("image/*") },
          modifier = Modifier.weight(1f) // Ensures centering in the row
          ) {
            Text("Add Images")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Add, contentDescription = null)
          }

      if (selectedImages.isNotEmpty()) {
        Spacer(modifier = Modifier.width(8.dp)) // Space between buttons

        Button(
            onClick = { onStartAnalyzing() },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)) {
              Text("Analyze")
              Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
      }
    }
  }
}

@Composable
fun StepHeader(@DrawableRes iconRes: Int, title: String) {
  // Header Row
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
    // Icon Number
    Box(
        modifier =
            Modifier.size(60.dp).background(Color(0xFF2196F3), shape = CircleShape), // Blue Circle
        contentAlignment = Alignment.Center) {
          Icon(
              painter = painterResource(id = iconRes), // Replace with proper icon
              contentDescription = "Stepper Icon",
              tint = Color.White,
              modifier = Modifier.size(40.dp))
        }

    // Title
    Box(
        modifier =
            Modifier.offset(x = -14.dp)
                .fillMaxWidth()
                .background(
                    Color(0xFF2196F3),
                    shape =
                        RoundedCornerShape(
                            topStart = 0.dp, topEnd = 18.dp, bottomStart = 0.dp, bottomEnd = 18.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center) {
          Text(
              text = title,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = Color.White,
              textAlign = TextAlign.Center)
        }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewAIAssistantDialog() {
  StepHeader(R.drawable.circle_one_icon, title = "Upload Images")
}

@Composable
fun MultiStepDialog(
    requestViewModel: ServiceRequestViewModel,
    showDialog: Boolean,
    currentStep: Int,
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onStartAnalyzing: () -> Unit,
    onAnalyzeComplete: (String, String, String) -> Unit,
    onClose: () -> Unit
) {
  var isLoading by remember { mutableStateOf(false) }
  var showSparkleEffect by remember { mutableStateOf(false) } // Control for the sparkle effect

  if (showDialog) {
    Dialog(onDismissRequest = { onClose() }) {
      Surface(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(16.dp)) {
        when (currentStep) {
          1 ->
              ImagePickerStep(
                  selectedImages = selectedImages,
                  onImagesSelected = onImagesSelected,
                  onRemoveImage = onRemoveImage,
                  onStartAnalyzing = onStartAnalyzing)
          2 -> {
            LaunchedEffect(selectedImages) {
              if (selectedImages.isNotEmpty() && !isLoading) {
                isLoading = true
                try {
                  val (type, title, description) =
                      uploadAndAnalyze(requestViewModel, selectedImages)
                  onAnalyzeComplete(type, title, description)
                } catch (e: Exception) {
                  Log.e("MultiStepDialog", "Error: ${e.message}")
                } finally {
                  isLoading = false
                }
              }
            }

            if (isLoading) {
              Column(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally) {
                    StepHeader(R.drawable.circle_two_icon, title = "Analyzing Images")
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing your uploaded images. Please wait...")
                  }
            }
          }
          3 -> {
            LaunchedEffect(Unit) {
              showSparkleEffect = true
              kotlinx.coroutines.delay(2000L) // Let the effect play for 2 seconds
              showSparkleEffect = false
              onClose()
            }
            Column(
                modifier =
                    Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  StepHeader(R.drawable.circle_three_icon, title = "Analysis Completed")
                  Spacer(modifier = Modifier.height(16.dp))
                  Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Complete",
                      tint = Color.Green,
                      modifier = Modifier.size(48.dp))
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(
                      text = "Analysis complete! Fields have been filled with AI suggestions.",
                      style = MaterialTheme.typography.bodyMedium,
                      textAlign = TextAlign.Center)
                }
          }
        }
      }
    }
    // Show the animated sparkle effect
    if (showSparkleEffect) {
      AnimatedSparkleEffectOverlay()
    }
  }
}

suspend fun uploadAndAnalyze(
    requestViewModel: ServiceRequestViewModel,
    imageUris: List<Uri>
): Triple<String, String, String> {
  return withContext(Dispatchers.IO) {
    try {
      // Step 1: Upload images and get their URLs
      val imageUrls =
          suspendCoroutine<List<String>> { continuation ->
            requestViewModel.uploadMultipleImages(
                imageUris = imageUris,
                onSuccess = { urls -> continuation.resume(urls) },
                onFailure = { exception -> continuation.resumeWithException(exception) })
          }

      // Log uploaded image URLs
      Log.i("uploadAndAnalyze", "Uploaded image URLs: $imageUrls")

      // Step 2: Analyze uploaded images with OpenAI
      analyzeImagesWithOpenAI(imageUrls)
    } catch (e: Exception) {
      Log.e("uploadAndAnalyze", "Error: ${e.message}", e)
      throw Exception("Error during upload and analysis: ${e.message}")
    }
  }
}

@Composable
fun AnimatedSparkleEffectOverlay() {
  val sparkles = remember { List(20) { SparkleState() } } // Generate multiple sparkles
  val infiniteTransition = rememberInfiniteTransition()

  // Animate properties for each sparkle
  sparkles.forEach { sparkle -> sparkle.animateSparkle(infiniteTransition) }

  // Overlay to display the sparkles
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(Color.Transparent)
              .zIndex(1f), // Ensure it overlays the dialog
      contentAlignment = Alignment.Center) {
        // Render each sparkle
        sparkles.forEach { sparkle ->
          Box(
              modifier =
                  Modifier.size(sparkle.size.dp)
                      .offset(x = sparkle.xOffset.dp, y = sparkle.yOffset.dp)
                      .graphicsLayer {
                        alpha = sparkle.alpha
                        scaleX = sparkle.scale
                        scaleY = sparkle.scale
                      }
                      .background(Color.Yellow.copy(alpha = 0.8f), shape = CircleShape))
        }
      }
}

data class SparkleState(
    var xOffset: Float = (0..300).random().toFloat(), // Random horizontal offset
    var yOffset: Float = (0..300).random().toFloat(), // Random vertical offset
    var alpha: Float = (50..100).random() / 100f, // Random transparency
    var scale: Float = (50..100).random() / 100f, // Random initial scale
    var size: Float = (10..20).random().toFloat() // Random size for the sparkle
) {
  @Composable
  fun animateSparkle(infiniteTransition: InfiniteTransition) {
    // Animate horizontal offset
    xOffset =
        infiniteTransition
            .animateFloat(
                initialValue = xOffset,
                targetValue = xOffset + (0..50).random(),
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse),
                label = "xOffset")
            .value

    // Animate vertical offset
    yOffset =
        infiniteTransition
            .animateFloat(
                initialValue = yOffset,
                targetValue = yOffset + (0..50).random(),
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse),
                label = "yOffset")
            .value

    // Animate transparency (alpha)
    alpha =
        infiniteTransition
            .animateFloat(
                initialValue = alpha,
                targetValue = 0.2f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse),
                label = "alpha")
            .value

    // Animate scale
    scale =
        infiniteTransition
            .animateFloat(
                initialValue = scale,
                targetValue = 1.2f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse),
                label = "scale")
            .value
  }
}
