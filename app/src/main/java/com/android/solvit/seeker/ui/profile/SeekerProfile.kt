package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SeekerProfileScreen(
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
  var email by remember { mutableStateOf("") }

  LaunchedEffect(userProfile) {
    fullName = userProfile.name
    email = userProfile.email
  }

  // State for logout dialog
  var showLogoutDialog by remember { mutableStateOf(false) }

  // Display the profile information if it's available
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Profile",
                  modifier = Modifier.testTag("ProfileTitle"),
                  fontWeight = FontWeight.Bold,
              )
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("BackButton")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                  }
            },
            actions = {
              IconButton(
                  onClick = { showLogoutDialog = true },
                  modifier = Modifier.testTag("LogoutButton")) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log out")
                  }
            },
            modifier = Modifier.testTag("ProfileTopBar"),
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground))
      },
      bottomBar = {},
      containerColor = Color.Transparent,
  ) { paddingValues ->
    LazyColumn(
        modifier =
            Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).testTag("ProfileContent"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
          item {
            // Profile Info Card with Edit Button
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ProfileInfoCard"),
                colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Image(
                              painter = painterResource(id = R.drawable.empty_profile_img),
                              contentDescription = "Profile Picture",
                              modifier =
                                  Modifier.size(50.dp)
                                      .clip(CircleShape)
                                      .border(4.dp, colorScheme.onPrimary, CircleShape)
                                      .testTag("ProfileImage"))
                          Spacer(modifier = Modifier.width(8.dp))
                          Column {
                            Text(
                                text = fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = colorScheme.onPrimary,
                                modifier = Modifier.testTag("ProfileName"))
                            Text(
                                text = email,
                                fontSize = 14.sp,
                                color = colorScheme.onPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.testTag("ProfileEmail"))
                          }
                        }
                        IconButton(
                            onClick = { navigationActions.navigateTo(Screen.EDIT_SEEKER_PROFILE) },
                            modifier = Modifier.testTag("EditProfileButton")) {
                              Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                            }
                      }
                }
          }

          item {
            // First Group of Profile Options
            Card(
                modifier = Modifier.fillMaxWidth().testTag("FirstGroupCard"),
                colors = CardDefaults.cardColors(containerColor = colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                  Column(
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.padding(16.dp)) {
                        ProfileOptionItem(
                            icon = Icons.Default.Person,
                            optionName = "My Account",
                            subtitle = "Make changes to your account",
                            onClick = {
                              Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("MyAccountOption"))
                        ProfileOptionItem(
                            icon = Icons.Default.ShoppingCart,
                            optionName = "Order History",
                            subtitle = "Manage your requested services and their statuses",
                            onClick = {
                              Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("OrdersOption"))
                        ProfileOptionItem(
                            icon = Icons.Default.Lock,
                            optionName = "Privacy Settings",
                            subtitle = "Manage your privacy settings",
                            onClick = {
                              Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("PrivacySettingsOption"))
                        ProfileOptionItem(
                            icon = Icons.Default.Lock,
                            optionName = "Billing",
                            subtitle = "Manage your billing information",
                            onClick = {
                              Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("BillingOption"))
                        ProfileOptionItem(
                            icon = Icons.Default.Favorite,
                            optionName = "Preferences",
                            subtitle = "Set your preferences",
                            onClick = { navigationActions.navigateTo(Screen.EDIT_PREFERENCES) },
                            modifier = Modifier.testTag("PreferencesOption"))
                      }
                }
          }

          item {
            // Text "More Options" to indicate more options, aligned to the left. Bold text.
            Text(
                text = "More Options",
                style =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(start = 16.dp).testTag("MoreOptionsText"))
          }

          item {
            // Second Group of Profile Options
            Card(
                modifier = Modifier.fillMaxWidth().testTag("SecondGroupCard"),
                colors = CardDefaults.cardColors(containerColor = colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                  Column(
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier.padding(16.dp)) {
                        ProfileOptionItem(
                            icon = Icons.Default.Notifications,
                            optionName = "Help & Support",
                            onClick = {
                              Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("HelpSupportOption"))
                        ProfileOptionItem(
                            icon = Icons.Default.Settings,
                            optionName = "About App",
                            onClick = {
                              Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("AboutAppOption"))
                      }
                }
          }
        }
  }

  // Logout Confirmation Dialog
  if (showLogoutDialog) {
    AlertDialog(
        onDismissRequest = { showLogoutDialog = false },
        title = { Text("Log out", modifier = Modifier.testTag("LogoutDialogTitle")) },
        text = {
          Text("Are you sure you want to log out?", modifier = Modifier.testTag("LogoutDialogText"))
        },
        confirmButton = {
          Button(
              onClick = {
                authViewModel.logout {}
                showLogoutDialog = false
              },
              modifier = Modifier.testTag("LogoutDialogConfirmButton")) {
                Text("Log out")
              }
        },
        dismissButton = {
          Button(
              onClick = { showLogoutDialog = false },
              modifier = Modifier.testTag("LogoutDialogDismissButton")) {
                Text("Cancel")
              }
        },
        modifier = Modifier.testTag("LogoutDialog"))
  }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    optionName: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    iconColor: Color = colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = optionName,
            tint = iconColor,
            modifier = Modifier.size(32.dp).testTag("ProfileOptionIcon"))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
              optionName,
              style = MaterialTheme.typography.bodyLarge,
              color = colorScheme.onBackground,
              modifier = Modifier.testTag("ProfileOptionName"))
          subtitle?.let {
            Text(
                text = it,
                style =
                    TextStyle(
                        fontSize = 12.sp, color = colorScheme.onBackground.copy(alpha = 0.5f)),
                modifier = Modifier.padding(4.dp).testTag("ProfileOptionSubtitle"))
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (optionName == "My Account") {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Attention",
                tint = colorScheme.error,
                modifier = Modifier.testTag("AttentionIcon"))
          }
          Icon(
              Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null,
              tint = iconColor,
              modifier = Modifier.testTag("ArrowIcon"))
        }
      }
}
