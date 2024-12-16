package com.android.solvit.seeker.ui.booking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun BookingMonthDayItem(
    date: LocalDate,
    isCurrentMonth: Boolean,
    hasAvailabilities: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = LocalDate.now()
    val isSelectable = date.isEqual(now) || date.isAfter(now)
    
    val backgroundColor = when {
        hasAvailabilities && isSelectable -> colorScheme.tertiary.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    val textColor = when {
        !isCurrentMonth || !isSelectable -> colorScheme.onSurface.copy(alpha = 0.5f)
        else -> colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = isSelectable && hasAvailabilities) { onDateSelected(date) }
            .padding(4.dp)
            .testTag("bookingMonthDayItem_${date}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.testTag("bookingMonthDayNumber_${date}")
        )

        if (hasAvailabilities && isSelectable) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(6.dp)
                    .background(colorScheme.tertiary, RoundedCornerShape(50))
                    .testTag("bookingMonthDayIndicator_${date}")
            )
        }
    }
}
