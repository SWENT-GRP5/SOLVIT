package com.android.solvit.shared.ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.solvit.shared.model.chat.ChatAssistantViewModel

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
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  TONES_LIST.forEach { tone ->
                    ToneItem(tone, { selectedTones += tone }, { selectedTones -= tone })
                  }
                }

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Optional additional infos") },
                shape = RoundedCornerShape(8.dp))

            Button(
                onClick = {
                  chatAssistantViewModel.updateSelectedTones(selectedTones)
                  chatAssistantViewModel.generateMessage(input) {
                    onResponse(it)
                    onDismiss()
                  }
                }) {
                  Text("Generate Response")
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
          Modifier.clickable {
                isSelected = !isSelected
                if (isSelected) onSelect(tone) else onUnselect(tone)
              }
              .border(
                  1.dp, if (isSelected) Color.Black else Color.LightGray, RoundedCornerShape(8.dp))
              .padding(8.dp),
      color = if (isSelected) Color.Black else Color.Gray)
}
