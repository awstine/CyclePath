package com.siaka.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siaka.data.local.CompletedRide
import com.siaka.data.local.CompletedRideDao
import com.siaka.data.repository.AuthRepository
import com.siaka.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val completedRideDao: CompletedRideDao,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _userName = MutableStateFlow("Siaka Rider")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl.asStateFlow()

    private val _isRefreshing = MutableStateFlow(value = false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var profileJob: Job? = null

    init {
        observeUserProfile()
    }

    private fun observeUserProfile() {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                if (user != null) {
                    userRepository.getUserProfileFlow(user.uid).collect { data ->
                        data?.let {
                            _userName.value = (it["fullName"] as? String) ?: "Siaka Rider"
                            _userEmail.value = (it["email"] as? String) ?: (user.email ?: "")
                            _profileImageUrl.value = it["profileImageUrl"] as? String
                        }
                    }
                } else {
                    _userName.value = "Siaka Rider"
                    _userEmail.value = ""
                    _profileImageUrl.value = null
                }
            }
        }
    }

    fun fetchUserProfile() {
        val currentUser = authRepository.currentUser.value
        currentUser?.let { user ->
            viewModelScope.launch {
                _isRefreshing.value = true
                userRepository.getUserProfile(user.uid).onSuccess { data ->
                    data?.let {
                        _userName.value = (it["fullName"] as? String) ?: "Siaka Rider"
                        _userEmail.value = (it["email"] as? String) ?: (user.email ?: "")
                        _profileImageUrl.value = it["profileImageUrl"] as? String
                    }
                }
                _isRefreshing.value = false
            }
        }
    }

    fun updatePersonalInformation(newName: String) {
        val currentUser = authRepository.currentUser.value
        currentUser?.let { user ->
            viewModelScope.launch {
                val updates = mapOf(
                    "fullName" to newName
                )
                userRepository.updateProfile(user.uid, updates).onSuccess {
                    _userName.value = newName
                }
            }
        }
    }

    fun updateProfileImage(url: String) {
        val currentUser = authRepository.currentUser.value
        currentUser?.let { user ->
            viewModelScope.launch {
                val updates = mapOf(
                    "profileImageUrl" to url
                )
                userRepository.updateProfile(user.uid, updates).onSuccess {
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
