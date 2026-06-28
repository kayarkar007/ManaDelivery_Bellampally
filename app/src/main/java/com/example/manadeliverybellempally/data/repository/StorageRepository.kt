package com.example.manadeliverybellempally.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(uri: Uri, path: String): Result<String> {
        return try {
            val fileName = UUID.randomUUID().toString()
            val ref = storage.reference.child("$path/$fileName")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProductImage(uri: Uri, vendorId: String): Result<String> {
        return uploadImage(uri, "vendors/$vendorId/products")
    }

    suspend fun uploadVendorLogo(uri: Uri, vendorId: String): Result<String> {
        return uploadImage(uri, "vendors/$vendorId/logo")
    }

    suspend fun uploadPrescription(uri: Uri, userId: String): Result<String> {
        return uploadImage(uri, "users/$userId/prescriptions")
    }
}
