package com.siaka.ui.screens.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siaka.data.local.RouteDao
import com.siaka.data.local.SavedRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val routeDao: RouteDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val savedRoutes: StateFlow<List<SavedRoute>> = combine(
        routeDao.getAllRoutes(),
        _searchQuery
    ) { routes, query ->
        if (query.isBlank()) {
            routes
        } else {
            routes.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.distanceKm.toString().contains(query)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteRoute(route: SavedRoute) {
        viewModelScope.launch {
            routeDao.deleteRoute(route)
        }
    }
}
