package com.android.solvit.seeker.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SeekerProfileScreen(
    viewModel: SeekerProfileViewModel,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    navigationActions: NavigationActions
) {
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

  userProfile?.let { profile ->
    Scaffold(
        modifier = Modifier.background(colorScheme.background),
        backgroundColor = colorScheme.background,
        topBar = {
          TopAppBar(
              modifier = Modifier.testTag("ProfileTopBar"),
              backgroundColor = colorScheme.background,
              navigationIcon = {
                IconButton(
                    onClick = { navigationActions.goBack() },
                    modifier = Modifier.testTag("BackButton")) {
                      Icon(
                          Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Back",
                          tint = colorScheme.onBackground)
                    }
              },
              title = {
                Text(
                    text = "Profile",
                    style =
                        TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground),
                    modifier = Modifier.testTag("ProfileTitle"))
              })
        },
        content = {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(horizontal = 16.dp, vertical = 8.dp)
                      .background(colorScheme.background)
                      .testTag("ProfileContent"),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Header Row (Profile Image, Name, Email, Edit Icon)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier =
                        Modifier.fillMaxWidth().background(colorScheme.primary).padding(16.dp)) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile Picture, Name, and Email on the left
                        Image(
                            painter = painterResource(id = R.drawable.empty_profile_img),
                            contentDescription = "Profile Picture",
                            modifier =
                                Modifier.size(53.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, colorScheme.onPrimary, CircleShape)
                                    .testTag("ProfileImage"))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                          Text(
                              text = profile.name,
                              fontWeight = FontWeight.Bold,
                              style =
                                  MaterialTheme.typography.h6.copy(color = colorScheme.onPrimary),
                              modifier = Modifier.testTag("ProfileName"))
                          Text(
                              text = profile.email,
                              style =
                                  MaterialTheme.typography.body2.copy(
                                      color = colorScheme.onPrimary),
                              modifier = Modifier.testTag("ProfileEmail"))
                        }
                      }

                      // Edit icon on the right
                      IconButton(onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = colorScheme.onBackground)
                      }
                    }

                // Profile Options List
                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth()
                            .background(colorScheme.background)
                            .testTag("ProfileOptionsList"),
                    contentPadding = PaddingValues(vertical = 8.dp)) {
                      item {
                        ProfileOptionItem(
                            icon = Icons.Default.Person,
                            optionName = "My Account",
                            subtitle = "Make changes to your account",
                            onClick = { /* Handle click */},
                            modifier = Modifier.testTag("MyAccountOption"))
                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileOptionItem(
                            icon = Icons.Default.ShoppingCart,
                            optionName = "Order History",
                            subtitle = "Manage your requested services and their statuses",
                            onClick = {
                              Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("OrdersOption"))
                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileOptionItem(
                            icon = Icons.Default.Lock,
                            optionName = "Billing",
                            subtitle = "Manage your billing information",
                            onClick = {
                              Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("BillingOption"))
                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileOptionItem(
                            icon = Icons.Default.Favorite,
                            optionName = "Preferences",
                            subtitle = "Set your preferences",
                            onClick = {
                                navigationActions.navigateTo(Screen.EDIT_PREFERENCES)
                            },
                            modifier = Modifier.testTag("PreferencesOption"))
                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileOptionItem(
                            icon = Icons.Default.ExitToApp,
                            optionName = "Log out",
                            subtitle = "Log out of your account",
                            onClick = { authViewModel.logout {} },
                            modifier = Modifier.testTag("LogoutOption"))
                        Spacer(modifier = Modifier.height(8.dp))

                        ProfileOptionItem(
                            icon = Icons.Default.Notifications,
                            optionName = "Help & Support",
                            subtitle = "Get help and support",
                            onClick = {
                              Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("HelpSupportOption"))

                        ProfileOptionItem(
                            icon = Icons.Default.Settings,
                            optionName = "About App",
                            subtitle = "Learn more about the app",
                            onClick = {
                              Toast.makeText(context, "Not yet implemented", Toast.LENGTH_SHORT)
                                  .show()
                            },
                            modifier = Modifier.testTag("AboutAppOption"))
                      }
                    }
              }
        })
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
    subtitle: String,
    onClick: () -> Unit = {},
    iconColor: Color = colorScheme.onBackground,
    modifier: Modifier = Modifier
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .clickable { onClick() }
              .padding(vertical = 12.dp), // Adjust vertical padding for uniformity across screens
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = optionName,
            tint = iconColor,
            modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
              optionName,
              style = MaterialTheme.typography.body1.copy(color = colorScheme.onBackground))
          Text(
              text = subtitle,
              style = TextStyle(fontSize = 12.sp, color = colorScheme.onSurfaceVariant),
              modifier = Modifier.padding(4.dp))
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = iconColor)
      }
}
