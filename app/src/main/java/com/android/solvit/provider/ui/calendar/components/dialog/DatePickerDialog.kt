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

/**
 * A composable function that displays a date picker dialog with customizable actions for confirming
 * or dismissing the selection. It leverages Material Design components for modern UI.
 *
 * @param onDismissRequest Callback triggered when the dialog is dismissed without selection.
 * @param onDateSelected Callback invoked when a date is selected and confirmed.
 * @param selectedDate The initially selected date displayed in the date picker.
 * @param modifier Modifier for customizing the appearance and layout of the dialog.
 * @OptIn(ExperimentalMaterial3Api::class) Indicates usage of experimental Material 3 API features.
 */
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
