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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import com.android.solvit.shared.ui.theme.GradientBlue
import com.android.solvit.shared.ui.theme.GradientGreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
      topBar = {
        TopAppBar(
            title = { Text("") },
            navigationIcon = { GoBackButton(navigationActions) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
            modifier = Modifier.testTag("backButton"))
      },
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
              onCheckedChange = { isChecked = it },
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
              onCheckedChange = { isChecked = it },
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

@Composable
fun GoBackButton(navigationActions: NavigationActions) {
  var canGoBack by remember { mutableStateOf(true) }
  val coroutineScope = rememberCoroutineScope()
  IconButton(
      onClick = {
        if (canGoBack) {
          canGoBack = false
          navigationActions.goBack()
          coroutineScope.launch {
            delay(500)
            canGoBack = true
          }
        }
      },
      enabled = canGoBack) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "goBackButton",
            modifier = Modifier.testTag("backButton"))
      }
}

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
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = colorScheme.primary,
        modifier = Modifier.testTag("welcomeText"))
    // Spacer(modifier = Modifier.height(4.dp))
    Text(text = "Sign in to continue", color = colorScheme.onBackground)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
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
  val goodFormEmail = email.contains("@") && email.contains(".")
  val passwordLengthComplete = password.length >= 6
  // Email input
  OutlinedTextField(
      value = email,
      onValueChange = onEmailChange,
      label = { Text("Email") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("emailInput"),
      keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
      leadingIcon = {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_dialog_email),
            contentDescription = "Email Icon",
            tint = colorScheme.secondary)
      },
      shape = RoundedCornerShape(8.dp),
      colors =
          TextFieldDefaults.outlinedTextFieldColors(
              focusedBorderColor = colorScheme.secondary, // Green color when focused
              unfocusedBorderColor = colorScheme.secondary // Green color when not focused
              ))

  Spacer(modifier = Modifier.height(8.dp))

  // Password input
  OutlinedTextField(
      value = password,
      onValueChange = onPasswordChange,
      label = { Text("Password") },
      singleLine = true,
      visualTransformation =
          if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      leadingIcon = {
        Icon(Icons.Default.Lock, contentDescription = "Email Icon", tint = colorScheme.secondary)
      },
      trailingIcon = {
        val image =
            if (passwordVisible) painterResource(id = android.R.drawable.ic_menu_view)
            else painterResource(id = android.R.drawable.ic_secure)

        IconButton(onClick = onPasswordVisibilityChange) {
          Icon(
              painter = image,
              contentDescription = null,
              tint = colorScheme.secondary,
              modifier = Modifier.size(24.dp))
        }
      },
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.fillMaxWidth().testTag("password"),
      colors =
          TextFieldDefaults.outlinedTextFieldColors(
              focusedBorderColor = colorScheme.secondary, // Green color when focused
              unfocusedBorderColor = colorScheme.secondary // Green color when not focused
              ))

  Spacer(modifier = Modifier.height(16.dp))

  // Remember me & Forgot password
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
              checked = isChecked,
              onCheckedChange = onCheckedChange,
              modifier = Modifier.size(24.dp),
              colors =
                  CheckboxDefaults.colors(
                      checkmarkColor = colorScheme.background,
                      uncheckedColor = colorScheme.secondary,
                      checkedColor = colorScheme.secondary))
          Text(text = " Remember me", modifier = Modifier.testTag("rememberMeCheckbox"))
        }

        ClickableText(
            text = AnnotatedString("Forgot password?"),
            onClick = { navigationActions.navigateTo(Screen.FORGOT_PASSWORD) },
            style =
                TextStyle(
                    color = colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.Underline),
            modifier = Modifier.testTag("forgotPasswordLink"))
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

  Text("OR", color = colorScheme.onSurface)

  Spacer(modifier = Modifier.height(4.dp))

  // Google sign in button
  GoogleSignInButton(
      onSignInClick = {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(token)
                .requestEmail()
                .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
      })
}

@Composable
fun SignUpSection(navigationActions: NavigationActions) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text("I'm a new user, ", color = colorScheme.onSurface)
    ClickableText(
        text = AnnotatedString("Sign up"),
        onClick = { navigationActions.navigateTo(Screen.SIGN_UP) },
        style = TextStyle(color = colorScheme.primary, textDecoration = TextDecoration.Underline),
        modifier = Modifier.testTag("signUpLink"))
  }
}

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
                        Brush.horizontalGradient(colors = listOf(GradientGreen, GradientBlue))
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
            fontSize = 16.sp)
      }
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), // Button color
      shape = RoundedCornerShape(25.dp), // Circular edges for the button
      border = BorderStroke(1.dp, colorScheme.onSurfaceVariant), // Border color
      modifier =
          Modifier.fillMaxWidth()
              .height(50.dp) // Adjust height as needed
              .testTag("googleSignInButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(30.dp) // Size of the Google logo
                          .padding(end = 8.dp))

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  color = colorScheme.onSurface, // Text color
                  fontSize = 16.sp, // Font size
                  fontWeight = FontWeight.Medium)
            }
      }
}

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
