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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.android.solvit.shared.model.provider.Language
import kotlinx.coroutines.delay

val TONES_LIST = listOf("Formal", "Neutral", "Friendly", "Positive", "Negative", "Professional")

@Composable
fun ChatAssistantDialog(
    chatAssistantViewModel: ChatAssistantViewModel,
    onDismiss: () -> Unit,
    onResponse: (String) -> Unit
) {
  var translationMode by remember { mutableStateOf(false) }
  var input by remember { mutableStateOf("") }
  var selectedTones by remember { mutableStateOf(emptyList<String>()) }
  var languageFrom by remember { mutableStateOf("") }
  var languageTo by remember { mutableStateOf("") }
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

            // Assistant mode selection row
            Row(
                modifier = Modifier.testTag("modeSelectionRow").fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  Text("Generation", modifier = Modifier.testTag("generationText"))
                  Switch(
                      checked = translationMode,
                      onCheckedChange = { translationMode = it },
                      modifier = Modifier.testTag("modeSwitch"))
                  Text("Translation", modifier = Modifier.testTag("translationText"))
                }

            if (translationMode) {
              // Language selection
              LanguageSelection(
                  onSelectFrom = { languageFrom = it }, onSelectTo = { languageTo = it })

              // Message input field
              OutlinedTextField(
                  value = input,
                  onValueChange = { input = it },
                  modifier = Modifier.testTag("messageField"),
                  label = { Text("The message to translate") },
                  shape = RoundedCornerShape(8.dp))
            } else {
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
            }

            // Generate response button
            var isGenerating by remember { mutableStateOf(false) }
            Button(
                onClick = {
                  if (translationMode) {
                    isGenerating = true
                    chatAssistantViewModel.generateTranslation(input, languageFrom, languageTo) {
                      onResponse(it)
                      onDismiss()
                    }
                  } else {
                    chatAssistantViewModel.updateSelectedTones(selectedTones)
                    isGenerating = true
                    chatAssistantViewModel.generateMessage(input) {
                      onResponse(it)
                      onDismiss()
                    }
                  }
                },
                modifier = Modifier.fillMaxWidth(.6f).testTag("generateButton")) {
                  if (isGenerating) {
                    var dotCount by remember { mutableIntStateOf(1) }
                    LaunchedEffect(Unit) {
                      while (isGenerating) {
                        dotCount = (dotCount % 3) + 1
                        delay(500)
                      }
                    }
                    Text(".".repeat(dotCount))
                  } else {
                    if (translationMode) {
                      Text("Translate Message")
                    } else {
                      Text("Generate Response")
                    }
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

@Composable
fun LanguageSelection(onSelectFrom: (String) -> Unit, onSelectTo: (String) -> Unit) {
  Column(modifier = Modifier.testTag("languageSelection")) {
    LanguageSelection(onSelect = onSelectFrom, label = "From")
    LanguageSelection(onSelect = onSelectTo, label = "To")
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelection(onSelect: (String) -> Unit, label: String) {
  val languages = Language.entries
  var expanded by remember { mutableStateOf(false) }
  var selectedLanguage by remember { mutableStateOf("") }
  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = it },
  ) {
    TextField(
        value = selectedLanguage,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        modifier = Modifier.menuAnchor().testTag("languageSelection$label"),
        colors =
            TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent))
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
      languages.forEach { language ->
        DropdownMenuItem(
            text = { Text(language.name, style = MaterialTheme.typography.bodyLarge) },
            onClick = {
              selectedLanguage = language.name
              onSelect(language.name)
              expanded = false
            },
            modifier = Modifier.testTag("languageItem${language.name}"),
            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
    }
  }
}
