package com.android.solvit.shared.ui.authentication

import android.content.pm.ActivityInfo
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.solvit.R
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
            title = { Text("") },
            navigationIcon = {
              Icon(
                  Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "goBackButton",
                  modifier =
                      Modifier.testTag("backButton").clickable { navigationActions.goBack() })
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor))
      },
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {}
      })

  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        VerticalSpacer(50.dp)

        Stepper(currentStep = 1, isFormComplete = false)

        VerticalSpacer(height = 30.dp)

        // Illustration
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
                    { navigationActions.navigateTo(Screen.PROVIDER_REGISTRATION_PROFILE) }, {})
              } else {
                authViewModel.registerWithGoogle(
                    { navigationActions.navigateTo(Screen.PROVIDER_REGISTRATION_PROFILE) }, {})
              }
            })

        VerticalSpacer(height = 30.dp)

        LearnMoreSection()
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(onBackClick: () -> Unit = {}, testTag: String = "") {
  TopAppBar(
      title = { Text("") },
      navigationIcon = {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.testTag(testTag).clickable { onBackClick() })
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
fun Stepper(currentStep: Int, isFormComplete: Boolean) {
  val stepLabels = listOf("Role", "Details", "Preferences")
  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        stepLabels.forEachIndexed { index, label ->
          StepCircle(
              stepNumber = index + 1,
              isCompleted = (index == 0 && isFormComplete) || currentStep > index + 1,
              label = label)
          if (index < stepLabels.size - 1) {
            Spacer(modifier = Modifier.width(8.dp))
          }
        }
      }
}

@Composable
fun StepCircle(stepNumber: Int, isCompleted: Boolean, label: String) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.widthIn(min = 80.dp)) {
        Box(
            modifier =
                Modifier.size(40.dp)
                    .background(
                        color = if (isCompleted) Color(0xFF28A745) else Color.Gray,
                        shape = CircleShape),
            contentAlignment = Alignment.Center) {
              Text(text = if (isCompleted) "✔" else stepNumber.toString(), color = Color.White)
            }

        Text(
            text = label,
            color = Color.Black,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            softWrap = false)
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
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Not sure? ", color = Color.Gray)
          ClickableText(
              text = AnnotatedString("Learn more"),
              onClick = { /* TODO: Learn more action */},
              style =
                  TextStyle(color = Color(0, 153, 255), textDecoration = TextDecoration.Underline),
              modifier = Modifier.testTag("learnMoreLink"))
          Text(" about becoming a", color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Customer or Provider.", color = Color.Gray)
      }
}
