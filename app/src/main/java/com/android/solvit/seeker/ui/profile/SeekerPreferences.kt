package com.android.solvit.seeker.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.shared.ui.theme.Typography

/**
 * Composable function to display a list of suggestions for the user to choose from. This allows the
 * user to select preferences like services (e.g., plumbing, event planning) that they might like to
 * use or provide.
 *
 * The function retrieves the user's current preferences and displays them in a grid of selectable
 * items. The user can click an item to either add or remove it from their preferences.
 *
 * @param userId The unique identifier of the user whose preferences are being managed.
 * @param viewModel The ViewModel instance managing the user's profile data and preferences.
 */
@Composable
fun Preferences(userId: String, viewModel: SeekerProfileViewModel) {
  // State to track selected items
  val selectedItems by viewModel.userPreferences.collectAsStateWithLifecycle()

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start) {
        Text(
            text = "You might like;",
            modifier = Modifier.align(Alignment.Start).testTag("preferences_title"),
            style =
                Typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ))
        Spacer(modifier = Modifier.height(8.dp))
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

        // Suggestion Grid
        SuggestionsGrid(
            suggestions = allSuggestions,
            selectedItems = selectedItems.toSet(),
            onSuggestionClick = { suggestion ->
              if (selectedItems.contains(suggestion)) {
                // If item is selected, remove it
                viewModel.deleteUserPreference(userId, suggestion)
              } else {
                // If item is not selected, add it
                viewModel.addUserPreference(userId, suggestion)
              }
            })
      }
}

/**
 * A Composable function to display the suggestions in a grid. The grid divides the suggestions into
 * rows of two items each.
 *
 * @param suggestions A list of suggestion items to be displayed.
 * @param selectedItems A set of currently selected items.
 * @param onSuggestionClick Callback to handle when a suggestion is clicked.
 */
@Composable
fun SuggestionsGrid(
    suggestions: List<String>,
    selectedItems: Set<String>,
    onSuggestionClick: (String) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth().testTag("preference_button")) {
    val chunkedSuggestions = suggestions.chunked(2) // Divide into rows of 2 items
    chunkedSuggestions.forEach { rowItems ->
      Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.fillMaxWidth()) {
        rowItems.forEach { suggestion ->
          SuggestionButton(
              text = suggestion,
              isSelected = selectedItems.contains(suggestion),
              onClick = { onSuggestionClick(suggestion) })
        }
      }
    }
  }
}

/**
 * A Composable function to display a button for each suggestion. The button's background color
 * changes based on whether it is selected or not.
 *
 * @param text The text to display on the button.
 * @param isSelected Boolean flag to indicate if the suggestion is selected.
 * @param onClick Callback to handle the click event of the suggestion button.
 */
@Composable
fun SuggestionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
  // Update color based on selection state
  val backgroundColor = if (isSelected) colorScheme.secondary else colorScheme.surfaceVariant
  val contentColor = if (isSelected) colorScheme.onSecondary else colorScheme.onBackground

  Surface(
      modifier = Modifier.padding(8.dp).clickable { onClick() },
      shape = RoundedCornerShape(16.dp),
      color = backgroundColor) {
        Text(
            text = text,
            style = Typography.bodyMedium.copy(color = contentColor),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
      }
}
