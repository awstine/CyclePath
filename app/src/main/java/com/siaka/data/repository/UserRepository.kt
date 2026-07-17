package com.siaka.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
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
            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
