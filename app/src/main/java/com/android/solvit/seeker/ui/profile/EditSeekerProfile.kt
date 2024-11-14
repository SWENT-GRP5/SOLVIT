package com.android.solvit.seeker.ui.profile

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
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
  // Collect the user profile from the StateFlow
  val user by authViewModel.user.collectAsState()
  val userProfile by viewModel.seekerProfile.collectAsState()
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
      backgroundColor = colorScheme.background,
      topBar = {
        TopAppBar(
            backgroundColor = colorScheme.background,
            title = {
              Box(
                  modifier = Modifier.fillMaxWidth().testTag("goBackButton"),
                  contentAlignment = Alignment.Center) {
                    Text("Bio-data", color = colorScheme.onBackground, fontWeight = FontWeight.Bold)
                  }
            },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colorScheme.onBackground)
              }
            },
            // Set actions parameter to an empty Box to balance the alignment caused by
            // navigationIcon
            actions = { Box(modifier = Modifier.size(48.dp)) })
      }) { padding -> // Apply padding from Scaffold
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(padding) // Use Scaffold's padding here
                    .padding(top = 32.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                  painter = painterResource(id = R.drawable.empty_profile_img),
                  contentDescription = "Profile Picture",
                  modifier =
                      Modifier.size(74.dp)
                          .clip(CircleShape)
                          .border(2.dp, colorScheme.primaryContainer, CircleShape))

              Text(
                  text = fullName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp,
                  color = colorScheme.onBackground,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(top = 8.dp))

              Text(
                  text = email,
                  fontSize = 14.sp,
                  color = colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(top = 4.dp))

              Spacer(modifier = Modifier.height(16.dp)) // Move spacer inside the column

              // Full Name Input

              Spacer(modifier = Modifier.height(8.dp))
              OutlinedTextField(
                  value = fullName,
                  onValueChange = { fullName = it },
                  label = { Text("Enter your full name", color = colorScheme.onBackground) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("profileName")
                          .padding(horizontal = 16.dp), // Add padding to avoid edge clipping
                  colors =
                      TextFieldDefaults.outlinedTextFieldColors(
                          focusedBorderColor = colorScheme.secondary,
                          unfocusedBorderColor = colorScheme.onSurfaceVariant))

              Spacer(modifier = Modifier.height(16.dp))

              // Username Input
              OutlinedTextField(
                  value = username,
                  onValueChange = { username = it },
                  label = { Text("Enter your username", color = colorScheme.onBackground) },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("profileUsername")
                          .padding(horizontal = 16.dp),
                  colors =
                      TextFieldDefaults.outlinedTextFieldColors(
                          focusedBorderColor = colorScheme.secondary,
                          unfocusedBorderColor = colorScheme.onSurfaceVariant))

              Spacer(modifier = Modifier.height(16.dp))

              // Email Input
              OutlinedTextField(
                  value = email,
                  onValueChange = { email = it },
                  label = { Text("Enter your email", color = colorScheme.onBackground) },
                  modifier =
                      Modifier.fillMaxWidth().testTag("profileEmail").padding(horizontal = 16.dp),
                  colors =
                      TextFieldDefaults.outlinedTextFieldColors(
                          focusedBorderColor = colorScheme.secondary,
                          unfocusedBorderColor = colorScheme.onSurfaceVariant))

              Spacer(modifier = Modifier.height(16.dp))

              CountryDropdownMenu()

              Spacer(modifier = Modifier.height(16.dp))

              Spacer(modifier = Modifier.height(16.dp))

              // Address Input
              OutlinedTextField(
                  value = address,
                  onValueChange = { address = it },
                  label = { Text("Enter your address", color = colorScheme.onBackground) },
                  modifier =
                      Modifier.fillMaxWidth().testTag("profileAddress").padding(horizontal = 16.dp),
                  colors =
                      TextFieldDefaults.outlinedTextFieldColors(
                          focusedBorderColor = colorScheme.secondary,
                          unfocusedBorderColor = colorScheme.onSurfaceVariant))

              Spacer(modifier = Modifier.height(16.dp))

              // Save Button
              Button(
                  onClick = {
                    userProfile.let { profile ->
                      // Update the profile with the new values
                      viewModel.updateUserProfile(
                          SeekerProfile(
                              uid = userProfile.uid, // Use the existing UID
                              name = fullName,
                              username = username,
                              email = email,
                              phone = phone,
                              address = address))
                    }
                    navigationActions.goBack()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                  shape = RoundedCornerShape(25.dp),
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(
                              brush =
                                  Brush.horizontalGradient(
                                      listOf(colorScheme.secondary, colorScheme.secondary)),
                              shape = RoundedCornerShape(25.dp),
                              // add padding
                          )
                          .height(60.dp)
                          .padding(horizontal = 32.dp), // Add padding to button
              ) {
                Text("Update Profile", color = colorScheme.onPrimary)
              }
            }
      }
}

data class Country(val name: String, val code: String, val flagResId: Int)

val countries =
    listOf(
        Country("United States", "+1", R.drawable.us_flag),
        Country("Morocco", "+212", R.drawable.maroc_flag),
        Country("France", "+33", R.drawable.france_flag),
        Country("Switzerland", "+41", R.drawable.switzerland_flag))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDropdownMenu() {
  var expanded by remember { mutableStateOf(false) }
  var selectedCountry by remember { mutableStateOf(countries[0]) }
  var phoneNumber by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = selectedCountry.code,
        onValueChange = { /* Country code is static, don't update */},
        label = { Text("Country code", color = colorScheme.onBackground) },
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("CountryCode").clickable {
              expanded = true
            }, // Open dropdown on click
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        trailingIcon = {
          Icon(
              Icons.Default.ArrowDropDown,
              contentDescription = "Dropdown Icon",
              tint = colorScheme.onBackground)
        },
        colors =
            TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = colorScheme.secondary,
                unfocusedBorderColor = colorScheme.onSurfaceVariant))

    // Dropdown Menu
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth()) {
          countries.forEach { country ->
            DropdownMenuItem(
                onClick = {
                  selectedCountry = country
                  expanded = false
                }) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = country.flagResId),
                        contentDescription = country.name,
                        modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(country.name) // Display the country name for clarity
                  }
                }
          }
        }

    Spacer(modifier = Modifier.height(16.dp))

    // Input field for the phone number
    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { phoneNumber = it },
        label = { Text("Enter your phone number", color = colorScheme.onBackground) },
        modifier = Modifier.fillMaxWidth().testTag("profilePhone").padding(horizontal = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        colors =
            TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = colorScheme.secondary,
                unfocusedBorderColor = colorScheme.onSurfaceVariant))
  }
}
