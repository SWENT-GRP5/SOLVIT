package com.android.solvit.seeker.ui.profile

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox

/**
 * A Composable function for the "Edit Preferences" screen.
 *
 * This screen allows users to view and modify their preferences by adding or removing items from a
 * list. It consists of a top app bar, an image, a list of currently selected preferences, and
 * suggestions for additional preferences.
 *
 * @param userId The ID of the current user whose preferences are being edited.
 * @param viewModel The ViewModel responsible for handling the user's preferences data.
 * @param navigationActions An object that handles navigation actions (e.g., going back to the
 *   previous screen).
 */
@Composable
fun EditPreferences(
    userId: String,
    viewModel: SeekerProfileViewModel,
    navigationActions: NavigationActions
) {

  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }

  // Collect the current user preferences from the ViewModel
  val selectedItems by viewModel.userPreferences.collectAsStateWithLifecycle()

  // Fetch preferences when the screen is composed
  LaunchedEffect(Unit) {
    if (selectedItems.isEmpty()) { // Only fetch from Firestore if local state is empty
      viewModel.getUserPreferences(userId)
    }
  }

  Scaffold(
      topBar = {
        TopAppBarInbox(
            title = "Edit Preferences",
            testTagTitle = "edit_preferences_title",
            leftButtonAction = { navigationActions.goBack() },
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack,
            testTagLeft = "goBackButton")
      }) { paddingValues ->
        // Main content of the screen with padding
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Main content of the screen with padding
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(16.dp)
                      .padding(bottom = 64.dp), // Add extra padding for the button
              verticalArrangement = Arrangement.Top,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.pref),
                    contentDescription = "Edit Preferences Image",
                    modifier =
                        Modifier.size(100.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("preferencesIllustration"))
                // User's existing content...
                val allSuggestions =
                    listOf(
                        "🔧 Plumbing",
                        "⚡ Electrical Work",
                        "📚 Tutoring",
                        "🎉 Event Planning",
                        "💇 Hair Styling",
                        "🧹 Cleaning",
                        "🪚 Carpentry",
                        "📸 Photography",
                        "🏋️ Personal Training",
                        "✍️ Writing")
                val remainingSuggestions = allSuggestions.filterNot { it in selectedItems }

                Text(
                    text = "Selected preferences:",
                    modifier =
                        Modifier.align(Alignment.Start).testTag("selected_preferences_title"),
                    style =
                        Typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ))
                Spacer(modifier = Modifier.height(8.dp))

                SuggestionsGrid2(
                    suggestions = selectedItems,
                    selectedItems = selectedItems.toSet(),
                    onSuggestionClick = { suggestion ->
                      viewModel.deleteUserPreference(userId, suggestion)
                    })

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You might like:",
                    modifier =
                        Modifier.align(Alignment.Start).testTag("available_preferences_title"),
                    style =
                        Typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ))
                Spacer(modifier = Modifier.height(8.dp))

                SuggestionsGrid2(
                    suggestions = remainingSuggestions,
                    selectedItems = selectedItems.toSet(),
                    onSuggestionClick = { suggestion ->
                      viewModel.addUserPreference(userId, suggestion)
                    })
              }

          // The button always at the bottom
          Button(
              onClick = { navigationActions.goBack() },
              modifier =
                  Modifier.align(Alignment.BottomCenter)
                      .height(55.dp)
                      .width(250.dp)
                      .testTag("updatePreferencesButton")
                      .clip(
                          RoundedCornerShape(
                              topStart = 15.dp,
                              topEnd = 0.dp,
                              bottomStart = 0.dp,
                              bottomEnd = 0.dp)),
              colors = ButtonDefaults.buttonColors(colorScheme.secondary)) {
                Text(text = "Update Preferences", style = Typography.bodyLarge)
              }
        }
      }
}

/**
 * A Composable function to display suggestions in a grid format, with each suggestion as a
 * clickable button.
 *
 * @param suggestions The list of suggestions to be displayed.
 * @param selectedItems The set of items that are currently selected by the user.
 * @param onSuggestionClick A callback function that is called when a suggestion is clicked (to add
 *   or remove it).
 */
@Composable
fun SuggestionsGrid2(
    suggestions: List<String>,
    selectedItems: Set<String>,
    onSuggestionClick: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    val chunkedSuggestions = suggestions.chunked(2) // Divide into rows of 2 items
    chunkedSuggestions.forEach { rowItems ->
      Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
        rowItems.forEach { suggestion ->
          SuggestionButton2(
              text = suggestion,
              isSelected = selectedItems.contains(suggestion),
              onClick = { onSuggestionClick(suggestion) })
        }
      }
    }
  }
}

/**
 * A Composable function that represents each suggestion as a button with a selectable state.
 *
 * @param text The text to be displayed on the button.
 * @param isSelected Whether the button is selected or not.
 * @param onClick The action to perform when the button is clicked.
 */
@Composable
fun SuggestionButton2(text: String, isSelected: Boolean, onClick: () -> Unit) {
  // Update color based on selection state
  val backgroundColor = if (isSelected) colorScheme.secondary else colorScheme.surfaceVariant
  val contentColor = if (isSelected) colorScheme.onSecondary else colorScheme.onBackground

  Surface(
      modifier = Modifier.padding(8.dp).clickable { onClick() },
      shape = RoundedCornerShape(16.dp),
      color = backgroundColor) {
        Text(
            text = text,
            style = Typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            color = contentColor)
      }
}
