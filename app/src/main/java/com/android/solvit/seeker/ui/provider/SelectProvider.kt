package com.android.solvit.seeker.ui.provider

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.service.ProviderItem
import com.android.solvit.seeker.ui.service.SERVICES_LIST
import com.android.solvit.seeker.ui.service.ServicesListItem
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.map.haversineDistance
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.theme.Yellow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

/**
 * Composable function for displaying a top app bar in the provider selection screen.
 *
 * @param navigationActions Actions for navigating between screens.
 * @param selectedService Currently selected service.
 * @param onClickAction Callback when the location filter is clicked.
 * @param seekerProfileViewModel ViewModel for accessing and managing seeker profile data.
 */
@Composable
fun TopAppBar(
    navigationActions: NavigationActions,
    selectedService: Services?,
    serviceItem: ServicesListItem,
    onClickAction: () -> Unit,
    seekerProfileViewModel: SeekerProfileViewModel
) {
  val location by seekerProfileViewModel.locationSearched.collectAsState()
  val serviceName = selectedService?.let { Services.format(it) } ?: "Service Providers"
  val serviceIcon = serviceItem.icon

  Box(
      modifier =
          Modifier.fillMaxWidth()
              .height(180.dp)
              .background(
                  brush =
                      Brush.verticalGradient(
                          colors =
                              listOf(
                                  serviceItem.color.copy(alpha = 0.85f), // Darker at the top
                                  serviceItem.color.copy(alpha = 0.5f), // Lighter at the center
                                  Color.Transparent // Transparent at the bottom
                                  )))
              .testTag("topAppBar")) {
        // Content
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
              // Top Row: Navigation and Location
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    // Back Button
                    IconButton(onClick = { navigationActions.goBack() }) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Navigate Back",
                          tint = colorScheme.onPrimary)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.clickable(onClick = onClickAction)
                                .testTag("filterByLocation")) {
                          Icon(
                              imageVector = Icons.Outlined.LocationOn,
                              contentDescription = "Location Icon",
                              tint = colorScheme.onPrimary,
                              modifier = Modifier.size(20.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                              text =
                                  if (location != null) location!!.name.substring(0, 6) + "..."
                                  else "Worldwide",
                              style =
                                  Typography.bodyMedium.copy(
                                      color = colorScheme.onPrimary,
                                      fontWeight = FontWeight.Medium),
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis)
                        }
                  }

              Spacer(modifier = Modifier.height(8.dp))

              // Service Display
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Start) {
                    Icon(
                        painter = painterResource(id = serviceIcon),
                        contentDescription = "$serviceName Icon",
                        tint = Color.Unspecified,
                        modifier =
                            Modifier.size(60.dp) // Larger for better visibility
                                .padding(end = 16.dp))

                    Column {
                      Text(
                          text = serviceName,
                          style =
                              Typography.headlineMedium.copy(
                                  color = colorScheme.onPrimary, fontWeight = FontWeight.Bold))
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = "Find the best $serviceName near you",
                          style = Typography.bodyMedium.copy(color = colorScheme.onPrimary),
                          fontWeight = FontWeight.Medium)
                    }
                  }

              Spacer(modifier = Modifier.height(16.dp))
            }
      }
}

/**
 * Composable function for the filter bar, allowing users to filter providers by predefined
 * categories.
 *
 * @param display Callback to trigger the display of the filter modal.
 * @param listProviderViewModel ViewModel for managing provider data and filters.
 */
