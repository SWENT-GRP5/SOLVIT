package com.android.solvit.shared.ui.authentication

import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
import com.android.solvit.seeker.ui.profile.Stepper
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpChooseProfile(
    navigationActions: NavigationActions,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {

  val backgroundColor = Color(0xFFFFFFFF)
  val context = LocalContext.current

  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Stepper(currentStep = 1, isFormComplete = false) },
            navigationIcon = { GoBackButton(navigationActions) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor))
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.background(backgroundColor)
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
              VerticalSpacer(80.dp)

              Image(
                  painter = painterResource(id = R.drawable.sign_up_choose_profile_logo),
                  contentDescription = "Illustration",
                  modifier = Modifier.size(300.dp).testTag("roleIllustration"))

              SectionTitle(text = "Sign up as :", testTag = "signUpAsTitle")

              VerticalSpacer(height = 30.dp)

              ButtonCustomerProvider(
                  text = "Customer",
                  description = "I want to request services.",
                  testTag = "customerButton",
                  onClickButton = {
                    authViewModel.setRole("seeker")
                    if (authViewModel.googleAccount.value == null) {
                      authViewModel.registerWithEmailAndPassword(
                          { navigationActions.navigateTo(Screen.SEEKER_REGISTRATION_PROFILE) }, {})
                    } else {
                      authViewModel.registerWithGoogle(
                          { navigationActions.navigateTo(Screen.SEEKER_REGISTRATION_PROFILE) }, {})
                    }
                  })

              VerticalSpacer(height = 16.dp)

              Text(text = "OR")

              VerticalSpacer(height = 16.dp)

              ButtonCustomerProvider(
                  text = "Professional",
                  description = "I want to offer services.",
                  testTag = "professionalButton",
                  onClickButton = {
                    authViewModel.setRole("provider")
                    if (authViewModel.googleAccount.value == null) {
                      authViewModel.registerWithEmailAndPassword(
                          { navigationActions.navigateTo(Screen.PROVIDER_REGISTRATION_PROFILE) },
                          {})
                    } else {
                      authViewModel.registerWithGoogle(
                          { navigationActions.navigateTo(Screen.PROVIDER_REGISTRATION_PROFILE) },
                          {})
                    }
                  })

              VerticalSpacer(height = 30.dp)

              LearnMoreSection()
            }
      })
}

@Composable
fun ButtonCustomerProvider(
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
                                colors = listOf(Color(0, 200, 81), Color(0, 153, 255))),
                        shape = RoundedCornerShape(10.dp))
                    .clickable { onClickButton() },
            contentAlignment = Alignment.Center) {
              Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = description, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
      }
}

@Composable
fun SectionTitle(text: String, testTag: String = "") {
  Text(
      text = text,
      fontSize = 25.sp,
      color = Color(51, 51, 51),
      modifier = Modifier.testTag(testTag))
}

@Composable
fun LearnMoreSection() {
  val context = LocalContext.current // Obtenez le contexte ici

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Not sure? ", color = Color.Gray)
          Text(
              text = "Learn more",
              color = Color(0, 153, 255),
              style = TextStyle(textDecoration = TextDecoration.Underline),
              modifier =
                  Modifier.clickable {
                        Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
                      }
                      .testTag("learnMoreLink"))
          Text(" about becoming a", color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text("Customer or Provider.", color = Color.Gray)
      }
}
