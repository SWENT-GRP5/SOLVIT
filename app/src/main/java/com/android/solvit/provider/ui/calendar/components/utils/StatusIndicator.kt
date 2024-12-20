package com.android.solvit.provider.ui.calendar.components.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.solvit.shared.model.request.ServiceRequestStatus

/**
 * A composable function that displays a circular status indicator representing the status of a
 * service request. The indicator's color changes dynamically based on the provided status.
 *
 * @param status The `ServiceRequestStatus` representing the current status of the service request.
 * @param modifier Modifier for customizing the appearance and layout of the indicator.
 *
 * **Visual Representation:**
 * - **Outer Circle:** Colored based on the request status using
 *   `ServiceRequestStatus.getStatusColor()`.
 * - **Inner Circle:** A smaller white circle centered inside the outer circle for visual emphasis.
 *
 * The component supports testing tags for easier UI automation and testing.
 */
@Composable
fun StatusIndicator(status: ServiceRequestStatus, modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .size(8.dp)
              .background(color = ServiceRequestStatus.getStatusColor(status), shape = CircleShape)
              .testTag("statusIndicator_${status.name}")) {
        Box(
            modifier =
                Modifier.size(4.dp)
                    .background(colorScheme.onPrimary, CircleShape)
                    .align(Alignment.Center)
                    .testTag("statusIndicatorInner_${status.name}"))
      }
}
