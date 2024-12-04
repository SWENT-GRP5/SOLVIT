package com.android.solvit.shared.ui.authentication

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.solvit.R
import com.android.solvit.shared.ui.navigation.NavigationActions

/**
 * A composable function that provides a "Forgot Password" screen for users to reset their password.
 * The screen includes an input field for the email address and validates the email format.
 *
 * @param navigationActions A set of navigation actions to handle transitions between screens.
 *
 * This function:
 * - Locks the screen orientation to portrait mode while the screen is active.
 * - Displays an image and a text prompt instructing the user to enter their email.
 * - Validates the email input to ensure it matches the standard email format.
 * - Provides feedback to the user through Toast messages for empty or invalid email input.
 * - Includes a button to submit the reset request (currently not implemented).
 */
@SuppressLint(
    "SuspiciousIndentation",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassword(navigationActions: NavigationActions) {
  var email by remember { mutableStateOf("") }

  val context = LocalContext.current

  val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
  val goodFormEmail = emailRegex.matches(email)

  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("") },
            navigationIcon = { GoBackButton(navigationActions) },
            modifier = Modifier.testTag("topAppBar"),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background))
      },
      content = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              Spacer(modifier = Modifier.height(60.dp))

              Image(
                  painter = rememberAsyncImagePainter(R.drawable.passwordforgot),
                  contentDescription = "Checkmark",
                  modifier = Modifier.size(240.dp).testTag("forgotPasswordImage"))

              Spacer(modifier = Modifier.height(30.dp))

              Text(
                  text = "Please enter your email address to reset your password",
                  color = colorScheme.onSurfaceVariant,
                  fontSize = 20.sp,
                  modifier = Modifier.padding(top = 4.dp).fillMaxWidth().testTag("bigText"))

              Spacer(modifier = Modifier.height(10.dp))

              CustomOutlinedTextField(
                  value = email,
                  onValueChange = { email = it },
                  label = "Email",
                  placeholder = "Enter your email",
                  isValueOk = goodFormEmail,
                  leadingIcon = Icons.Default.Email,
                  leadingIconDescription = "Email Icon",
                  testTag = "emailInputField")

              Spacer(modifier = Modifier.height(20.dp))

              Button(
                  onClick = {
                    if (email.isEmpty()) {
                      Toast.makeText(context, "Please enter your email address", Toast.LENGTH_LONG)
                          .show()
                    } else if (!goodFormEmail) {
                      Toast.makeText(
                              context, "your email must contain \"@\" and \".\"", Toast.LENGTH_LONG)
                          .show()
                    } else {
                      Toast.makeText(context, "Not yet implemented", Toast.LENGTH_LONG).show()
                      // Toast.makeText(context, "An email has been send to $email",
                      // Toast.LENGTH_LONG).show()
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                  shape = RoundedCornerShape(25.dp),
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(50.dp)
                          .background(
                              brush =
                                  if (goodFormEmail) {
                                    Brush.horizontalGradient(
                                        colors =
                                            listOf(colorScheme.secondary, colorScheme.secondary))
                                  } else {
                                    Brush.horizontalGradient(
                                        colors =
                                            listOf(
                                                colorScheme.onSurfaceVariant,
                                                colorScheme.onSurfaceVariant))
                                  },
                              shape = RoundedCornerShape(25.dp))
                          .testTag("Send reset link")) {
                    Text(
                        "Send reset link",
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp)
                  }
            }
      })
}
