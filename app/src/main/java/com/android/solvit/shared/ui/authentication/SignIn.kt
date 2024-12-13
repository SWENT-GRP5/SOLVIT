package com.android.solvit.shared.ui.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.utils.CustomOutlinedTextField
import com.android.solvit.shared.ui.utils.PasswordTextField
import com.android.solvit.shared.ui.utils.ValidationRegex
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.ValidationRegex
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

/**
 * A composable function that displays the sign-in screen, allowing users to log in using
 * email/password or Google Sign-In.
 *
 * @param navigationActions A set of navigation actions to handle screen transitions.
 * @param authViewModel The ViewModel managing authentication and user-related data.
 */
@SuppressLint("InvalidColorHexValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  val context = LocalContext.current

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var isChecked by remember { mutableStateOf(false) }

  val onSuccess: () -> Unit = {
    Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
    authViewModel.registered()
  }
  val onFailure: () -> Unit = { Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show() }

  val launcher = googleSignInLauncher(authViewModel, onSuccess, onFailure)
  val token = stringResource(R.string.default_web_client_id)

  val backgroundColor = colorScheme.background // White background color

  Scaffold(
      content = { padding ->
        val modifier =
            Modifier.fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
                .verticalScroll(rememberScrollState())

        if (isLandscape) {
          LandscapeLayout(
              modifier = modifier,
              context = context,
              email = email,
              onEmailChange = { email = it },
              password = password,
              onPasswordChange = { password = it },
              passwordVisible = passwordVisible,
              onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
              isChecked = isChecked,
              onCheckedChange = {
                isChecked = it
                Toast.makeText(context, "Not yet Implemented", Toast.LENGTH_SHORT).show()
              },
              navigationActions = navigationActions,
              authViewModel = authViewModel,
              onSuccess = onSuccess,
              onFailure = onFailure,
              launcher = launcher,
              token = token,
          )
        } else {
          PortraitLayout(
              modifier = modifier,
              context = context,
              email = email,
              onEmailChange = { email = it },
              password = password,
              onPasswordChange = { password = it },
              passwordVisible = passwordVisible,
              onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
              isChecked = isChecked,
              onCheckedChange = {
                isChecked = it
                Toast.makeText(context, "Not yet Implemented", Toast.LENGTH_SHORT).show()
              },
              navigationActions = navigationActions,
              authViewModel = authViewModel,
              onSuccess = onSuccess,
              onFailure = onFailure,
              launcher = launcher,
              token = token,
          )
        }
      })
}

/**
 * A composable function that defines the portrait layout for the sign-in screen.
 *
 * @param modifier Modifier applied to the root column.
 * @param context The current context for showing Toast messages.
 * @param email The user's email address.
 * @param onEmailChange Lambda to update the email address.
 * @param password The user's password.
 * @param onPasswordChange Lambda to update the password.
 * @param passwordVisible Boolean indicating whether the password is visible.
 * @param onPasswordVisibilityChange Lambda to toggle password visibility.
 * @param isChecked Boolean indicating whether "Remember Me" is checked.
 * @param onCheckedChange Lambda to update the "Remember Me" state.
 * @param navigationActions A set of navigation actions for transitions.
 * @param authViewModel The ViewModel managing authentication.
 * @param onSuccess Callback invoked upon successful login.
 * @param onFailure Callback invoked upon login failure.
 * @param launcher Managed activity result launcher for Google Sign-In.
 * @param token The Google Sign-In client token.
 */
