package com.android.solvit.provider.ui.profile

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.profile.Stepper
import com.android.solvit.seeker.ui.request.LocationDropdown
import com.android.solvit.shared.model.authentication.AuthViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.packages.PackageProposal
import com.android.solvit.shared.model.packages.PackageProposalViewModel
import com.android.solvit.shared.model.packages.PackagesAssistantViewModel
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.authentication.GoBackButton
import com.android.solvit.shared.ui.booking.PackageCard
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.utils.CustomOutlinedTextField
import com.android.solvit.shared.ui.utils.ValidationRegex

/**
 * Composable function to display the provider registration screen.
 *
 * @param viewModel the [ListProviderViewModel] to handle the provider registration
 * @param navigationActions the navigation actions to navigate between screens
 * @param locationViewModel the [LocationViewModel] to handle the location suggestions
 * @param authViewModel the [AuthViewModel] to handle the authentication
 * @param packageViewModel the [PackageProposalViewModel] to handle the package proposals
 * @param assistantViewModel the [PackagesAssistantViewModel] to handle generate packages
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint(
    "UnusedMaterialScaffoldPaddingParameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "SourceLockedOrientationActivity")
@Composable
fun ProviderRegistrationScreen(
    viewModel: ListProviderViewModel = viewModel(factory = ListProviderViewModel.Factory),
    navigationActions: NavigationActions,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    packageViewModel: PackageProposalViewModel =
        viewModel(factory = PackageProposalViewModel.Factory),
    assistantViewModel: PackagesAssistantViewModel =
        viewModel(factory = PackagesAssistantViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose {
      locationViewModel.clear()
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
  }

  // Form fields
  var fullName by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var selectedLocation by remember { mutableStateOf<Location?>(null) }
  val locationQuery by locationViewModel.query.collectAsState()

  var showDropdown by remember { mutableStateOf(false) }
  val locationSuggestions by
      locationViewModel.locationSuggestions.collectAsState(initial = emptyList<Location?>())

  // represent the current authenticated user
  val user by authViewModel.user.collectAsState()

  // Additional Informations about the provider
  var selectedService by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var startingPrice by remember { mutableStateOf("") }
  val selectedLanguages = remember { mutableStateListOf<String>() }
  var providerImageUri by remember { mutableStateOf<Uri?>(null) }
  var providerImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

  // Additional Informations about provider packages
  val offerPackages = remember { mutableStateOf(false) }
  val packagesNames = remember { mutableStateListOf(*Array(3) { "" }) }
  val packagesPrices = remember { mutableStateListOf(*Array(3) { "" }) }
  val packagesDetails = remember { mutableStateListOf(*Array(3) { "" }) }
  val packagesFeatures = remember {
    mutableStateListOf(
        mutableStateListOf("", "", ""), // Package 1 features
        mutableStateListOf("", "", ""), // Package 2 features
        mutableStateListOf("", "", "") // Package 3 features
        )
  }

  // Step tracking: Role, Details, Preferences
  var currentStep by remember { mutableIntStateOf(1) }

  val backgroundColor = colorScheme.background

  val localContext = LocalContext.current

  val isFullNameOk = ValidationRegex.FULL_NAME_REGEX.matches(fullName)

  val isPhoneOk = ValidationRegex.PHONE_REGEX.matches(phone)

  val isCompanyNameOk = companyName.isNotBlank() && companyName.length > 2

  val isLocationOK = selectedLocation != null

  val isFormComplete = isFullNameOk && isPhoneOk && isCompanyNameOk && isLocationOK

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Provider Registration") },
            navigationIcon = {
              if (currentStep > 1) {
                IconButton(onClick = { currentStep -= 1 }) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
              } else {
                GoBackButton(navigationActions)
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor))
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.background(backgroundColor)
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())) {
              Stepper(currentStep = currentStep, isFormComplete)
              Spacer(modifier = Modifier.height(16.dp))

              if (currentStep == 1) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo), // Update with your logo
                    contentDescription = "App Logo",
                    modifier =
                        Modifier.testTag("signUpIcon")
                            .size(150.dp) // Adjust the size as per your logo
                            .align(Alignment.CenterHorizontally))
                Text(
                    text = "Sign Up as a Provider",
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        Modifier.testTag("signUpProviderTitle").align(Alignment.CenterHorizontally))

                Spacer(modifier = Modifier.height(16.dp))
                // Full Name
                CustomOutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    isValueOk = isFullNameOk,
                    errorMessage = "Enter a valid first and last name",
                    leadingIcon = Icons.Default.Person,
                    leadingIconDescription = "Person Icon",
                    testTag = "fullNameInput",
                    errorTestTag = "fullNameErrorProviderRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                // Phone Number
                CustomOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    placeholder = "Enter your phone number",
                    isValueOk = isPhoneOk,
                    errorMessage = "Your phone number must be at least 6 digits",
                    leadingIcon = Icons.Default.Phone,
                    leadingIconDescription = "Phone Icon",
                    testTag = "phoneNumberInput",
                    errorTestTag = "phoneNumberErrorProviderRegistration",
                    keyboardType = KeyboardType.Number)

                Spacer(modifier = Modifier.height(10.dp))

                CustomOutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = "Business/Company Name",
                    placeholder = "Enter your business name (optional for independent providers)",
                    isValueOk = isCompanyNameOk,
                    errorMessage = "Your company name must be at least 3 characters",
                    leadingIcon = Icons.Default.Build,
                    leadingIconDescription = "Company Icon",
                    testTag = "companyNameInput",
                    errorTestTag = "companyNameErrorProviderRegistration")

                Spacer(modifier = Modifier.height(10.dp))

                LocationDropdown(
                    locationQuery = locationQuery,
                    onLocationQueryChange = { locationViewModel.setQuery(it) },
                    showDropdownLocation = showDropdown,
                    onShowDropdownLocationChange = { showDropdown = it },
                    locationSuggestions = locationSuggestions.filterNotNull(),
                    userLocations = user?.locations ?: emptyList(),
                    onLocationSelected = { selectedLocation = it },
                    requestLocation = null,
                    backgroundColor = colorScheme.background,
                    isValueOk = isLocationOK)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                      if (isFormComplete) {
                        // Move to next step (Step 2: Preferences)
                        currentStep = 2
                      } else {
                        Toast.makeText(
                                context,
                                "Please fill in all the correct information",
                                Toast.LENGTH_SHORT)
                            .show()
                      }
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(60.dp)
                            .testTag("completeRegistrationButton")
                            .background(
                                if (isFormComplete) {
                                  colorScheme.secondary
                                } else {
                                  colorScheme.onSurfaceVariant
                                },
                                shape =
                                    RoundedCornerShape(
                                        25.dp,
                                    )),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent)) // Green button
                {
                      Text("Complete registration", color = colorScheme.onSecondary)
                    }
              }
              // Preferences Step
              if (currentStep == 2) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth() // Ensure content takes up full width
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
                    verticalArrangement = Arrangement.Center // Center vertically
                    ) {
                      Text(
                          text = "Finish Your Inscription",
                          style = MaterialTheme.typography.titleLarge,
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally)
                                  .testTag("preferencesTitle"),
                          textAlign = TextAlign.Center // Center the text
                          )
                      Spacer(modifier = Modifier.height(16.dp))
                      Image(
                          painter = painterResource(id = R.drawable.providerpref),
                          contentDescription = "Completion Image",
                          modifier =
                              Modifier.size(300.dp)
                                  .testTag("preferencesIllustration")
                                  .align(Alignment.CenterHorizontally))
                      Spacer(modifier = Modifier.height(10.dp))
                      ProviderDetails(
                          selectedService = selectedService,
                          onSelectedServiceChange = { s: String -> selectedService = s },
                          description = description,
                          onDescriptionChange = { d: String -> description = d },
                          startingPrice = startingPrice,
                          onStartingPriceChange = { sP: String -> startingPrice = sP },
                          selectedLanguages = selectedLanguages,
                          providerImageUri = providerImageUri,
                          onImageSelected = { uri: Uri? ->
                            providerImageUri = uri
                            uri?.let { providerImageBitmap = loadBitmapFromUri(localContext, it) }
                          },
                          onClickButton = { currentStep = 3 })
                      Spacer(modifier = Modifier.height(15.dp))
                      Text(
                          text =
                              "You can always update your informations in your profile settings.",
                          style = MaterialTheme.typography.bodyLarge,
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally).testTag("footerText"),
                          textAlign = TextAlign.Center)
                    }
              }

              if (currentStep == 3) {

                Column(
                    modifier =
                        Modifier.fillMaxWidth() // Ensure content takes up full width
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
                    verticalArrangement = Arrangement.Center // Center vertically
                    ) {
                      Text(
                          text = "Offer Service Packages",
                          style = MaterialTheme.typography.titleLarge,
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally)
                                  .testTag("preferencesTitle"),
                          textAlign = TextAlign.Center // Center the text
                          )
                      Spacer(modifier = Modifier.height(16.dp))
                      user?.let {
                        ProviderPackages(
                            packagesNames = packagesNames,
                            packagePrices = packagesPrices,
                            packagesDetails = packagesDetails,
                            packagesFeatures = packagesFeatures,
                            providePackages = offerPackages,
                            assistantViewModel = assistantViewModel,
                            packageViewModel = packageViewModel,
                            providerId = it.uid,
                            type = Services.valueOf(selectedService.uppercase().replace(" ", "_")))
                      }
                      Spacer(modifier = Modifier.height(30.dp))
                      Button(
                          onClick = {
                            assistantViewModel.clearPackageProposals()
                            currentStep = 4
                          },
                          modifier = Modifier.fillMaxWidth().testTag("savePreferences2Button"),
                          colors = ButtonDefaults.buttonColors(colorScheme.secondary)) {
                            Text("Complete Registration", color = colorScheme.onSecondary)
                          }
                      Spacer(modifier = Modifier.height(15.dp))
                      Text(
                          text =
                              "You can always update your informations in your profile settings.",
                          style = MaterialTheme.typography.bodyLarge,
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally).testTag("footer2Text"),
                          textAlign = TextAlign.Center)
                    }
              }

              // Completion Step
              if (currentStep == 4) {
                // Completion screen
                Text(
                    text = "You're All Set!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally).testTag("confirmationTitle"))

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter =
                        painterResource(id = R.drawable.alldoneprovider), // Your image resource
                    contentDescription = "Completion Image",
                    modifier =
                        Modifier.size(200.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("celebrationIllustration") // Adjust size as needed
                    )
                Spacer(modifier = Modifier.height(100.dp))
                // Completion message
                Text(
                    text =
                        "Your profile has been successfully created. " +
                            "You're ready to start offering your services to customers." +
                            "Start connecting with customers, respond to requests, and grow your business on Solvit.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally)
                            .testTag("successMessageText"), // Add horizontal padding
                    textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                      // Complete registration and navigate
                      val loc = selectedLocation ?: Location(0.0, 0.0, "")
                      val newProviderProfile =
                          Provider(
                              uid = user!!.uid,
                              name = fullName,
                              phone = phone,
                              companyName = companyName,
                              service =
                                  Services.valueOf(selectedService.uppercase().replace(" ", "_")),
                              description = description,
                              price = startingPrice.toDouble(),
                              languages = selectedLanguages.map { Language.valueOf(it) },
                              location = loc)
                      // Check that the provider want to offer service packages
                      Log.e("ServicePackages", "offerPackages : ${offerPackages.value}")
                      if (offerPackages.value && packagesNames.isNotEmpty()) {
                        for (i in 0..2) {
                          val packageProposal =
                              PackageProposal(
                                  uid = packageViewModel.getNewUid(),
                                  packageNumber = i + 1.0,
                                  providerId = user!!.uid,
                                  title = packagesNames[i],
                                  description = packagesDetails[i],
                                  price =
                                      if (packagesPrices[i] != "") packagesPrices[i].toDouble()
                                      else null,
                                  bulletPoints = packagesFeatures[i])
                          try {
                            packageViewModel.addPackageProposal(packageProposal)
                          } catch (e: Exception) {
                            Log.e("Provider Registration", "Failed to add package $i : $e")
                          }
                        }
                      }
                      viewModel.addProvider(newProviderProfile, providerImageUri)
                      authViewModel.setUserName(fullName)
                      authViewModel.registered()
                      // navigationActions.goBack() // Navigate after saving
                    },
                    modifier = Modifier.fillMaxWidth().testTag("continueDashboardButton"),
                    colors = ButtonDefaults.buttonColors(colorScheme.secondary) // Green button
                    ) {
                      Text("Continue to My Dashboard", color = colorScheme.onSecondary)
                    }
              }
            }
      })
}

/**
 * A composable function that displays a detailed form for provider registration. The form allows
 * the user to input essential details about their services, personal description, starting price,
 * languages, and upload a profile image.
 *
 * @param selectedService The currently selected service offered by the provider.
 * @param onSelectedServiceChange A lambda function to handle changes in the selected service.
 * @param description A brief description provided by the provider about their skills and services.
 * @param onDescriptionChange A lambda function to handle changes in the description.
 * @param startingPrice The minimum price at which the provider offers their services.
 * @param onStartingPriceChange A lambda function to handle changes in the starting price.
 * @param selectedLanguages A mutable list of languages selected by the provider.
 * @param providerImageUri The URI of the provider's uploaded profile image.
 * @param onImageSelected A lambda function triggered when a new image is selected by the provider.
 * @param onClickButton A lambda function to handle the completion of the registration process.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetails(
    selectedService: String,
    onSelectedServiceChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    startingPrice: String,
    onStartingPriceChange: (String) -> Unit,
    selectedLanguages: MutableList<String>,
    providerImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    onClickButton: () -> Unit
) {
  val context = LocalContext.current

  var servicesExpanded by remember { mutableStateOf(false) }
  var languagesExpanded by remember { mutableStateOf(false) }

  val services = Services.entries.toTypedArray()
  val availableLanguages = Language.entries.toList().map { it.toString() }

  val isDescriptionOk =
      description.isNotBlank() &&
          description.length < 250 // (we assume here that a word on average is 5 character)
  val isStartingPriceOk =
      startingPrice.isNotBlank() && ValidationRegex.STARTING_PRICE_REGEX.matches(startingPrice)

  val allIsOk =
      selectedService.isNotEmpty() &&
          isDescriptionOk &&
          isStartingPriceOk &&
          selectedLanguages.isNotEmpty()

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // DropDown Menu to select provider's service
        ExposedDropdownMenuBox(
            expanded = servicesExpanded,
            onExpandedChange = { servicesExpanded = !servicesExpanded },
            modifier = Modifier.testTag("servicesDropDown")) {
              OutlinedTextField(
                  value = selectedService,
                  onValueChange = { onSelectedServiceChange(it) },
                  label = { Text("What Services Do You Offer?") },
                  readOnly = true,
                  modifier = Modifier.fillMaxWidth().menuAnchor())
              ExposedDropdownMenu(
                  expanded = servicesExpanded,
                  onDismissRequest = { servicesExpanded = false },
                  modifier = Modifier.testTag("servicesDropdownMenu")) {
                    services.forEach { service ->
                      DropdownMenuItem(
                          modifier = Modifier.testTag("$service"),
                          text = { Text(text = Services.format(service)) },
                          onClick = {
                            onSelectedServiceChange(Services.format(service))
                            servicesExpanded = false
                          })
                    }
                  }
            }

        // Upload photo provider section
        UploadImage(
            selectedImageUri = providerImageUri,
            imageUrl = null,
            onImageSelected = { uri -> onImageSelected(uri) })

        // Enter Provider Brief description
        CustomOutlinedTextField(
            value = description,
            onValueChange = { onDescriptionChange(it) },
            label = "About you",
            placeholder =
                "Briefly describe your services, skills, and what sets you apart to attract clients.",
            isValueOk = isDescriptionOk,
            errorMessage = "Enter a valid Description (less than 50 words)",
            leadingIcon = Icons.Default.Check,
            leadingIconDescription = "Check Icon",
            testTag = "descriptionInputProviderRegistration",
            errorTestTag = "descriptionErrorInputProviderRegistration")

        // Enter Provider Starting Price
        CustomOutlinedTextField(
            value = startingPrice,
            onValueChange = { onStartingPriceChange(it) },
            label = "Starting Price",
            placeholder = "Enter the minimum price at which your services are available. (CHF)",
            isValueOk = isStartingPriceOk,
            errorMessage = "Enter a valid starting price",
            leadingIcon = Icons.Default.Check,
            leadingIconDescription = "Check Icon",
            testTag = "startingPriceInputProviderRegistration",
            errorTestTag = "startingPriceErrorInputProviderRegistration",
            keyboardType = KeyboardType.Number)

        // Dropdown menu to select provider's languages
        ExposedDropdownMenuBox(
            expanded = languagesExpanded,
            onExpandedChange = { languagesExpanded = !languagesExpanded },
            modifier = Modifier.testTag("languageDropdown")) {
              OutlinedTextField(
                  value =
                      if (selectedLanguages.isEmpty()) "Select Languages"
                      else selectedLanguages.joinToString(", "),
                  onValueChange = {},
                  label = { Text("Languages") },
                  readOnly = true,
                  modifier = Modifier.fillMaxWidth().menuAnchor())

              DropdownMenu(
                  expanded = languagesExpanded,
                  onDismissRequest = { languagesExpanded = false },
                  modifier = Modifier.fillMaxWidth().testTag("languageDropdownMenu")) {
                    availableLanguages.forEach { language ->
                      val isSelected = language in selectedLanguages
                      DropdownMenuItem(
                          modifier = Modifier.testTag(language),
                          text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                              Checkbox(
                                  checked = isSelected,
                                  onCheckedChange = null // Handle click on row instead
                                  )
                              Spacer(modifier = Modifier.width(8.dp))
                              Text(text = language)
                            }
                          },
                          onClick = {
                            if (isSelected) {
                              selectedLanguages.remove(language)
                            } else {
                              selectedLanguages.add(language)
                            }
                          })
                    }
                  }
            }
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {
              if (allIsOk) {
                onClickButton()
              } else {
                Toast.makeText(
                        context, "Please fill in all the correct information", Toast.LENGTH_SHORT)
                    .show()
              }
            },
            modifier =
                Modifier.fillMaxWidth()
                    .testTag("savePreferencesButton")
                    .background(
                        if (allIsOk) {
                          colorScheme.secondary
                        } else {
                          colorScheme.onSurfaceVariant
                        },
                        shape = RoundedCornerShape(25.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
              Text(
                  "Complete Registration",
                  color = colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  modifier = Modifier.testTag("saveButton"))
            }
      }
}

/**
 * Composable function to upload an image.
 *
 * @param selectedImageUri the selected image URI
 * @param imageUrl the image URL
 * @param onImageSelected the callback to select the image
 */
