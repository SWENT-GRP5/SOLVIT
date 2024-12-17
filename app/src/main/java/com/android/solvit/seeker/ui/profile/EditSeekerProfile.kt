package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun EditSeekerProfileScreen(
    viewModel: SeekerProfileViewModel = viewModel(factory = SeekerProfileViewModel.Factory),
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  val horizontalPadding = if (screenWidth < 360.dp) 8.dp else 16.dp
  val verticalSpacing = if (screenHeight < 640.dp) 8.dp else 16.dp

  val user by authViewModel.user.collectAsStateWithLifecycle()
  val userProfile by viewModel.seekerProfile.collectAsStateWithLifecycle()
  user?.let { viewModel.getUserProfile(it.uid) }

  var fullName by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }

  LaunchedEffect(userProfile) {
    fullName = userProfile.name
    username = userProfile.username
    email = userProfile.email
    phone = userProfile.phone
    address = userProfile.address
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
                    .padding(padding)
                    .padding(top = 32.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Profile Picture
              Image(
                  painter = painterResource(id = R.drawable.empty_profile_img),
                  contentDescription = "Profile Picture",
                  modifier =
                      Modifier.size(if (screenWidth < 360.dp) 60.dp else 74.dp)
                          .clip(CircleShape)
                          .border(2.dp, colorScheme.primaryContainer, CircleShape))

              Spacer(modifier = Modifier.height(verticalSpacing))

              // Full Name Text
              Text(
                  text = fullName,
                  modifier = Modifier.padding(top = 8.dp),
                  style =
                      Typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold,
                          fontSize = if (screenWidth < 360.dp) 18.sp else 20.sp,
                          color = colorScheme.onBackground,
                          textAlign = TextAlign.Center,
                      ))

              // Email Text
              Text(
                  text = email,
                  modifier = Modifier.padding(top = 4.dp),
                  style =
                      Typography.bodyLarge.copy(
                          fontSize = if (screenWidth < 360.dp) 12.sp else 14.sp,
                          color = colorScheme.onSurfaceVariant,
                          textAlign = TextAlign.Center,
                      ))

              Spacer(modifier = Modifier.height(verticalSpacing))

              // Full Name Input
              OutlinedTextField(
                  value = fullName,
                  onValueChange = { fullName = it },
                  label = { Text("Enter your full name", style = Typography.bodyLarge) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("profileName")
                          .padding(horizontal = horizontalPadding))

              Spacer(modifier = Modifier.height(verticalSpacing))

              // Username Input
              OutlinedTextField(
                  value = username,
                  onValueChange = { username = it },
                  label = { Text("Enter your username", style = Typography.bodyLarge) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("profileUsername")
                          .padding(horizontal = horizontalPadding))

              Spacer(modifier = Modifier.height(verticalSpacing))

              // Email Input
              OutlinedTextField(
                  value = email,
                  onValueChange = { email = it },
                  label = { Text("Enter your email", style = Typography.bodyLarge) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("profileEmail")
                          .padding(horizontal = horizontalPadding))

              Spacer(modifier = Modifier.height(verticalSpacing))

              // Country Dropdown and Phone Number
              CountryDropdownMenu(screenWidth)

              Spacer(modifier = Modifier.height(verticalSpacing))

              // Address Input
              OutlinedTextField(
                  value = address,
                  onValueChange = { address = it },
                  label = { Text("Enter your address", style = Typography.bodyLarge) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("profileAddress")
                          .padding(horizontal = horizontalPadding))

              Spacer(modifier = Modifier.height(verticalSpacing))

              // Save Button
              Button(
                  onClick = {
                    userProfile.let {
                      viewModel.updateUserProfile(
                          SeekerProfile(
                              uid = userProfile.uid,
                              name = fullName,
                              username = username,
                              email = email,
                              phone = phone,
                              address = address))
                      authViewModel.setUserName(username)
                    }
                    navigationActions.goBack()
                  },
                  shape = RoundedCornerShape(25.dp),
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(60.dp)
                          .padding(horizontal = horizontalPadding)
                          .background(
                              brush =
                                  Brush.horizontalGradient(
                                      listOf(colorScheme.secondary, colorScheme.secondary)),
                              shape = RoundedCornerShape(25.dp))) {
                    Text(
                        "Update Profile",
                        style = Typography.bodyLarge.copy(color = colorScheme.onPrimary))
                  }
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

@OptIn(ExperimentalMaterial3Api::class)
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
