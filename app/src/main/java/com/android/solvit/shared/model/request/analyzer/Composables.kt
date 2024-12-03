package com.android.solvit.shared.model.request.analyzer

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.android.solvit.R
import com.android.solvit.shared.model.utils.loadBitmapFromUri
import com.android.solvit.shared.ui.theme.Background
import com.android.solvit.shared.ui.theme.Error
import com.android.solvit.shared.ui.theme.OnBackground
import com.android.solvit.shared.ui.theme.OnError
import com.android.solvit.shared.ui.theme.OnErrorContainer
import com.android.solvit.shared.ui.theme.OnPrimary
import com.android.solvit.shared.ui.theme.OnSecondary
import com.android.solvit.shared.ui.theme.Primary
import com.android.solvit.shared.ui.theme.Secondary
import com.android.solvit.shared.ui.theme.SurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AIAssistantDialog(onCancel: () -> Unit, onUploadPictures: () -> Unit) {
  Dialog(onDismissRequest = { onCancel() }) {
    Box(
        modifier =
            Modifier.fillMaxWidth(0.9f) // Adjust width as 90% of the screen
                .wrapContentHeight()
                .background(Background, shape = RoundedCornerShape(16.dp))
                .padding(16.dp) // Padding around the dialog content
                .testTag("aIAssistantDialog")) {
          Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp).testTag("aiAssistantHeaderRow")) {
                      // Icon
                      Box(
                          modifier =
                              Modifier.size(64.dp)
                                  .background(Primary, shape = CircleShape), // Blue Circle
                          contentAlignment = Alignment.Center) {
                            Icon(
                                painter =
                                    painterResource(
                                        id =
                                            R.drawable.ic_ai_assistant), // Replace with proper icon
                                contentDescription = "AI Assistant",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp).testTag("aiAssistantIcon"))
                          }

                      // Title
                      Box(
                          modifier =
                              Modifier.offset(x = (-14).dp)
                                  .fillMaxWidth()
                                  .background(
                                      Primary,
                                      shape =
                                          RoundedCornerShape(
                                              topStart = 0.dp,
                                              topEnd = 18.dp,
                                              bottomStart = 0.dp,
                                              bottomEnd = 18.dp))
                                  .padding(horizontal = 8.dp, vertical = 10.dp)
                                  .testTag("aiAssistantTitleBox")) {
                            Text(
                                text = "Your AI-Powered Assistant",
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold),
                                color = OnPrimary,
                                textAlign = TextAlign.Center)
                          }
                    }

                // Dialog Content
                Text(
                    text =
                        "Would you like to use the AI assistant to create your request by uploading pictures of your issue?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = OnBackground,
                    modifier = Modifier.padding(vertical = 16.dp).testTag("aiAssistantDescription"))

                // Buttons
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()) {
                      Button(
                          onClick = { onCancel() },
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = Error, contentColor = OnError),
                          modifier =
                              Modifier.padding(end = 8.dp)
                                  .testTag("cancelButton") // Space between buttons
                          ) {
                            Text("Cancel")
                          }

                      Button(
                          onClick = { onUploadPictures() },
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = Secondary, contentColor = OnSecondary),
                          modifier = Modifier.testTag("uploadPicturesButton")) {
                            Text("Upload Pictures")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                          }
                    }
              }
        }
  }
}

@Composable
fun ImagePickerStep(
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onStartAnalyzing: () -> Unit
) {
  val imagePickerLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
          onImagesSelected(selectedImages + uris)
      }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(16.dp)
              .verticalScroll(rememberScrollState())
              .testTag("imagePickerStep")) {
        StepHeader(R.drawable.circle_one_icon, title = "Upload Images")

        Spacer(modifier = Modifier.height(16.dp)) // Spacing below the header

        if (selectedImages.isEmpty()) {
          Text(
              text = "No Images added",
              style = MaterialTheme.typography.bodyMedium,
              color = OnBackground,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth().testTag("noImagesText"))
        } else {
          LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("selectedImagesRow")) {
                items(selectedImages) { uri ->
                  Box(
                      modifier =
                          Modifier.size(80.dp)
                              .clip(RoundedCornerShape(8.dp))
                              .background(SurfaceVariant)
                              .padding(4.dp)
                              .testTag("imageThumbnail")) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize())
                        Box(
                            Modifier.align(Alignment.TopEnd)
                                .background(OnErrorContainer, shape = CircleShape)
                                .clickable { onRemoveImage(uri) }
                                .padding(4.dp)
                                .testTag("removeImageButton")) {
                              Icon(
                                  Icons.Default.Delete,
                                  contentDescription = "Remove Image",
                                  tint = Error)
                            }
                      }
                }
              }
        }

        Spacer(modifier = Modifier.height(16.dp)) // Spacing before the buttons

        // Buttons Row
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().testTag("imagePickerButtonsRow")) {
              Button(
                  onClick = { imagePickerLauncher.launch("image/*") },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Primary, contentColor = OnPrimary),
                  modifier =
                      Modifier.weight(1f).testTag("addImagesButton") // Ensures centering in the row
                  ) {
                    Text("Add Images")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Add, contentDescription = null)
                  }

              if (selectedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp)) // Space between buttons

                Button(
                    onClick = { onStartAnalyzing() },
                    modifier = Modifier.weight(1f).testTag("analyzeButton"),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Secondary, contentColor = OnSecondary)) {
                      Text("Analyze")
                      Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
              }
            }
      Spacer(modifier = Modifier.height(8.dp))
      // Note Text
      Text(
          text = "The initial image you select will be displayed in the service request. Feel free to update it after analysis.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("imagePickerNote")
      )

      }
}

