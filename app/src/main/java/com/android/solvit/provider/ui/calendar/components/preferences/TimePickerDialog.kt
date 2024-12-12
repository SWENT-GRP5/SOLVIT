package com.android.solvit.provider.ui.calendar.components.preferences

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    initialTime: LocalTime,
    title: String? = null
) {
  val selectedHour by remember { mutableIntStateOf(initialTime.hour) }
  val selectedMinute by remember { mutableIntStateOf(initialTime.minute) }

  val timePickerState =
      rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)

  AlertDialog(
      onDismissRequest = onDismiss,
      title = title?.let { { Text(it) } },
      confirmButton = {
        TextButton(
            onClick = {
              onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
              Text("OK")
            }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
      text = { TimePicker(state = timePickerState, layoutType = TimePickerLayoutType.Vertical) },
      modifier = Modifier.testTag(("timePickerDialog")))
}
