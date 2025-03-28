package com.example.frutti

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHostContainer(navController, Modifier.padding(innerPadding))
    }
}

// Define Bottom Navigation Items (✅ Fixed ImageVector reference)
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Analyze : BottomNavItem("analyze", Icons.Filled.Analytics, "Analyze")
    object History : BottomNavItem("history", Icons.Filled.History, "History")
}

// Bottom Navigation Bar UI
@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Analyze, BottomNavItem.History)
    val currentRoute = currentRoute(navController)

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// Get current route to highlight selected tab
@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

// Navigation Host - Handles Screen Switching
@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = BottomNavItem.Home.route, modifier = modifier) {
        composable(BottomNavItem.Home.route) { HomeScreen()  }
        composable(BottomNavItem.Analyze.route) { AnalyzeFruitScreen() }
        composable(BottomNavItem.History.route) { ResultScreen() }
    }
}
