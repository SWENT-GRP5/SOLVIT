package com.android.solvit.shared.model.request

import android.net.Uri
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.service.Services
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class ServiceRequestViewModelTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel

  private val serviceRequest =
      ServiceRequest(
          uid = "1",
          title = "Test Request",
          description = "Test Description",
          userId = "1",
          providerId = "1",
          dueDate = Timestamp.now(),
          meetingDate = Timestamp.now(),
          location = Location(name = "EPFL", latitude = 0.0, longitude = 0.0),
          imageUrl = null,
          packageId = "1",
          agreedPrice = 200.15,
          type = Services.PLUMBER,
          status = ServiceRequestStatus.PENDING)

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    `when`(serviceRequestRepository.init(any())).thenAnswer {
      (it.arguments[0] as () -> Unit).invoke()
    }
    // Mock the addListenerOnServiceRequests to avoid side effects
    `when`(serviceRequestRepository.addListenerOnServiceRequests(any(), any())).thenAnswer {}

    // Mock all repository methods to avoid NPEs
    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getPendingServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getAcceptedServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getScheduledServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getCompletedServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getCancelledServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getArchivedServiceRequests(any(), any())).thenAnswer {}

    serviceRequestViewModel =
        ServiceRequestViewModel(
            repository = serviceRequestRepository,
            notificationManager = null,
            authViewModel = null,
            fcmTokenManager = null)
  }

  @Test
  fun getNewUid() {
    `when`(serviceRequestRepository.getNewUid()).thenReturn("uid")
    assertThat(serviceRequestViewModel.getNewUid(), `is`("uid"))
  }

  @Test
  fun getTodosCallsRepository() {
    // Reset to clear initialization calls
    org.mockito.Mockito.reset(serviceRequestRepository)

    serviceRequestViewModel.getServiceRequests()
    verify(serviceRequestRepository).getServiceRequests(any(), any())
  }

  @Test
  fun addToDoCallsRepository() {
    serviceRequestViewModel.saveServiceRequest(serviceRequest)
    verify(serviceRequestRepository).saveServiceRequest(eq(serviceRequest), any(), any())
  }

  @Test
  fun saveServiceRequestWithImage_callsRepository() {
    val serviceRequest = mock(ServiceRequest::class.java)
    val imageUri = mock(Uri::class.java)

    serviceRequestViewModel.saveServiceRequestWithImage(serviceRequest, imageUri)

    verify(serviceRequestRepository)
        .saveServiceRequestWithImage(eq(serviceRequest), eq(imageUri), any(), any())
  }

  @Test
  fun saveServiceRequestWithImage_onSuccessUpdatesRequests() {
    val serviceRequest = mock(ServiceRequest::class.java)
    val imageUri = mock(Uri::class.java)
    val serviceRequests = listOf(serviceRequest)

    `when`(
            serviceRequestRepository.saveServiceRequestWithImage(
                eq(serviceRequest), eq(imageUri), any(), any()))
        .thenAnswer { (it.arguments[2] as () -> Unit).invoke() }
    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer {
      (it.arguments[0] as (List<ServiceRequest>) -> Unit).invoke(serviceRequests)
    }

    serviceRequestViewModel.saveServiceRequestWithImage(serviceRequest, imageUri)

    assertThat(serviceRequestViewModel.requests.value, `is`(serviceRequests))
  }

  @Test
  fun saveServiceRequestWithImage_onFailureDoesNotUpdateRequests() {
    val serviceRequest = mock(ServiceRequest::class.java)
    val imageUri = mock(Uri::class.java)

    `when`(
            serviceRequestRepository.saveServiceRequestWithImage(
                eq(serviceRequest), eq(imageUri), any(), any()))
        .thenAnswer { (it.arguments[3] as (Exception) -> Unit).invoke(Exception("Test exception")) }

    serviceRequestViewModel.saveServiceRequestWithImage(serviceRequest, imageUri)

    assertThat(serviceRequestViewModel.requests.value, `is`(emptyList()))
  }

  @Test
  fun deleteServiceRequestById_callsRepository() {
    val id = "testId"

    serviceRequestViewModel.deleteServiceRequestById(id)

    verify(serviceRequestRepository).deleteServiceRequestById(eq(id), any(), any())
  }

  @Test
  fun deleteServiceRequestById_onSuccessUpdatesRequests() {
    val id = "testId"
    val serviceRequests = listOf(serviceRequest)

    `when`(serviceRequestRepository.deleteServiceRequestById(eq(id), any(), any())).thenAnswer {
      (it.arguments[1] as () -> Unit).invoke()
    }
    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer {
      (it.arguments[0] as (List<ServiceRequest>) -> Unit).invoke(serviceRequests)
    }

    serviceRequestViewModel.deleteServiceRequestById(id)

    assertThat(serviceRequestViewModel.requests.value, `is`(serviceRequests))
  }

  @Test
  fun deleteServiceRequestById_onFailureDoesNotUpdateRequests() {
    val id = "testId"

    `when`(serviceRequestRepository.deleteServiceRequestById(eq(id), any(), any())).thenAnswer {
      (it.arguments[2] as (Exception) -> Unit).invoke(Exception("Test exception"))
    }

    serviceRequestViewModel.deleteServiceRequestById(id)

    assertThat(serviceRequestViewModel.requests.value, `is`(emptyList()))
  }

  @Test
  fun unConfirmRequest_callsRepository() {
    serviceRequestViewModel.unConfirmRequest(serviceRequest)
    verify(serviceRequestRepository)
        .saveServiceRequest(
            eq(serviceRequest.copy(status = ServiceRequestStatus.PENDING)), any(), any())
  }

  @Test
  fun confirmRequest_callsRepository() {
    serviceRequestViewModel.confirmRequest(serviceRequest, providerName = "Test Provider")
    verify(serviceRequestRepository)
        .saveServiceRequest(
            eq(serviceRequest.copy(status = ServiceRequestStatus.ACCEPTED)), any(), any())
  }

  @Test
  fun scheduleRequest_callsRepository() {
    serviceRequestViewModel.scheduleRequest(serviceRequest)
    verify(serviceRequestRepository)
        .saveServiceRequest(
            eq(serviceRequest.copy(status = ServiceRequestStatus.SCHEDULED)), any(), any())
  }

  @Test
  fun completeRequest_callsRepository() {
    serviceRequestViewModel.completeRequest(serviceRequest)
    verify(serviceRequestRepository)
        .saveServiceRequest(
            eq(serviceRequest.copy(status = ServiceRequestStatus.COMPLETED)), any(), any())
  }

  @Test
  fun cancelRequest_callsRepository() {
    serviceRequestViewModel.cancelRequest(serviceRequest)
    verify(serviceRequestRepository)
        .saveServiceRequest(
            eq(serviceRequest.copy(status = ServiceRequestStatus.CANCELED)), any(), any())
  }

  @Test
  fun archiveRequest_callsRepository() {
    serviceRequestViewModel.archiveRequest(serviceRequest)
    verify(serviceRequestRepository)
        .saveServiceRequest(
            eq(serviceRequest.copy(status = ServiceRequestStatus.ARCHIVED)), any(), any())
  }

  @Test
  fun updateAllRequests_callsAllGetMethods() {
    // Reset the mock to clear the init calls
    org.mockito.Mockito.reset(serviceRequestRepository)

    // Set up the init mock again after reset
    `when`(serviceRequestRepository.init(any())).thenAnswer {
      (it.arguments[0] as () -> Unit).invoke()
    }

    // Create a new ViewModel to trigger init
    serviceRequestViewModel =
        ServiceRequestViewModel(
            repository = serviceRequestRepository,
            notificationManager = null,
            authViewModel = null,
            fcmTokenManager = null)

    // Verify all get methods are called
    verify(serviceRequestRepository).getServiceRequests(any(), any())
    verify(serviceRequestRepository).getPendingServiceRequests(any(), any())
    verify(serviceRequestRepository).getAcceptedServiceRequests(any(), any())
    verify(serviceRequestRepository).getScheduledServiceRequests(any(), any())
    verify(serviceRequestRepository).getCompletedServiceRequests(any(), any())
    verify(serviceRequestRepository).getCancelledServiceRequests(any(), any())
    verify(serviceRequestRepository).getArchivedServiceRequests(any(), any())
  }

  @Test
  fun selectRequest_updatesSelectedRequest() {
    serviceRequestViewModel.selectRequest(serviceRequest)
    assertThat(serviceRequestViewModel.selectedRequest.value, `is`(serviceRequest))
  }

  @Test
  fun selectProvider_updatesProviderIdAndService() {
    val providerId = "testProvider"
    val service = Services.PLUMBER

    serviceRequestViewModel.selectProvider(providerId, service)

    assertThat(serviceRequestViewModel.selectedProviderId.value, `is`(providerId))
    assertThat(serviceRequestViewModel.selectedProviderService.value, `is`(service))
  }

  @Test
  fun unSelectProvider_clearsProviderIdAndService() {
    // First set some values
    serviceRequestViewModel.selectProvider("testProvider", Services.PLUMBER)

    // Verify initial state
    assertThat(serviceRequestViewModel.selectedProviderId.value, `is`("testProvider"))
    assertThat(serviceRequestViewModel.selectedProviderService.value, `is`(Services.PLUMBER))

    // Then unselect
    serviceRequestViewModel.unSelectProvider()

    // Verify cleared state
    assertThat(serviceRequestViewModel.selectedProviderId.value, `is`(nullValue()))
    assertThat(serviceRequestViewModel.selectedProviderService.value, `is`(nullValue()))
  }

  @Test
  fun getServiceRequestById_callsRepository() {
    val id = "testId"
    val onSuccess: (ServiceRequest) -> Unit = {}

    serviceRequestViewModel.getServiceRequestById(id, onSuccess)

    verify(serviceRequestRepository).getServiceRequestById(eq(id), eq(onSuccess), any())
  }

  @Test
  fun getTodayScheduledRequests_returnsCorrectlyFilteredAndSortedList() {
    // Create test data with different dates
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)

    // Convert LocalDate to Timestamp with specific times for sorting
    val todayTimestamp =
        Timestamp(Date.from(today.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant()))
    val tomorrowTimestamp =
        Timestamp(Date.from(tomorrow.atTime(11, 0).atZone(ZoneId.systemDefault()).toInstant()))
    val yesterdayTimestamp =
        Timestamp(Date.from(yesterday.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant()))

    val request1 = serviceRequest.copy(uid = "1", meetingDate = todayTimestamp)
    val request2 = serviceRequest.copy(uid = "2", meetingDate = tomorrowTimestamp)
    val request3 = serviceRequest.copy(uid = "3", meetingDate = yesterdayTimestamp)
    val requests = listOf(request1, request2, request3)

    // Reset to clear initialization calls
    org.mockito.Mockito.reset(serviceRequestRepository)

    // Re-mock all repository methods to avoid NPEs
    `when`(serviceRequestRepository.init(any())).thenAnswer {
      (it.arguments[0] as () -> Unit).invoke()
    }
    `when`(serviceRequestRepository.addListenerOnServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getServiceRequests(any(), any())).thenAnswer {}

    // Set up the scheduled requests with our test data - immediately invoke the callback
    `when`(serviceRequestRepository.getScheduledServiceRequests(any(), any())).thenAnswer {
      (it.arguments[0] as (List<ServiceRequest>) -> Unit).invoke(requests)
    }

    // Mock other repository methods
    `when`(serviceRequestRepository.getPendingServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getAcceptedServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getCompletedServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getCancelledServiceRequests(any(), any())).thenAnswer {}
    `when`(serviceRequestRepository.getArchivedServiceRequests(any(), any())).thenAnswer {}

    // Initialize the ViewModel again to trigger updateAllRequests
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)

    // Get today's scheduled requests
    val todayRequests = serviceRequestViewModel.getTodayScheduledRequests()

    // Verify only today's request is returned and it's correctly sorted
    assertThat(todayRequests.size, `is`(1))
    assertThat(todayRequests[0].uid, `is`("1"))
  }
}