@Composable
fun StepHeader(@DrawableRes iconRes: Int, title: String) {
  // Header Row
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
    // Icon Number
    Box(
        modifier = Modifier.size(60.dp).background(Primary, shape = CircleShape), // Blue Circle
        contentAlignment = Alignment.Center) {
          Icon(
              painter = painterResource(id = iconRes), // Replace with proper icon
              contentDescription = "Stepper Icon",
              tint = OnPrimary,
              modifier = Modifier.size(40.dp))
        }

    // Title
    Box(
        modifier =
            Modifier.offset(x = (-14).dp)
                .fillMaxWidth()
                .background(
                    Primary,
                    shape =
                        RoundedCornerShape(
                            topStart = 0.dp, topEnd = 18.dp, bottomStart = 0.dp, bottomEnd = 18.dp))
                .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center) {
          Text(
              text = title,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              color = OnPrimary,
              textAlign = TextAlign.Center)
        }
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewAIAssistantDialog() {
  StepHeader(R.drawable.circle_one_icon, title = "Upload Images")
}

@Composable
fun MultiStepDialog(
    context: Context,
    showDialog: Boolean,
    currentStep: Int,
    selectedImages: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onStartAnalyzing: () -> Unit,
    onAnalyzeComplete: (String, String, String) -> Unit,
    onClose: () -> Unit
) {
  var isLoading by remember { mutableStateOf(false) }

  if (showDialog) {
    Dialog(onDismissRequest = { onClose() }) {
      Surface(
          shape = RoundedCornerShape(16.dp),
          color = Background,
          contentColor = OnBackground,
          modifier = Modifier.padding(16.dp).testTag("multiStepDialog")) {
            when (currentStep) {
              1 ->
                  ImagePickerStep(
                      selectedImages = selectedImages,
                      onImagesSelected = onImagesSelected,
                      onRemoveImage = onRemoveImage,
                      onStartAnalyzing = onStartAnalyzing)
              2 -> {
                LaunchedEffect(selectedImages) {
                  if (selectedImages.isNotEmpty() && !isLoading) {
                    isLoading = true
                    try {
                      val (title, type, description) =
                          uploadAndAnalyze(context,selectedImages)
                      onAnalyzeComplete(title, type, description)
                    } catch (e: Exception) {
                      Log.e("MultiStepDialog", "Error: ${e.message}")
                    } finally {
                      isLoading = false
                    }
                  }
                }

                if (isLoading) {
                  Column(
                      modifier = Modifier.fillMaxWidth().padding(16.dp),
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        StepHeader(R.drawable.circle_two_icon, title = "Analyzing Images")
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(48.dp).testTag("stepTwoProgressBar"))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Analyzing your uploaded images. Please wait...",
                            modifier = Modifier.testTag("stepTwoLoadingMessage"))
                      }
                }
              }
              3 -> {
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                      StepHeader(R.drawable.circle_three_icon, title = "Analysis Completed")
                      Spacer(modifier = Modifier.height(16.dp))
                      Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = "Complete",
                          tint = Secondary,
                          modifier = Modifier.size(48.dp).testTag("stepThreeIcon"))
                      Spacer(modifier = Modifier.height(16.dp))
                      Text(
                          text = "Analysis complete! Fields have been filled with AI suggestions.",
                          style = MaterialTheme.typography.bodyMedium,
                          textAlign = TextAlign.Center,
                          color = OnBackground,
                          modifier = Modifier.testTag("stepThreeMessage"))
                    Spacer(modifier = Modifier.height(16.dp))
                    // New Note for Address and Deadline
                    Text(
                        text = "Please note: You still need to manually add the address and deadline for your service request.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 8.dp).testTag("completionNote")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // New Proceed Button
                    Button(
                        onClick = { onClose() }, // Closes the dialog
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).testTag("proceedButton"),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                    ) {
                        Text("Apply Changes")
                    }
                    }
              }
            }
          }
    }
  }
}

suspend fun uploadAndAnalyze(
    context: Context,
    imageUris: List<Uri>
): Triple<String, String, String> {
    return withContext(Dispatchers.IO) {
        try {
            // Step 1: Convert URIs to Base64 strings
            val bitMapImages = imageUris.mapNotNull { uri ->
                loadBitmapFromUri(context,uri)
            }

            // Step 2: Analyze images using the Gemini model
            analyzeImagesGemini(bitMapImages)
        } catch (e: Exception) {
            Log.e("uploadAndAnalyze", "Error: ${e.message}", e)
            throw Exception("Error during conversion and analysis: ${e.message}")
        }
    }
}
