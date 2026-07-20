package com.siaka.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siaka.data.repository.AuthRepository
import com.siaka.data.repository.UserPreferencesRepository
import com.siaka.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val isOnboardingCompleted = userPreferencesRepository.isOnboardingCompleted.first()
            val isLoggedIn = authRepository.isUserLoggedIn()

            _startDestination.value = when {
                !isOnboardingCompleted -> Screen.Onboarding.route
                !isLoggedIn -> Screen.Login.route
                else -> Screen.Map.route
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }
}
