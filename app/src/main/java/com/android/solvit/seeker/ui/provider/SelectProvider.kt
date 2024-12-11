package com.android.solvit.seeker.ui.provider

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.seeker.model.profile.SeekerProfileViewModel
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.map.LocationViewModel
import com.android.solvit.shared.model.map.haversineDistance
import com.android.solvit.shared.model.provider.Language
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.service.Services
import com.android.solvit.shared.ui.map.RequestLocationPermission
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.GradientBlue
import com.android.solvit.shared.ui.theme.GradientGreen
import com.android.solvit.shared.ui.theme.OnSurfaceVariant
import com.android.solvit.shared.ui.theme.SurfaceVariant
import com.android.solvit.shared.ui.theme.Yellow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

@Composable
fun SpTopAppBar(
    navigationActions: NavigationActions,
    selectedService: Services?,
    onClickAction: () -> Unit,
    seekerProfileViewModel: SeekerProfileViewModel
) {

  val location by seekerProfileViewModel.locationSearched.collectAsState()
  Box(modifier = Modifier.fillMaxWidth().testTag("topAppBar")) {
    Image(
        modifier = Modifier.fillMaxWidth().height(200.dp).testTag("serviceImage"),
        painter =
            painterResource(
                id = ServicesImages().serviceMap[selectedService] ?: R.drawable.cleaner_image),
        contentDescription = "image description",
        contentScale = ContentScale.FillBounds)
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { navigationActions.goBack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go Back Option")
          }

          Spacer(Modifier.weight(1f))

          Row(
              modifier = Modifier.wrapContentSize().height(IntrinsicSize.Min),
              horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Image(
                modifier = Modifier.padding(1.dp).height(16.dp).width(16.dp),
                painter = painterResource(id = R.drawable.location),
                contentDescription = "image description",
                contentScale = ContentScale.Crop)
            Text(
                text = location.name,
                style =
                    TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight(400),
                        color = colorScheme.onSurfaceVariant,
                    ))
            Image(
                modifier =
                    Modifier.clickable { onClickAction() }
                        .padding(1.dp)
                        .width(16.dp)
                        .height(16.dp)
                        .testTag("filterByLocation"),
                painter = painterResource(id = R.drawable.arrowdown),
                contentDescription = "image description",
                contentScale = ContentScale.Crop)
          }
        }
  }
}

@Composable
fun SpFilterBar(display: () -> Unit, listProviderViewModel: ListProviderViewModel) {
  val context = LocalContext.current
  val filters = listOf("Top Rates", "Top Prices", "Time")

  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("filterBar"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          filters.forEach { filter ->
            Card(
                modifier =
                    Modifier.wrapContentSize()
                        .clickable {
                          Toast.makeText(context, "Not Yet Implemented", Toast.LENGTH_LONG).show()
                        }
                        .testTag("filterIcon"),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.background),
                border = BorderStroke(1.dp, colorScheme.primary),
                shape = RoundedCornerShape(50)) {
                  Text(
                      text = filter,
                      fontSize = MaterialTheme.typography.bodySmall.fontSize,
                      lineHeight = 34.sp,
                      fontFamily = FontFamily(Font(R.font.roboto)),
                      fontWeight = FontWeight(400),
                      color = colorScheme.primary,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier =
                Modifier.clickable {
                      listProviderViewModel.refreshFilters()
                      display()
                    }
                    .testTag("filterOption"),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.background)) {
              Icon(
                  painter =
                      painterResource(id = R.drawable.tune), // Make sure to use your SVG resource
                  contentDescription = "filter options",
                  modifier = Modifier.padding(8.dp).size(32.dp).testTag("filterIcon"),
                  tint = colorScheme.onBackground)
            }
      }
}

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

