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