@Composable
fun FilterBar(
    display: () -> Unit,
    listProviderViewModel: ListProviderViewModel,
    serviceItem: ServicesListItem
) {

  val filters = listOf("Top Rates", "Top Prices", "Highest Activity")

  val isSelected = remember { mutableStateListOf(-1, -1, -1) }

  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("filterBar"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly) {
              filters.forEach { filter ->
                Card(
                    modifier =
                        Modifier.wrapContentSize()
                            .clickable {
                              val index = filters.indexOf(filter)
                              isSelected[index] = if (isSelected[index] == index) -1 else index
                              listProviderViewModel.sortProviders(
                                  filter,
                                  isSelected[index] == index,
                              )
                            }
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = serviceItem.color)
                            .testTag("filterOption"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.background),
                    shape = RoundedCornerShape(16.dp),
                    border =
                        if (isSelected[filters.indexOf(filter)] == filters.indexOf(filter))
                            BorderStroke(1.dp, colorScheme.primary)
                        else null) {
                      Text(
                          text = filter,
                          fontSize = Typography.bodySmall.fontSize,
                          lineHeight = 34.sp,
                          fontFamily = FontFamily(Font(R.font.roboto)),
                          fontWeight = FontWeight(400),
                          color = serviceItem.color,
                          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
              }
            }

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            painter = painterResource(id = R.drawable.tune),
            contentDescription = "filter options",
            modifier =
                Modifier.padding(8.dp)
                    .size(32.dp)
                    .shadow(12.dp, shape = CircleShape)
                    .testTag("filterIcon")
                    .clickable { display() },
            tint = colorScheme.onBackground)
      }
}

/**
 * Displays a title text with a specific style.
 *
 * @param title The text to display as the title.
 */
@Composable
fun Title(title: String) {
  Row(modifier = Modifier.padding(16.dp)) {
    Text(
        modifier = Modifier.width(73.dp).height(22.dp),
        text = title,
        style =
            TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight(600),
                color = colorScheme.onBackground,
            ))
  }
}

/**
 * Displays a rating bubble with a star icon and a rating value.
 *
 * @param note The rating value to display (default is "5").
 * @param modifier Modifier for the rating bubble.
 */
@Composable
fun Note(note: String = "5", modifier: Modifier = Modifier) {
  Box(
      modifier =
          modifier
              .width(46.dp)
              .height(24.dp)
              .background(color = colorScheme.onSurface, shape = RoundedCornerShape(size = 59.dp))
              .testTag("Rating")) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = "Rating star",
                  modifier = Modifier.size(24.dp).padding(2.dp),
                  tint = Yellow)

              Spacer(modifier = Modifier.width(2.dp))

              Text(
                  text = note,
                  color = colorScheme.surface,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium)
            }
      }
}

/**
 * Displays a horizontal list of popular providers with their images and ratings.
 *
 * @param providers List of providers to display.
 * @param listProviderViewModel ViewModel for managing provider data and interactions.
 * @param navigationActions Actions for navigating between screens.
 */
@Composable
fun DisplayPopularProviders(
    providers: List<Provider>,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  LazyRow(
      modifier = Modifier.fillMaxWidth().testTag("popularProviders"),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
      verticalAlignment = Alignment.Top,
      userScrollEnabled = true,
  ) {
    items(providers.filter { it.popular }) { provider ->
      ProviderItem(provider, false) {
        listProviderViewModel.selectProvider(provider)
        navigationActions.navigateTo(Route.PROVIDER_INFO)
      }
    }
  }
}

/**
 * Displays a vertical list of all providers, categorized into "Popular" and "See All."
 *
 * @param providers List of providers to display.
 * @param listProviderViewModel ViewModel for managing provider data and interactions.
 * @param navigationActions Actions for navigating between screens.
 */
