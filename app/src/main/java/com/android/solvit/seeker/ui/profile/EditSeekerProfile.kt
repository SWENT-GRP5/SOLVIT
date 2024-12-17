package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.ui.request.LocationDropdown
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.utils.EditProfileHeader
import com.android.solvit.shared.model.utils.SaveButton
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.CustomOutlinedTextField
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import com.android.solvit.shared.ui.utils.ValidationRegex

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun EditSeekerProfileScreen(
    viewModel: SeekerProfileViewModel = viewModel(factory = SeekerProfileViewModel.Factory),
    navigationActions: NavigationActions,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose {
      locationViewModel.clear()
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
  }

  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsStateWithLifecycle(
          initialValue = emptyList<Location?>())
  var selectedLocation by remember { mutableStateOf<Location?>(null) }

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  val horizontalPadding = if (screenWidth < 360.dp) 8.dp else 16.dp
  val verticalSpacing = if (screenHeight < 640.dp) 8.dp else 16.dp

  val user by authViewModel.user.collectAsStateWithLifecycle()
  val userProfile by viewModel.seekerProfile.collectAsStateWithLifecycle()
  user?.let { viewModel.getUserProfile(it.uid) }
  var fullName by remember { mutableStateOf(userProfile.name) }

  var username by remember { mutableStateOf(userProfile.username) }
  var email by remember { mutableStateOf(userProfile.email) }

  var phone by remember { mutableStateOf(userProfile.phone) }
  var address by remember { mutableStateOf(userProfile.address) }
  var imageUrl by remember { mutableStateOf(userProfile.imageUrl) }

  var showDropdown by remember { mutableStateOf(false) }

  val okNewName = ValidationRegex.NAME_REGEX.matches(fullName)
  val okNewLocation = address.name.isNotEmpty()
  val okNewPhoneNumber = ValidationRegex.PHONE_REGEX.matches(phone)
  val isUserNameOk = username.isNotBlank() && username.length > 2

  val allIsGood = okNewName && okNewPhoneNumber && okNewLocation && isUserNameOk

  LaunchedEffect(userProfile) {
    fullName = userProfile.name
    imageUrl = userProfile.imageUrl
    username = userProfile.username
    address = userProfile.address
    email = userProfile.email
    phone = userProfile.phone
  }

  Scaffold(
      modifier = Modifier.background(color = colorScheme.background),
      topBar = {
        TopAppBarInbox(
            title = "Bio-data",
            testTagTitle = "notifications_title",
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack,
            leftButtonAction = { navigationActions.goBack() },
            testTagLeft = "goBackButton")
      }) { padding ->
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontalPadding)
                    .padding(top = 32.dp)
                    .padding(16.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly) {
              EditProfileHeader(
                  imageUrl = imageUrl,
                  fullName = fullName,
                  email = email,
                  screenWidth = screenWidth,
                  verticalSpacing = verticalSpacing)

              Spacer(modifier = Modifier.height(10.dp))

              // Full Name Input
              CustomOutlinedTextField(
                  value = fullName,
                  onValueChange = { fullName = it },
                  label = "Name",
                  placeholder = "Enter your full name",
                  isValueOk = okNewName,
                  leadingIcon = Icons.Default.AccountCircle,
                  leadingIconDescription = "Name Icon",
                  testTag = "profileName",
                  errorMessage = "Your name must have at least 2 characters and less than 50",
                  errorTestTag = "nameErrorMessage",
                  maxLines = 2)

              Spacer(modifier = Modifier.height(10.dp))

              // Username Input
              CustomOutlinedTextField(
                  value = username,
                  onValueChange = { username = it },
                  label = "Username",
                  placeholder = "Enter your new username",
                  isValueOk = isUserNameOk,
                  leadingIcon = Icons.Default.Person,
                  leadingIconDescription = "Phone Number Icon",
                  testTag = "profileUsername",
                  errorMessage = "Enter a valid username",
                  errorTestTag = "newPhoneNumberErrorMessage",
                  maxLines = 1)

              Spacer(modifier = Modifier.height(10.dp))

              CustomOutlinedTextField(
                  value = phone,
                  onValueChange = { phone = it },
                  label = "Phone Number",
                  placeholder = "Enter your phone number",
                  isValueOk = okNewPhoneNumber,
                  errorMessage = "Your phone number must be at least 6 digits",
                  leadingIcon = Icons.Default.Phone,
                  leadingIconDescription = "Phone Icon",
                  testTag = "profilePhone",
                  errorTestTag = "phoneNumberErrorSeekerRegistration",
                  keyboardType = KeyboardType.Number)
              // Address Input
              Spacer(modifier = Modifier.height(10.dp))
              LocationDropdown(
                  locationQuery = address!!.name,
                  onLocationQueryChange = { locationViewModel.setQuery(it) },
                  showDropdownLocation = showDropdown,
                  onShowDropdownLocationChange = { showDropdown = it },
                  locationSuggestions = locationSuggestions.filterNotNull(),
                  userLocations = user?.locations ?: emptyList(),
                  onLocationSelected = {
                    selectedLocation = it
                    authViewModel.addUserLocation(it, {}, {})
                  },
                  requestLocation = null,
                  backgroundColor = colorScheme.background,
                  isValueOk = okNewLocation,
                  testTag = "profileAddress")

              Spacer(modifier = Modifier.height(20.dp))

              SaveButton(
                  onClick = {
                    if (allIsGood) {
                      val updatedUser =
                          userProfile.copy(
                              uid = userProfile.uid,
                              name = fullName,
                              username = username,
                              email = email,
                              phone = phone,
                              address = selectedLocation ?: userProfile.address)
                      viewModel.updateUserProfile(updatedUser)
                      authViewModel.setUserName(username)
                      navigationActions.goBack()
                    } else {
                      Toast.makeText(
                              context,
                              "Please fill in all the correct information before modify it",
                              Toast.LENGTH_SHORT)
                          .show()
                    }
                  },
                  allIsGood = allIsGood)

              Text(
                  text =
                      "Don't forget to save your changes by clicking the button before leaving the page!",
                  color = colorScheme.onSurfaceVariant,
                  fontSize = 12.sp,
                  textAlign = TextAlign.Center,
                  style = Typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                  modifier = Modifier.padding(top = 4.dp).fillMaxWidth())
            }
      }
}

