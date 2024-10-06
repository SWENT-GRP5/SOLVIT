package com.android.solvit.model

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ProviderRepositoryFirestore(private val db:FirebaseFirestore) : ProviderRepository {
    private val collectionPath = "providers"

    private fun convertDoc(doc :DocumentSnapshot) : Provider?{
        try{
            val name = doc.getString("name") ?: return null
            val serviceString = doc.getString("service") ?: return null
            val service = Services.valueOf(serviceString)
            val strongPoints = (doc.get("strongPoints") as List<*>).map { it as String }
            val rating = doc.getDouble("rating") ?: return null
            val price = doc.getDouble("price") ?: return null
            val deliveryTime = doc.getTimestamp("deliveryTime") ?: return null
            val languages = (doc.get("languages") as List<*>).map {  Language.valueOf(it as String) }
            return Provider(doc.id,name,service,strongPoints,rating,price,deliveryTime,languages)
        }catch (e: Exception){
            Log.e("ProviderRepositoryFirestore","failed to convert doc $e")
            return null
        }


    }
    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }

    override fun addProvider(
        provider: Provider,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        performFirestoreOperation( db.collection(collectionPath).document(provider.uid).set(provider),
            onSuccess,onFailure)
    }

    override fun deleteProvider(
        provider: Provider,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        performFirestoreOperation(db.collection(collectionPath).document(provider.uid).delete(),onSuccess,
            onFailure)
    }

    override fun updateProvider(
        provider: Provider,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        performFirestoreOperation(db.collection(collectionPath).document(provider.uid).set(provider),
            onSuccess,onFailure)
    }

    override fun getProviders(
        service: Services?,
        onSuccess: (List<Provider>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val query = if(service !=null){
            db.collection(collectionPath).whereEqualTo("service",service.toString())
        }else{
            db.collection(collectionPath)
        }
        query.get().addOnCompleteListener{task->
            if(task.isSuccessful){
                val providers = task.result?.mapNotNull { document -> convertDoc(document) } ?: emptyList()
                onSuccess(providers)
            }else{
                Log.e("ProviderRepositoryFirestore", "failed to get Providers")
                task.exception?.let {onFailure(it)}
            }
        }

    }

    private fun performFirestoreOperation(
        task : Task<Void>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ){
        task.addOnCompleteListener{result ->
            if(result.isSuccessful){
                onSuccess()
            }else{
                result.exception?.let { e->
                    Log.e("ProviderRepositoryFirestore","Failed to perform Task ")
                    onFailure(e)
                }

            }
        }
    }
}