package com.android.solvit.shared.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.R
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

@Composable
fun OpeningScreen(navigationActions: NavigationActions) {
  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFFFFF)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Image(
              painter = painterResource(id = R.drawable.logosolvit_firstpage),
              contentDescription = null,
              modifier = Modifier.size(200.dp).testTag("appLogo"))
          Spacer(modifier = Modifier.height(7.dp))
          Text(
              text =
                  buildAnnotatedString {
                    append(
                        AnnotatedString(
                            "Solv",
                            spanStyle =
                                SpanStyle(color = Color(51, 51, 51), fontWeight = FontWeight.Bold)))
                    append(
                        AnnotatedString(
                            "it",
                            spanStyle =
                                SpanStyle(
                                    color = Color(64, 165, 72), fontWeight = FontWeight.Bold)))
                  },
              fontSize = 60.sp,
              modifier = Modifier.testTag("appName"))
          Spacer(modifier = Modifier.height(175.dp))
          Text(text = "Your Problem, Our Priority", fontSize = 18.sp, color = Color(102, 102, 102), modifier = Modifier.testTag("tagline"))
          Text(
              text = "Tap to Continue",
              fontSize = 18.sp,
              textDecoration = TextDecoration.Underline,
              color = Color(0, 200, 83),
              modifier =
                  Modifier.clickable { navigationActions.navigateTo(Screen.SIGN_IN) }
                      .testTag("ctaButton"))
        }
  }
}
