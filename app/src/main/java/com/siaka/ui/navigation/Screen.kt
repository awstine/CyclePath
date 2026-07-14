package com.siaka.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

import com.siaka.R

sealed class Screen(
    val route: String,
    val title: String,
    val iconRes: Int? = null,
    val selectedIconRes: Int? = null,
    val iconVector: ImageVector? = null,
    val selectedIconVector: ImageVector? = null
) {
    object Onboarding : Screen(
        "onboarding",
        "Onboarding",
        iconVector = Icons.Outlined.Map,
        selectedIconVector = Icons.Default.Map)
    object Map : Screen(
        route = "map",
        title = "Map",
        iconRes = R.drawable.map_icon_outlined,
        selectedIconRes = R.drawable.map_icon_filled
    )
    object Routes : Screen(
        route = "routes",
        title = "Routes",
        iconRes = R.drawable.route_outlined,
        selectedIconRes = R.drawable.route_filled
    )
    object Profile : Screen(
        route = "profile",
        title = "Profile",
        iconRes = R.drawable.profile_outlined,
        selectedIconRes = R.drawable.profile_filled
    )

    companion object {
        val items = listOf(Map, Routes, Profile)
    }
}
