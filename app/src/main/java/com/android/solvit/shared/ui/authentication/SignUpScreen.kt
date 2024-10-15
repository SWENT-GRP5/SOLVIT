package com.android.solvit.shared.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.R
import com.android.solvit.shared.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navigationActions: NavigationActions) {
  val email = remember { mutableStateOf("") }

  Column(
      modifier =
          Modifier.fillMaxWidth().background(Color(0xFFFFFFFF)).padding(start = 16.dp, end = 16.dp),
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFFFF)),
        )

        // Rest of your UI components
        Image(
            painter = painterResource(id = R.drawable.sign_up_image),
            contentDescription = "Logo",
            modifier = Modifier.size(250.dp).testTag("signUpIllustration"))

        Text(
            text = "Sign up",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("signUpTitle"))

        Spacer(modifier = Modifier.height(50.dp))

        // Facebook Button
        SocialSignUpButton(
            text = "Sign Up with Facebook",
            logoResId = R.drawable.facebook_logo,
            backgroundColor = Color(24, 119, 242),
            onClick = { /* Facebook sign up */},
            testTag = "facebookSignUpButton")

        Spacer(modifier = Modifier.height(8.dp))

        // Google Button
        Box(modifier = Modifier.border(1.dp, Color(174, 171, 171), RoundedCornerShape(8.dp))) {
          SocialSignUpButton(
              text = "Sign Up with Google",
              logoResId = R.drawable.google_logo,
              backgroundColor = Color.White,
              textColor = Color.Black,
              onClick = { /* Google sign up */},
              testTag = "googleSignUpButton")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Apple Button
        SocialSignUpButton(
            text = "Sign Up with Apple",
            logoResId = R.drawable.apple_logo,
            backgroundColor = Color.Black,
            onClick = { /* Apple sign up */},
            testTag = "appleSignUpButton")

        Spacer(modifier = Modifier.height(10.dp))

        Text("OR", color = Color.Gray)

        Spacer(modifier = Modifier.height(10.dp))

        // Email TextField (Outlined)
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Enter your email address") },
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
                    focusedBorderColor = Color(0xFF5AC561), // Green color when focused
                    unfocusedBorderColor = Color(0xFF5AC561) // Green color when not focused
                    ))

        Spacer(modifier = Modifier.height(50.dp))

        // Sign Up Button
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors = listOf(Color(0, 200, 81), Color(0, 153, 255))),
                        shape = RoundedCornerShape(8.dp))
                    .clickable { // TODO: Sign up
                    }
                    .testTag("signUpButton"),
            contentAlignment = Alignment.Center) {
              Text("Sign up", color = Color.White, fontWeight = FontWeight.Bold)
            }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Already have an account? ", color = Color.Gray)
          ClickableText(
              text = AnnotatedString("Log up in here!"),
              onClick = { // TODO: Navigate to Log In screen
              },
              style =
                  TextStyle(
                      color = Color(147, 168, 255), textDecoration = TextDecoration.Underline),
              modifier = Modifier.testTag("logInLink"))
        }
      }
}

@Composable
fun SocialSignUpButton(
    text: String,
    logoResId: Int,
    backgroundColor: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit,
    testTag: String
) {
  Spacer(modifier = Modifier.height(10.dp))
  Button(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth().height(48.dp).testTag(testTag),
      colors = ButtonDefaults.buttonColors(backgroundColor),
      shape = RoundedCornerShape(8.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    painter = painterResource(id = logoResId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(25.dp))
              }
          Text(
              text = text,
              color = textColor,
              fontSize = 16.sp,
              modifier = Modifier.align(Alignment.Center))
        }
      }
  Spacer(modifier = Modifier.height(10.dp))
}
