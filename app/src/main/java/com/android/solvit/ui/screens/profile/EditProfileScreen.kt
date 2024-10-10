package com.android.solvit.ui.screens.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.solvit.R
import com.android.solvit.ui.navigation.NavigationActions

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EditProfileScreen(viewModel: ProfileViewModel, navigationActions: NavigationActions) {
  // Collect the user profile from the StateFlow
  val userProfile by viewModel.userProfile.collectAsState()

  // Assuming a single user profile (adjust as needed)
  val currentProfile = userProfile.firstOrNull()

  // Initialize fields with existing user data
  var fullName by remember { mutableStateOf(currentProfile?.name ?: "") }
  var username by remember { mutableStateOf(currentProfile?.username ?: "") }
  var email by remember { mutableStateOf(currentProfile?.email ?: "") }
  var phone by remember { mutableStateOf(currentProfile?.phone ?: "") }
  var address by remember { mutableStateOf(currentProfile?.address ?: "") }

  Scaffold(
      modifier = Modifier.testTag("editProfileScreen"),
      topBar = {
        TopAppBar(
            backgroundColor = Color(0xFF002366), // Set blue background
            title = { Text("Edit Profile", color = Color.White) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
              }
            })
      }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
          Spacer(modifier = Modifier.height(16.dp))

          // Profile Picture
          Image(
              painter =
                  painterResource(
                      id = R.drawable.empty_profile_img), // Replace with actual profile image later
              contentDescription = "Profile Picture",
              modifier =
                  Modifier.size(100.dp)
                      .clip(CircleShape)
                      .border(2.dp, Color.Gray, CircleShape)
                      .padding(bottom = 16.dp)
                      .testTag("profilePicture"))
          TextButton(onClick = { /* Handle change picture */}) { Text("Change Picture") }

          Spacer(modifier = Modifier.height(16.dp))

          // Full Name Input
          OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Full Name") },
              modifier = Modifier.fillMaxWidth().testTag("fullNameInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Username Input
          OutlinedTextField(
              value = username,
              onValueChange = { username = it },
              label = { Text("Username") },
              modifier = Modifier.fillMaxWidth().testTag("usernameInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Email Input
          OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email") },
              modifier = Modifier.fillMaxWidth().testTag("emailInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Phone Number Input
          OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("Phone Number") },
              modifier = Modifier.fillMaxWidth().testTag("phoneInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Address Input
          OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("Address") },
              modifier = Modifier.fillMaxWidth().testTag("addressInput"))

          Spacer(modifier = Modifier.height(16.dp))

          // Save Button
          Button(
              onClick = {
                currentProfile?.let { profile ->
                  // Update the profile with the new values
                  viewModel.updateUserProfile(
                      UserProfile(
                          uid = profile.uid, // Use the existing UID
                          name = fullName,
                          email = email,
                          phone = phone))
                }
                navigationActions.goBack()
              },
              modifier = Modifier.fillMaxWidth().testTag("saveButton"),
              colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)) {
                Text("Save", color = Color.White)
              }
        }
      }
}
