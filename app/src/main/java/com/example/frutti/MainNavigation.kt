package com.example.frutti

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold

// Define Bottom Navigation Items
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

@Composable
fun MainNavigation(fruitClassifier: FruitQualityModelBinding? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var backPressedOnce by remember { mutableStateOf(false) }
    val handler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }

    val currentRoute = currentRoute(navController)

    // Handle back press only on the Home screen
    if (currentRoute == BottomNavItem.Home.route) {
        BackHandler {
            if (backPressedOnce) {
                (context as? ComponentActivity)?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                handler.postDelayed({ backPressedOnce = false }, 2000)
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHostContainer(navController, Modifier.padding(innerPadding), fruitClassifier)
    }
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    fruitClassifier: FruitQualityModelBinding? = null
) {
    val sampleFruits = remember {
        mutableStateListOf(
            FruitItem("Apple", "Fresh", R.drawable.ic_fruit, true),
            FruitItem("Banana", "Overripe", R.drawable.ic_fruit, false),
            FruitItem("Mango", "Good", R.drawable.ic_fruit, true)
        )
    }

    NavHost(navController, startDestination = BottomNavItem.Home.route, modifier = modifier) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Analyze.route) {
            AnalyzeFruitScreen(
                fruitClassifier = fruitClassifier,
                navController = navController
            )
        }
        composable(BottomNavItem.History.route) {
            ResultsHistoryScreen(
                fruitList = sampleFruits,
                onItemClick = { fruit ->
                    navController.navigate("fruitDetail/${fruit.name}")
                },
                onClearHistory = { sampleFruits.clear() }
            )
        }

        composable(
            "fruitDetail/{fruitName}",
            arguments = listOf(navArgument("fruitName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fruitName = backStackEntry.arguments?.getString("fruitName") ?: "Unknown"
            FruitDetailScreen(fruitName = fruitName)
        }
    }
}