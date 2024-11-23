package com.android.solvit.provider.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.request.LocationDropdown
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.authentication.CustomOutlinedTextField
import com.android.solvit.shared.ui.authentication.GoBackButton
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint(
    "SourceLockedOrientationActivity",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "SuspiciousIndentation")
@Composable
fun ModifyProviderInformationScreen(
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Modify your profile information") },
            navigationIcon = { GoBackButton(navigationActions) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background))
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .background(colorScheme.background)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              val user = authViewModel.user.collectAsState()
              val userId = user.value?.uid ?: "-1"
              val provider =
                  listProviderViewModel.providersList.collectAsState().value.find {
                    it.uid == userId
                  } ?: return@Column

              ModifyInput(
                  provider = provider,
                  locationViewModel,
                  listProviderViewModel,
                  authViewModel,
                  navigationActions = navigationActions)
            }
      })
}

@Composable
fun ModifyInput(
    provider: Provider,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    navigationActions: NavigationActions
) {
  val context = LocalContext.current

  var newName by remember { mutableStateOf(provider.name) }
  val okNewName = newName.length >= 2 && newName.isNotBlank()

  var newCompanyName by remember { mutableStateOf(provider.companyName) }
  val okNewCompanyName = newCompanyName.length >= 2 && newCompanyName.isNotBlank()

  var newService by remember { mutableStateOf(provider.service) }

  var newPhoneNumber by remember { mutableStateOf(provider.phone) }
  val okNewPhoneNumber =
      newPhoneNumber.isNotBlank() &&
          newPhoneNumber.all { it.isDigit() || it == '+' } &&
          newPhoneNumber.length > 6

  val newLocation by remember { mutableStateOf(provider.location.name) }

  var showDropdown by remember { mutableStateOf(false) }

  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())
  var selectedLocation by remember { mutableStateOf<Location?>(provider.location) }
  val okNewLocation = selectedLocation != null

  val user by authViewModel.user.collectAsState()

  var newLanguage by remember { mutableStateOf(provider.languages) }

  val allIsGood = okNewCompanyName && okNewPhoneNumber && okNewLocation

  CustomOutlinedTextField(
      value = newName,
      onValueChange = { newName = it },
      label = "Name",
      placeholder = "Enter your new name",
      isValueOk = okNewName,
      leadingIcon = Icons.Default.AccountCircle,
      leadingIconDescription = "Name Icon",
      testTag = "newNameInputField",
      errorMessage = "Your provider name must have at least 2 characters",
      errorTestTag = "nameErrorMessage",
      maxLines = 2)

  Spacer(modifier = Modifier.height(10.dp))

  CustomOutlinedTextField(
      value = newCompanyName,
      onValueChange = { newCompanyName = it },
      label = "Company Name",
      placeholder = "Enter your new Company name",
      isValueOk = okNewCompanyName,
      leadingIcon = Icons.Default.AccountCircle,
      leadingIconDescription = "Company Name Icon",
      testTag = "newCompanyNameInputField",
      errorMessage = "Your company name must have at least 2 characters",
      errorTestTag = "companyNameErrorMessage",
      maxLines = 2)

  Spacer(modifier = Modifier.height(10.dp))

  ServiceDropdownMenu(selectedService = newService, onServiceSelected = { newService = it })

  Spacer(modifier = Modifier.height(10.dp))

  CustomOutlinedTextField(
      value = newPhoneNumber,
      onValueChange = { newPhoneNumber = it },
      label = "Phone number",
      placeholder = "Enter your new phone number",
      isValueOk = okNewPhoneNumber,
      leadingIcon = Icons.Default.Phone,
      leadingIconDescription = "Phone Number Icon",
      testTag = "newPhoneNumberInputField",
      errorMessage = "Your phone number name must have at least 7 characters",
      errorTestTag = "newPhoneNumberErrorMessage")

  Spacer(modifier = Modifier.height(10.dp))

  LocationDropdown(
      locationQuery = newLocation,
      onLocationQueryChange = { locationViewModel.setQuery(it) },
      showDropdownLocation = showDropdown,
      onShowDropdownLocationChange = { showDropdown = it },
      locationSuggestions = locationSuggestions.filterNotNull(),
      userLocations = user?.locations ?: emptyList(),
      onLocationSelected = { selectedLocation = it },
      requestLocation = null,
      backgroundColor = colorScheme.background,
      isValueOk = okNewLocation,
      testTag = "newLocationInputField")

  Spacer(modifier = Modifier.height(10.dp))

  LanguageDropdownMenu(
      selectedLanguages = newLanguage,
      onLanguageSelected = { language, isChecked ->
        newLanguage =
            if (isChecked) {
              newLanguage + language
            } else {
              newLanguage - language
            }
      })

  Spacer(modifier = Modifier.height(10.dp))

  Button(
      onClick = {
        if (allIsGood) {
          provider.name = newName
          provider.companyName = newCompanyName
          provider.service = newService
          provider.phone = newPhoneNumber
          provider.location = selectedLocation ?: provider.location
          provider.languages = newLanguage

          listProviderViewModel.updateProvider(provider = provider)

          navigationActions.goBack()
        } else {
          Toast.makeText(
                  context,
                  "Please fill in all the correct information before modify it",
                  Toast.LENGTH_SHORT)
              .show()
        }
      },
      modifier =
          Modifier.fillMaxWidth()
              .height(50.dp)
              .background(
                  brush =
                      if (allIsGood) {
                        Brush.horizontalGradient(
                            colors = listOf(colorScheme.primary, colorScheme.secondary))
                      } else {
                        Brush.horizontalGradient(
                            colors =
                                listOf(colorScheme.onSurfaceVariant, colorScheme.onSurfaceVariant))
                      },
                  shape =
                      RoundedCornerShape(
                          25.dp,
                      )),
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
        Text(
            "Save !", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }

  Spacer(modifier = Modifier.height(3.dp))

  Text(
      text = "Don't forget to save your changes by clicking the button before leaving the page!",
      color = colorScheme.onSurfaceVariant,
      fontSize = 12.sp,
      textAlign = TextAlign.Center,
      style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
      modifier = Modifier.padding(top = 4.dp).fillMaxWidth())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDropdownMenu(selectedService: Services, onServiceSelected: (Services) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  val servicesList = Services.entries

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = !expanded },
      modifier = Modifier.fillMaxWidth().testTag("newServiceInputField")) {
        // The read-only text field displaying the selected service
        TextField(
            readOnly = true,
            value = Services.format(selectedService),
            onValueChange = {},
            label = { Text("Select Service") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors =
                TextFieldDefaults.textFieldColors(
                    // Set the background color of the TextField to match the screen or be white
                    containerColor = colorScheme.background,
                    unfocusedIndicatorColor = colorScheme.secondary,
                    focusedIndicatorColor = colorScheme.secondary,
                ),
            modifier =
                Modifier.fillMaxWidth()
                    .menuAnchor()
                    .border(1.dp, colorScheme.secondary, RoundedCornerShape(8.dp)))

        // The dropdown menu showing all services
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier =
                Modifier.background(colorScheme.background)
                    .border(1.dp, colorScheme.onBackground)) {
              servicesList.forEach { service ->
                DropdownMenuItem(
                    text = { Text(Services.format(service)) },
                    onClick = {
                      onServiceSelected(service)
                      expanded = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
              }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdownMenu(
    selectedLanguages: List<Language>,
    onLanguageSelected: (Language, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  val languagesList = Language.entries

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = !expanded },
      modifier = modifier.fillMaxWidth().testTag("languageInputField")) {
        TextField(
            readOnly = true,
            value = selectedLanguages.joinToString(", ") { it.name },
            onValueChange = {},
            label = { Text("Select languages") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors =
                TextFieldDefaults.textFieldColors(
                    containerColor = colorScheme.background,
                    unfocusedIndicatorColor = colorScheme.secondary,
                    focusedIndicatorColor = colorScheme.secondary,
                ),
            modifier =
                Modifier.menuAnchor()
                    .fillMaxWidth()
                    .border(1.dp, colorScheme.secondary, RoundedCornerShape(8.dp)))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier =
                Modifier.background(colorScheme.background)
                    .border(1.dp, colorScheme.onBackground)) {
              languagesList.forEach { language ->
                val isSelected = language in selectedLanguages

                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {},
                    leadingIcon = {
                      Checkbox(
                          checked = isSelected,
                          onCheckedChange = { isChecked ->
                            onLanguageSelected(language, isChecked)
                          })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
              }
            }
      }
}
