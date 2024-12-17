package com.android.solvit.shared.model.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.solvit.R

@Composable
fun SaveButton(onClick: () -> Unit, allIsGood: Boolean) {

  Button(
      onClick = { onClick() },
      modifier =
          Modifier.fillMaxWidth()
              .height(50.dp)
              .background(
                  brush =
                      if (allIsGood) {
                        Brush.horizontalGradient(
                            colors = listOf(colorScheme.primary, colorScheme.secondary))
                      } else {
                        Brush.horizontalGradient(
                            colors =
                                listOf(colorScheme.onSurfaceVariant, colorScheme.onSurfaceVariant))
                      },
                  shape =
                      RoundedCornerShape(
                          25.dp,
                      )),
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
        Text(
            "Save !",
            color = colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.testTag("saveButton"))
      }
}

@Composable
fun EditProfileHeader(
    imageUrl: String,
    fullName: String,
    email: String,
    screenWidth: Dp,
    verticalSpacing: Dp
) {
  AsyncImage(
      modifier =
          Modifier.size(if (screenWidth < 360.dp) 60.dp else 74.dp)
              .clip(CircleShape)
              .border(2.dp, colorScheme.primaryContainer, CircleShape),
      model = if (imageUrl.isNotEmpty()) imageUrl else R.drawable.empty_profile_img,
      placeholder = painterResource(id = R.drawable.loading),
      error = painterResource(id = R.drawable.error),
      contentDescription = "problem_image",
      contentScale = ContentScale.Crop)

  // Full Name Text
  androidx.compose.material.Text(
      text = fullName,
      fontWeight = FontWeight.Bold,
      fontSize = if (screenWidth < 360.dp) 18.sp else 20.sp,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 8.dp))

  // Email Text
  androidx.compose.material.Text(
      text = email,
      fontSize = if (screenWidth < 360.dp) 12.sp else 14.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 4.dp))
}
