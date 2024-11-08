package com.android.solvit.shared.ui.authentication

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.android.solvit.R
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Screen

@Composable
fun OpeningScreen(navigationActions: NavigationActions) {
  val configuration = LocalConfiguration.current
  if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
    OpeningScreenPortrait(navigationActions)
  } else {
    OpeningScreenLandscape(navigationActions)
  }
}

@Composable
fun OpeningScreenPortrait(navigationActions: NavigationActions) {
  Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
    Column(
        modifier = Modifier.fillMaxSize().background(colorScheme.background).padding(16.dp),
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
                                SpanStyle(
                                    color = colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold)))
                    append(
                        AnnotatedString(
                            "it",
                            spanStyle =
                                SpanStyle(
                                    color = colorScheme.secondary, fontWeight = FontWeight.Bold)))
                  },
              fontSize = 60.sp,
              modifier = Modifier.testTag("appName"))
          Spacer(modifier = Modifier.height(175.dp))
          Text(
              text = "Your Problem, Our Priority",
              fontSize = 18.sp,
              color = colorScheme.onSurfaceVariant,
              modifier = Modifier.testTag("tagline"))
          Text(
              text = "Tap to Continue",
              fontSize = 18.sp,
              textDecoration = TextDecoration.Underline,
              color = colorScheme.secondary,
              modifier =
                  Modifier.clickable { navigationActions.navigateTo(Screen.SIGN_IN) }
                      .testTag("ctaButton"))
        }
  }
}

@Composable
fun OpeningScreenLandscape(navigationActions: NavigationActions) {
  Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
    Row(
        modifier = Modifier.fillMaxSize().background(colorScheme.background).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly) {
          // Logo Image
          Image(
              painter = painterResource(id = R.drawable.logosolvit_firstpage),
              contentDescription = null,
              modifier = Modifier.size(200.dp).testTag("appLogo"))

          // Right Side Texts
          Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
            Spacer(modifier = Modifier.height(8.dp))

            // App Name with Colored "Solvit"
            Text(
                text =
                    buildAnnotatedString {
                      append(
                          AnnotatedString(
                              "Solv",
                              spanStyle =
                                  SpanStyle(
                                      color = colorScheme.onBackground,
                                      fontWeight = FontWeight.Bold)))
                      append(
                          AnnotatedString(
                              "it",
                              spanStyle =
                                  SpanStyle(
                                      color = colorScheme.secondary, fontWeight = FontWeight.Bold)))
                    },
                fontSize = 60.sp,
                modifier = Modifier.testTag("appName"))

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline Text
            Text(
                text = "Your Problem, Our Priority",
                fontSize = 18.sp,
                color = colorScheme.onSurface,
                modifier = Modifier.testTag("tagline"))

            Spacer(modifier = Modifier.height(8.dp))

            // Call-to-Action Button
            Text(
                text = "Tap to Continue",
                fontSize = 18.sp,
                textDecoration = TextDecoration.Underline,
                color = colorScheme.secondary,
                modifier =
                    Modifier.clickable { navigationActions.navigateTo(Screen.SIGN_IN) }
                        .testTag("ctaButton"))
          }
        }
  }
}

// Preview function for testing
@Preview(showBackground = true)
@Composable
fun PreviewOpeningScreen() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  OpeningScreenPortrait(navigationActions)
}