@Composable
fun ListProviders(
    providers: List<Provider>,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  LazyColumn(
      modifier =
          Modifier.fillMaxSize()
              .background(colorScheme.background)
              .padding(horizontal = 16.dp)
              .testTag("providersList"),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Popular providers section
        item {
          Text(
              text = "Popular",
              style = Typography.titleMedium,
              modifier = Modifier.padding(vertical = 8.dp))
        }
        item { DisplayPopularProviders(providers, listProviderViewModel, navigationActions) }

        // All providers section
        item {
          Text(
              text = "See All",
              style = Typography.titleMedium,
              modifier = Modifier.padding(vertical = 8.dp))
        }
        items(providers) { provider ->
          ProviderRowCard(provider, listProviderViewModel, navigationActions)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
      }
}

/**
 * Displays a card for a single provider, with their image, name, description, and rating.
 *
 * @param provider The provider to display.
 * @param listProviderViewModel ViewModel for managing provider data and interactions.
 * @param navigationActions Actions for navigating between screens.
 */
@Composable
fun ProviderRowCard(
    provider: Provider,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  OutlinedCard(
      modifier =
          Modifier.fillMaxWidth()
              .clickable {
                listProviderViewModel.selectProvider(provider)
                navigationActions.navigateTo(Route.PROVIDER_INFO)
              }
              .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
      border = BorderStroke(1.dp, Services.getColor(provider.service)),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(colorScheme.background)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
          // Provider Image
          AsyncImage(
              model = provider.imageUrl.ifEmpty { R.drawable.empty_profile_img },
              placeholder = painterResource(id = R.drawable.loading),
              contentDescription = "Provider Image",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(60.dp).clip(CircleShape).background(colorScheme.surfaceVariant))

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            // Provider Name
            Text(
                text = provider.name,
                style = Typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)

            // Provider Description with Progressive Disclosure
            var isExpanded by remember { mutableStateOf(false) }
            var textOverflow by remember { mutableStateOf(false) }
            Text(
                text = provider.description,
                style = Typography.bodySmall.copy(color = colorScheme.onSurfaceVariant),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                  if (!isExpanded) {
                    textOverflow = textLayoutResult.hasVisualOverflow
                  }
                })

            // "Read More" or "Read Less" Link
            if (textOverflow) {
              Text(
                  text = if (isExpanded) "Read Less" else "Read More",
                  modifier = Modifier.clickable { isExpanded = !isExpanded }.padding(top = 4.dp),
                  style =
                      Typography.bodySmall.copy(
                          color = colorScheme.primary, fontWeight = FontWeight.Bold))
            }
          }

          // Provider Rating
          Note(
              note = provider.rating.toInt().toString(),
              modifier = Modifier.align(Alignment.CenterVertically))
        }
      }
}

/**
 * Composable function for filtering providers by price using a range slider and input fields.
 *
 * @param listProviderViewModel ViewModel for managing provider filters.
 */
