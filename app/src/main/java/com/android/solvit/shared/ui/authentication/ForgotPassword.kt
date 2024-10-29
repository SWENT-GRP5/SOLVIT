package com.android.solvit.shared.ui.authentication

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassword(navigationActions: NavigationActions) {
  var email by remember { mutableStateOf("") }
  val context = LocalContext.current

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
            modifier = Modifier.testTag("topAppBar"))
      },
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {}
      })

  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        VerticalSpacer(60.dp)

        Image(
            painter = rememberAsyncImagePainter(R.drawable.passwordforgot),
            contentDescription = "Checkmark",
            modifier = Modifier.size(240.dp).testTag("forgotPasswordImage"))

        VerticalSpacer(30.dp)

        Text(
            text = "Please enter your email address to reset your password",
            color = Color.DarkGray,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 4.dp).fillMaxWidth().testTag("bigText"))

        VerticalSpacer(height = 10.dp)

        EmailTextField(
            email, onValueChange = { email = it }, "Enter your email address", "emailInputField")

        VerticalSpacer(height = 20.dp)

        Button(
            onClick = {
              if (email.isEmpty()) {
                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_LONG).show()
              } else if (!(email.contains("@") && email.contains("."))) {
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
                            if (email.isNotBlank() && email.contains("@") && email.contains(".")) {
                              Brush.horizontalGradient(
                                  colors = listOf(Color(0, 200, 83), Color(0, 153, 255)))
                            } else {
                              Brush.horizontalGradient(colors = listOf(Color.Gray, Color.Gray))
                            },
                        shape = RoundedCornerShape(25.dp))
                    .testTag("Send reset link")) {
              Text(
                  "Send reset link",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp)
            }
      }
}