@Composable
fun UploadImage(selectedImageUri: Uri?, imageUrl: String?, onImageSelected: (Uri?) -> Unit) {
  // Manage the interaction to upload an image from user's gallery
  val imagePickerLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.GetContent(),
          onResult = { uri: Uri? -> onImageSelected(uri) })

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(150.dp)
              .border(1.dp, colorScheme.onSurfaceVariant, shape = RoundedCornerShape(12.dp))
              .clip(RoundedCornerShape(12.dp))
              .background(Color.Transparent)
              .testTag("providerImageButton"),
      contentAlignment = Alignment.Center) {
        if (selectedImageUri == null && imageUrl == null) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Click to upload a picture of you",
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") },
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = TextDecoration.Underline))
          }
        } else {
          AsyncImage(
              model =
                  selectedImageUri?.toString()
                      ?: imageUrl, // Show selected image URI or fallback URL
              contentDescription = "Uploaded Image",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize())
        }
      }
}

/**
 * Composable function to enter the provider's packages.
 *
 * @param packagesNames the names of the packages
 * @param packagePrices the prices of the packages
 * @param packagesDetails the details of the packages
 * @param packagesFeatures the features of the packages
 * @param providePackages the state to provide packages
 * @param assistantViewModel the [PackagesAssistantViewModel] to generate packages
 * @param packageViewModel the [PackageProposalViewModel] to handle the package proposals
 * @param providerId the provider ID
 * @param type the service type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPackages(
    packagesNames: MutableList<String>,
    packagePrices: MutableList<String>,
    packagesDetails: MutableList<String>,
    packagesFeatures: SnapshotStateList<SnapshotStateList<String>>,
    providePackages: MutableState<Boolean>,
    assistantViewModel: PackagesAssistantViewModel,
    packageViewModel: PackageProposalViewModel,
    providerId: String,
    type: Services
) {
  // States to manage the menus and dialogs
  var expanded by remember { mutableStateOf(false) }
  var showDialog by remember { mutableStateOf(false) }
  var showForm by remember { mutableStateOf(false) }
  val packagesVisibilityStates = remember { mutableStateMapOf<Int, Boolean>() }
  val assistantPackages = assistantViewModel.packageProposals.collectAsState()
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.testTag("offerPackagesDropDown")) {
              OutlinedTextField(
                  value = if (providePackages.value) "Yes" else "No",
                  onValueChange = {},
                  label = { Text("Do you want to offer service Packages") },
                  readOnly = true,
                  modifier = Modifier.fillMaxWidth().menuAnchor())

              DropdownMenu(
                  expanded = expanded,
                  onDismissRequest = { expanded = false },
              ) {
                DropdownMenuItem(
                    modifier = Modifier.testTag("Yes"),
                    text = { Text("Yes") },
                    onClick = {
                      providePackages.value = true
                      expanded = false
                    })
                DropdownMenuItem(
                    modifier = Modifier.testTag("No"),
                    text = { Text("No") },
                    onClick = {
                      providePackages.value = false
                      expanded = false
                    })
              }
            }

        if (providePackages.value) {
          // Generate packages with AI
          Button(
              onClick = { showDialog = true },
              modifier = Modifier.fillMaxWidth().height(60.dp).testTag("generatePackagesButton"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(colorScheme.secondary) // Green button
              ) {
                Text("Generate packages with AI", color = colorScheme.onSecondary)
              }
          if (showDialog) {
            // Show dialog
            PackageProposalDialog(
                onDismiss = { showDialog = false },
                assistantViewModel = assistantViewModel,
                packageViewModel = packageViewModel,
                providerId = providerId,
                type = type)
          }
          assistantPackages.value.let {
            if (it.isNotEmpty()) {
              Text(
                  text = "Here are the packages we generated for you:",
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.testTag("generatedPackagesTitle"))
              // Display the generated packages
              LazyRow(
                  contentPadding = PaddingValues(horizontal = 16.dp),
                  horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(it) { packageProposal ->
                      PackageCard(
                          packageProposal = packageProposal,
                          modifier =
                              Modifier.width(260.dp)
                                  .height(320.dp)
                                  .verticalScroll(rememberScrollState())
                                  .testTag("PackageCard"))
                    }
                  }
            }
          }
          // Display the 3 packages to fill
          Button(
              onClick = { showForm = true },
              modifier = Modifier.fillMaxWidth().height(60.dp).testTag("enterPackagesButton"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(colorScheme.secondary) // Green button
              ) {
                Text("Enter the packages manually", color = colorScheme.onSecondary)
              }
          if (showForm) {
            // Show packages form
            for (i in 1..3) {
              PackageInputSection(
                  i,
                  expanded = packagesVisibilityStates[i - 1] ?: true,
                  onToggleVisibility = { isExpanded: Boolean ->
                    packagesVisibilityStates[i - 1] = isExpanded
                  },
                  packageName = packagesNames[i - 1],
                  onPackageNameChange = { packagesNames[i - 1] = it },
                  packagePrice = packagePrices[i - 1],
                  onPackagePriceChange = { packagePrices[i - 1] = it },
                  packageDetails = packagesDetails[i - 1],
                  onPackageDetailsChange = { packagesDetails[i - 1] = it },
                  packageFeatures = packagesFeatures,
              )
            }
          }
        }
      }
}

/**
 * Composable function to enter the package details.
 *
 * @param packageNumber the package number
 * @param expanded the state to expand the package
 * @param onToggleVisibility the callback to toggle the visibility
 * @param packageName the package name
 * @param onPackageNameChange the callback to change the package name
 * @param packagePrice the package price
 * @param onPackagePriceChange the callback to change the package price
 * @param packageDetails the package details
 * @param onPackageDetailsChange the callback to change the package details
 * @param packageFeatures the package features
 */