@Composable
fun PriceFilter(listProviderViewModel: ListProviderViewModel) {
  val minPrice by listProviderViewModel.minPrice.collectAsState()
  val maxPrice by listProviderViewModel.maxPrice.collectAsState()

  Column {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          Box(
              modifier =
                  Modifier.weight(1f)
                      .background(colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                      .padding(16.dp)) {
                if (minPrice!!.isEmpty()) {
                  Text(
                      text = "Min Price",
                      style = TextStyle(color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                }

                BasicTextField(
                    value = minPrice ?: "",
                    textStyle = TextStyle(color = colorScheme.onSurfaceVariant),
                    onValueChange = { listProviderViewModel.updateMinPrice(it) },
                    modifier = Modifier.fillMaxWidth().testTag("minPrice"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
              }

          // Max Price Text Field
          Box(
              modifier =
                  Modifier.weight(1f)
                      .background(colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                      .padding(16.dp)) {
                if (maxPrice!!.isEmpty()) {
                  Text(
                      text = "Max Price",
                      style = TextStyle(color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                }

                BasicTextField(
                    value = maxPrice ?: "",
                    textStyle = TextStyle(color = colorScheme.onSurfaceVariant),
                    onValueChange = { listProviderViewModel.updateMaxPrice(it) },
                    modifier = Modifier.fillMaxWidth().testTag("maxPrice"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
              }
        }
  }
}

/**
 * Displays a subheading text for the filter categories.
 *
 * @param text The subheading text to display.
 */
@Composable
fun FilterSubText(text: String) {
  Text(
      text = text,
      style =
          TextStyle(
              fontSize = 17.sp,
              fontWeight = FontWeight(500),
              color = colorScheme.onBackground,
          ),
      modifier = Modifier.padding(top = 24.dp, bottom = 4.dp))
}

/**
 * Helper function for filtering providers based on string fields, such as languages or ratings.
 *
 * @param selectedFields Currently selected filter fields.
 * @param iconsPressed List of boolean values indicating which icons are pressed.
 * @param iconsColor List of colors for the icons.
 * @param updateStateField Callback to update the selectedFields state.
 * @param updateStateIconsPressed Callback to update the iconsPressed state.
 * @param updateStateIconsColor Callback to update the iconsColor state.
 * @param idx Index of the current element.
 * @param elem The current element being filtered.
 * @param listProviderViewModel ViewModel for managing provider filters.
 * @param filterAction Action to filter providers based on the current field.
 * @param defaultFilterAction Action to apply a default filter when no specific filters are
 *   selected.
 * @param filterField The name of the filter field (e.g., "Language" or "Rating").
 */
fun filterStringFields(
    selectedFields: List<String>,
    iconsPressed: List<Boolean>,
    idx: Int,
    listProviderViewModel: ListProviderViewModel,
    filterAction: (Provider) -> Boolean,
    defaultFilterAction: (Provider) -> Boolean,
    filterField: String
) {

  // val newIconsPressed = iconsPressed.toMutableList().apply { set(idx, !iconsPressed[idx]) }

  /*val newIconsColor =
  iconsColor.toMutableList().apply {
    set(idx, if (newIconsPressed[idx]) OnSurfaceVariant else SurfaceVariant)
  }*/

  // val newSelectedFields =
  // selectedFields.toMutableList().apply { if (newIconsPressed[idx]) add(elem) else remove(elem) }

  // updateStateField(newSelectedFields)
  // updateStateIconsPressed(newIconsPressed)
  // updateStateIconsColor(newIconsColor)
  Log.e("filterStringFields", "${iconsPressed[idx]}")
  if (iconsPressed[idx]) {
    listProviderViewModel.filterProviders({ provider -> filterAction(provider) }, filterField)
  } else {
    if (selectedFields.isNotEmpty()) {
      listProviderViewModel.filterProviders({ provider -> filterAction(provider) }, filterField)
    } else {
      listProviderViewModel.filterProviders(
          { provider -> defaultFilterAction(provider) }, filterField)
    }
  }
}

/**
 * Displays a list of language filter options, allowing users to filter providers by their supported
 * languages.
 *
 * @param list List of available language options.
 * @param listProviderViewModel ViewModel for managing provider filters.
 */
@Composable
fun LanguageFilterField(list: List<String>, listProviderViewModel: ListProviderViewModel) {
  val selectedLanguages = listProviderViewModel.selectedLanguages.collectAsState().value
  val languages = list.map { Language.valueOf(it.uppercase()) }
  val iconsPressed = languages.map { it in selectedLanguages }

  Log.e("selectedLanguages", "$selectedLanguages")
  Log.e("icons Pressed", "$iconsPressed")

  LazyVerticalGrid(
      columns = GridCells.Fixed(list.size / 2),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list.size) { idx ->
          val currentLanguage = languages[idx]
          Box(
              Modifier.fillMaxWidth()
                  .background(
                      color =
                          if (currentLanguage in selectedLanguages) colorScheme.primary
                          else colorScheme.surfaceVariant,
                      shape = RoundedCornerShape(size = 8.dp))
                  .testTag("filterAct")
                  .padding(8.dp)
                  .clickable {
                    val newSet =
                        if (iconsPressed[idx]) {
                          selectedLanguages - currentLanguage
                        } else {
                          selectedLanguages + currentLanguage
                        }
                    listProviderViewModel.updateSelectedLanguages(
                        newSet, languagePressed = currentLanguage)
                  }) {
                Text(
                    text = list[idx],
                    fontSize = 18.sp,
                    modifier = Modifier.padding(3.dp).align(Alignment.Center),
                    color =
                        if (iconsPressed[idx]) colorScheme.onPrimary
                        else colorScheme.onSurfaceVariant)
              }
        }
      }
}

/**
 * Displays a row of rating filter options, allowing users to filter providers by their ratings.
 *
 * @param list List of available rating options.
 * @param listProviderViewModel ViewModel for managing provider filters.
 */
@Composable
fun RatingFilterField(list: List<Double>, listProviderViewModel: ListProviderViewModel) {
  val selectedRatings = listProviderViewModel.selectedRatings.collectAsState().value
  val iconsPressed = list.map { it in selectedRatings }
  Log.e("selectedRating", "$selectedRatings")
  Log.e("ratingsPressed", "$iconsPressed")

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        list.forEachIndexed { idx, rating ->
          Box(
              modifier =
                  Modifier.weight(1f)
                      .height(30.dp)
                      .background(
                          color =
                              if (iconsPressed[idx]) colorScheme.primary
                              else colorScheme.surfaceVariant,
                          shape = RoundedCornerShape(size = 59.dp))
                      .testTag("filterRating")) {
                Row(
                    modifier =
                        Modifier.fillMaxSize().clickable {
                          val newSet =
                              if (iconsPressed[idx]) {
                                selectedRatings - rating
                              } else {
                                selectedRatings + rating
                              }
                          listProviderViewModel.updateSelectedRatings(newSet, rating)
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.Star,
                          contentDescription = "Rating star",
                          modifier = Modifier.size(24.dp).padding(end = 4.dp),
                          tint = Yellow)
                      Text(
                          text = rating.toInt().toString(),
                          color =
                              if (iconsPressed[idx]) colorScheme.onPrimary
                              else colorScheme.onSurfaceVariant,
                          fontSize = 14.sp)
                    }
              }
        }
      }
}

/**
 * Displays a button for applying the selected filters and updating the provider list.
 *
 * @param listProviderViewModel ViewModel for managing provider filters.
 * @param display Callback to hide the filter modal.
 */
@Composable
fun ApplyButton(listProviderViewModel: ListProviderViewModel, display: () -> Unit) {
  val filteredList by listProviderViewModel.providersListFiltered.collectAsState()
  Box(
      modifier =
          Modifier.testTag("applyFilterButton")
              .width(249.dp)
              .height(56.dp)
              .background(
                  brush =
                      Brush.horizontalGradient(
                          colors = listOf(colorScheme.primary, colorScheme.secondary)),
                  shape = RoundedCornerShape(50))
              .clickable {
                // listProviderViewModel.applyFilters()
                display()
              },
  ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()) {
          Text(
              text = "Apply",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = colorScheme.onPrimary)
          Text(
              text = "${filteredList.size} providers",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = colorScheme.onPrimary)
        }
  }
}

/**
 * Composable function for displaying the filter modal, with options for filtering by price,
 * languages, and ratings.
 *
 * @param listProviderViewModel ViewModel for managing provider filters.
 * @param display Callback to hide the filter modal.
 */
@Composable
fun FilterComposable(listProviderViewModel: ListProviderViewModel, display: () -> Unit) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("filterSheet"),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Filters",
            style =
                TextStyle(
                    fontSize = 21.sp,
                    fontWeight = FontWeight(600),
                    color = colorScheme.onBackground,
                ))
        FilterSubText("Price")
        PriceFilter(listProviderViewModel)
        FilterSubText("Languages")
        LanguageFilterField(
            listOf("french", "english", "german", "arabic", "italian", "spanish"),
            listProviderViewModel)
        FilterSubText("Rating")
        RatingFilterField(listOf(1.0, 2.0, 3.0, 4.0, 5.0), listProviderViewModel)
        Spacer(Modifier.height(16.dp))
        ApplyButton(listProviderViewModel, display)
      }
}
/**
 * Retrieves a human-readable location name from latitude and longitude coordinates.
 *
 * @param latLng The latitude and longitude coordinates.
 * @param context The context for accessing geocoding services.
 * @return A readable location name (e.g., city and country) or "Unknown Location" if unavailable.
 */
fun getLocationName(latLng: LatLng, context: Context): String {
  val geocoder = Geocoder(context, Locale.getDefault())
  return try {
    // Get a list of addresses for the provided latitude and longitude
    val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    // Extract the address name if available
    if (!addresses.isNullOrEmpty()) {
      val address = addresses[0]
      // Get a readable location name, like city and country
      "${address.locality ?: address.subAdminArea ?: address.adminArea}, ${address.countryName}"
    } else {
      "Unknown location"
    }
  } catch (e: Exception) {
    Log.e("Geocoder Exception", "$e")
    "Unknown Location"
  }
}

/**
 * Composable function for a search bar to input and search for locations.
 *
 * @param searchedAddress The current input in the search bar.
 * @param onSearchChanged Callback when the search input changes.
 */
@Composable
fun SearchLocBar(searchedAddress: String, onSearchChanged: (String) -> Unit) {
  TextField(
      value = searchedAddress,
      onValueChange = onSearchChanged,
      placeholder = { Text("Enter a new address", color = colorScheme.onSurface) },
      leadingIcon = {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Icon",
            tint = colorScheme.onSurface)
      },
      modifier =
          Modifier.fillMaxWidth()
              .height(56.dp)
              .background(color = colorScheme.surface, shape = RoundedCornerShape(16.dp))
              .testTag("SearchLocBar"))
}

/**
 * Displays a location suggestion with its name and an icon.
 *
 * @param location The location object containing the name and other details.
 * @param index Index of the location in the list.
 * @param onClickAction Callback when the location is clicked.
 */
@Composable
fun LocationSuggestion(location: Location, index: Int, onClickAction: () -> Unit) {

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  if (index == 0) colorScheme.surfaceVariant.copy(1.5f) else Color.Transparent)
              .clickable { onClickAction() }
              .padding(16.dp)
              .testTag("suggestedLocation"),
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location Icon",
            tint = colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
              text = location.name,
              style =
                  TextStyle(
                      fontSize = 16.sp,
                      lineHeight = 20.sp,
                      fontWeight = FontWeight(500),
                      color = colorScheme.onBackground,
                  ))
          Text(
              text = "CA", // TODO
              style =
                  TextStyle(
                      fontSize = 16.sp,
                      lineHeight = 20.sp,
                      fontWeight = FontWeight(500),
                      color = colorScheme.onBackground,
                  ))
        }
      }
}

