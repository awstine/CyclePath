package com.siaka.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
    object Login : Screen(
        route = "login",
        title = "Login"
    )
    object SignUp : Screen(
        route = "signup",
        title = "Sign Up"
    )
    object Map : Screen(
        route = "map?routeId={routeId}",
        title = "Map",
        iconRes = R.drawable.map_icon_outlined,
        selectedIconRes = R.drawable.map_icon_filled
    ) {
        fun createRoute(routeId: Long? = null) = if (routeId != null) "map?routeId=$routeId" else "map"
    }
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
    object RideHistory : Screen(
        route = "ride_history",
        title = "Ride History"
    )
    object PersonalInfo : Screen(
        route = "personal_info",
        title = "Personal Information"
    )
    object Settings : Screen(
        route = "settings",
        title = "App Settings"
    )
    object HelpSupport : Screen(
        route = "help_support",
        title = "Help & Support"
    )

    companion object {
        val items: List<Screen> by lazy { listOf(Map, Routes, Profile) }
    }
}
