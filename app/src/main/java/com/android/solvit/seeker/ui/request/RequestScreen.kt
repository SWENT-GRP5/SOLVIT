package com.android.solvit.seeker.ui.request

import android.net.Uri
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.seeker.ui.navigation.SeekerBottomNavigationMenu
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_CUSTOMMER
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
  Scaffold(
      modifier = Modifier.padding(16.dp).testTag("requestScreen"),
      bottomBar = {
        if (screenTitle == "Create a new request") {
          SeekerBottomNavigationMenu(
              onTabSelect = { route -> navigationActions.navigateTo(route) },
              tabList = LIST_TOP_LEVEL_DESTINATION_CUSTOMMER,
              selectedItem = navigationActions.currentRoute())
        }
      },
      topBar = {
        TopAppBar(
            title = { Text(screenTitle, Modifier.testTag("screenTitle")) },
            // HJ : Comment this line as these screens have a bottom navigation menu with current
            // version

            navigationIcon = {
              if (screenTitle == "Edit your request") {
                IconButton(
                    onClick = {
                      // HJ : Comment this line as these screens have a bottom navigation menu
                      navigationActions.goBack()
                    },
                    modifier = Modifier.testTag("goBackButton")) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                          contentDescription = "Back")
                    }
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
                  locationQuery,
                  onLocationQueryChange,
                  showDropdownLocation,
                  onShowDropdownLocationChange,
                  locationSuggestions,
                  onLocationSelected,
                  selectedRequest?.location)
              DueDateInput(dueDate, onDueDateChange)
              ImagePicker(selectedImageUri, imageUrl, onImageSelected)
              Button(
                  onClick = onSubmit,
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(start = 80.dp, end = 80.dp)
                          .height(40.dp)
                          .testTag("requestSubmit"),
                  shape = RoundedCornerShape(25.dp),
                  enabled = title.isNotBlank() && description.isNotBlank() && dueDate.isNotBlank(),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Color(0xFFCA97FC),
                          disabledContainerColor = Color(0xFFDECBFC),
                          contentColor = Color.Black,
                          disabledContentColor = Color.Gray)) {
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