/**
 * Displays a location filter modal, allowing users to filter providers by their location.
 *
 * @param userId Current user's ID.
 * @param seekerProfileViewModel ViewModel for managing seeker profile data.
 * @param onClick Callback when a location is selected.
 * @param locationViewModel ViewModel for managing location data and queries.
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun FilterByLocation(
    userId: String,
    seekerProfileViewModel: SeekerProfileViewModel,
    onClick: (Location) -> Unit,
    locationViewModel: LocationViewModel
) {
  // Represent the address user is searching
  var searchedAddress by remember { mutableStateOf("") }
  // List of location suggestions given the searched address
  val locationSuggestions by locationViewModel.locationSuggestions.collectAsState()
  // List of saved locations of user
  val cachedLocations by seekerProfileViewModel.cachedLocations.collectAsState()
  // User current location
  var userLocation by remember { mutableStateOf<LatLng?>(null) }
  // Get the current context
  val context = LocalContext.current
  // Initialize the FusedLocationProviderClient
  val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
  // Check whether user want to use his current location
  var enableCurrentLocation by remember { mutableStateOf(false) }

  if (enableCurrentLocation)
      RequestLocationPermission(context, fusedLocationClient) { location ->
        userLocation = location
      }
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("filterByLocationSheet"),
  ) {
    // SearchBar
    Box(
        modifier =
            Modifier.fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally)
                .background(
                    color = colorScheme.surface, shape = RoundedCornerShape(size = 16.dp))) {
          SearchLocBar(
              searchedAddress = searchedAddress,
              onSearchChanged = {
                searchedAddress = it
                locationViewModel.setQuery(searchedAddress)
              })
        }

    Spacer(Modifier.height(15.dp))
    // Display either saved locations or new location if searching in bar
    if (searchedAddress.isEmpty()) {
      // User Current Location

      Text(
          text = "Nearby",
          style =
              TextStyle(
                  fontSize = 16.sp,
                  lineHeight = 20.sp,
                  fontWeight = FontWeight(500),
                  color = colorScheme.onBackground,
              ))

      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .testTag("currentLocation")
                  .clickable {
                    enableCurrentLocation = true
                    Log.e("LOCATE USER", "${userLocation?.latitude}")
                    userLocation
                        ?.let { Location(it.latitude, it.longitude, getLocationName(it, context)) }
                        ?.let {
                          seekerProfileViewModel.setLocationSearched(it)
                          onClick(it)
                        }
                  }
                  .padding(8.dp)) {
            Icon(Icons.Outlined.Home, contentDescription = null, tint = colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Current location",
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(500),
                        color = colorScheme.onBackground,
                    ))
          }
      // Add the line separator
      HorizontalDivider(color = colorScheme.onSurfaceVariant)

      Spacer(Modifier.height(6.dp))
      Text(
          text = "Recent locations",
          style =
              TextStyle(
                  fontSize = 16.sp,
                  lineHeight = 20.sp,
                  fontWeight = FontWeight(500),
                  color = colorScheme.onBackground,
              ))
      LazyColumn(modifier = Modifier.testTag("cachedLocations")) {
        itemsIndexed(cachedLocations) { index, location ->
          // TODO see the index if it's the first element set its background color to the green
          LocationSuggestion(
              location,
              index,
              onClickAction = {
                seekerProfileViewModel.setLocationSearched(location)
                onClick(location)
              })
        }
      }
    } else {
      LazyColumn(modifier = Modifier.testTag("suggestedLocations")) {
        itemsIndexed(locationSuggestions) { index, location ->
          LocationSuggestion(
              location,
              index,
              onClickAction = {
                seekerProfileViewModel.updateCachedLocations(userId, location)
                seekerProfileViewModel.setLocationSearched(location)
                onClick(location)
              })
        }
      }
    }
  }
}

/**
 * Main screen for selecting providers, with options for filtering by location, price, rating, and
 * more.
 *
 * @param seekerProfileViewModel ViewModel for managing seeker profile data.
 * @param listProviderViewModel ViewModel for managing provider data and interactions.
 * @param userId Current user's ID.
 * @param navigationActions Actions for navigating between screens.
 * @param locationViewModel ViewModel for managing location data and queries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectProviderScreen(
    seekerProfileViewModel: SeekerProfileViewModel =
        viewModel(factory = SeekerProfileViewModel.Factory),
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory),
    userId: String,
    navigationActions: NavigationActions,
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
) {
  // Lock Orientation to Portrait
  val context = LocalContext.current

  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose {
      locationViewModel.clear()
      seekerProfileViewModel.clearLocation()
      listProviderViewModel.refreshFilters()
      listProviderViewModel.clearFilterFields()
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
  }

  val selectedService by listProviderViewModel.selectedService.collectAsState()
  listProviderViewModel.getProviders()
  val providers by listProviderViewModel.providersListFiltered.collectAsState()

  var displayFilters by remember { mutableStateOf(false) }
  var displayByLocation by remember { mutableStateOf(false) }
  val sheetStateFilter = rememberModalBottomSheetState()
  val sheetStateLocation = rememberModalBottomSheetState()
  val servicesListItem =
      SERVICES_LIST.find { it.service == selectedService } ?: SERVICES_LIST.last()
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        TopAppBar(
            navigationActions,
            selectedService,
            servicesListItem,
            { displayByLocation = true },
            seekerProfileViewModel)
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          FilterBar(display = { displayFilters = true }, listProviderViewModel, servicesListItem)
          ListProviders(providers, listProviderViewModel, navigationActions)
        }
        if (displayFilters) {
          ModalBottomSheet(
              onDismissRequest = { displayFilters = false },
              sheetState = sheetStateFilter,
              containerColor = colorScheme.surface) {
                FilterComposable(listProviderViewModel) { displayFilters = false }
              }
        }
        if (displayByLocation) {
          seekerProfileViewModel.getCachedLocations(userId)
          ModalBottomSheet(
              onDismissRequest = { displayByLocation = false },
              sheetState = sheetStateLocation,
              containerColor = colorScheme.surface) {
                FilterByLocation(
                    userId,
                    seekerProfileViewModel,
                    onClick = { location ->
                      // listProviderViewModel.filterProviders(filter = { true }, "location")
                      listProviderViewModel.filterProviders(
                          filter = { provider ->
                            // filter providers within a 25.0 km radius
                            haversineDistance(
                                location.latitude,
                                location.longitude,
                                provider.location.latitude,
                                provider.location.longitude) <= 25.0
                          },
                          "location")
                      Log.e("FILTER PROVIDER BY LOC", "$providers")
                      // listProviderViewModel.applyFilters()
                      displayByLocation = false
                    },
                    locationViewModel)
              }
        }
      }
}