@Composable
fun PortraitLayout(
    modifier: Modifier,
    context: Context,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    token: String
) {
  Column(
      modifier = modifier.padding(16.dp).testTag("portraitLayout"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        LogoSection()
        Spacer(modifier = Modifier.height(20.dp))
        FormSection(
            context = context,
            email = email,
            onEmailChange = onEmailChange,
            password = password,
            onPasswordChange = onPasswordChange,
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            isChecked = isChecked,
            onCheckedChange = onCheckedChange,
            authViewModel = authViewModel,
            onSuccess = onSuccess,
            onFailure = onFailure,
            launcher = launcher,
            token = token,
            navigationActions = navigationActions)
        Spacer(modifier = Modifier.height(20.dp))

        SignUpSection(navigationActions)
      }
}

/**
 * A composable function that defines the landscape layout for the sign-in screen.
 *
 * @param modifier Modifier applied to the root row.
 * @param context The current context for showing Toast messages.
 * @param email The user's email address.
 * @param onEmailChange Lambda to update the email address.
 * @param password The user's password.
 * @param onPasswordChange Lambda to update the password.
 * @param passwordVisible Boolean indicating whether the password is visible.
 * @param onPasswordVisibilityChange Lambda to toggle password visibility.
 * @param isChecked Boolean indicating whether "Remember Me" is checked.
 * @param onCheckedChange Lambda to update the "Remember Me" state.
 * @param navigationActions A set of navigation actions for transitions.
 * @param authViewModel The ViewModel managing authentication.
 * @param onSuccess Callback invoked upon successful login.
 * @param onFailure Callback invoked upon login failure.
 * @param launcher Managed activity result launcher for Google Sign-In.
 * @param token The Google Sign-In client token.
 */
@Composable
fun LandscapeLayout(
    modifier: Modifier,
    context: Context,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    token: String
) {
  Row(
      modifier = modifier.padding(16.dp).testTag("landscapeLayout"),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.weight(1f).testTag("leftColumnLandScape"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              LogoSection()
              SignUpSection(navigationActions)
            }

        Column(
            modifier = Modifier.weight(1f).testTag("rightColumnLandScape"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              FormSection(
                  context = context,
                  email = email,
                  onEmailChange = onEmailChange,
                  password = password,
                  onPasswordChange = onPasswordChange,
                  passwordVisible = passwordVisible,
                  onPasswordVisibilityChange = onPasswordVisibilityChange,
                  isChecked = isChecked,
                  onCheckedChange = onCheckedChange,
                  authViewModel = authViewModel,
                  onSuccess = onSuccess,
                  onFailure = onFailure,
                  launcher = launcher,
                  token = token,
                  navigationActions = navigationActions)
            }
      }
}

@Composable
fun LogoSection() {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Image(
        painter = painterResource(id = R.drawable.sign_in),
        contentDescription = "Checkmark",
        modifier = Modifier.size(230.dp).testTag("loginImage"))
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Welcome!",
        style = Typography.bodyLarge.copy(fontSize = 28.sp),
        fontWeight = FontWeight.Bold,
        color = colorScheme.primary,
        modifier = Modifier.testTag("welcomeText"))
    Text(text = "Sign in to continue", color = colorScheme.onBackground)
  }
}

/**
 * A composable function that displays the main form section for user sign-in.
 *
 * @param context The current context used for actions like Toast messages.
 * @param email The email entered by the user.
 * @param onEmailChange A lambda function to handle updates to the email field.
 * @param password The password entered by the user.
 * @param onPasswordChange A lambda function to handle updates to the password field.
 * @param passwordVisible A boolean indicating whether the password is visible.
 * @param onPasswordVisibilityChange A lambda function to toggle password visibility.
 * @param isChecked A boolean indicating whether the "Remember me" checkbox is selected.
 * @param onCheckedChange A lambda function to handle changes to the "Remember me" checkbox.
 * @param authViewModel The ViewModel managing authentication and user-related data.
 * @param onSuccess A lambda function executed upon successful login.
 * @param onFailure A lambda function executed when login fails.
 * @param launcher A managed activity result launcher for handling Google Sign-In.
 * @param token The Google Sign-In client token.
 * @param navigationActions A set of navigation actions to handle screen transitions.
 */
