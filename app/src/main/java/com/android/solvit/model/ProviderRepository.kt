package com.android.solvit.model

interface ProviderRepository {
    fun getNewUid():String

    fun addProvider(provider: Provider,onSuccess : ()->Unit, onFailure : (Exception) -> Unit)

    fun deleteProvider (provider: Provider,onSuccess : ()->Unit, onFailure : (Exception) -> Unit )

    fun updateProvider(provider: Provider,onSuccess : ()->Unit, onFailure : (Exception) -> Unit)

    fun getProviders (service : Services?, onSuccess : (List<Provider>)->Unit, onFailure : (Exception) -> Unit )

}