package com.android.solvit.seeker.ui.request

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavController
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.NotificationsRepository
import com.android.solvit.shared.model.NotificationsViewModel
import com.android.solvit.shared.model.authentication.AuthRep
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationRepository
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.provider.ProviderRepository
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestRepository
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.request.analyzer.ImagePickerStep
import com.android.solvit.shared.model.request.analyzer.MultiStepDialog
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.google.firebase.Timestamp
import java.util.GregorianCalendar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never

class CreateRequestScreenTest {
  private lateinit var serviceRequestRepository: ServiceRequestRepository
  private lateinit var serviceRequestViewModel: ServiceRequestViewModel
  private lateinit var locationRepository: LocationRepository
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var authRepository: AuthRep
  private lateinit var authViewModel: AuthViewModel
  private lateinit var navController: NavController
  private lateinit var navigationActions: NavigationActions
  private lateinit var notificationsRepository: NotificationsRepository
  private lateinit var notificationsViewModel: NotificationsViewModel
  private lateinit var providerRepository: ProviderRepository
  private lateinit var listProviderViewModel: ListProviderViewModel

  @get:Rule val composeTestRule = createComposeRule()

  private val locations =
      listOf(
          Location(37.7749, -122.4194, "San Francisco"),
          Location(34.0522, -118.2437, "Los Angeles"),
          Location(40.7128, -74.0060, "New York"))

  private val serviceRequest =
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

  @Before
  fun setUp() {
    serviceRequestRepository = Mockito.mock(ServiceRequestRepository::class.java)
    serviceRequestViewModel = ServiceRequestViewModel(serviceRequestRepository)
    locationRepository = Mockito.mock(LocationRepository::class.java)
    locationViewModel = LocationViewModel(locationRepository)
    authRepository = Mockito.mock(AuthRep::class.java)
    authViewModel = AuthViewModel(authRepository)
    navController = Mockito.mock(NavController::class.java)
    navigationActions = NavigationActions(navController)
    notificationsRepository = Mockito.mock(NotificationsRepository::class.java)
    notificationsViewModel = NotificationsViewModel(notificationsRepository)
    providerRepository = Mockito.mock(ProviderRepository::class.java)
    listProviderViewModel = ListProviderViewModel(providerRepository)

    `when`(locationRepository.search(ArgumentMatchers.anyString(), anyOrNull(), anyOrNull()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<Location>) -> Unit>(1)
          onSuccess(locations)
        }

    `when`(serviceRequestRepository.saveServiceRequest(any(), any(), any())).thenAnswer { invocation
      ->
      val onSuccess = invocation.getArgument<(ServiceRequest) -> Unit>(1)
      onSuccess(serviceRequest)
    }

    `when`(serviceRequestRepository.getNewUid()).thenAnswer { "1" }

    `when`(authRepository.updateUserLocations(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
  }

  fun mockUri(): Uri {
    return Mockito.mock(Uri::class.java)
  }

  fun mockContext(): Context {
    return Mockito.mock(Context::class.java)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
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
    composeTestRule.onNodeWithTag("bottomNavigationMenu").isDisplayed()
  }

  @Test
  fun doesNotSubmitWithInvalidDate() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestDate").performTextClearance()
    composeTestRule.onNodeWithTag("inputRequestDate").performTextInput("notadate")
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository, never()).saveServiceRequest(any(), any(), any())
  }

