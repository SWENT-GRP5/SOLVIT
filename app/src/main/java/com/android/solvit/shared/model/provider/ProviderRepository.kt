package com.android.solvit.shared.model.provider

interface ProviderRepository {

  fun init(onSuccess: () -> Unit)

  fun getNewUid(): String

  fun addProvider(provider: Provider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteProvider(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateProvider(provider: Provider, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun getProviders(
      service: Services?,
      onSuccess: (List<Provider>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun filterProviders(filter: () -> Unit)
}