@Composable
fun PackageInputSection(
    packageNumber: Int,
    expanded: Boolean,
    onToggleVisibility: (Boolean) -> Unit,
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    packagePrice: String,
    onPackagePriceChange: (String) -> Unit,
    packageDetails: String,
    onPackageDetailsChange: (String) -> Unit,
    packageFeatures: SnapshotStateList<SnapshotStateList<String>>
) {
  Log.e("Packages", "$packageFeatures")
  Column(modifier = Modifier.fillMaxWidth()) {
    // Package Header with Collapse/Expand Icon
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("package$packageNumber")
                .clickable { onToggleVisibility(!expanded) }) {
          Text(
              text = "Package $packageNumber",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.weight(1f))
          Icon(
              imageVector =
                  if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
              contentDescription = "Toggle Package Visibility")
        }
    if (expanded) {
      // Package Inputs
      Text("Package Name", style = MaterialTheme.typography.bodyLarge)
      Spacer(modifier = Modifier.height(8.dp))
      CustomOutlinedTextField(
          value = packageName,
          onValueChange = { onPackageNameChange(it) },
          placeholder = "Give your package a catchy and descriptive name",
          leadingIcon = Icons.Default.Create,
          isValueOk = true,
          testTag = "packageName$packageNumber",
          errorTestTag = "packageNameError")
      Spacer(modifier = Modifier.height(8.dp))

      Text("Set Your Price", style = MaterialTheme.typography.bodyLarge)
      Spacer(modifier = Modifier.height(8.dp))
      CustomOutlinedTextField(
          value = packagePrice,
          onValueChange = { onPackagePriceChange(it) },
          placeholder = "Enter the cost for this package (CHF)",
          leadingIcon = Icons.Default.Create,
          isValueOk = true,
          testTag = "packagePrice$packageNumber",
          errorTestTag = "packagePriceError")
      Spacer(modifier = Modifier.height(8.dp))

      Text("Package Details", style = MaterialTheme.typography.bodyLarge)
      Spacer(modifier = Modifier.height(8.dp))
      CustomOutlinedTextField(
          value = packageDetails,
          onValueChange = { onPackageDetailsChange(it) },
          placeholder = "Briefly explain what this package offers",
          leadingIcon = Icons.Default.Create,
          isValueOk = true,
          testTag = "packageDetails$packageNumber",
          errorTestTag = "packageDetailsError")

      Spacer(modifier = Modifier.height(8.dp))

      Text("Key Features", style = MaterialTheme.typography.bodyLarge)
      Spacer(modifier = Modifier.height(8.dp))
      repeat(3) { featureNumber ->
        CustomOutlinedTextField(
            value = packageFeatures[packageNumber - 1][featureNumber],
            onValueChange = { packageFeatures[packageNumber - 1][featureNumber] = it },
            placeholder = "Feature ${featureNumber + 1}",
            leadingIcon = Icons.Default.Create,
            isValueOk = true,
            testTag = "packageFeatures$packageNumber$featureNumber",
            errorTestTag = "packageFeaturesError")
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}

/**
 * Composable function to display the package AI generation dialog.
 *
 * @param onDismiss the callback to dismiss the dialog
 * @param providerId the provider ID
 * @param type the service type
 * @param assistantViewModel the [PackagesAssistantViewModel] to generate packages
 * @param packageViewModel the [PackageProposalViewModel] to handle the package proposals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageProposalDialog(
    onDismiss: () -> Unit,
    providerId: String,
    type: Services,
    assistantViewModel: PackagesAssistantViewModel,
    packageViewModel: PackageProposalViewModel
) {
  // States to manage the dialog
  var numberOfPackages by remember { mutableIntStateOf(0) }
  var expanded by remember { mutableStateOf(false) }
  val assistantPackages = assistantViewModel.packageProposals.collectAsState()
  val isLoading = assistantViewModel.isLoading.collectAsState()
  val query = remember { mutableStateOf("") }

  // Dialog to generate packages with AI
  AlertDialog(
      modifier =
          Modifier.border(1.dp, colorScheme.onBackground, RoundedCornerShape(30.dp))
              .testTag("packageProposalDialog"),
      containerColor = colorScheme.background,
      textContentColor = colorScheme.onBackground,
      onDismissRequest = onDismiss,
      title = {
        // Dialog Title
        Text(
            modifier = Modifier.testTag("packageProposalDialogTitle"),
            text = "Generate Packages with AI",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center)
      },
      text = {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .background(colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              if (isLoading.value) {
                // Show loading indicator while fetching packages
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
              } else if (assistantPackages.value.isNotEmpty()) {
                // Display the generated packages
                LazyRow(
                    modifier = Modifier.fillMaxWidth().testTag("packagesScrollableList"),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 40.dp)) {
                      items(assistantPackages.value) { packageProposal ->
                        PackageCard(
                            packageProposal = packageProposal,
                            modifier =
                                Modifier.width(260.dp)
                                    .height(320.dp)
                                    .verticalScroll(rememberScrollState())
                                    .testTag("PackageCard"))
                      }
                    }
              }
              // Allow users to provide more context or information to generate packages
              TextField(
                  value = query.value,
                  onValueChange = { query.value = it },
                  label = { Text("Additional Information") },
                  placeholder = { Text("Provide more details for package generation...") },
                  colors =
                      TextFieldDefaults.textFieldColors(containerColor = colorScheme.background),
                  modifier =
                      Modifier.fillMaxWidth().padding(top = 16.dp).testTag("additionalInfoInput"))
              // Select the number of packages
              ExposedDropdownMenuBox(
                  modifier = Modifier.testTag("numberOfPackagesDropDown"),
                  expanded = expanded,
                  onExpandedChange = { expanded = !expanded }) {
                    TextField(
                        value = numberOfPackages.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Packages number") },
                        trailingIcon = {
                          ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors =
                            TextFieldDefaults.textFieldColors(
                                containerColor = colorScheme.background),
                        modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(
                        modifier = Modifier.background(colorScheme.background),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                          (1..3).forEach { number ->
                            DropdownMenuItem(
                                text = { Text(text = "$number") },
                                onClick = {
                                  numberOfPackages = number
                                  expanded = false
                                })
                          }
                        }
                  }
              // Button to generate packages
              Button(
                  onClick = {
                    assistantViewModel.fetchPackageProposals(
                        type = type,
                        numberOfPackages = numberOfPackages,
                        providerId = providerId,
                        viewModel = packageViewModel,
                        providerQuery = query.value)
                  },
                  modifier = Modifier.fillMaxWidth().testTag("generateButton"),
              ) {
                Text("Generate")
              }
            }
      },
      // Confirm button to accept the generated packages, save them and dismiss the dialog
      confirmButton = {
        TextButton(
            modifier = Modifier.testTag("acceptSuggestionsButton"),
            onClick = {
              assistantPackages.value.forEach { packageProposal ->
                packageViewModel.addPackageProposal(packageProposal)
              }
              onDismiss()
            }) {
              Text("Accept suggestions")
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss, Modifier.testTag("cancelButton")) { Text("Cancel") }
      })
}
