package com.android.solvit.ui.overview

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.model.provider.Language
import com.android.solvit.model.provider.ListProviderViewModel
import com.android.solvit.model.provider.Provider
import com.android.solvit.ui.navigation.NavigationActions
import kotlinx.coroutines.launch

@Composable
fun SpTopAppBar(navigationActions: NavigationActions) {
  Box(modifier = Modifier.fillMaxWidth().testTag("topAppBar")) {
    Image(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        painter = painterResource(id = R.drawable.plumbier), // TODO Link each service to an image
        contentDescription = "image description",
        contentScale = ContentScale.FillBounds)
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
          IconButton(
              onClick = {
                navigationActions.goBack()
              }) {
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
                text = "Aspen, USA", // TODO() edit with user location
                style =
                    TextStyle(
                        fontSize = 12.sp,
                        // fontFamily = FontFamily(Font(R.font.alata)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF606060),
                    ))
            Image(
                modifier = Modifier.clickable { TODO() }.padding(1.dp).width(16.dp).height(16.dp),
                painter = painterResource(id = R.drawable.arrowdown),
                contentDescription = "image description",
                contentScale = ContentScale.Crop)
          }
        }
  }
}

@Composable
fun SpFilterBar(display: () -> Unit,listProviderViewModel: ListProviderViewModel) {
  val filters = listOf("Top Rates", "Top Prices", "Time") // TODO update with decided list of filters
  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("filterBar"),
      verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
            verticalAlignment = Alignment.Top,
        ) {
          items(filters) { filter ->
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(
                            brush =
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE0E0E0), Color(0xFFFFFFFF))),
                            shape = RoundedCornerShape(50))
                        .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)) {
                  Text(
                      text = filter,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      color = Color(0xFF4D5E29))
                }
          }
        }
        Spacer(Modifier.weight(1f))
        Image(
            modifier =
                Modifier.clickable {
                    listProviderViewModel.refreshFilters()
                    display()
                }
                    .padding(1.dp)
                    .width(18.dp)
                    .height(18.dp)
                    .testTag("filterOption"),
            painter = painterResource(id = R.drawable.vector),
            contentDescription = "image description",
            contentScale = ContentScale.Crop)
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
                color = Color(0xFF232323),
            ))
  }
}

@Composable
fun Note(note: String = "5") {

  Box(
      modifier =
          Modifier.width(46.dp)
              .height(24.dp)
              .background(color = Color(0xFF4D5652), shape = RoundedCornerShape(size = 59.dp))) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // part has to be modified we can add function here
            modifier = Modifier.clickable {}) {
              Image(
                  modifier = Modifier.padding(1.90667.dp).width(16.dp).height(16.dp),
                  painter = painterResource(id = R.drawable.star),
                  contentDescription = "image description",
                  contentScale = ContentScale.Crop)

              Text(
                  text = note, // TODO
                  color = Color.White,
                  fontSize = 14.sp)
            }
      }
}

@Composable
fun DisplayPopularProviders(providers: List<Provider>) {

  LazyRow(
      modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("popularProviders"),
      horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
      verticalAlignment = Alignment.Top,
      userScrollEnabled = true,
  ) {
    items(providers.filter { it.popular }) { provider ->
      Box(modifier = Modifier.clip(RoundedCornerShape(16.dp))) {
        AsyncImage(
            modifier = Modifier.width(141.dp).height(172.dp),
            model = provider.imageUrl,
            placeholder = painterResource(id = R.drawable.loading),
            error = painterResource(id = R.drawable.error),
            contentDescription = "provider image",
            contentScale = ContentScale.Crop)
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color(android.graphics.Color.parseColor("#2A5A52")),
                                        Color(android.graphics.Color.parseColor("#DBD1B9"))))))

        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = provider.name.uppercase(),
                  style =
                      TextStyle(
                          fontSize = 16.sp,
                          fontWeight = FontWeight(400),
                          color = Color(0xFFFFFFFF),
                      ))
              Spacer(Modifier.width(40.dp))
              Note(provider.rating.toString())
            }
      }
    }
  }
}

