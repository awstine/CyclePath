package com.siaka.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siaka.data.repository.AuthRepository
import com.siaka.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email, password)
            result.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Login failed")
            }
        }
    }

    fun signUp(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signUp(email, password)
            result.onSuccess { user ->
                val profileResult = userRepository.createUserProfile(user.uid, fullName, email)
                if (profileResult.isSuccess) {
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error("Account created, but failed to save profile name.")
                }
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
