package com.android.solvit.shared.ui.authentication

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
  val email = remember { mutableStateOf("") }

  val context = LocalContext.current
  val launcher =
      googleRegisterLauncher(
          authViewModel, { navigationActions.navigateTo(Screen.SIGN_UP_CHOOSE_ROLE) }, {})
  val token = stringResource(R.string.default_web_client_id)

  Column(
      modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFFFF)).padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
              Icon(
                  Icons.Filled.ArrowBack,
                  contentDescription = "Back",
                  modifier =
                      Modifier.testTag("backButton").clickable { navigationActions.goBack() })
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFFFF)))

        Image(
            painter = painterResource(id = R.drawable.sign_up_image),
            contentDescription = "Logo",
            modifier = Modifier.size(250.dp).testTag("signUpIllustration"))

        ScreenTitle("Sign up", "signUpTitle")
        VerticalSpacer(height = 50.dp)

        // Social Sign Up Buttons
        SocialSignUpButton(
            "Sign Up with Facebook",
            R.drawable.facebook_logo,
            "facebookSignUpButton",
            Color(24, 119, 242)) { /* Facebook sign up */}
        VerticalSpacer(height = 10.dp)
        SocialSignUpButton(
            "Sign Up with Google",
            R.drawable.google_logo,
            "googleSignUpButton",
            Color.White,
            Color.Black,
            Color.Gray) {
              authViewModel.setRole("seeker")
              val gso =
                  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                      .requestIdToken(token)
                      .requestEmail()
                      .build()
              val googleSignInClient = GoogleSignIn.getClient(context, gso)
              launcher.launch(googleSignInClient.signInIntent)
            }
        VerticalSpacer(height = 10.dp)
        SocialSignUpButton(
            "Sign Up with Apple",
            R.drawable.apple_logo,
            "appleSignUpButton",
            Color.Black) { /* Apple sign up */}

        VerticalSpacer(height = 10.dp)
        Text("OR", color = Color.Gray)
        VerticalSpacer(height = 10.dp)

        CustomOutlinedTextField(
            "Enter your email address", email.value, onValueChange = { email.value = it })
        VerticalSpacer(height = 50.dp)

        SignUpButton {
          if (email.value.isNotEmpty()) {
            authViewModel.setEmail(email.value)
            navigationActions.navigateTo(Screen.SIGN_UP_CHOOSE_ROLE)
          }
        }
        VerticalSpacer(height = 16.dp)

        AlreadyHaveAccountText()
      }
}

@Composable
fun VerticalSpacer(height: Dp) {
  Spacer(modifier = Modifier.height(height))
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
fun CustomOutlinedTextField(label: String, value: String, onValueChange: (String) -> Unit) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("emailInputField"),
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
              focusedBorderColor = Color(0xFF5AC561), unfocusedBorderColor = Color(0xFF5AC561)))
}

@Composable
fun SignUpButton(onClick: () -> Unit) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(48.dp)
              .background(
                  brush =
                      Brush.horizontalGradient(
                          colors = listOf(Color(0, 200, 81), Color(0, 153, 255))),
                  shape = RoundedCornerShape(8.dp))
              .clickable { onClick() }
              .testTag("signUpButton"),
      contentAlignment = Alignment.Center) {
        Text("Sign up", color = Color.White, fontWeight = FontWeight.Bold)
      }
}

@Composable
fun AlreadyHaveAccountText() {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text("Already have an account? ", color = Color.Gray)
    ClickableText(
        text = AnnotatedString("Log up in here!"),
        onClick = { /* TODO: Navigate to Log In screen */},
        style = TextStyle(color = Color(147, 168, 255), textDecoration = TextDecoration.Underline),
        modifier = Modifier.testTag("logInLink"))
  }
}

@Composable
fun SocialSignUpButton(
    text: String,
    logoResId: Int,
    testTag: String = "",
    backgroundColor: Color,
    textColor: Color = Color.White,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit = {}
) {
  Button(
      onClick = onClick,
      modifier =
          Modifier.fillMaxWidth()
              .height(48.dp)
              .border(BorderStroke(1.dp, borderColor), shape = RoundedCornerShape(8.dp))
              .testTag(testTag),
      colors = ButtonDefaults.buttonColors(backgroundColor),
      shape = RoundedCornerShape(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
              Icon(
                  painter = painterResource(id = logoResId),
                  contentDescription = null,
                  tint = Color.Unspecified,
                  modifier = Modifier.size(25.dp))
              Text(
                  text = text,
                  color = textColor,
                  fontSize = 16.sp,
                  modifier = Modifier.padding(start = 8.dp))
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