  @Test
  fun locationMenuExpandsWithInput() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
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
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
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
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumber")
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertExists()
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Plumber"))
  }

  @Test
  fun serviceTypeDropdown_closesOnSelection() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumber")
    composeTestRule.onNodeWithTag("serviceTypeResult").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun serviceTypeDropdown_showsNoResultsMessage() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel,
      )
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("NonExistentType")
    composeTestRule.onNodeWithTag("serviceTypeResult").assert(hasText("Other"))
  }

  @Test
  fun serviceTypeDropdown_closesOnFocusLost() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel,
      )
    }

    composeTestRule.onNodeWithTag("inputServiceType").performTextInput("Plumbing")
    composeTestRule.onNodeWithTag("inputRequestTitle").performClick()
    composeTestRule.onNodeWithTag("serviceTypeMenu").assertDoesNotExist()
  }

  @Test
  fun doesNotSubmitWithInvalidTitle() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
    }

    composeTestRule.onNodeWithTag("inputRequestTitle").performTextClearance()
    composeTestRule.onNodeWithTag("requestSubmit").performClick()

    Mockito.verify(serviceRequestRepository, never()).saveServiceRequest(any(), any(), any())
  }

  @Test
  fun aiAssistantDialog_displaysCorrectlyAndCanBeDismissed() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
    }

    // Check that the AI Assistant Dialog is displayed initially
    composeTestRule.onNodeWithTag("aIAssistantDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("aiAssistantHeaderRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("aiAssistantIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("aiAssistantTitleBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("aiAssistantDescription").assertIsDisplayed()
    composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("uploadPicturesButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("cancelButton").performClick() // Dismiss the dialog

    // Ensure the dialog is dismissed
    composeTestRule.onNodeWithTag("aIAssistantDialog").assertDoesNotExist()
    composeTestRule.onNodeWithTag("aiAssistantHeaderRow").assertDoesNotExist()
    composeTestRule.onNodeWithTag("aiAssistantIcon").assertDoesNotExist()
    composeTestRule.onNodeWithTag("aiAssistantTitleBox").assertDoesNotExist()
    composeTestRule.onNodeWithTag("aiAssistantDescription").assertDoesNotExist()
    composeTestRule.onNodeWithTag("cancelButton").assertDoesNotExist()
  }

  @Test
  fun aiAssistantDialog_navigatesToImagePickerStep() {
    composeTestRule.setContent {
      CreateRequestScreen(
          navigationActions,
          serviceRequestViewModel,
          locationViewModel,
          authViewModel,
          notificationsViewModel,
          listProviderViewModel)
    }

    // Check that the AI Assistant Dialog is displayed initially
    composeTestRule.onNodeWithTag("aIAssistantDialog").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("uploadPicturesButton")
        .performClick() // Navigate to ImagePickerStep

    // Ensure Image Picker Step is displayed
    composeTestRule.onNodeWithTag("imagePickerStep").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noImagesText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addImagesButton").assertIsDisplayed()
  }

  @Test
  fun imagePickerStep_displaysComponentsCorrectly() {
    val selectedImages = mutableListOf<Uri>() // Simulate empty images initially

    composeTestRule.setContent {
      ImagePickerStep(
          selectedImages = selectedImages,
          onImagesSelected = { newImages -> selectedImages.addAll(newImages) },
          onRemoveImage = { uri -> selectedImages.remove(uri) },
          onStartAnalyzing = { /* Trigger Analyze Action */})
    }

    // Verify header, message, and buttons exist
    composeTestRule.onNodeWithTag("imagePickerStep").assertIsDisplayed()
    composeTestRule.onNodeWithTag("noImagesText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addImagesButton").assertIsDisplayed()

    // Test button interactions
    composeTestRule.onNodeWithTag("addImagesButton").performClick()
    // Simulate adding images (manually mock `onImagesSelected` behavior)

    selectedImages.add(mockUri()) // Mock a Uri object
    composeTestRule.runOnIdle { assert(selectedImages.isNotEmpty()) }
  }

  @Test
  fun imagePickerStep_removesImage() {
    val selectedImages = mutableListOf(mockUri()) // Mock an initial image

    composeTestRule.setContent {
      ImagePickerStep(
          selectedImages = selectedImages,
          onImagesSelected = { newImages -> selectedImages.addAll(newImages) },
          onRemoveImage = { uri -> selectedImages.remove(uri) },
          onStartAnalyzing = { /* Trigger Analyze Action */})
    }

    // Simulate removing the image
    composeTestRule.onNodeWithTag("removeImageButton").performClick()
    composeTestRule.runOnIdle { assert(selectedImages.isEmpty()) }
  }

  @Test
  fun multiStepDialog_stepOne() {
    var showDialog = true
    var currentStep = 1

    composeTestRule.setContent {
      MultiStepDialog(
          context = mockContext(),
          showDialog = showDialog,
          currentStep = currentStep,
          selectedImages = emptyList(),
          onImagesSelected = { /* Mock action */},
          onRemoveImage = { /* Mock action */},
          onStartAnalyzing = { /* Mock action */},
          onAnalyzeComplete = { _, _, _ -> /* Mock action */ },
          onClose = { showDialog = false })
    }

    // Verify dialog components
    composeTestRule.onNodeWithTag("multiStepDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("imagePickerStep").assertIsDisplayed()
  }

  @Test
  fun multiStepDialog_stepTwo() {
    var showDialog = true
    var currentStep = 1
    // Move to next step and verify
    currentStep = 2
    composeTestRule.setContent {
      MultiStepDialog(
          context = mockContext(),
          showDialog = showDialog,
          currentStep = currentStep,
          selectedImages = listOf(mockUri()),
          onImagesSelected = { /* Mock action */},
          onRemoveImage = { /* Mock action */},
          onStartAnalyzing = { /* Mock action */},
          onAnalyzeComplete = { _, _, _ -> /* Mock action */ },
          onClose = { showDialog = false })
    }

    composeTestRule.onNodeWithTag("stepTwoProgressBar").assertIsDisplayed()
  }

  @Test
  fun multiStepDialog_stepThreeDisplaysAndCloses() {
    var showDialog = true

    composeTestRule.setContent {
      MultiStepDialog(
          context = mockContext(),
          showDialog = showDialog,
          currentStep = 3,
          selectedImages = listOf(mockUri()),
          onImagesSelected = { /* Mock action */},
          onRemoveImage = { /* Mock action */},
          onStartAnalyzing = { /* Mock action */},
          onAnalyzeComplete = { _, _, _ -> /* Mock action */ },
          onClose = { showDialog = false })
    }

    // Verify Step 3 UI components
    composeTestRule.onNodeWithTag("stepThreeIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("completionNote").assertIsDisplayed()
    composeTestRule.onNodeWithTag("proceedButton").assertIsDisplayed()

    // Simulate closing the dialog
    composeTestRule.onNodeWithTag("proceedButton").performClick()
    composeTestRule.runOnIdle { assert(!showDialog) }
  }
}