@Composable
fun ListProviders(providers: List<Provider>) {
  LazyColumn(
      modifier = Modifier.fillMaxWidth().testTag("providersList"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      userScrollEnabled = true,
  ) {
    items(providers) { provider ->
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 16.dp)),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)) {
            AsyncImage(
                modifier = Modifier.width(116.dp).height(85.dp).clip(RoundedCornerShape(12.dp)),
                model = provider.imageUrl,
                placeholder = painterResource(id = R.drawable.loading),
                error = painterResource(id = R.drawable.plumbierprvd),
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
                            color = Color(0xFF6E7146),
                            textAlign = TextAlign.Center,
                        ))
                Spacer(Modifier.weight(1f))
                Note(provider.rating.toString())
              }
              Spacer(Modifier.height(12.dp))
              Text(
                  text = provider.description,
                  style =
                      TextStyle(
                          fontSize = 8.sp,
                          lineHeight = 10.sp,
                          fontWeight = FontWeight(400),
                          color = Color(0xFF000000),
                      ))
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
    ) {
      BasicTextField(
          value = minPrice,
          onValueChange = {
            minPrice = it
            val minPriceValue = it.toDoubleOrNull()
            if (minPriceValue != null) {
              listProviderViewModel.filterProviders(
                  filter = { provider -> provider.price >= minPriceValue },"Price")
            } else {
              listProviderViewModel.filterProviders(filter = { provider -> provider.price >= 0 },"Price")
            }
          },
          modifier =
              Modifier.weight(1f)
                  .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                  .padding(16.dp)
                  .testTag("minPrice"),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

      BasicTextField(
          value = maxPrice,
          onValueChange = {
            maxPrice = it
            val maxPriceValue = it.toDoubleOrNull()
            val minPriceValue = minPrice.toDoubleOrNull()
            if (maxPriceValue != null && minPriceValue != null && minPriceValue < maxPriceValue) {
              listProviderViewModel.filterProviders ({ prvd -> maxPriceValue >= prvd.price },"Price")
            } else {
              listProviderViewModel.filterProviders(filter = { provider -> provider.price >= 0 },"Price")
            }
          },
          modifier =
              Modifier.weight(1f)
                  .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
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
              color = Color(0xFF000000),
          ))
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
    filterField : String
) {


  val newIconsPressed = iconsPressed.toMutableList().apply { set(idx, !iconsPressed[idx]) }

  val newIconsColor =
      iconsColor.toMutableList().apply {
        set(idx, if (newIconsPressed[idx]) Color(0xFF4D5652) else Color.White)
      }

  val newSelectedFields =
      selectedFields.toMutableList().apply { if (newIconsPressed[idx]) add(elem) else remove(elem) }

    updateStateField(newSelectedFields)
    updateStateIconsPressed(newIconsPressed)
    updateStateIconsColor(newIconsColor)
  if (newIconsPressed[idx]) {
    listProviderViewModel.filterProviders ({ provider -> filterAction(provider) },filterField)
  }
  else {
    if (newSelectedFields.isNotEmpty()) {
      listProviderViewModel.filterProviders ({ provider -> filterAction(provider) },filterField)
    } else {
      listProviderViewModel.filterProviders ({ provider -> defaultFilterAction(provider) },filterField)
    }
  }


}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageFilterField(list: List<String>, listProviderViewModel: ListProviderViewModel) {
  var selectedFields by remember { mutableStateOf(listOf<String>()) }
  var iconsPressed by remember { mutableStateOf(List(list.size) { false }) }
  var iconsColor by remember { mutableStateOf(List(list.size) { Color(0xFFF6F6F6) }) }
  Log.e("COLORS", "$iconsColor")
  Log.e("Icons Pressed", "$iconsPressed")

  FlowRow(
      Modifier.padding(20.dp).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    repeat(list.size) { idx ->
      Box(
          Modifier.background(color = iconsColor[idx],shape = RoundedCornerShape(size = 8.dp))
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
                    { prvd ->
                      selectedFields
                          .map { u -> Language.valueOf(u.uppercase()) }
                          .intersect(prvd.languages.toSet())
                          .isNotEmpty()
                    },
                    { prvd -> prvd.languages.isNotEmpty() },"Language")
              }) {
            Text(text = list[idx], fontSize = 18.sp, modifier = Modifier.padding(3.dp))
          }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RatingFilterField(list: List<String>, listProviderViewModel: ListProviderViewModel) {
  var selectedFields by remember { mutableStateOf(listOf<String>()) }
  var iconsPressed by remember { mutableStateOf(List(list.size) { false }) }
  var colors by remember { mutableStateOf(List(list.size) { Color(0xFFF6F6F6) }) }

  FlowRow(
      Modifier.padding(20.dp).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    repeat(list.size) { idx ->
      Box(
          modifier =
              Modifier
                  .width(55.dp)
                  .height(30.dp)
                  .background(color = colors[idx], shape = RoundedCornerShape(size = 59.dp))
                  .testTag("filterRating")) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier.clickable {
                      filterStringFields(
                          selectedFields.toMutableList(),
                          iconsPressed.toMutableList(),
                          colors.toMutableList(),
                          { a -> selectedFields = a.toMutableList() },
                          { b -> iconsPressed = b.toMutableList() },
                          { c -> colors = c.toMutableList() },
                          idx,
                          list[idx],
                          listProviderViewModel,
                          { prvd ->
                            selectedFields.map { u -> u.toDouble() }.contains(prvd.rating)
                          },
                          { prvd -> prvd.rating >= 1.0 },"Rating")
                    }) {
                  Image(
                      modifier = Modifier.padding(1.90667.dp).width(16.dp).height(16.dp),
                      painter = painterResource(id = R.drawable.star),
                      contentDescription = "image description",
                      contentScale = ContentScale.Crop)

                  Text(text = list[idx], color = Color.Black, fontSize = 14.sp)
                }
          }
    }
  }
}