@Composable
fun Note(note: String = "5") {
  Box(
      modifier =
          Modifier.width(46.dp)
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

@Composable
fun DisplayPopularProviders(
    providers: List<Provider>,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  LazyRow(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("popularProviders"),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
      verticalAlignment = Alignment.Top,
      userScrollEnabled = true,
  ) {
    items(providers.filter { it.popular }) { provider ->
      Card(
          modifier =
              Modifier.width(141.dp).height(172.dp).clickable {
                listProviderViewModel.selectProvider(provider)
                navigationActions.navigateTo(Route.PROVIDER_INFO)
              },
          elevation =
              CardDefaults.cardElevation(
                  defaultElevation = 8.dp, pressedElevation = 4.dp, focusedElevation = 10.dp),
          shape = RoundedCornerShape(16.dp)) {
            Box(modifier = Modifier.fillMaxSize()) {
              AsyncImage(
                  modifier = Modifier.fillMaxSize(),
                  model = provider.imageUrl.ifEmpty { R.drawable.empty_profile_img },
                  placeholder = painterResource(id = R.drawable.loading),
                  error = painterResource(id = R.drawable.error),
                  contentDescription = "provider image",
                  contentScale = ContentScale.Crop)

              // Add gradient overlay
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(72.dp)
                          .align(Alignment.BottomCenter)
                          .background(
                              brush =
                                  Brush.verticalGradient(
                                      colors = listOf(Color.Transparent, colorScheme.background))))

              Row(
                  modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = provider.name.uppercase(),
                        style =
                            TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(400),
                                color = colorScheme.onBackground),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Note(provider.rating.toInt().toString())
                  }
            }
          }
    }
  }
}

@Composable
fun ListProviders(
    providers: List<Provider>,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  LazyColumn(
      modifier = Modifier.fillMaxWidth().testTag("providersList"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      userScrollEnabled = true,
  ) {
    items(providers) { provider ->
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)) {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                  Row(
                      modifier =
                          Modifier.fillMaxWidth()
                              .background(
                                  color = colorScheme.background,
                                  shape = RoundedCornerShape(size = 16.dp))
                              .clickable {
                                listProviderViewModel.selectProvider(provider)
                                navigationActions.navigateTo(Route.PROVIDER_INFO)
                              },
                      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)) {
                        AsyncImage(
                            modifier =
                                Modifier.width(116.dp)
                                    .height(85.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            model = provider.imageUrl.ifEmpty { R.drawable.empty_profile_img },
                            placeholder = painterResource(id = R.drawable.loading),
                            error = painterResource(id = R.drawable.error),
                            contentDescription = "provider image",
                            contentScale = ContentScale.Crop)
                        Column(modifier = Modifier.weight(1f).padding(10.dp)) {
                          Row {
                            Text(
                                text = provider.name,
                                style =
                                    TextStyle(
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        fontWeight = FontWeight(400),
                                        color = colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                    ))
                            Spacer(Modifier.weight(1f))
                            Note(provider.rating.toInt().toString())
                          }
                          Spacer(Modifier.height(12.dp))
                          Text(
                              text = provider.description,
                              style =
                                  TextStyle(
                                      fontSize = 14.sp,
                                      lineHeight = 15.sp,
                                      fontWeight = FontWeight(400),
                                      color = colorScheme.onBackground.copy(alpha = 0.6f),
                                  ),
                              maxLines = 2,
                              overflow = TextOverflow.Ellipsis)
                        }
                      }
                }
          }
    }
  }
}

