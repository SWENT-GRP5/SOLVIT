package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.ui.navigation.BottomNavigationMenu
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.LIST_TOP_LEVEL_DESTINATION_SEEKER
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "SourceLockedOrientationActivity")
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
  val user by authViewModel.user.collectAsStateWithLifecycle()
  val userProfile by viewModel.seekerProfile.collectAsStateWithLifecycle()
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
        TopAppBarInbox(
            title = "Profile",
            testTagTitle = "ProfileTitle",
            rightButton = { showLogoutDialog = true },
            rightButtonForm = Icons.AutoMirrored.Filled.ExitToApp,
            testTagRight = "LogoutButton",
            testTagGeneral = "ProfileTopBar")
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { navigationActions.navigateTo(it.route) },
            tabList = LIST_TOP_LEVEL_DESTINATION_SEEKER,
            selectedItem = Route.PROFILE)
      },
      containerColor = Color.Transparent,
  ) { paddingValues ->
    LazyColumn(
        modifier =
            Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).testTag("ProfileContent"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
          item {
            // Profile Info Card with Edit Button
            ProfileInfoCard(
                fullName,
                email,
                imageUrl = userProfile.imageUrl,
                onEdit = { navigationActions.navigateTo(Screen.EDIT_SEEKER_PROFILE) })
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
                    Typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(start = 16.dp).testTag("MoreOptionsText"))
          }

          item {
            // Second Group of Profile Options
            AboutAppCard(context)
          }
        }
  }

  // Logout Confirmation Dialog
  if (showLogoutDialog) {
    LogoutDialog(
        onLogout = {
          authViewModel.logout {}
          showLogoutDialog = false
        },
        onDismiss = { showLogoutDialog = false })
  }
}

@Composable
fun ProfileInfoCard(
    fullName: String = "",
    email: String = "",
    imageUrl: String = "",
    onEdit: () -> Unit
) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag("ProfileInfoCard"),
      colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = imageUrl.ifBlank { R.drawable.empty_profile_img },
                    placeholder = painterResource(R.drawable.loading),
                    error = painterResource(R.drawable.error),
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier.size(50.dp)
                            .clip(CircleShape)
                            .border(4.dp, colorScheme.onPrimary, CircleShape)
                            .testTag("ProfileImage"),
                    contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                      text = fullName,
                      color = colorScheme.onPrimary,
                      modifier = Modifier.testTag("ProfileName"),
                      style = Typography.titleLarge.copy(fontSize = 20.sp))
                  Text(
                      text = email,
                      color = colorScheme.onPrimary.copy(alpha = 0.6f),
                      modifier = Modifier.testTag("ProfileEmail"),
                      style = Typography.bodyMedium)
                }
              }
              IconButton(onClick = onEdit, modifier = Modifier.testTag("EditProfileButton")) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
              }
            }
      }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    optionName: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    iconColor: Color = colorScheme.onSurfaceVariant,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
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
              style = Typography.bodyLarge.copy(color = colorScheme.onBackground),
              modifier = Modifier.testTag("ProfileOptionName"),
          )
          subtitle?.let {
            Text(
                text = it,
                style =
                    Typography.bodySmall.copy(color = colorScheme.onBackground.copy(alpha = 0.5f)),
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

@Composable
fun AboutAppCard(context: Context) {
  Card(
      modifier = Modifier.fillMaxWidth().testTag("SecondGroupCard"),
      colors = CardDefaults.cardColors(containerColor = colorScheme.background),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
              ProfileOptionItem(
                  icon = Icons.Default.Notifications,
                  optionName = "Help & Support",
                  onClick = {
                    Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("HelpSupportOption"))
              ProfileOptionItem(
                  icon = Icons.Default.Settings,
                  optionName = "About App",
                  onClick = {
                    Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("AboutAppOption"))
            }
      }
}

@Composable
fun LogoutDialog(onLogout: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
            "Log out",
            style = Typography.bodyLarge,
            modifier = Modifier.testTag("LogoutDialogTitle"))
      },
      text = {
        Text(
            "Are you sure you want to log out?",
            style = Typography.bodyLarge,
            modifier = Modifier.testTag("LogoutDialogText"))
      },
      confirmButton = {
        Button(onClick = onLogout, modifier = Modifier.testTag("LogoutDialogConfirmButton")) {
          Text(
              "Log out",
              style = Typography.bodyLarge,
          )
        }
      },
      dismissButton = {
        Button(onClick = onDismiss, modifier = Modifier.testTag("LogoutDialogDismissButton")) {
          Text(
              "Cancel",
              style = Typography.bodyLarge,
          )
        }
      },
      modifier = Modifier.testTag("LogoutDialog"))
}
