package com.github.se.bootcamp.ui.authentication

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.android.solvit.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.ui.navigation.NavigationActions
import com.android.solvit.ui.navigation.Route
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun TitleWithIcon(){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            modifier = Modifier.testTag("loginTitle"),
            text = "Solv",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Box{
            Text(
                text = "it",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color(android.graphics.Color.parseColor("#3DFC9A")) // Green color for "it"
            )
            // Icon overlaying the "it" text, aligned at the top right
            Image(
                painter = painterResource(id = R.drawable.check_arrow), // Replace with your check vector
                contentDescription = "Checkmark",
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = 12.dp, y = -8.dp) // Position the icon at the top right
            )
        }
    }
}
@Composable
fun SignInScreen(navigationActions: NavigationActions) {
    val context = LocalContext.current

    val launcher =
        rememberFirebaseAuthLauncher(
            onAuthComplete = { result ->
                Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")
                Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
                navigationActions.navigateTo(Route.SERVICES)
            },
            onAuthError = {
                Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}")
                Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
            })
    val token = stringResource(R.string.default_web_client_id)
    // The main container for the screen

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // App Logo Image
                Image(
                    painter = painterResource(id = R.drawable.solvit_logo ), // Ensure this drawable exists
                    contentDescription = "App Logo",
                    modifier = Modifier.size(250.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Text
                /*Text(
                    modifier = Modifier.testTag("loginTitle"),
                    text = "Welcome",
                    style =
                    MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp, lineHeight = 64.sp),
                    fontWeight = FontWeight.Bold,
                    // center the text

                    textAlign = TextAlign.Center)*/
                TitleWithIcon()

                Spacer(modifier = Modifier.height(48.dp))

                // Authenticate With Google Button
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
        })
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
    Button(
        onClick = onSignInClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Button color
        shape = RoundedCornerShape(50), // Circular edges for the button
        border = BorderStroke(1.dp, Color.LightGray),
        modifier =
        Modifier.padding(8.dp)
            .height(48.dp) // Adjust height as needed
            .testTag("loginButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
            // Load the Google logo from resources
            Image(
                painter =
                painterResource(id = R.drawable.google_logo),
                contentDescription = "Google Logo",
                modifier =
                Modifier.size(30.dp) // Size of the Google logo
                    .padding(end = 8.dp))

            // Text for the button
            Text(
                text = "Sign in with Google",
                color = Color.Gray, // Text color
                fontSize = 16.sp, // Font size
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        } catch (e: ApiException) {
            onAuthError(e)
        }
    }
}