package com.android.solvit.shared.model.request

import android.net.Uri
import com.android.solvit.shared.model.map.Location
import com.google.firebase.Timestamp
import org.hamcrest.CoreMatchers.`is`
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
          assigneeName = "Test Assignee",
          dueDate = Timestamp.now(),
          location = Location(name = "EPFL", latitude = 0.0, longitude = 0.0),
          imageUrl = null,
          type = ServiceRequestType.CLEANING,
          status = ServiceRequestStatus.PENDING)

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
  }

  @Test
  fun getNewUid() {
    `when`(serviceRequestRepository.getNewUid()).thenReturn("uid")
    assertThat(serviceRequestViewModel.getNewUid(), `is`("uid"))
  }

  @Test
  fun getTodosCallsRepository() {
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
}
