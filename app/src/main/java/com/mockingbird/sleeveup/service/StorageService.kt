package com.mockingbird.sleeveup.service

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

class StorageService {

    private val storageLink: String = "gs://sleeveup-9d087.firebasestorage.app"
    private val storage: StorageReference = Firebase.storage(storageLink).reference
    private val TAG = "StorageService"

    suspend fun fetchImage(url: String): ByteArray? {
        return try {
            val bytes = storage.child(url).getBytes(Long.MAX_VALUE).await()
            Log.d(TAG, "Successfully fetched image: $url")
            bytes
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching image: $url", e) // Log the exception
            null
        }
    }

    suspend fun uploadImage(imageUri: Uri, destinationPath: String): Boolean {
        return try {
            storage.child(destinationPath).putFile(imageUri).await()
            Log.d(TAG, "Successfully uploaded image to: $destinationPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: $destinationPath", e) // Log the exception
            false
        }
    }

    suspend fun deleteImage(path: String): Boolean {
        return try {
            storage.child(path).delete().await()
            Log.d(TAG, "Successfully deleted image: $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image: $path", e) // Log the exception
            false
        }
    }

    suspend fun listImages(path: String): List<StorageReference>? {
        return try {
            val listResult: ListResult = storage.child(path).listAll().await()
            val items = listResult.items
            Log.d(TAG, "Successfully listed images in: $path. Found ${items.size} items.")
            items
        } catch (e: Exception) {
            Log.e(TAG, "Error listing images: $path", e) // Log the exception
            null
        }
    }
}