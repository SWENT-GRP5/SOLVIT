package com.android.solvit.seeker.ui.review

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.solvit.R
import com.android.solvit.seeker.model.provider.ListProviderViewModel
import com.android.solvit.seeker.ui.service.ProviderItem
import com.android.solvit.shared.model.map.Location
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.request.ServiceRequestStatus
import com.android.solvit.shared.model.request.ServiceRequestViewModel
import com.android.solvit.shared.model.review.Review
import com.android.solvit.shared.model.review.ReviewViewModel
import com.android.solvit.shared.ui.navigation.NavigationActions
import com.android.solvit.shared.ui.navigation.Route
import com.android.solvit.shared.ui.theme.Typography
import com.android.solvit.shared.ui.utils.TopAppBarInbox
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun CreateReviewScreen(
    reviewViewModel: ReviewViewModel,
    serviceRequestViewModel: ServiceRequestViewModel,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  val requestState = serviceRequestViewModel.selectedRequest.collectAsStateWithLifecycle()
  if (requestState.value == null || requestState.value!!.status != ServiceRequestStatus.COMPLETED) {
    navigationActions.goBack()
  }
  val ratingState = remember { mutableIntStateOf(0) }
  var comment by remember { mutableStateOf("") }
  val providerState = remember { mutableStateOf<Provider?>(null) }

  // Lock Orientation to Portrait
  val context = LocalContext.current
  DisposableEffect(Unit) {
    val activity = context as? ComponentActivity
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
  }
  // We fetch the
  LaunchedEffect(navigationActions.currentRoute()) {
    if (requestState.value != null &&
        (requestState.value!!.status == ServiceRequestStatus.COMPLETED ||
            requestState.value!!.status == ServiceRequestStatus.ARCHIVED)) {
      providerState.value =
          requestState.value!!.providerId?.let { listProviderViewModel.fetchProviderById(it) }
    }
  }
  Scaffold(
      topBar = {
        TopAppBarInbox(
            title = "Leave a Review",
            testTagGeneral = "reviewTopBar",
            leftButtonForm = Icons.AutoMirrored.Filled.ArrowBack,
            leftButtonAction = { navigationActions.goBack() },
            testTagLeft = "goBackButton")
      }) { paddingValues ->
        Column(
            modifier =
                Modifier.padding(paddingValues)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
              RequestBox(requestState, listProviderViewModel, navigationActions)
              RatingBar(ratingState)
              OutlinedTextField(
                  value = comment,
                  onValueChange = { comment = it },
                  modifier = Modifier.fillMaxWidth().height(160.dp).testTag("reviewComment"),
                  label = { Text("Comment") },
                  placeholder = { Text("Leave a comment", style = Typography.bodyLarge) },
                  shape = RoundedCornerShape(16.dp),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          unfocusedContainerColor = colorScheme.background,
                          focusedBorderColor = colorScheme.secondary,
                          unfocusedBorderColor = colorScheme.onSurfaceVariant))
              Spacer(modifier = Modifier.size(16.dp))
              Button(
                  onClick = {
                    if (requestState.value != null && requestState.value!!.providerId != null) {
                      // We use here so that code is executed sequentially to calculate the correct
                      // average rating(suspend function) before updating provider
                      CoroutineScope(Dispatchers.Main).launch {
                        val review =
                            Review(
                                uid = reviewViewModel.getNewUid(),
                                authorId = requestState.value!!.userId,
                                serviceRequestId = requestState.value!!.uid,
                                providerId = requestState.value!!.providerId!!,
                                rating = ratingState.intValue,
                                comment = comment)
                        reviewViewModel.addReview(review)
                        val averageRating =
                            reviewViewModel.getAverageRatingByProvider(
                                requestState.value!!.providerId!!)
                        providerState.value?.let {
                          listProviderViewModel.updateProvider(
                              it.copy(rating = kotlin.math.ceil(averageRating).coerceIn(1.0, 5.0)))
                        }

                        Toast.makeText(context, "Review Submitted", Toast.LENGTH_SHORT).show()
                      }
                    } else {
                      Toast.makeText(context, "Error Submitting Review", Toast.LENGTH_SHORT).show()
                    }
                    navigationActions.navigateAndSetBackStack(Route.REQUESTS_OVERVIEW, listOf())
                  },
                  modifier = Modifier.testTag("submitReviewButton")) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                          Text("Submit Your Review", style = Typography.bodyLarge)
                          Icon(
                              imageVector = Icons.Filled.Done, contentDescription = "Submit Review")
                        }
                  }
            }
      }
}

@Composable
fun RequestBox(
    requestState: State<ServiceRequest?>,
    listProviderViewModel: ListProviderViewModel,
    navigationActions: NavigationActions
) {
  val request = requestState.value ?: return
  val providerListState = listProviderViewModel.providersList.collectAsStateWithLifecycle()
  val provider = providerListState.value.find { it.uid == request.providerId }
  val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
  OutlinedCard(
      colors =
          CardColors(
              colorScheme.background,
              colorScheme.onBackground,
              colorScheme.background,
              colorScheme.onBackground),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("requestBox"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start) {
          Text(
              text = request.title,
              modifier = Modifier.testTag("requestTitle"),
              style =
                  Typography.bodyLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold))
          Text(
              text = request.description,
              modifier = Modifier.testTag("requestDescription"),
              style = Typography.bodyLarge)
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                request.agreedPrice?.let {
                  Text(
                      text = "${request.agreedPrice} $",
                      modifier = Modifier.testTag("requestPrice"),
                      style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
                request.meetingDate?.let {
                  Text(
                      text = dateFormat.format(request.meetingDate.toDate()),
                      modifier = Modifier.testTag("requestDate"),
                      style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
              }
          Row(
              modifier = Modifier.height(160.dp).testTag("requestProviderAndLocation"),
              horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                provider?.let {
                  ProviderItem(provider = it) {
                    listProviderViewModel.selectProvider(it)
                    navigationActions.navigateTo(Route.PROVIDER_INFO)
                  }
                }
                request.location?.let { MapCard(it) }
              }
        }
  }
}

@Composable
fun MapCard(location: Location) {
  val mapPosition = rememberCameraPositionState {
    position =
        com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(location.latitude, location.longitude), 13f)
  }
  Box {
    GoogleMap(
        cameraPositionState = mapPosition,
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).testTag("mapCard")) {
          Marker(
              state = rememberMarkerState(position = LatLng(location.latitude, location.longitude)))
        }
  }
}

@Composable
fun RatingBar(
    ratingState: MutableIntState,
) {
  val selectedColor = colorScheme.primary
  val unselectedColor = colorScheme.primaryContainer
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 32.dp, vertical = 16.dp)
              .testTag("ratingBar"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        for (value in 1..5) {
          StarIcon(
              ratingValue = value,
              ratingState = ratingState,
              selectedColor = selectedColor,
              unselectedColor = unselectedColor,
          )
        }
      }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StarIcon(
    ratingState: MutableIntState,
    ratingValue: Int,
    selectedColor: Color,
    unselectedColor: Color
) {
  val tint by
      animateColorAsState(
          targetValue = if (ratingValue <= ratingState.intValue) selectedColor else unselectedColor,
          label = "")
  Icon(
      painter = painterResource(id = R.drawable.star),
      contentDescription = null,
      modifier =
          Modifier.size(40.dp).testTag("reviewStar$ratingValue").pointerInteropFilter {
            when (it.action) {
              MotionEvent.ACTION_DOWN -> {
                ratingState.intValue = ratingValue
              }
            }
            true
          },
      tint = tint)
}
