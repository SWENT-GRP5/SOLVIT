package com.android.solvit.provider.ui.calendar.components.dialog

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.solvit.shared.ui.theme.Typography
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
  val datePickerState =
      rememberDatePickerState(
          initialSelectedDateMillis =
              selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())

  DatePickerDialog(
      onDismissRequest = onDismissRequest,
      confirmButton = {
        TextButton(
            onClick = {
              datePickerState.selectedDateMillis?.let { millis ->
                val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                onDateSelected(date)
              }
            },
            modifier = Modifier.testTag("confirmDateButton")) {
              Text("OK", color = colorScheme.primary, style = Typography.bodyLarge)
            }
      },
      dismissButton = {
        TextButton(onClick = onDismissRequest, modifier = Modifier.testTag("cancelDateButton")) {
          Text("Cancel", color = colorScheme.primary, style = Typography.bodyLarge)
        }
      },
      modifier = modifier.testTag("datePickerDialog")) {
        DatePicker(state = datePickerState, modifier = Modifier.testTag("datePicker"))
      }
}
