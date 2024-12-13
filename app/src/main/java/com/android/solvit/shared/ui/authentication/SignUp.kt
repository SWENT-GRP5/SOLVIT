package com.android.solvit.shared.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.CustomOutlinedTextField
import com.android.solvit.shared.ui.utils.PasswordTextField
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import com.android.solvit.shared.ui.utils.ValidationRegex
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A composable function that displays the Sign-Up screen for user registration.
 *
 * @param navigationActions A set of navigation actions to handle screen transitions.
 * @param authViewModel The ViewModel managing authentication and user-related data.
 *
 * This function:
 * - Locks the screen orientation to portrait mode while active.
 * - Provides a form for users to input their email, password, and confirm their password.
 * - Includes validation for email format, password length, and password matching.
 * - Offers a "Sign up with Google" option using Google Sign-In.
 * - Provides a button to generate a secure password via an API, copying it to the clipboard.
 * - Navigates to the role selection screen upon successful registration.
 * - Displays error messages for invalid or incomplete form fields.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  val launcher =
      googleRegisterLauncher(
          authViewModel, { navigationActions.navigateTo(Screen.CHOOSE_ROLE) }, {})
  val token = stringResource(R.string.default_web_client_id)

  val goodFormEmail = ValidationRegex.EMAIL_REGEX.matches(email)

  val passwordLengthComplete = password.length >= 6
  val samePassword = password == confirmPassword

  val isFormComplete = goodFormEmail && passwordLengthComplete && samePassword

  val passwordApiService = createPasswordService()
  val scope = rememberCoroutineScope()
  val clipboardManager = LocalClipboardManager.current

  // Function to generate a password and copy it to the clipboard when the button is clicked
  // We use a lambda function to generate the password asynchronously
  val generatePassword: () -> Unit = {
    scope.launch {
      try {
        val response =
            passwordApiService.createPassword(
                includeDigits = true,
                includeLowercase = true,
                includeUppercase = true,
                includeSpecialCharacters = true,
                passwordLength = 16,
                quantity = 1)
        val generatedPassword = response.passwords.first()
        password = generatedPassword
        confirmPassword = generatedPassword

        val annotatedString = AnnotatedString(generatedPassword)
        clipboardManager.setText(annotatedString)
        Toast.makeText(
                context, "Password copied to clipboard : $annotatedString", Toast.LENGTH_SHORT)
            .show()
      } catch (e: Exception) {
        Toast.makeText(context, "Error generating password", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Scaffold(
      topBar = {
        TopAppBarInbox(
            leftButtonAction = { navigationActions.goBack() },
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack)
      },
      content = {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(colorScheme.background)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              Image(
                  painter = painterResource(id = R.drawable.sign_up_image),
                  contentDescription = "Logo",
                  modifier = Modifier.size(250.dp).testTag("signUpIllustration"))

              ScreenTitle("Sign up", "signUpTitle")
              Spacer(modifier = Modifier.height(30.dp))

              GoogleButton(
                  onClick = {
                    authViewModel.setRole("seeker")
                    val gso =
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(token)
                            .requestEmail()
                            .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    launcher.launch(googleSignInClient.signInIntent)
                  },
                  text = "Sign up with Google",
                  testTag = "googleSignUpButton",
                  roundedCornerShape = RoundedCornerShape(12.dp))

              Spacer(modifier = Modifier.height(20.dp))
              Text("OR", color = colorScheme.onSurfaceVariant)

              Spacer(modifier = Modifier.height(10.dp))

              CustomOutlinedTextField(
                  value = email,
                  onValueChange = { email = it },
                  label = "Email",
                  placeholder = "Enter your email",
                  isValueOk = goodFormEmail,
                  leadingIcon = Icons.Default.Email,
                  leadingIconDescription = "Email Icon",
                  testTag = "emailInputField",
                  errorMessage = "Your email must have \"@\" and \".\"",
                  errorTestTag = "emailErrorMessage")

              Spacer(modifier = Modifier.height(10.dp))

              PasswordTextField(
                  value = password,
                  onValueChange = { password = it },
                  label = "Password",
                  placeholder = "Enter your password",
                  contentDescription = "Password",
                  testTag = "passwordInputField",
                  passwordLengthComplete = passwordLengthComplete,
                  testTagErrorPassword = "passwordErrorMessage")

              Spacer(modifier = Modifier.height(10.dp))

              // Confirm Password Field
              PasswordTextField(
                  value = confirmPassword,
                  onValueChange = { confirmPassword = it },
                  label = "Confirm Password",
                  placeholder = "Re-enter your password",
                  contentDescription = "Confirm Password",
                  testTag = "confirmPasswordInputField",
                  passwordLengthComplete = (passwordLengthComplete && samePassword),
                  testTagErrorPassword = "confirmPasswordErrorMessage")

              if (!samePassword && confirmPassword.isNotEmpty() && password.isNotEmpty()) {
                Text(
                    text = "Password and Confirm Password must be the same",
                    color = colorScheme.error,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth())
              }

              Text(
                  text = "Your password must have at least 6 characters",
                  color = colorScheme.onSurfaceVariant,
                  fontSize = 12.sp,
                  textAlign = TextAlign.Start,
                  style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
                  modifier = Modifier.padding(top = 4.dp).fillMaxWidth())

              Button(
                  onClick = generatePassword,
                  modifier =
                      Modifier.align(Alignment.End)
                          .padding(top = 16.dp)
                          .height(50.dp)
                          .testTag("generatePasswordButton"),
                  shape = RoundedCornerShape(25.dp),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = colorScheme.primary,
                          contentColor = colorScheme.background)) {
                    Text("Generate a password", style = Typography.bodyLarge)
                  }

              Spacer(modifier = Modifier.height(20.dp))

              SignUpButton(
                  {
                    authViewModel.setEmail(email)
                    authViewModel.setPassword(password)
                    navigationActions.navigateTo(Screen.CHOOSE_ROLE)
                  },
                  isFormComplete,
                  goodFormEmail,
                  passwordLengthComplete,
                  samePassword)

              Spacer(modifier = Modifier.height(16.dp))

              AlreadyHaveAccountText(navigationActions)
            }
      })
}

@Composable
fun ScreenTitle(title: String, testTag: String) {
  Text(text = title, style = Typography.titleLarge, modifier = Modifier.testTag(testTag))
}

/**
 * A composable function that displays the "Sign Up" button with validation and error handling.
 *
 * @param onClick A lambda function to execute upon successful validation and button click.
 * @param isComplete A boolean indicating if all form fields are valid and complete.
 * @param goodFormEmail A boolean indicating if the email format is valid.
 * @param passwordLengthComplete A boolean indicating if the password meets the length requirement.
 * @param samePassword A boolean indicating if the password and confirm password match.
 */
@Composable
fun SignUpButton(
    onClick: () -> Unit,
    isComplete: Boolean = false,
    goodFormEmail: Boolean = false,
    passwordLengthComplete: Boolean = false,
    samePassword: Boolean = false
) {
  val context = LocalContext.current
  Button(
      onClick = {
        if (isComplete && goodFormEmail && samePassword && passwordLengthComplete) {
          onClick()
        }
        if (!isComplete) {
          Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
        } else if (!goodFormEmail) {
          Toast.makeText(context, "Your email must have \"@\" and \".\"", Toast.LENGTH_SHORT).show()
        } else if (!samePassword) {
          Toast.makeText(
                  context, "Password and Confirm Password must be the same", Toast.LENGTH_SHORT)
              .show()
        } else if (!passwordLengthComplete) {
          Toast.makeText(
                  context, "Your password must have at least 6 characters", Toast.LENGTH_SHORT)
              .show()
        }
      },
      modifier =
          Modifier.fillMaxWidth()
              .height(50.dp)
              .background(
                  brush =
                      if (isComplete && goodFormEmail && passwordLengthComplete && samePassword) {
                        Brush.horizontalGradient(
                            colors = listOf(colorScheme.primary, colorScheme.secondary))
                      } else {
                        Brush.horizontalGradient(
                            colors =
                                listOf(colorScheme.onSurfaceVariant, colorScheme.onSurfaceVariant))
                      },
                  shape = RoundedCornerShape(25.dp))
              .testTag("signUpButton"),
      shape = RoundedCornerShape(25.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
        Text(
            "Sign Up",
            color = colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            style = Typography.bodyLarge)
      }
}

/**
 * A composable function that returns a launcher for Google Sign-In registration.
 *
 * @param authViewModel The ViewModel managing authentication and user-related data.
 * @param onSuccess A lambda function executed when the Google Sign-In process succeeds.
 * @param onFailure A lambda function executed when the Google Sign-In process fails.
 */
@Composable
fun googleRegisterLauncher(
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
        onSuccess()
      }
    } catch (e: ApiException) {
      onFailure()
    }
  }
}

