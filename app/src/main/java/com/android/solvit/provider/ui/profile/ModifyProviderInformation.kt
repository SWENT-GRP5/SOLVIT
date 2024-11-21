package com.android.solvit.provider.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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

              ModifyInput(provider = provider, navigationActions = navigationActions)
            }
      })
}

@Composable
fun ModifyInput(
    provider: Provider,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory),
    navigationActions: NavigationActions
) {
  val context = LocalContext.current
  var newCompanyName by remember { mutableStateOf(provider.companyName) }
  val okNewCompanyName = newCompanyName.length >= 2 && newCompanyName.isNotBlank()
  var newProfession by remember { mutableStateOf(provider.service) }

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

  var newLanguage by remember { mutableStateOf(provider.languages) }

  val allIsGood = okNewCompanyName && okNewPhoneNumber && okNewLocation

  CustomOutlinedTextField(
      value = newCompanyName,
      onValueChange = { newCompanyName = it },
      label = "Provider Name",
      placeholder = "Enter your new provider name",
      isValueOk = okNewCompanyName,
      leadingIcon = Icons.Default.AccountCircle,
      leadingIconDescription = "Provider Name Icon",
      testTag = "newProviderCompanyNameInputField",
      errorMessage = "Your company name must have at least 2 characters",
      errorTestTag = "providerNameErrorMessage")

  Spacer(modifier = Modifier.height(10.dp))

  ServiceDropdownMenu(selectedService = newProfession, onServiceSelected = { newProfession = it })

  Spacer(modifier = Modifier.height(10.dp))

  CustomOutlinedTextField(
      value = newPhoneNumber,
      onValueChange = { newPhoneNumber = it },
      label = "Phone number",
      placeholder = "Enter your new phone number",
      isValueOk = okNewPhoneNumber,
      leadingIcon = Icons.Default.Phone,
      leadingIconDescription = "Phone Number Icon",
      testTag = "NewPhoneNumberInputField",
      errorMessage = "Your phone number name must have at least 7 characters",
      errorTestTag = "newPhoneNumberErrorMessage")

  Spacer(modifier = Modifier.height(10.dp))

  LocationDropdown(
      locationQuery = newLocation,
      onLocationQueryChange = { locationViewModel.setQuery(it) },
      showDropdownLocation = showDropdown,
      onShowDropdownLocationChange = { showDropdown = it },
      locationSuggestions = locationSuggestions.filterNotNull(),
      onLocationSelected = { selectedLocation = it },
      requestLocation = null,
      backgroundColor = colorScheme.background,
      isValueOk = okNewLocation,
      testTag = "newLocationInputField")

  LanguageDropdownMenu(
      selectedLanguages = newLanguage, onLanguageSelected = { language, isChecked ->
        if (isChecked) {
          newLanguage = newLanguage + language
        } else {
          newLanguage = newLanguage - language
        }
      })

  Spacer(modifier = Modifier.height(10.dp))

  Button(
      onClick = {
        if (allIsGood) {
          provider.companyName = newCompanyName
          provider.service = newProfession
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
                    disabledIndicatorColor = colorScheme.secondary,
                    unfocusedIndicatorColor = colorScheme.secondary,
                    focusedIndicatorColor = colorScheme.secondary,
                    disabledTextColor = colorScheme.onBackground,
                    disabledTrailingIconColor = colorScheme.onBackground,
                    disabledLabelColor = colorScheme.onBackground,
                ),
            modifier = Modifier.fillMaxWidth().menuAnchor())

        // The dropdown menu showing all services
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
        modifier = modifier
            .fillMaxWidth()
            .testTag("languageInputField")
    ) {
        // Champ de texte affichant les langues sélectionnées
        TextField(
            readOnly = true,
            value = selectedLanguages.joinToString(", ") { it.name },
            onValueChange = {},
            label = { Text("Sélectionnez les langues") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.background,
                disabledIndicatorColor = MaterialTheme.colorScheme.secondary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onBackground,
                disabledLabelColor = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        // Menu déroulant personnalisé permettant la sélection multiple
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            languagesList.forEach { language ->
                val isSelected = language in selectedLanguages

                // Utilisation de DropdownMenuItem avec une case à cocher
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {}, // Empêche la fermeture du menu
                    leadingIcon = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { isChecked ->
                                onLanguageSelected(language, isChecked)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
