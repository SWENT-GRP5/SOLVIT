package com.android.solvit.provider.ui.calendar.components.preferences

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    initialTime: LocalTime = LocalTime.now(),
    title: String? = null
) {
  val selectedHour by remember { mutableIntStateOf(initialTime.hour) }
  val selectedMinute by remember { mutableIntStateOf(initialTime.minute) }

  val timePickerState =
      rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)

  AlertDialog(
      onDismissRequest = onDismissRequest,
      title = title?.let { { Text(it) } },
      modifier = Modifier.testTag("time_picker_dialog"),
      confirmButton = {
        TextButton(
            onClick = {
              onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            },
            modifier = Modifier.testTag("time_picker_confirm_button")) {
              Text("OK")
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismissRequest, modifier = Modifier.testTag("time_picker_dismiss_button")) {
              Text("Cancel")
            }
      },
      text = { TimePicker(state = timePickerState, layoutType = TimePickerLayoutType.Vertical) },
  )
}