@Composable
fun FormSection(
    context: Context,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    token: String,
    navigationActions: NavigationActions
) {
  val isFormComplete = email.isNotBlank() && password.isNotBlank()
  val passwordLengthComplete = password.length >= 6

  val goodFormEmail = ValidationRegex.EMAIL_REGEX.matches(email)

  CustomOutlinedTextField(
      value = email,
      onValueChange = onEmailChange,
      label = "Email",
      placeholder = "Enter your email",
      isValueOk = goodFormEmail,
      leadingIcon = Icons.Default.Email,
      leadingIconDescription = "Email Icon",
      testTag = "emailInput",
      errorMessage = "Your email must have \"@\" and \".\"",
      errorTestTag = "emailErrorMessage")

  Spacer(modifier = Modifier.height(10.dp))

  // Password input
  PasswordTextField(
      value = password,
      onValueChange = onPasswordChange,
      label = "Password",
      placeholder = "Enter your password",
      contentDescription = "Password",
      testTag = "passwordInput",
      passwordLengthComplete = passwordLengthComplete)

  Text(
      text = "Your password must have at least 6 characters",
      color = colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Start,
      style = Typography.bodySmall,
      modifier = Modifier.padding(top = 4.dp).fillMaxWidth())

  Spacer(modifier = Modifier.height(8.dp))

  // Remember me & Forgot password
  Row(modifier = Modifier.fillMaxWidth()) {
    // Section Remember Me
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
          checked = isChecked,
          onCheckedChange = onCheckedChange,
          modifier = Modifier.size(24.dp),
          colors =
              CheckboxDefaults.colors(
                  checkmarkColor = colorScheme.onSecondary,
                  uncheckedColor = colorScheme.onSurfaceVariant,
                  checkedColor = colorScheme.secondary))
      Spacer(modifier = Modifier.width(4.dp))
      Text(
          text = "Remember me",
          modifier = Modifier.testTag("rememberMeCheckbox"),
          color = colorScheme.onSurfaceVariant,
          style = Typography.bodyLarge)
    }

    // Section Forgot Password
    Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = "Forgot password?",
              color = colorScheme.onSurfaceVariant,
              style = Typography.bodyLarge,
              textDecoration = TextDecoration.Underline,
              modifier =
                  Modifier.clickable { navigationActions.navigateTo(Screen.FORGOT_PASSWORD) }
                      .testTag("forgotPasswordLink"),
              textAlign = TextAlign.End)
        }
  }

  Spacer(modifier = Modifier.height(16.dp))

  // Sign in button
  SignInButton(
      email = email,
      password = password,
      isFormComplete = isFormComplete,
      goodFormEmail = goodFormEmail,
      passwordLengthComplete = passwordLengthComplete,
      authViewModel = authViewModel,
      onSuccess = onSuccess,
      onFailure = onFailure)

  Spacer(modifier = Modifier.height(4.dp))

  Text("OR", color = colorScheme.onSurface, style = Typography.bodyLarge)

  Spacer(modifier = Modifier.height(4.dp))

  // Google sign in button
  GoogleButton(
      onClick = {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(token)
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
      },
      text = "Sign in with Google",
      testTag = "googleSignInButton",
      roundedCornerShape = RoundedCornerShape(25.dp))
}

/**
 * A composable function that displays a clickable "Sign-Up" section for new users.
 *
 * @param navigationActions A set of navigation actions to handle screen transitions.
 */
@Composable
fun SignUpSection(navigationActions: NavigationActions) {
  val annotatedText = buildAnnotatedString {
    append("I'm a new user, ")

    pushStringAnnotation(tag = "Sign up", annotation = "sign up")
    withStyle(
        style = SpanStyle(color = colorScheme.primary, textDecoration = TextDecoration.Underline)) {
          append("Sign-Up")
        }
    pop()
  }

  Box(
      modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
      contentAlignment = Alignment.Center,
  ) {
    ClickableText(
        text = annotatedText,
        style =
            Typography.bodyLarge.copy(color = colorScheme.onSurface, textAlign = TextAlign.Center),
        onClick = { navigationActions.navigateTo(Screen.SIGN_UP) },
        modifier = Modifier.fillMaxWidth().testTag("signUpLink"))
  }
}