@Composable
fun ApplyButton(listProviderViewModel: ListProviderViewModel,display: () -> Unit) {
  val filteredList by listProviderViewModel.providersListFiltered.collectAsState()
  Box(
      modifier =
          Modifier.width(249.dp)
              .height(43.dp)
              .background(
                  brush =
                      Brush.horizontalGradient(
                          colors =
                              listOf(
                                  Color(android.graphics.Color.parseColor("#EFEBDE")),
                                  Color(android.graphics.Color.parseColor("#4D5652")))),
                  shape = RoundedCornerShape(50)).clickable {
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
              color = Color(0xFF4D5E29))
          Text(
              text = "${filteredList.size} providers", // TODO()
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF4D5E29))
        }
  }
}

@Composable
fun FilterComposable(hide: () -> Unit, listProviderViewModel: ListProviderViewModel,display :()->Unit) {
  Column(
      modifier = Modifier.fillMaxWidth().testTag("filterSheet"),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Filters",
            style =
                TextStyle(
                    fontSize = 21.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF000000),
                ))
        FilterSubText("Price")
        PriceFilter(listProviderViewModel)
        FilterSubText("Languages")
        LanguageFilterField(
            listOf("French", "English", "German", "Arabic", "Italian", "Spanish"),
            listProviderViewModel)
        FilterSubText("Rating")
        RatingFilterField(listOf("5", "4", "3", "2", "1"), listProviderViewModel)
        ApplyButton(listProviderViewModel,display)
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  SelectProviderScreen(
    listProviderViewModel: ListProviderViewModel =
        viewModel(factory = ListProviderViewModel.Factory),
    navigationActions: NavigationActions
) {
  val providers by listProviderViewModel.providersList.collectAsState()
  var displayFilters by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  Log.e("Select Provider Screen", "providers : $providers")
  Scaffold(modifier = Modifier.fillMaxSize(), topBar = { SpTopAppBar(navigationActions) }) {
      paddingValues ->
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      SpFilterBar(display = { displayFilters = true },listProviderViewModel)
      Title("Popular")
      DisplayPopularProviders(providers)
      Title("See All")
      ListProviders(providers)
    }
    if (displayFilters) {
      ModalBottomSheet(onDismissRequest = { displayFilters = false }, sheetState = sheetState,
          containerColor = Color.White
          ) {
        FilterComposable (
            {
              scope
                  .launch { sheetState.hide() }
                  .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                      displayFilters = false
                    }
                  }
            },
            listProviderViewModel,{displayFilters = false})
      }
    }
  }
}
