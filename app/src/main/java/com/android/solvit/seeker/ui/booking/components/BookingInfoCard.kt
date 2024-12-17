package com.android.solvit.seeker.ui.booking.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.solvit.shared.model.provider.Provider
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.service.Services
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BookingInfoCard(
    provider: Provider?,
    serviceRequest: ServiceRequest?,
    modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  // Format the due date
  val formattedDueDate =
      remember(serviceRequest?.dueDate) {
        serviceRequest
            ?.dueDate
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
      }

  Box(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
    if (provider == null || serviceRequest == null) {
      // Placeholder state
      Card(
          modifier = Modifier.fillMaxWidth(),
          colors =
              CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              ),
      ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = "Select a provider and time slot to view booking details",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
      }
      return
    }

    AnimatedContent(targetState = expanded, label = "expand_animation") { isExpanded ->
      if (!isExpanded) {
        // Collapsed state - basic info with deadline
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().clickable { expanded = true },
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp)) {
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(
                              brush =
                                  Brush.horizontalGradient(
                                      colors =
                                          listOf(
                                              Services.getColor(provider.service),
                                              Services.getColor(provider.service)
                                                  .copy(alpha = 0.1f))),
                              shape = RoundedCornerShape(16.dp))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Row(
                              horizontalArrangement = Arrangement.spacedBy(12.dp),
                              verticalAlignment = Alignment.CenterVertically,
                              modifier = Modifier.weight(1f)) {
                                // Provider profile picture with border
                                Box(
                                    modifier =
                                        Modifier.size(44.dp)
                                            .border(
                                                width = 2.dp,
                                                color = Color.White,
                                                shape = CircleShape)) {
                                      AsyncImage(
                                          model = provider.imageUrl,
                                          placeholder =
                                              painterResource(
                                                  id = Services.getProfileImage(provider.service)),
                                          error =
                                              painterResource(
                                                  id = Services.getProfileImage(provider.service)),
                                          contentDescription = null,
                                          contentScale = ContentScale.Crop,
                                          modifier = Modifier.fillMaxSize().clip(CircleShape))
                                    }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                  Text(
                                      text = serviceRequest.title,
                                      style = MaterialTheme.typography.titleMedium,
                                      fontWeight = FontWeight.Bold,
                                      maxLines = 1,
                                      overflow = TextOverflow.Ellipsis,
                                      color = Color.White)
                                  Text(
                                      text = provider.name,
                                      style = MaterialTheme.typography.bodyMedium,
                                      color = Color.White.copy(alpha = 0.9f))
                                }
                              }

                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                  Text(
                                      text = "Due by",
                                      style = MaterialTheme.typography.labelSmall,
                                      color = colorScheme.onBackground.copy(alpha = 0.7f))
                                  Text(
                                      text = formattedDueDate ?: "No date",
                                      style = MaterialTheme.typography.bodyMedium,
                                      fontWeight = FontWeight.Medium,
                                      color = colorScheme.error)
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Show details",
                                    tint = colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp))
                              }
                        }
                  }
            }
      } else {
        // Expanded state - full details with provider card
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = false },
            colors =
                CardDefaults.cardColors(
                    containerColor = Services.getColor(provider.service).copy(alpha = 0.1f)),
            border = BorderStroke(width = 1.dp, color = Services.getColor(provider.service))) {
              Column(
                  modifier = Modifier.padding(16.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Service request image with provider overlay
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                      // Service request background image
                      AsyncImage(
                          model = serviceRequest.imageUrl,
                          placeholder = painterResource(id = Services.getIcon(provider.service)),
                          error = painterResource(id = Services.getIcon(provider.service)),
                          contentDescription = null,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)))

                      // Provider profile overlay
                      Box(
                          modifier =
                              Modifier.padding(12.dp)
                                  .size(56.dp)
                                  .clip(CircleShape)
                                  .background(MaterialTheme.colorScheme.surface)
                                  .align(Alignment.TopEnd)) {
                            AsyncImage(
                                model = provider.imageUrl,
                                placeholder =
                                    painterResource(
                                        id = Services.getProfileImage(provider.service)),
                                error =
                                    painterResource(
                                        id = Services.getProfileImage(provider.service)),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape))
                          }

                      // Service type icon
                      Box(
                          modifier =
                              Modifier.padding(12.dp)
                                  .size(32.dp)
                                  .clip(RoundedCornerShape(8.dp))
                                  .background(MaterialTheme.colorScheme.surface)
                                  .align(Alignment.TopStart),
                          contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = Services.getIcon(provider.service)),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp))
                          }

                      // Gradient overlay
                      Box(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .height(80.dp)
                                  .align(Alignment.BottomCenter)
                                  .background(
                                      brush =
                                          Brush.verticalGradient(
                                              colors =
                                                  listOf(
                                                      Color.Transparent,
                                                      Services.getColor(provider.service)
                                                          .copy(alpha = 0.7f)))))

                      // Bottom text
                      Column(
                          modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                          verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = serviceRequest.title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                  Row(
                                      verticalAlignment = Alignment.CenterVertically,
                                      horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = provider.name,
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "•",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                              Icon(
                                                  imageVector = Icons.Default.Star,
                                                  contentDescription = null,
                                                  tint = Color.Yellow,
                                                  modifier = Modifier.size(16.dp))
                                              Text(
                                                  text = provider.rating.toString(),
                                                  color = Color.White,
                                                  style = MaterialTheme.typography.bodyMedium)
                                            }
                                      }
                                }
                          }
                    }

                    // Service request details
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Due by",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(
                                text = formattedDueDate ?: "No date",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.error,
                                fontWeight = FontWeight.Medium)
                          }
                      if (serviceRequest.description.isNotBlank()) {
                        Text(
                            text = serviceRequest.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }
                  }
            }
      }
    }
  }
}
