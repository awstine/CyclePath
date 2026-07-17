package com.siaka.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
