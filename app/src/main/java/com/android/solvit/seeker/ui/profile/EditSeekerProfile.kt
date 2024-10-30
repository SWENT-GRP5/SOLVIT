package com.android.solvit.seeker.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions

@Composable
fun EditSeekerProfileScreen(
    viewModel: SeekerProfileViewModel = viewModel(factory = SeekerProfileViewModel.Factory),
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
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
      topBar = {
        TopAppBar(
            backgroundColor = Color.White,
            title = { Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Bio-data", color = Color.Black, fontWeight = FontWeight.Bold)
            } },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
              }
            })
      }) { padding ->
      Column(
          modifier = Modifier
              .fillMaxWidth()
              .padding(top = 32.dp)
              .verticalScroll(rememberScrollState()),
          horizontalAlignment = Alignment.CenterHorizontally, // Align everything to the center
      ) {
          Image(
              painter = painterResource(id = R.drawable.empty_profile_img), // Replace with actual profile image later
              contentDescription = "Profile Picture",
              modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .border(2.dp, Color.Gray, CircleShape)
                  .padding(bottom = 16.dp)
          )

          Text(
              text = fullName,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = Color.Black,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 8.dp)
          )

          Text(
              text = email,
              fontSize = 14.sp,
              color = Color.Gray,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 4.dp)
          )
      }

            Spacer(modifier = Modifier.height(16.dp))

          // Full Name Input
            Text("Name", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
              value = fullName,
              onValueChange = { fullName = it },
              label = { Text("Enter your full name")},
              modifier = Modifier.fillMaxWidth().testTag("profileName"))

          Spacer(modifier = Modifier.height(16.dp))

          // Username Input
          OutlinedTextField(
              value = username,
              onValueChange = { username = it },
              label = { Text("Enter your username") },
              modifier = Modifier.fillMaxWidth().testTag("profileUsername"))

          Spacer(modifier = Modifier.height(16.dp))

          // Email Input
          OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Enter your email") },
              modifier = Modifier.fillMaxWidth().testTag("profileEmail"))

          Spacer(modifier = Modifier.height(16.dp))

          // Phone Number Input
          OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              label = { Text("Enter your phone Number") },
              modifier = Modifier.fillMaxWidth().testTag("profilePhone"))

          Spacer(modifier = Modifier.height(16.dp))

          // Address Input
          OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("Enter your address") },
              modifier = Modifier.fillMaxWidth().testTag("profileAddress"))

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
              modifier = Modifier.fillMaxWidth().height(60.dp),
              colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00C853))) {
                Text("Update Profile", color = Color.White)
              }
        }
      }

