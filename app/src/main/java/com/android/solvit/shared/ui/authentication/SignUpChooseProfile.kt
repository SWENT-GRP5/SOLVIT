package com.android.solvit.shared.ui.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.ui.profile.Stepper
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox

/**
 * A composable function that displays the "Choose Your Profile" screen during the sign-up process.
 * This screen allows users to select their role as either a "Seeker" (requesting services) or
 * "Provider" (offering services).
 *
 * @param navigationActions A set of navigation actions to handle screen transitions.
 * @param authViewModel The ViewModel managing authentication and user-related data.
 */
@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpChooseProfile(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

  val context = LocalContext.current

  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  Scaffold(
      topBar = {
        TopAppBarInbox(
            title = "Choose your profile",
            leftButtonAction = { navigationActions.goBack() },
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack)
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.background(colorScheme.background)
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              Stepper(currentStep = 1, isFormComplete = false)
              Spacer(modifier = Modifier.height(16.dp))

              Image(
                  painter = painterResource(id = R.drawable.sign_up_choose_profile_logo),
                  contentDescription = "Illustration",
                  modifier = Modifier.size(300.dp).testTag("roleIllustration"))

              SectionTitle(text = "Sign up as :", testTag = "signUpAsTitle")

              Spacer(modifier = Modifier.height(30.dp))

              ButtonSeekerProvider(
                  text = "Seeker",
                  description = "I want to request services.",
                  testTag = "seekerButton",
                  onClickButton = {
                    handleRoleSelection("seeker", authViewModel, context, navigationActions)
                  })

              Spacer(modifier = Modifier.height(16.dp))

              Text(text = "OR")

              Spacer(modifier = Modifier.height(16.dp))

              ButtonSeekerProvider(
                  text = "Provider",
                  description = "I want to offer services.",
                  testTag = "providerButton",
                  onClickButton = {
                    handleRoleSelection("provider", authViewModel, context, navigationActions)
                  })
            }
      })
}

/**
 * A composable function that displays a styled button for selecting a role (e.g., "Seeker" or
 * "Provider").
 *
 * @param text The label of the button.
 * @param description A brief description of the role.
 * @param testTag A test tag for UI testing.
 * @param onClickButton A lambda function to execute when the button is clicked.
 */
@Composable
fun ButtonSeekerProvider(
    text: String,
    description: String,
    testTag: String = "",
    onClickButton: () -> Unit = {}
) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(horizontal = 40.dp).testTag(testTag)) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(50.dp)
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors = listOf(colorScheme.secondary, colorScheme.secondary)),
                        shape = RoundedCornerShape(10.dp))
                    .clickable { onClickButton() },
            contentAlignment = Alignment.Center) {
              Text(
                  text = text,
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  style = Typography.bodyLarge.copy(fontSize = 20.sp))
            }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = Typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
      }
}

/**
 * A composable function that displays a section title with customizable text and a test tag.
 *
 * @param text The text to display as the section title.
 * @param testTag An optional test tag for UI testing purposes.
 */
@Composable
fun SectionTitle(text: String, testTag: String = "") {
  Text(
      text = text,
      fontSize = 25.sp,
      color = colorScheme.onBackground,
      modifier = Modifier.testTag(testTag),
      style = Typography.titleLarge)
}

/**
 * Handles the role selection for the user during the sign-up process. This method sets the user's
 * role and initiates the registration process based on whether the user is signing up with
 * email/password or Google account.
 *
 * @param role The role selected by the user ("seeker" or "provider").
 * @param authViewModel The ViewModel managing authentication and user-related data.
 * @param context The context in which the method is called.
 * @param navigationActions A set of navigation actions to handle screen transitions.
 */
fun handleRoleSelection(
    role: String,
    authViewModel: AuthViewModel,
    context: Context,
    navigationActions: NavigationActions
) {
  authViewModel.setRole(role)
  if (authViewModel.googleAccount.value == null) {
    authViewModel.registerWithEmailAndPassword(
        {
          Toast.makeText(context, "You are Signed up!", Toast.LENGTH_SHORT).show()
          if (role == "seeker") {
            navigationActions.navigateTo(Route.SEEKER_REGISTRATION)
          } else {
            navigationActions.navigateTo(Route.PROVIDER_REGISTRATION)
          }
        },
        {
          if (!authViewModel.userRegistered.value) {
            Toast.makeText(context, "You already have an account", Toast.LENGTH_SHORT).show()
            navigationActions.navigateTo(Screen.SIGN_IN)
          } else {
            Toast.makeText(context, "Failed to register", Toast.LENGTH_SHORT).show()
          }
        })
  } else {
    authViewModel.registerWithGoogle(
        {
          Toast.makeText(context, "You are Signed up!", Toast.LENGTH_SHORT).show()
          if (role == "seeker") {
            navigationActions.navigateTo(Route.SEEKER_REGISTRATION)
          } else {
            navigationActions.navigateTo(Route.PROVIDER_REGISTRATION)
          }
        },
        { Toast.makeText(context, "Failed to register", Toast.LENGTH_SHORT).show() })
  }
}
