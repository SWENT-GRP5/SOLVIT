package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  // Collect the user profile from the StateFlow
  val userProfile by viewModel.seekerProfile.collectAsState()
  val scrollState = rememberScrollState()

  // Display the profile information if it's available
  userProfile?.let { profile ->
    Scaffold(
        topBar = {
          Column {
            Text(
                text = "Profile",
                style =
                    TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground),
                modifier =
                    Modifier.padding(start = 16.dp, top = 16.dp)
                        .verticalScroll(scrollState)
                        .testTag("ProfileTitle"), // testTag for the title
            )
            Spacer(modifier = Modifier.height(16.dp))

            TopAppBar(
                modifier = Modifier.testTag("ProfileTopBar"),
                backgroundColor = colorScheme.primary, // Match background color from Figma
                contentPadding = PaddingValues(16.dp)) {
                  // Row layout for profile image, name, email, and edit icon
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween // Space between items
                      ) {
                        // Profile Picture, Name, and Email on the left
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          // Profile Picture
                          Image(
                              painter = painterResource(id = R.drawable.empty_profile_img),
                              contentDescription = "Profile Picture",
                              modifier =
                                  Modifier.size(53.dp) // Set size to 53px
                                      .clip(CircleShape)
                                      .border(2.dp, colorScheme.onPrimary, CircleShape)
                                      .testTag("ProfileImage") // testTag for profile image
                              )
                          Spacer(modifier = Modifier.width(16.dp))

                          // Name and Email
                          Column {
                            Text(
                                text = profile.name,
                                fontWeight = FontWeight.Bold,
                                style =
                                    MaterialTheme.typography.h6.copy(color = colorScheme.onPrimary),
                                modifier = Modifier.testTag("ProfileName") // testTag for name
                                )
                            Text(
                                text = profile.email,
                                style =
                                    MaterialTheme.typography.body2.copy(
                                        color = colorScheme.onPrimary),
                                modifier = Modifier.testTag("ProfileEmail") // testTag for email
                                )
                          }
                        }

                        // Edit icon on the right
                        IconButton(
                            onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) }) {
                              Icon(
                                  Icons.Default.Edit,
                                  contentDescription = "Edit Profile",
                                  tint = colorScheme.onPrimary)
                            }
                      }
                }
          }
        }) {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(16.dp)
                      .testTag("ProfileContent"), // Adding testTag for profile content
              horizontalAlignment = Alignment.CenterHorizontally) {
                LazyColumn(
                    modifier = Modifier.testTag("ProfileOptionsList") // testTag for options list
                    ) {
                      item {
                        ProfileOptionItem(
                            icon = Icons.Default.Person,
                            optionName = "My Account",
                            subtitle = "Make changes to your account",
                            onClick = { /* Handle click */},
                            modifier = Modifier.testTag("MyAccountOption") // testTag for option
                            )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileOptionItem(
                            icon = Icons.Default.ShoppingCart,
                            optionName = "Order History",
                            subtitle = "Manage your requested services and their statuses",
                            onClick = { /* Handle click */},
                            modifier = Modifier.testTag("OrdersOption") // testTag for option
                            )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileOptionItem(
                            icon = Icons.Default.Lock,
                            optionName = "Billing",
                            subtitle = "Manage your billing information",
                            onClick = { /* Handle click */},
                            modifier = Modifier.testTag("BillingOption") // testTag for option
                            )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileOptionItem(
                            icon = Icons.Default.Favorite,
                            optionName = "Preferences",
                            subtitle = "Set your preferences", // Added subtitle
                            onClick = { /* Handle click */},
                            modifier = Modifier.testTag("PreferencesOption") // testTag for option
                            )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileOptionItem(
                            icon = Icons.Default.ExitToApp,
                            optionName = "Log out",
                            subtitle = "Log out of your account",
                            onClick = { /* Handle log out */},
                            modifier = Modifier.testTag("LogoutOption") // testTag for option
                            )
                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileOptionItem(
                            icon = Icons.Default.Notifications,
                            optionName = "Help & Support",
                            onClick = { /* Handle click */},
                            subtitle = "Get help and support",
                            modifier = Modifier.testTag("HelpSupportOption") // testTag for option
                            )
                        ProfileOptionItem(
                            icon = Icons.Default.Settings,
                            optionName = "About App",
                            onClick = { /* Handle click */},
                            subtitle = "Learn more about the app",
                            modifier = Modifier.testTag("AboutAppOption") // testTag for option
                            )
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
fun ProfileOptionItem(
    icon: ImageVector,
    optionName: String,
    subtitle: String, // New parameter for subtitle
    onClick: () -> Unit = {},
    iconColor: Color = colorScheme.onBackground,
    modifier: Modifier = Modifier // Adding modifier for testTag
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable { onClick() }
              .padding(vertical = 12.dp), // Padding for the whole row
      verticalAlignment = Alignment.CenterVertically // Center vertically for icon and text
      ) {
        Icon(
            imageVector = icon,
            contentDescription = optionName,
            tint = iconColor,
            modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text

        Column(modifier = Modifier.weight(1f)) {
          Text(optionName, style = MaterialTheme.typography.body1)
          Text(
              text = subtitle,
              style =
                  TextStyle(
                      fontSize = 12.sp, // Set a smaller font size for the subtitle
                      color =
                          colorScheme.onBackground.copy(
                              alpha = 0.3F)), // Use onBackground color for the subtitle
              modifier = Modifier.padding(4.dp) // Padding for the subtitle
              )
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = iconColor)
      }
}
