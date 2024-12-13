package com.android.solvit.seeker.ui.request

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class RequestsOverviewScreenTest {
  private lateinit var navigationActions: NavigationActions
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel

  private val request =
      ServiceRequest(
          uid = "uid",
          title = "title",
          type = Services.CLEANER,
          description = "description",
          userId = "-1",
          dueDate = Timestamp(GregorianCalendar(2024, 0, 1).time),
          location = Location(37.7749, -122.4194, "San Francisco"),
          imageUrl = "imageUrl",
          status = ServiceRequestStatus.PENDING)

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Route.REQUESTS_OVERVIEW)
  }

  @Test
  fun hasRequiredComponents() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    composeTestRule.onNodeWithTag("requestsOverviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bottomNavigationMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topOrdersSection").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filterRequestsBar").assertIsDisplayed()
  }

  @Test
  fun displayTextWhenEmpty() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).then {
      it.getArgument<(List<ServiceRequest>) -> Unit>(0)(listOf())
    }
    serviceRequestViewModel.getServiceRequests()

    composeTestRule.onNodeWithTag("noServiceRequestsScreen").assertIsDisplayed()
  }

  @Test
  fun displayRequestsWhenNotEmpty() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).then {
      it.getArgument<(List<ServiceRequest>) -> Unit>(0)(listOf(request))
    }
    serviceRequestViewModel.getServiceRequests()

    composeTestRule.onNodeWithTag("requestsList").assertIsDisplayed()
  }

  @Test
  fun categoriesSettingsShowsFilters() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    composeTestRule.onNodeWithTag("categoriesSettings").assertIsDisplayed()
    composeTestRule.onNodeWithTag("categoriesFilter").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("categoriesSettings").performClick()
    composeTestRule.onNodeWithTag("categoriesFilter").assertIsDisplayed()
  }

  @Test
  fun categoriesSortShowFilters() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    composeTestRule.onNodeWithTag("categoriesSort").assertIsDisplayed()
    composeTestRule.onNodeWithTag("categoriesSortFilter").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("categoriesSort").performClick()
    composeTestRule.onNodeWithTag("categoriesSortFilter").assertIsDisplayed()
  }

  @Test
  fun categoryFiltersWorks() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    assert(serviceRequestViewModel.selectedServices.value.isEmpty())

    composeTestRule.onNodeWithTag("categoriesSettings").performClick()
    composeTestRule.onNodeWithTag("Plumber FilterItem").performClick()

    assert(serviceRequestViewModel.selectedServices.value == listOf(Services.PLUMBER))
  }

  @Test
  fun categorySortWorks() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    assertFalse(serviceRequestViewModel.sortSelected.value)

    composeTestRule.onNodeWithTag("categoriesSort").performClick()
    composeTestRule.onNodeWithTag("Sort by date FilterItem").performClick()

    assertTrue(serviceRequestViewModel.sortSelected.value)
  }

  @Test
  fun clickOnRequestNavigatesToBookingScreen() {
    composeTestRule.setContent {
      RequestsOverviewScreen(navigationActions, serviceRequestViewModel)
    }

    `when`(serviceRequestRepository.getServiceRequests(any(), any())).then {
      it.getArgument<(List<ServiceRequest>) -> Unit>(0)(listOf(request))
    }
    serviceRequestViewModel.getServiceRequests()

    composeTestRule.onNodeWithText("title").performClick()
    verify(navigationActions).navigateTo(Route.BOOKING_DETAILS)
  }

  @Test
  fun requestItemDisplaysCorrectData() {
    composeTestRule.setContent { RequestItemRow(request = request, onClick = {}) }

    composeTestRule.onNodeWithText("title").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cleaner").assertIsDisplayed()

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val expectedDate = dateFormat.format(request.dueDate.toDate())
    composeTestRule.onNodeWithText("Until:").assertIsDisplayed()
    composeTestRule.onNodeWithText(expectedDate).assertIsDisplayed()

    composeTestRule.onNodeWithText("Pending").assertIsDisplayed()
  }

  @Test
  fun requestItemRowClickCallsOnClick() {
    val onClickMock = mock<() -> Unit>()

    composeTestRule.setContent { RequestItemRow(request = request, onClick = onClickMock) }

    composeTestRule.onNodeWithTag("requestListItem").performClick()
    verify(onClickMock).invoke()
  }
}
