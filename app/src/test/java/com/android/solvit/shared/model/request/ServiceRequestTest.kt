package com.android.solvit.shared.model.request

import com.android.solvit.shared.ui.theme.ACCEPTED_color
import com.android.solvit.shared.ui.theme.ARCHIVED_color
import com.android.solvit.shared.ui.theme.CANCELLED_color
import com.android.solvit.shared.ui.theme.ENDED_color
import com.android.solvit.shared.ui.theme.PENDING_color
import com.android.solvit.shared.ui.theme.STARTED_color
import org.junit.Assert.assertEquals
import org.junit.Test

class ServiceRequestTest {
  @Test
  fun serviceRequestStatusFormat_returnsFormattedString() {
    assertEquals("Pending", ServiceRequestStatus.format(ServiceRequestStatus.PENDING))
    assertEquals("Accepted", ServiceRequestStatus.format(ServiceRequestStatus.ACCEPTED))
    assertEquals("Scheduled", ServiceRequestStatus.format(ServiceRequestStatus.SCHEDULED))
    assertEquals("Completed", ServiceRequestStatus.format(ServiceRequestStatus.COMPLETED))
    assertEquals("Canceled", ServiceRequestStatus.format(ServiceRequestStatus.CANCELED))
    assertEquals("Archived", ServiceRequestStatus.format(ServiceRequestStatus.ARCHIVED))
  }

  @Test
  fun serviceRequestStatusGetStatusColor_returnsCorrectColor() {
    assertEquals(PENDING_color, ServiceRequestStatus.getStatusColor(ServiceRequestStatus.PENDING))
    assertEquals(ACCEPTED_color, ServiceRequestStatus.getStatusColor(ServiceRequestStatus.ACCEPTED))
    assertEquals(STARTED_color, ServiceRequestStatus.getStatusColor(ServiceRequestStatus.SCHEDULED))
    assertEquals(ENDED_color, ServiceRequestStatus.getStatusColor(ServiceRequestStatus.COMPLETED))
    assertEquals(
        CANCELLED_color, ServiceRequestStatus.getStatusColor(ServiceRequestStatus.CANCELED))
    assertEquals(ARCHIVED_color, ServiceRequestStatus.getStatusColor(ServiceRequestStatus.ARCHIVED))
  }
}
