package com.android.solvit.ui.requests

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.android.solvit.model.map.Location
import com.android.solvit.model.requests.ServiceRequest
import com.android.solvit.model.requests.ServiceRequestType
import com.android.solvit.model.requests.ServiceRequestViewModel
import java.util.Locale

@Composable
fun TitleInput(title: String, onTitleChange: (String) -> Unit) {
  OutlinedTextField(
      value = title,
      onValueChange = onTitleChange,
      label = { Text("Title") },
      placeholder = { Text("Name your Request") },
      modifier = Modifier.fillMaxWidth().testTag("inputRequestTitle"),
      colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent))
}

@Composable
fun DescriptionInput(description: String, onDescriptionChange: (String) -> Unit) {
  OutlinedTextField(
      value = description,
      onValueChange = onDescriptionChange,
      label = { Text("Description") },
      placeholder = { Text("Describe your request") },
      modifier = Modifier.fillMaxWidth().height(150.dp).testTag("inputRequestDescription"),
      colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent))
}

@Composable
fun ServiceTypeDropdown(
    typeQuery: String,
    onTypeQueryChange: (String) -> Unit,
    showDropdownType: Boolean,
    onShowDropdownTypeChange: (Boolean) -> Unit,
    filteredServiceTypes: List<ServiceRequestType>,
    onServiceTypeSelected: (ServiceRequestType) -> Unit
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
        modifier =
            Modifier.fillMaxWidth().testTag("inputServiceType").onFocusChanged { focusState ->
              if (!focusState.isFocused) onShowDropdownTypeChange(false)
            },
        singleLine = true)

    DropdownMenu(
        expanded = showDropdownType,
        onDismissRequest = { onShowDropdownTypeChange(false) },
        properties = PopupProperties(focusable = false),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(max = 200.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(8.dp))
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
                      ServiceRequestType.OTHER.name
                          .lowercase(Locale.getDefault())
                          .replaceFirstChar { it.uppercase(Locale.getDefault()) })
                },
                onClick = {
                  onServiceTypeSelected(ServiceRequestType.OTHER)
                  onShowDropdownTypeChange(false)
                })
            HorizontalDivider()
          }
        }
  }
}

@Composable
fun LocationDropdown(
    locationQuery: String,
    onLocationQueryChange: (String) -> Unit,
    showDropdownLocation: Boolean,
    onShowDropdownLocationChange: (Boolean) -> Unit,
    locationSuggestions: List<Location>,
    onLocationSelected: (Location) -> Unit,
    requestLocation: Location?
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = locationQuery,
        onValueChange = {
          onLocationQueryChange(it)
          onShowDropdownLocationChange(true)
        },
        label = { Text("Address") },
        placeholder = { requestLocation?.name?.let { Text(it) } ?: Text("Enter your address") },
        modifier = Modifier.fillMaxWidth().testTag("inputRequestAddress"),
        singleLine = true)

    DropdownMenu(
        expanded = showDropdownLocation && locationSuggestions.isNotEmpty(),
        onDismissRequest = { onShowDropdownLocationChange(false) },
        properties = PopupProperties(focusable = false),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(max = 200.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(8.dp))
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
                  onLocationSelected(location)
                  onShowDropdownLocationChange(false)
                })
            HorizontalDivider()
          }
        }
  }
}

@Composable
fun DueDateInput(dueDate: String, onDueDateChange: (String) -> Unit) {
  OutlinedTextField(
      value = dueDate,
      onValueChange = onDueDateChange,
      label = { Text("What is the deadline for this request?") },
      placeholder = { Text("--/--/----") },
      modifier = Modifier.fillMaxWidth().testTag("inputRequestDate"),
      colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent))
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

  // Image display button
  Button(
      onClick = { imagePickerLauncher.launch("image/*") },
      modifier =
          Modifier.fillMaxWidth()
              .height(150.dp)
              .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)) // Added grey border
              .background(Color.Transparent)
              .testTag("imagePickerButton"),
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      content = {
        if (selectedImageUri == null && imageUrl == null) {
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
        } else {
          AsyncImage(
              model =
                  selectedImageUri?.toString()
                      ?: imageUrl, // Show selected image URI or fallback URL
              contentDescription = "Service Request Image",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
          )
        }
      })
}

@Composable
fun DeleteButton(request: ServiceRequest, requestViewModel: ServiceRequestViewModel) {
  Button(
      modifier = Modifier.testTag("deleteRequestButton").fillMaxWidth().height(40.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      onClick = {
        try {
          requestViewModel.deleteServiceRequestById(request.uid)
          // navigationActions.navigateTo(Screen.OVERVIEW)
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
                  tint = Color.Red,
                  modifier = Modifier.padding(end = 8.dp))
              Text("Delete", color = Color.Red)
            }
      }
}