// Country Model and Dropdown Menu
data class Country(val name: String, val code: String, val flagResId: Int)

val countries =
    listOf(
        Country("United States", "+1", R.drawable.us_flag),
        Country("Morocco", "+212", R.drawable.maroc_flag),
        Country("France", "+33", R.drawable.france_flag),
        Country("Switzerland", "+41", R.drawable.switzerland_flag))

@Composable
fun CountryDropdownMenu(screenWidth: Dp) {
  var expanded by remember { mutableStateOf(false) }
  var selectedCountry by remember { mutableStateOf(countries[0]) }
  var phoneNumber by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxWidth()) {
    // Country Code Field with Dropdown
    OutlinedTextField(
        value = selectedCountry.code,
        onValueChange = {},
        label = { Text("Country code", style = Typography.bodyLarge) },
        modifier =
            Modifier.fillMaxWidth()
                .testTag("CountryCode")
                .padding(horizontal = if (screenWidth < 360.dp) 8.dp else 16.dp)
                .clickable { expanded = true },
        leadingIcon = {
          Row(
              modifier = Modifier.padding(start = 8.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = selectedCountry.flagResId),
                    contentDescription = "Country Flag",
                    modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
              }
        },
        trailingIcon = {
          Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown Arrow")
        },
        readOnly = true)

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      countries.forEach { country ->
        DropdownMenuItem(
            onClick = {
              selectedCountry = country
              expanded = false
            }) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = country.flagResId),
                    contentDescription = "Country Flag",
                    modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(country.name, style = Typography.bodyLarge)
              }
            }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Phone Number Field
    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { phoneNumber = it },
        label = { Text("Phone number", style = Typography.bodyLarge) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier =
            Modifier.fillMaxWidth()
                .testTag("profilePhone")
                .padding(horizontal = if (screenWidth < 360.dp) 8.dp else 16.dp))
  }
}
