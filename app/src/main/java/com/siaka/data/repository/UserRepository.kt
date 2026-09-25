package com.siaka.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    authRepository: AuthRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * PRE-CACHING ENGINE:
     * This Hot Flow stays alive as long as the app is open.
     * It eagerly fetches the user profile as soon as login is detected.
     */
    val currentUserProfile: StateFlow<Map<String, Any>?> = authRepository.currentUser
        .flatMapLatest { user ->
            if (user != null) getUserProfileFlow(user.uid)
            else flowOf(null)
        }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly, // Start loading immediately on app launch/login
            initialValue = null
        )

    fun getUserProfileFlow(userId: String): Flow<Map<String, Any>?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.data)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createUserProfile(userId: String, fullName: String, email: String): Result<Unit> {
        return try {
            val user = mapOf(
                "fullName" to fullName,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            Result.success(document.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(userId: String, uri: Uri): Result<String> = try {
        val reference = storage.reference.child("profile_images/$userId.jpg")
        reference.putFile(uri).await()
        // The Storage object keeps a stable download URL. Add a version query
        // parameter so Coil does not display the previous image from cache.
        val downloadUrl = reference.downloadUrl.await()
            .buildUpon()
            .appendQueryParameter("v", System.currentTimeMillis().toString())
            .build()
            .toString()
        Result.success(downloadUrl)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteProfileImage(userId: String): Result<Unit> = try {
        storage.reference.child("profile_images/$userId.jpg").delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        if (e.message?.contains("object-not-found") == true) Result.success(Unit)
        else Result.failure(e)
    }

    suspend fun deleteUserProfile(userId: String): Result<Unit> = try {
        firestore.collection("users").document(userId).delete().await()
        deleteProfileImage(userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
