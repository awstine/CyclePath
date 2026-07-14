package com.siaka.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.siaka.ui.navigation.Screen
import com.siaka.ui.screens.map.MapScreen
import com.siaka.ui.screens.onboarding.OnboardingScreen
import com.siaka.ui.screens.profile.ProfileScreen
import com.siaka.ui.screens.routes.RoutesScreen
import com.siaka.ui.theme.SoftGreen

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Hide bottom bar on Onboarding screen
    val showBottomBar = currentDestination?.route != null && currentDestination.route != Screen.Onboarding.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Onboarding.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onFinish = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(
                route = Screen.Map.route,
                arguments = listOf(navArgument("routeId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val routeId = backStackEntry.arguments?.getString("routeId")?.toLongOrNull()
                MapScreen(routeId = routeId)
            }
            composable(Screen.Routes.route) { 
                RoutesScreen(onRouteSelected = { id ->
                    navController.navigate(Screen.Map.createRoute(id)) {
                        popUpTo(Screen.Map.route) { inclusive = true }
                    }
                }) 
            }
            composable(Screen.Profile.route) { ProfileScreen() }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationBar(
                containerColor = Color(0xFFF5F5F7), // Light grey background
                modifier = Modifier.height(80.dp),
                tonalElevation = 0.dp
            ) {
                Screen.items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            val iconPainter = when {
                                selected && screen.selectedIconRes != null -> painterResource(screen.selectedIconRes)
                                !selected && screen.iconRes != null -> painterResource(screen.iconRes)
                                selected && screen.selectedIconVector != null -> rememberVectorPainter(screen.selectedIconVector)
                                !selected && screen.iconVector != null -> rememberVectorPainter(screen.iconVector)
                                else -> rememberVectorPainter(Icons.Default.QuestionMark)
                            }

                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .background(SoftGreen, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = iconPainter,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Black
                                    )
                                }
                            } else {
                                Icon(
                                    painter = iconPainter,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Gray
                                )
                            }
                        },
                        label = { 
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.Black else Color.Gray
                            ) 
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
