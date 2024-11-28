package com.android.solvit.shared.ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.solvit.R
import com.android.solvit.shared.model.chat.ChatAssistantViewModel
import kotlinx.coroutines.delay

val TONES_LIST = listOf("Formal", "Neutral", "Friendly", "Positive", "Negative", "Professional")

@Composable
fun ChatAssistantDialog(
    chatAssistantViewModel: ChatAssistantViewModel,
    onDismiss: () -> Unit,
    onResponse: (String) -> Unit
) {
  var input by remember { mutableStateOf("") }
  var selectedTones by remember { mutableStateOf(emptyList<String>()) }
  Dialog(onDismissRequest = onDismiss) {
    Surface(shape = RoundedCornerShape(32.dp), shadowElevation = 8.dp) {
      Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally) {
            // AI chat assistant Logo
            Icon(
                painter = painterResource(R.drawable.ai_stars),
                contentDescription = "AI Logo",
                modifier = Modifier.size(64.dp).testTag("aiLogo"))

            // Tones selection row
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).testTag("tonesRow"),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  TONES_LIST.forEach { tone ->
                    ToneItem(tone, { selectedTones += tone }, { selectedTones -= tone })
                  }
                }

            // Additional infos input field
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.testTag("inputField"),
                label = { Text("Optional additional infos") },
                shape = RoundedCornerShape(8.dp))

            // Generate response button
            var isGenerating by remember { mutableStateOf(false) }
            Button(
                onClick = {
                  chatAssistantViewModel.updateSelectedTones(selectedTones)
                  isGenerating = true
                  chatAssistantViewModel.generateMessage(input) {
                    onResponse(it)
                    onDismiss()
                  }
                },
                modifier = Modifier.fillMaxWidth(.6f).testTag("generateButton")) {
                  if (isGenerating) {
                    var dotCount by remember { mutableStateOf(1) }
                    LaunchedEffect(Unit) {
                      while (isGenerating) {
                        dotCount = (dotCount % 3) + 1
                        delay(500)
                      }
                    }
                    Text(".".repeat(dotCount))
                  } else {
                    Text("Generate Response")
                  }
                }
          }
    }
  }
}

@Composable
fun ToneItem(tone: String, onSelect: (String) -> Unit, onUnselect: (String) -> Unit) {
  var isSelected by remember { mutableStateOf(false) }
  Text(
      text = tone,
      modifier =
          Modifier.border(
                  1.dp, if (isSelected) Color.Black else Color.LightGray, RoundedCornerShape(8.dp))
              .clickable {
                isSelected = !isSelected
                if (isSelected) onSelect(tone) else onUnselect(tone)
              }
              .padding(8.dp)
              .testTag("toneItem$tone"),
      color = if (isSelected) Color.Black else Color.Gray)
}
