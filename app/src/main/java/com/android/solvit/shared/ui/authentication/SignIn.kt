package com.android.solvit.shared.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@SuppressLint("InvalidColorHexValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var rememberMeIsChecked by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val onSuccess: () -> Unit = {
    Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
    authViewModel.registered()
  }
  val onFailure: () -> Unit = { Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show() }

  val launcher =
      googleSignInLauncher(
          authViewModel = authViewModel, onSuccess = onSuccess, onFailure = onFailure)

  val token = stringResource(R.string.default_web_client_id)

  val isFormComplete = email.isNotBlank() && password.isNotBlank()
  val goodFormEmail = email.contains("@") && email.contains(".")
  val passwordLengthComplete = password.length >= 6

  val backgroundColor = Color(0xFFFFFFFF)

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "goBackButton",
                    modifier = Modifier.testTag("backButton"))
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
            modifier = Modifier.testTag("backButton"))
      },
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {}
      })

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Image(
            painter = painterResource(id = R.drawable.sign_in),
            contentDescription = "Checkmark",
            modifier = Modifier.size(240.dp).testTag("loginImage"))

        Spacer(modifier = Modifier.height(20.dp))

        // Welcome text
        Text(
            text = "Welcome!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0, 153, 255),
            modifier = Modifier.testTag("welcomeText"))

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Sign in to continue", color = Color.Black)

        Spacer(modifier = Modifier.height(20.dp))

        // Email input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("emailInput"),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            leadingIcon = {
              Icon(
                  painter = painterResource(id = android.R.drawable.ic_dialog_email),
                  contentDescription = "Email Icon",
                  tint = Color(90, 197, 97))
            },
            shape = RoundedCornerShape(8.dp),
            colors =
                TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF5AC561), // Green color when focused
                    unfocusedBorderColor = Color(0xFF5AC561) // Green color when not focused
                    ))

        Spacer(modifier = Modifier.height(16.dp))

        // Password input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation =
                if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = "Email Icon", tint = Color(90, 197, 97))
            },
            trailingIcon = {
              val image =
                  if (passwordVisible) painterResource(id = android.R.drawable.ic_menu_view)
                  else painterResource(id = android.R.drawable.ic_secure)

              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = image,
                    contentDescription = null,
                    tint = Color(90, 197, 97),
                    modifier = Modifier.size(24.dp))
              }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("password"),
            colors =
                TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF5AC561), // Green color when focused
                    unfocusedBorderColor = Color(0xFF5AC561) // Green color when not focused
                    ))

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMeIsChecked,
                    onCheckedChange = {
                      rememberMeIsChecked = it
                      Toast.makeText(context, "Not implemented yet", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.size(24.dp),
                    colors =
                        CheckboxDefaults.colors(
                            checkmarkColor = Color.White,
                            uncheckedColor = Color(90, 197, 97),
                            checkedColor = Color(90, 197, 97)))
                Text(text = " Remember me", modifier = Modifier.testTag("rememberMeCheckbox"))
              }

              ClickableText(
                  text = AnnotatedString("Forgot password?"),
                  onClick = { navigationActions.navigateTo(Screen.FORGOT_PASSWORD) },
                  style = TextStyle(color = Color.Gray, textDecoration = TextDecoration.Underline),
                  modifier = Modifier.testTag("forgotPasswordLink"))
            }

        Spacer(modifier = Modifier.height(16.dp))

        SignInButton(
            email = email,
            password = password,
            isFormComplete = isFormComplete,
            goodFormEmail = goodFormEmail,
            passwordLengthComplete = passwordLengthComplete,
            authViewModel = authViewModel,
            onSuccess = onSuccess,
            onFailure = onFailure)

        Spacer(modifier = Modifier.height(16.dp))

        Text("OR", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("I'm new user, ", color = Color.Gray)
          ClickableText(
              text = AnnotatedString("Sign up"),
              onClick = { navigationActions.navigateTo(Screen.SIGN_UP) },
              style = TextStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
              modifier = Modifier.testTag("signUpLink"))
        }
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
                        Brush.horizontalGradient(
                            colors = listOf(Color(0, 200, 83), Color(0, 153, 255)))
                      } else {
                        Brush.horizontalGradient(colors = listOf(Color.Gray, Color.Gray))
                      },
                  shape = RoundedCornerShape(25.dp))
              .testTag("signInButton")) {
        Text("Sign in", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), // Button color
      shape = RoundedCornerShape(50),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag("googleSignInButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier = Modifier.size(30.dp).padding(end = 8.dp))
              Text(text = "Sign In with Google", color = Color.Gray, fontSize = 16.sp)
              Spacer(Modifier.size(25.dp))
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
