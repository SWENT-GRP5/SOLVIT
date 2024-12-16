package com.android.solvit.shared.ui.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A composable function that displays a customizable outlined text field with validation and error
 * messages.
 *
 * @param value The current text value of the field.
 * @param onValueChange Lambda to update the text value.
 * @param label Optional label displayed inside the field.
 * @param placeholder Text displayed as a placeholder when the field is empty.
 * @param isValueOk Boolean indicating whether the current value passes validation.
 * @param modifier Modifier applied to the field's parent column.
 * @param errorMessage The error message displayed when the input is invalid.
 * @param leadingIcon Optional leading icon displayed inside the field.
 * @param leadingIconDescription Description for accessibility purposes.
 * @param testTag Test tag for UI testing.
 * @param errorTestTag Test tag for the error message.
 * @param maxLines The maximum number of lines the text field can display.
 * @param textAlign Text alignment for the field's content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String,
    isValueOk: Boolean,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    errorMessage: String = "Invalid input",
    leadingIcon: ImageVector? = null,
    leadingIconDescription: String = "",
    testTag: String,
    errorTestTag: String = "errorMessage",
    maxLines: Int = 1,
    textAlign: TextAlign = TextAlign.Unspecified,
    keyboardType: KeyboardType = KeyboardType.Text
) {
  // State to track if the field has been "visited" (focused and then unfocused)
  var hasBeenFocused by remember { mutableStateOf(false) }
  var hasLostFocusAfterTyping by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxWidth()) {
    // Text field with focus management
    OutlinedTextField(
        value = value,
        onValueChange = {
          onValueChange(it)
          // Reset the focus-loss tracking when the user starts typing
          if (it.isNotEmpty()) {
            hasLostFocusAfterTyping = false
          }
        },
        label = { if (label != null) Text(label, color = colorScheme.onBackground) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
          if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = leadingIconDescription,
                tint = if (isValueOk) colorScheme.secondary else colorScheme.onSurfaceVariant)
          }
        },
        modifier =
            Modifier.fillMaxWidth().testTag(testTag).wrapContentHeight().onFocusChanged { focusState
              ->
              // Mark the field as "visited" as soon as it loses focus after an entry
              if (!focusState.isFocused && value.isNotBlank()) {
                hasBeenFocused = true
                hasLostFocusAfterTyping = true
              }
            },
        shape = RoundedCornerShape(12.dp),
        colors =
            TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = colorScheme.onBackground,
                unfocusedTextColor =
                    if (value.isEmpty()) colorScheme.onSurfaceVariant
                    else if (!isValueOk) colorScheme.error else colorScheme.onBackground,
                focusedBorderColor = if (isValueOk) colorScheme.secondary else colorScheme.primary,
                unfocusedBorderColor =
                    when {
                      value.isEmpty() -> colorScheme.onSurfaceVariant
                      isValueOk -> colorScheme.secondary
                      else -> colorScheme.error
                    }),
        maxLines = maxLines,
        textStyle = TextStyle(textAlign = textAlign, color = colorScheme.onBackground),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType))

    // Display the error message if the field has been visited, input is incorrect, and focus was
    // lost after typing
    if (!isValueOk && hasBeenFocused && hasLostFocusAfterTyping) {
      Text(
          text = errorMessage,
          color = colorScheme.error,
          fontSize = 15.sp, // Error text size
          modifier = Modifier.padding(start = 16.dp, top = 4.dp).testTag(errorTestTag))
    }
  }
}

/**
 * A composable function that displays a password input field with visibility toggle and validation.
 *
 * @param value The current password value.
 * @param onValueChange Lambda to update the password value.
 * @param label The label displayed inside the field.
 * @param placeholder The placeholder text when the field is empty.
 * @param contentDescription Description for the password field.
 * @param testTag Test tag for UI testing.
 * @param passwordLengthComplete Boolean indicating whether the password meets the length
 *   requirement.
 * @param errorMessage The error message displayed for invalid password length.
 * @param testTagErrorPassword Test tag for the error message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    contentDescription: String = "",
    testTag: String,
    passwordLengthComplete: Boolean,
    errorMessage: String = "Password is too short",
    testTagErrorPassword: String = "passwordErrorMessage"
) {
  var passwordVisible by remember { mutableStateOf(false) }

  // State to track if the field has been focused and then unfocused
  var hasBeenFocused by remember { mutableStateOf(false) }
  var hasLostFocusAfterTyping by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value,
        onValueChange = {
          onValueChange(it)
          // Reset focus-loss tracking when the user starts typing
          if (it.isNotEmpty()) {
            hasLostFocusAfterTyping = false
          }
        },
        label = { Text(label, color = colorScheme.onBackground) },
        singleLine = true,
        placeholder = { Text(placeholder) },
        modifier =
            Modifier.fillMaxWidth().testTag(testTag).onFocusChanged { focusState ->
              // Mark the field as "visited" if it loses focus after an entry
              if (!focusState.isFocused && value.isNotBlank()) {
                hasBeenFocused = true
                hasLostFocusAfterTyping = true
              }
            },
        enabled = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
          Icon(
              imageVector = Icons.Filled.Lock,
              contentDescription = contentDescription,
              tint =
                  if (passwordLengthComplete) colorScheme.secondary
                  else colorScheme.onSurfaceVariant,
              modifier = Modifier.size(25.dp))
        },
        trailingIcon = {
          val image =
              if (passwordVisible) painterResource(id = android.R.drawable.ic_menu_view)
              else painterResource(id = android.R.drawable.ic_secure)

          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
                painter = image,
                contentDescription = null,
                tint =
                    if (passwordLengthComplete) colorScheme.secondary
                    else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp))
          }
        },
        colors =
            TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = colorScheme.onBackground,
                unfocusedTextColor =
                    if (value.isEmpty()) colorScheme.onSurfaceVariant
                    else if (!passwordLengthComplete) colorScheme.error
                    else colorScheme.onBackground,
                focusedBorderColor =
                    if (passwordLengthComplete) colorScheme.secondary else colorScheme.primary,
                unfocusedBorderColor =
                    when {
                      value.isEmpty() -> colorScheme.onSurfaceVariant
                      passwordLengthComplete -> colorScheme.secondary
                      else -> colorScheme.error
                    }))

    // Display the error message if the field has been visited, input is incorrect, and focus was
    // lost after typing
    if (!passwordLengthComplete && hasBeenFocused && hasLostFocusAfterTyping) {
      Text(
          text = errorMessage,
          color = colorScheme.error,
          fontSize = 15.sp, // Error text size
          modifier = Modifier.padding(start = 16.dp, top = 4.dp).testTag(testTagErrorPassword))
    }
  }
}
