package com.android.solvit.seeker.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel

@Composable
fun Preferences(userId: String, viewModel: SeekerProfileViewModel) {
  // State to track selected items
  val selectedItems by viewModel.userPreferences.collectAsState()

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start) {
        Text(
            text = "You might like;",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).testTag("preferences_title"))
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
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            color = contentColor)
      }
}
