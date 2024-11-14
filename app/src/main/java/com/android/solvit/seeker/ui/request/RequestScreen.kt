package com.android.solvit.seeker.ui.request

import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestScreen(
    navigationActions: NavigationActions,
    screenTitle: String,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    typeQuery: String,
    onTypeQueryChange: (String) -> Unit,
    showDropdownType: Boolean,
    onShowDropdownTypeChange: (Boolean) -> Unit,
    filteredServiceTypes: List<Services>,
    onServiceTypeSelected: (Services) -> Unit,
    locationQuery: String,
    onLocationQueryChange: (String) -> Unit,
    showDropdownLocation: Boolean,
    onShowDropdownLocationChange: (Boolean) -> Unit,
    locationSuggestions: List<Location>,
    onLocationSelected: (Location) -> Unit,
    selectedLocation: Location?,
    selectedRequest: ServiceRequest?,
    requestViewModel: ServiceRequestViewModel,
    dueDate: String,
    onDueDateChange: (String) -> Unit,
    selectedImageUri: Uri?,
    imageUrl: String?,
    onImageSelected: (Uri?) -> Unit,
    onSubmit: () -> Unit,
    submitButtonText: String
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  Scaffold(
      modifier = Modifier.padding(16.dp).testTag("requestScreen"),
      bottomBar = {},
      topBar = {
        TopAppBar(
            colors =
                TopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground,
                ),
            title = { Text(screenTitle, Modifier.testTag("screenTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
              TitleInput(title, onTitleChange)
              ServiceTypeDropdown(
                  typeQuery,
                  onTypeQueryChange,
                  showDropdownType,
                  onShowDropdownTypeChange,
                  filteredServiceTypes,
                  onServiceTypeSelected)
              DescriptionInput(description, onDescriptionChange)
              LocationDropdown(
                  locationQuery = locationQuery,
                  onLocationQueryChange = onLocationQueryChange,
                  showDropdownLocation = showDropdownLocation,
                  onShowDropdownLocationChange = onShowDropdownLocationChange,
                  locationSuggestions = locationSuggestions,
                  onLocationSelected = onLocationSelected,
                  requestLocation = selectedRequest?.location,
                  isValueOk = selectedLocation != null)
              DatePickerFieldToModal(dueDate = dueDate, onDateChange = onDueDateChange)
              ImagePicker(selectedImageUri, imageUrl, onImageSelected)
              Button(
                  onClick = onSubmit,
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(start = 80.dp, end = 80.dp)
                          .height(40.dp)
                          .testTag("requestSubmit"),
                  shape = RoundedCornerShape(25.dp),
                  enabled =
                      title.isNotBlank() &&
                          description.isNotBlank() &&
                          dueDate.isNotBlank() &&
                          selectedLocation != null,
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = colorScheme.primary,
                          disabledContainerColor = colorScheme.primaryContainer,
                          contentColor = colorScheme.onPrimary,
                          disabledContentColor = colorScheme.onPrimaryContainer)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()) {
                          Icon(
                              imageVector = Icons.Default.Done,
                              contentDescription = null,
                              modifier = Modifier.size(24.dp))
                          Spacer(modifier = Modifier.width(8.dp))
                          Text(submitButtonText)
                        }
                  }
              if (selectedRequest != null) {
                DeleteButton(
                    request = selectedRequest,
                    requestViewModel = requestViewModel,
                    navigationActions)
              }
            }
      })
}
