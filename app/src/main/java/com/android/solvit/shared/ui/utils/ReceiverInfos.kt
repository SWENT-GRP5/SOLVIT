package com.android.solvit.shared.ui.utils

import com.android.solvit.seeker.model.profile.SeekerProfile
import com.android.solvit.shared.model.provider.Provider

fun getReceiverName(receiver: Any): String {

  val receiverName =
      when (receiver) {
        is Provider -> (receiver as Provider).name
        is SeekerProfile -> (receiver as SeekerProfile).name
        else -> "Unknown"
      }
  return receiverName
}

fun getReceiverImageUrl(receiver: Any): String {
  val receiverPicture =
      when (receiver) {
        is Provider -> (receiver as Provider).imageUrl
        is SeekerProfile -> (receiver as SeekerProfile).imageUrl
        else -> "Unknown"
      }

  return receiverPicture
}
