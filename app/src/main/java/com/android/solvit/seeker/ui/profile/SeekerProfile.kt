package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

/*
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, navigationActions: NavigationActions) {
    // Collect the user profile from the StateFlow
    val userProfile by viewModel.userProfile.collectAsState()

    // Display the profile information if it's available
    if (userProfile.isNotEmpty()) {
        val profile = userProfile[0]  // Assuming only one profile in the list for simplicity

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.empty_profile_img),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 16.dp)
            )
            // Header Section with Title and Edit Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                }
            }

            // Display Name
            ProfileField(label = "Name", value = profile.name)

            // Spacer between fields
            Spacer(modifier = Modifier.height(16.dp))

            // Display Email with Icon
            ProfileFieldWithIcon(
                icon = Icons.Filled.Email,
                label = "Email",
                value = profile.email
            )

            // Spacer between fields
            Spacer(modifier = Modifier.height(16.dp))

            // Display Phone with Icon
            ProfileFieldWithIcon(
                icon = Icons.Filled.Phone,
                label = "Phone",
                value = profile.phone
            )
        }
    } else {
        // Fallback UI when the profile data is not available
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No profile data available", style = MaterialTheme.typography.body1)
        }
    }
}

// Reusable composable for displaying a profile field
@Composable
fun ProfileField(label: String, value: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ProfileFieldWithIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontWeight = FontWeight.SemiBold)
            Text(
                text = value,
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.body1
            )
        }
    }
}
*/
/*
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, navigationActions: NavigationActions) {
    // Collect the user profile from the StateFlow
    val userProfile by viewModel.userProfile.collectAsState()

    // Display the profile information if it's available
    userProfile.firstOrNull()?.let { profile ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = { navigationActions.goBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Handle notification click */ }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                Image(
                    painter = painterResource(id = R.drawable.empty_profile_img), // Replace with actual profile image later
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(bottom = 16.dp)
                )

                // Edit Profile Button
                Button(
                    onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sections
                LazyColumn {
                    item {
                        SectionTitle("Mimi Headline")
                        ProfileOptionItem("Popular")
                        ProfileOptionItem("Trending")
                        ProfileOptionItem("Today")
                    }
                    item {
                        SectionTitle("Content")
                        ProfileOptionItem("Favourite")
                        ProfileOptionItem("Download")
                    }
                    item {
                        SectionTitle("Preferences")
                        ProfileOptionItem("Language")
                        ProfileOptionItem("Darkmode")
                        ProfileOptionItem("Only Download via Wifi")
                    }
                }
            }
        }
    } ?: run {
        // Fallback when no profile data is available
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No profile data available")
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileOptionItem(optionName: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(optionName)
        Icon(Icons.Default.ArrowForward, contentDescription = null)
    }
}
*/

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SeekerProfileScreen(viewModel: SeekerProfileViewModel, navigationActions: NavigationActions) {
  // Collect the user profile from the StateFlow
  val userProfile by viewModel.seekerProfile.collectAsState()

  // Display the profile information if it's available
  userProfile.let { profile ->
    Scaffold(
        topBar = {
          TopAppBar(
              backgroundColor = Color.Blue, // Set blue background
              title = { Text("Profile", color = Color.White) },
              navigationIcon = {
                IconButton(onClick = { navigationActions.goBack() }) {
                  Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
              },
              actions = {
                IconButton(onClick = { /* Handle notification click */}) {
                  Icon(
                      Icons.Default.Notifications,
                      contentDescription = "Notifications",
                      tint = Color.White)
                }
              })
        }) {
          Column(
              modifier = Modifier.fillMaxSize().padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Picture
                Image(
                    painter =
                        painterResource(
                            id =
                                R.drawable
                                    .empty_profile_img), // Replace with actual profile image later
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier.size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                            .padding(bottom = 16.dp))

                // Edit Profile Button
                Button(
                    onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)) {
                      Text("Edit Profile", color = Color.White)
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Sections
                LazyColumn {
                  item {
                    SectionTitle("Headline")
                    ProfileOptionItem("Popular")
                  }
                  item {
                    SectionTitle("Content")
                    ProfileOptionItem("Favourite")
                    ProfileOptionItem("Billing") // Billing Section Added
                  }
                  item {
                    SectionTitle("Preferences")
                    ProfileOptionItem("Language")
                    ProfileOptionItem("Darkmode")
                  }
                }
              }
        }
  }
      ?: run {
        // Fallback when no profile data is available
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = "No profile data available")
        }
      }
}

@Composable
fun SectionTitle(title: String) {
  // Use Box to wrap the Text and apply a background color to the section title
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color(0xFFF6F6F6)) // Set background color
              .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1.copy(color = Color.Black), // Set text color
            fontWeight = FontWeight.Bold)
      }
}

@Composable
fun ProfileOptionItem(optionName: String, onClick: () -> Unit = {}) {
  Row(
      modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween) {
        Text(optionName)
        Icon(Icons.Default.ArrowForward, contentDescription = null)
      }
}