@Composable
fun PriceFilter(listProviderViewModel: ListProviderViewModel) {
  var minPrice by remember { mutableStateOf("min") }
  var maxPrice by remember { mutableStateOf("max") }
  var sliderPosition by remember { mutableStateOf(0f..100f) }

  Column {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          BasicTextField(
              value = minPrice,
              textStyle = TextStyle(color = colorScheme.onSurfaceVariant),
              onValueChange = {
                minPrice = it
                val minPriceValue = it.toDoubleOrNull()
                if (minPriceValue != null) {
                  listProviderViewModel.filterProviders(
                      filter = { provider -> provider.price >= minPriceValue }, "Price")
                } else {
                  listProviderViewModel.filterProviders(
                      filter = { provider -> provider.price >= 0 }, "Price")
                }
              },
              modifier =
                  Modifier.weight(1f)
                      .background(color = colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                      .padding(16.dp)
                      .testTag("minPrice"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

          BasicTextField(
              value = maxPrice,
              textStyle = TextStyle(color = colorScheme.onSurfaceVariant),
              onValueChange = {
                maxPrice = it
                val maxPriceValue = it.toDoubleOrNull()
                val minPriceValue = minPrice.toDoubleOrNull()
                if (maxPriceValue != null &&
                    minPriceValue != null &&
                    minPriceValue < maxPriceValue) {
                  listProviderViewModel.filterProviders(
                      { provider -> maxPriceValue >= provider.price }, "Price")
                } else {
                  listProviderViewModel.filterProviders(
                      filter = { provider -> provider.price >= 0 }, "Price")
                }
              },
              modifier =
                  Modifier.weight(1f)
                      .background(colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                      .padding(16.dp)
                      .testTag("maxPrice"),
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }

    Spacer(Modifier.height(8.dp))
    RangeSlider(
        value = sliderPosition,
        onValueChange = { sliderPosition = it },
        valueRange = 0f..100f,
        modifier = Modifier.fillMaxWidth())
  }
}

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

fun filterStringFields(
    selectedFields: MutableList<String>,
    iconsPressed: MutableList<Boolean>,
    iconsColor: MutableList<Color>,
    updateStateField: (List<String>) -> Unit,
    updateStateIconsPressed: (List<Boolean>) -> Unit,
    updateStateIconsColor: (List<Color>) -> Unit,
    idx: Int,
    elem: String,
    listProviderViewModel: ListProviderViewModel,
    filterAction: (Provider) -> Boolean,
    defaultFilterAction: (Provider) -> Boolean,
    filterField: String
) {

  val newIconsPressed = iconsPressed.toMutableList().apply { set(idx, !iconsPressed[idx]) }

  val newIconsColor =
      iconsColor.toMutableList().apply {
        set(idx, if (newIconsPressed[idx]) OnSurfaceVariant else SurfaceVariant)
      }

  val newSelectedFields =
      selectedFields.toMutableList().apply { if (newIconsPressed[idx]) add(elem) else remove(elem) }

  updateStateField(newSelectedFields)
  updateStateIconsPressed(newIconsPressed)
  updateStateIconsColor(newIconsColor)
  if (newIconsPressed[idx]) {
    listProviderViewModel.filterProviders({ provider -> filterAction(provider) }, filterField)
  } else {
    if (newSelectedFields.isNotEmpty()) {
      listProviderViewModel.filterProviders({ provider -> filterAction(provider) }, filterField)
    } else {
      listProviderViewModel.filterProviders(
          { provider -> defaultFilterAction(provider) }, filterField)
    }
  }
}

@Composable
fun LanguageFilterField(list: List<String>, listProviderViewModel: ListProviderViewModel) {
  var selectedFields by remember { mutableStateOf(listOf<String>()) }
  var iconsPressed by remember { mutableStateOf(List(list.size) { false }) }
  var iconsColor by remember { mutableStateOf(List(list.size) { SurfaceVariant }) }

  LazyVerticalGrid(
      columns = GridCells.Fixed(list.size / 2),
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list.size) { idx ->
          Box(
              Modifier.fillMaxWidth()
                  .background(
                      color =
                          if (iconsPressed[idx]) colorScheme.primary
                          else colorScheme.surfaceVariant,
                      shape = RoundedCornerShape(size = 8.dp))
                  .testTag("filterAct")
                  .padding(8.dp)
                  .clickable {
                    filterStringFields(
                        selectedFields.toMutableList(),
                        iconsPressed.toMutableList(),
                        iconsColor.toMutableList(),
                        { a -> selectedFields = a.toMutableList() },
                        { b -> iconsPressed = b.toMutableList() },
                        { c -> iconsColor = c.toMutableList() },
                        idx,
                        list[idx],
                        listProviderViewModel,
                        { provider ->
                          selectedFields
                              .map { u -> Language.valueOf(u.uppercase()) }
                              .intersect(provider.languages.toSet())
                              .isNotEmpty()
                        },
                        { provider -> provider.languages.isNotEmpty() },
                        "Language")
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

@Composable
fun RatingFilterField(list: List<String>, listProviderViewModel: ListProviderViewModel) {
  var selectedFields by remember { mutableStateOf(listOf<String>()) }
  var iconsPressed by remember { mutableStateOf(List(list.size) { false }) }
  var colors by remember { mutableStateOf(List(list.size) { SurfaceVariant }) }

  Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        list.asReversed().forEachIndexed { idx, rating ->
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
                          filterStringFields(
                              selectedFields.toMutableList(),
                              iconsPressed.toMutableList(),
                              colors.toMutableList(),
                              { a -> selectedFields = a.toMutableList() },
                              { b -> iconsPressed = b.toMutableList() },
                              { c -> colors = c.toMutableList() },
                              idx,
                              rating,
                              listProviderViewModel,
                              { provider ->
                                selectedFields.map { u -> u.toDouble() }.contains(provider.rating)
                              },
                              { provider -> provider.rating >= 1.0 },
                              "Rating")
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.Star,
                          contentDescription = "Rating star",
                          modifier = Modifier.size(24.dp).padding(end = 4.dp),
                          tint = Yellow)
                      Text(
                          text = rating,
                          color =
                              if (iconsPressed[idx]) colorScheme.onPrimary
                              else colorScheme.onSurfaceVariant,
                          fontSize = 14.sp)
                    }
              }
        }
      }
}

@Composable
fun ApplyButton(listProviderViewModel: ListProviderViewModel, display: () -> Unit) {
  val filteredList by listProviderViewModel.providersListFiltered.collectAsState()
  Box(
      modifier =
          Modifier.testTag("applyFilterButton")
              .width(249.dp)
              .height(56.dp)
              .background(
                  brush = Brush.horizontalGradient(colors = listOf(GradientBlue, GradientGreen)),
                  shape = RoundedCornerShape(50))
              .clickable {
                listProviderViewModel.applyFilters()
                display()
              },
  ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
        verticalArrangement = Arrangement.Center, // Center vertically
        modifier = Modifier.fillMaxSize() // Ensures the content fills the button
        ) {
          Text(
              text = "Apply", // TODO()
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = colorScheme.onPrimary)
          Text(
              text = "${filteredList.size} providers", // TODO()
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = colorScheme.onPrimary)
        }
  }
}

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
            listOf("French", "English", "German", "Arabic", "Italian", "Spanish"),
            listProviderViewModel)
        FilterSubText("Rating")
        RatingFilterField(listOf("5", "4", "3", "2", "1"), listProviderViewModel)
        Spacer(Modifier.height(16.dp))
        ApplyButton(listProviderViewModel, display)
      }
}

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
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
  }

  val selectedService by listProviderViewModel.selectedService.collectAsState()
  if (selectedService != null)
      listProviderViewModel.filterProviders(
          filter = { provider -> provider.service == selectedService }, "Service")
  val providers by listProviderViewModel.providersListFiltered.collectAsState()

  var displayFilters by remember { mutableStateOf(false) }
  var displayByLocation by remember { mutableStateOf(false) }
  val sheetStateFilter = rememberModalBottomSheetState()
  val sheetStateLocation = rememberModalBottomSheetState()
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        SpTopAppBar(
            navigationActions,
            selectedService,
            onClickAction = { displayByLocation = true },
            seekerProfileViewModel)
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          SpFilterBar(display = { displayFilters = true }, listProviderViewModel)
          Title("Popular")
          DisplayPopularProviders(providers, listProviderViewModel, navigationActions)
          Title("See All")
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
                      listProviderViewModel.filterProviders(filter = { true }, "location")
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
                      displayByLocation = false
                    },
                    locationViewModel)
              }
        }
      }
}
