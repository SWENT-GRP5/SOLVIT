package com.android.solvit.shared.model.provider

import android.net.Uri
import com.android.solvit.shared.model.request.ServiceRequest
import com.android.solvit.shared.model.service.Services

interface ProviderRepository {

  fun init(onSuccess: () -> Unit)

  fun addListenerOnProviders(onSuccess: (List<Provider>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNewUid(): String

  fun addProvider(
      provider: Provider,
      imageUri: Uri?,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteProvider(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateProvider(provider: Provider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getProviders(
      service: Services?,
      onSuccess: (List<Provider>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun filterProviders(filter: () -> Unit)

  fun getProvider(userId: String, onSuccess: (Provider?) -> Unit, onFailure: (Exception) -> Unit)

  fun addAcceptedRequest(request: ServiceRequest)

  fun removeAcceptedRequest(request: ServiceRequest)

  suspend fun returnProvider(uid: String): Provider?
}
