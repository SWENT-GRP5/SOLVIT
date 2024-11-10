package com.android.solvit.shared.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.ui.profile.CustomOutlinedTextField
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

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
          authViewModel, { navigationActions.navigateTo(Screen.SIGN_UP_CHOOSE_ROLE) }, {})
  val token = stringResource(R.string.default_web_client_id)

  val goodFormEmail =
      email.isNotBlank() &&
          Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
          email.contains(".") &&
          email.contains("@")
  val passwordLengthComplete = password.length >= 6
  val samePassword = password == confirmPassword

  val isFormComplete = goodFormEmail && passwordLengthComplete && samePassword

  val backgroundColor = Color(0xFFFFFFFF)

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("") },
            navigationIcon = { GoBackButton(navigationActions) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor))
      },
      content = {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(backgroundColor)
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
              SocialSignUpButton {
                authViewModel.setRole("seeker")
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
              }

              Spacer(modifier = Modifier.height(20.dp))
              Text("OR", color = Color.Gray)

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

              Spacer(modifier = Modifier.height(10.dp))

              PasswordTextField(
                  value = password,
                  onValueChange = { password = it },
                  label = "Password",
                  placeholder = "Enter your password",
                  contentDescription = "Password",
                  testTag = "passwordInput",
                  passwordLengthComplete = passwordLengthComplete)

              Spacer(modifier = Modifier.height(10.dp))

              // Confirm Password Field
              PasswordTextField(
                  value = confirmPassword,
                  onValueChange = { confirmPassword = it },
                  label = "Confirm Password",
                  placeholder = "Re-enter your password",
                  contentDescription = "Confirm Password",
                  testTag = "confirmPasswordInput",
                  passwordLengthComplete = (passwordLengthComplete && samePassword))

              Text(
                  text = "Your passport must have at least 6 characters",
                  color = Color.Gray,
                  fontSize = 12.sp,
                  textAlign = TextAlign.Start,
                  modifier = Modifier.padding(top = 4.dp).fillMaxWidth())

              Spacer(modifier = Modifier.height(20.dp))

              SignUpButton(
                  {
                    authViewModel.setEmail(email)
                    authViewModel.setPassword(password)
                    navigationActions.navigateTo(Screen.SIGN_UP_CHOOSE_ROLE)
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
  Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.testTag(testTag))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    contentDescription: String = "",
    testTag: String,
    passwordLengthComplete: Boolean
) {
  var passwordVisible by remember { mutableStateOf(false) }
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label, color = Color.Black) },
      singleLine = true,
      placeholder = { Text(placeholder) },
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      enabled = true,
      shape = RoundedCornerShape(12.dp),
      visualTransformation =
          if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      leadingIcon = {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = contentDescription,
            tint = if (passwordLengthComplete) Color(90, 197, 97) else Color.Gray,
            modifier = Modifier.size(25.dp))
      },
      trailingIcon = {
        val image =
            if (passwordVisible) painterResource(id = android.R.drawable.ic_menu_view)
            else painterResource(id = android.R.drawable.ic_secure)

        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(
              painter = image,
              contentDescription = null,
              tint = if (passwordLengthComplete) Color(90, 197, 97) else Color.Gray,
              modifier = Modifier.size(24.dp))
        }
      },
      colors =
          TextFieldDefaults.outlinedTextFieldColors(
              focusedTextColor = Color.Black,
              unfocusedTextColor =
                  if (value.isEmpty()) Color.Gray
                  else if (!passwordLengthComplete) Color.Red else Color.Black,
              focusedBorderColor = if (passwordLengthComplete) Color(0xFF5AC561) else Color.Blue,
              unfocusedBorderColor =
                  when {
                    value.isEmpty() -> Color.Gray
                    passwordLengthComplete -> Color(0xFF5AC561)
                    else -> Color.Red
                  },
          ))
}

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
        } else {
          Toast.makeText(context, "You are Signed up!", Toast.LENGTH_SHORT).show()
        }
      },
      modifier =
          Modifier.fillMaxWidth()
              .height(50.dp)
              .background(
                  brush =
                      if (isComplete && goodFormEmail && passwordLengthComplete && samePassword) {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0, 200, 83), Color(0, 153, 255)))
                      } else {
                        Brush.horizontalGradient(colors = listOf(Color.Gray, Color.Gray))
                      },
                  shape = RoundedCornerShape(25.dp))
              .testTag("signUpButton"),
      shape = RoundedCornerShape(25.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
        Text("Sign Up", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
}

@Composable
fun SocialSignUpButton(onClick: () -> Unit) {
  Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(8.dp),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("googleSignUpButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier = Modifier.size(30.dp).padding(end = 8.dp))
              Text(text = "Sign Up with Google", color = Color.Gray, fontSize = 16.sp)
              Spacer(Modifier.size(25.dp))
            }
      }
}

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

@Composable
fun AlreadyHaveAccountText(navigationActions: NavigationActions) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text("Already have an account? ", color = Color.Gray)
    ClickableText(
        text = AnnotatedString("Log up in here!"),
        onClick = { navigationActions.navigateTo(Screen.SIGN_IN) },
        style = TextStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
        modifier = Modifier.testTag("logInLink"))
  }
}
