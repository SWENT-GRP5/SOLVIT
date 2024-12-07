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
import androidx.compose.ui.graphics.Color
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
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "You might like;",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).testTag("preferences_title"))
        Spacer(modifier = Modifier.height(8.dp))

        // Suggestion Grid
        SuggestionsGrid(
            suggestions =
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
                    "✍️ Writing"),
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
  val backgroundColor = if (isSelected) colorScheme.secondary else Color(0xFFF1F1F1)
  val contentColor = if (isSelected) colorScheme.onSecondary else Color.Black
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
