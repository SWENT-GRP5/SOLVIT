package com.android.solvit.ui.requests

/*
class CreateRequestScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  private val serviceRequest =
      ServiceRequest(
          "uid",
          "title",
          ServiceRequestType.CLEANING,
          "description",
          "assigneeName",
          Timestamp(GregorianCalendar(2024, 0, 1).time),
          Location(37.7749, -122.4194, "San Francisco"),
          "imageUrl",
          ServiceRequestStatus.PENDING)

  @Before
  fun setUp() {
    serviceRequestRepository = mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    locationRepository = mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)
    navController = mock(NavController::class.java)
    navigationActions = NavigationActions(navController)

    `when`(locationRepository.search(anyString(), anyOrNull(), anyOrNull())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
      onSuccess(locations)
    }

    `when`(serviceRequestRepository.saveServiceRequest(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<(ServiceRequest) -> Unit>(1)
      onSuccess(serviceRequest)
    }

    `when`(serviceRequestRepository.getNewUid()).thenAnswer { "1" }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertTextEquals("Create a new request")
    composeTestRule.onNodeWithTag("requestSubmit").assertIsDisplayed()
    composeTestRule.onNodeWithTag("requestSubmit").assertTextEquals("Submit Request")

    composeTestRule.onNodeWithTag("inputRequestTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputServiceType").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestAddress").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputRequestDate").assertIsDisplayed()
    composeTestRule.onNodeWithTag("imagePickerButton").assertIsDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestDate").performTextInput("notadate")
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository, never()).saveServiceRequest(any(), any(), any())
  }

  @Test
  fun locationMenuExpandsWithInput() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[1].assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("locationResult")[2].assertIsDisplayed()
  }

  @Test
  fun locationSelectionFromDropdown() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestAddress").performTextInput("USA")
    composeTestRule.waitUntil { locationViewModel.locationSuggestions.value.isNotEmpty() }

    composeTestRule.onAllNodesWithTag("locationResult")[0].performClick()
    assert(locationViewModel.locationSuggestions.value == locations)
    assert(locationViewModel.query.value == "San Francisco")
  }

  @Test
  fun serviceTypeDropdown_showsFilteredResults() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertExists()
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Plumbing"))
  }

  @Test
  fun serviceTypeDropdown_closesOnSelection() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("serviceTypeResult").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun serviceTypeDropdown_showsNoResultsMessage() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("NonExistentType")
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Other"))
  }

  @Test
  fun serviceTypeDropdown_closesOnFocusLost() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("inputRequestTitle").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun doesNotSubmitWithInvalidTitle() {
    composeTestRule.setContent {
      CreateRequestScreen(navigationActions, serviceRequestViewModel, locationViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestTitle").performTextClearance()
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository, never()).saveServiceRequest(any(), any(), any())
  }
}
*/