/**
 * A composable function that displays a "Sign In" button with validation and login functionality.
 *
 * @param email The email entered by the user.
 * @param password The password entered by the user.
 * @param isFormComplete Boolean indicating if all required fields are filled.
 * @param goodFormEmail Boolean indicating if the email format is valid.
 * @param passwordLengthComplete Boolean indicating if the password meets the length requirement.
 * @param authViewModel The ViewModel managing authentication and user-related data.
 * @param onSuccess A lambda function executed upon successful login.
 * @param onFailure A lambda function executed when the login fails.
 *
 * This function:
 * - Validates the form fields (email and password) before attempting to log in.
 * - Displays appropriate Toast messages for invalid or incomplete input.
 * - Updates the `authViewModel` with the email and password and calls the login method.
 * - Dynamically styles the button based on the validation status.
 * - Invokes the `onSuccess` or `onFailure` callback based on the login outcome.
 */
@Composable
fun SignInButton(
    email: String,
    password: String,
    isFormComplete: Boolean,
    goodFormEmail: Boolean,
    passwordLengthComplete: Boolean,
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
  val context = LocalContext.current

  Button(
      onClick = {
        if (!isFormComplete) {
          Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
        } else if (!goodFormEmail) {
          Toast.makeText(context, "Your email must have \"@\" and \".\"", Toast.LENGTH_SHORT).show()
        } else if (!passwordLengthComplete) {
          Toast.makeText(
                  context, "Your password must have at least 6 characters", Toast.LENGTH_SHORT)
              .show()
        } else {
          authViewModel.setEmail(email)
          authViewModel.setPassword(password)
          authViewModel.loginWithEmailAndPassword(onSuccess, onFailure)
        }
      },
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(25.dp),
      modifier =
          Modifier.fillMaxWidth()
              .height(50.dp)
              .background(
                  brush =
                      if (isFormComplete && goodFormEmail && passwordLengthComplete) {
                        Brush.horizontalGradient(
                            colors = listOf(colorScheme.primary, colorScheme.secondary))
                      } else {
                        Brush.horizontalGradient(
                            colors =
                                listOf(colorScheme.onSurfaceVariant, colorScheme.onSurfaceVariant))
                      },
                  shape = RoundedCornerShape(25.dp))
              .testTag("signInButton")) {
        Text(
            "Sign in",
            color = colorScheme.background,
            fontWeight = FontWeight.Bold,
            style = Typography.bodyLarge)
      }
}

/**
 * A composable function that displays a styled button for Google Sign-In.
 *
 * @param onClick A lambda function to be executed when the button is clicked.
 * @param text The text displayed on the button.
 * @param testTag A test tag for UI testing purposes.
 * @param roundedCornerShape The shape of the button's corners.
 *
 * This function:
 * - Renders a button styled with a border and a transparent background.
 * - Displays the Google logo on the left and the provided text centered on the button.
 * - Ensures responsiveness by adjusting text alignment and handling overflow.
 * - Triggers the `onClick` action when the button is pressed.
 */
@Composable
fun GoogleButton(
    onClick: () -> Unit,
    text: String,
    testTag: String,
    roundedCornerShape: RoundedCornerShape
) {
  Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      shape = roundedCornerShape,
      border = BorderStroke(1.dp, colorScheme.onSurfaceVariant),
      modifier = Modifier.fillMaxWidth().wrapContentHeight().testTag(testTag)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
          Image(
              painter = painterResource(id = R.drawable.google_logo),
              contentDescription = "Google Logo",
              modifier = Modifier.size(30.dp).padding(end = 8.dp))

          Text(
              text = text,
              color = colorScheme.onSurface,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f),
              textAlign = TextAlign.Center,
              style = Typography.bodyLarge)
        }
      }
}

/**
 * A composable function that provides a launcher for handling Google Sign-In.
 *
 * @param authViewModel The ViewModel managing authentication and user-related data.
 * @param onSuccess A lambda function executed when the Google Sign-In process succeeds.
 * @param onFailure A lambda function executed when the Google Sign-In process fails.
 */
@Composable
fun googleSignInLauncher(
    authViewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  val scope = rememberCoroutineScope()
  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)!!
      scope.launch {
        authViewModel.setGoogleAccount(account)
        authViewModel.signInWithGoogle(onSuccess, onFailure)
      }
    } catch (e: ApiException) {
      onFailure()
    }
  }
}
