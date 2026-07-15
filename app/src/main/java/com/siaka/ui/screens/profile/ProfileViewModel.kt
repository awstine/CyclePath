package com.siaka.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siaka.data.local.CompletedRide
import com.siaka.data.local.CompletedRideDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val completedRideDao: CompletedRideDao
) : ViewModel() {

    val rideHistory: StateFlow<List<CompletedRide>> = completedRideDao.getAllCompletedRides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDistance: StateFlow<Double?> = completedRideDao.getTotalDistance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val rideCount: StateFlow<Int> = completedRideDao.getRideCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