/**
 * A composable function that displays a clickable text for navigating to the "Log In" screen.
 *
 * @param navigationActions A set of navigation actions to handle screen transitions.
 *
 * This function:
 * - Displays an annotated and styled text with a clickable "Log-In here!" link.
 * - Navigates to the "Log In" screen when the link is clicked.
 */
@Composable
fun AlreadyHaveAccountText(navigationActions: NavigationActions) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    val annotatedText = buildAnnotatedString {
      append("Already have an account? ")

      pushStringAnnotation(tag = "Log in", annotation = "log_in")
      withStyle(
          style =
              SpanStyle(color = colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append("Log-In here !")
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
              Typography.bodyLarge.copy(
                  color = colorScheme.onSurface, textAlign = TextAlign.Center),
          onClick = { offset ->
            annotatedText
                .getStringAnnotations(tag = "Log in", start = offset, end = offset)
                .firstOrNull()
                ?.let { navigationActions.navigateTo(Screen.SIGN_IN) }
          },
          modifier = Modifier.fillMaxWidth().testTag("logInLink"))
    }
  }
}

/**
 * A helper function that creates and returns a Retrofit instance for the Password API service.
 *
 * @return A `PasswordApiService` implementation for making password generation requests.
 */
fun createPasswordService(): PasswordApiService {
  val retrofit =
      Retrofit.Builder()
          .baseUrl("https://api.motdepasse.xyz/")
          .addConverterFactory(GsonConverterFactory.create())
          .build()

  return retrofit.create(PasswordApiService::class.java)
}
