package com.siaka.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import android.content.Context
import androidx.core.net.toUri
import com.siaka.data.local.CompletedRide
import com.siaka.data.local.CompletedRideDao
import com.siaka.data.repository.AuthRepository
import com.siaka.data.repository.UserPreferencesRepository
import com.siaka.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val completedRideDao: CompletedRideDao,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _userName = MutableStateFlow("CyclePath Rider")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()
    private var localProfileImagePath: String? = null

    private val _isRefreshing = MutableStateFlow(value = false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        authRepository.currentUser
            .filterNotNull()
            .flatMapLatest { user -> userPreferencesRepository.profileImagePath(user.uid) }
            .onEach { path ->
                localProfileImagePath = path
                if (path != null) _profileImageUrl.value = path
            }
            .launchIn(viewModelScope)

        // REACTIVE UI: Just subscribe to the pre-cached flow from the repository
        userRepository.currentUserProfile
            .onEach { data ->
                if (data != null) {
                    _userName.update { (data["fullName"] as? String) ?: "CyclePath Rider" }
                    _userEmail.update { (data["email"] as? String) ?: "" }
                    _profileImageUrl.update {
                        localProfileImagePath ?: (data["profileImageUrl"] as? String)
                    }
                } else {
                    _userName.update { "CyclePath Rider" }
                    _userEmail.update { "" }
                    _profileImageUrl.update { null }
                }
            }.launchIn(viewModelScope)
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isRefreshing.value = true
            authRepository.currentUser.value?.let { user ->
                userRepository.getUserProfile(user.uid).getOrNull()?.let { data ->
                    _userName.value = (data["fullName"] as? String) ?: "CyclePath Rider"
                    _userEmail.value = (data["email"] as? String) ?: user.email.orEmpty()
                    _profileImageUrl.value = localProfileImagePath ?: (data["profileImageUrl"] as? String)
                }
            }
            _isRefreshing.value = false
        }
    }

    fun updatePersonalInformation(newName: String) {
        val currentUser = authRepository.currentUser.value
        currentUser?.let { user ->
            // OPTIMISTIC UI: Update the name locally first so it feels instant
            val oldName = _userName.value
            _userName.update { newName }

            viewModelScope.launch {
                val updates = mapOf("fullName" to newName)
                userRepository.updateProfile(user.uid, updates).onFailure {
                    // ROLLBACK: If it fails, revert to the old name
                    _userName.update { oldName }
                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        val currentUser = authRepository.currentUser.value
        currentUser?.let { user ->
            viewModelScope.launch {
                _isRefreshing.value = true
                runCatching {
                    val destination = File(context.filesDir, "profile_image_${user.uid}.jpg")
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            destination.outputStream().use { output -> input.copyTo(output) }
                        } ?: error("Could not read selected image")
                    }
                    val localUri = Uri.fromFile(destination).toString()
                    userPreferencesRepository.setProfileImagePath(user.uid, localUri)
                    localProfileImagePath = localUri
                    _profileImageUrl.value = localUri
                }
                _isRefreshing.value = false
            }
        }
    }

    fun removeProfileImage() {
        val currentUser = authRepository.currentUser.value
        currentUser?.let { user ->
            viewModelScope.launch {
                _isRefreshing.value = true
                localProfileImagePath?.let { path ->
                    File(path.toUri().path.orEmpty()).delete()
                }
                userPreferencesRepository.clearProfileImagePath(user.uid)
                localProfileImagePath = null
                _profileImageUrl.value = null
                _isRefreshing.value = false
            }
        }
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        val user = authRepository.currentUser.value
        if (user == null) {
            onResult(false, "No signed-in account found")
            return
        }
        viewModelScope.launch {
            val profileResult = userRepository.deleteUserProfile(user.uid)
            if (profileResult.isFailure) {
                onResult(false, profileResult.exceptionOrNull()?.message ?: "Could not delete account data")
                return@launch
            }
            authRepository.deleteCurrentAccount()
                .onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.message ?: "Could not delete account") }
        }
    }

    fun updateProfileImage(url: String) {
        // Kept for compatibility with existing callers while image uploads use Storage.
        val currentUser = authRepository.currentUser.value
        currentUser?.let { user ->
            viewModelScope.launch {
                userRepository.updateProfile(user.uid, mapOf("profileImageUrl" to url)).onSuccess {
                    _profileImageUrl.value = url
                }
            }
        }
    }

    val rideHistory: StateFlow<List<CompletedRide>> = completedRideDao.getAllCompletedRides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDistance: StateFlow<Double?> = completedRideDao.getTotalDistance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val rideCount: StateFlow<Int> = completedRideDao.getRideCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logout() {
        authRepository.logout()
    }
}
