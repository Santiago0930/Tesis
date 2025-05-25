package com.example.frutti

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

// Define Bottom Navigation Items
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Analyze : BottomNavItem("analyze", Icons.Filled.Analytics, "Analyze")
    object History : BottomNavItem("history", Icons.Filled.History, "History")
}

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

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun MainNavigation(fruitClassifier: FruitClassifier? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var backPressedTime by remember { mutableStateOf(0L) }
    var showExitDialog by remember { mutableStateOf(false) }
    val currentRoute = currentRoute(navController)

    // Handle back press only when on home screen
    if (currentRoute == BottomNavItem.Home.route) {
        BackHandler(enabled = true) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                // Second back press within 2 seconds - show dialog
                showExitDialog = true
            } else {
                // First back press - show toast
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit App") },
            text = { Text("Are you sure you want to exit?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Exit the app completely
                        (context as? ComponentActivity)?.finishAffinity()
                    }
                ) {
                    Text("EXIT", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text("CANCEL")
                }
            }
        )
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHostContainer(navController, Modifier, fruitClassifier)
        }
    }
}

@Composable
fun NavHostContainer(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    fruitClassifier: FruitClassifier? = null
) {

    NavHost(navController, startDestination = BottomNavItem.Home.route, modifier = modifier) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Analyze.route) {
            AnalyzeFruitScreen(
                fruitClassifier = fruitClassifier,
                navController = navController
            )
        }
        composable(BottomNavItem.History.route) {
            ResultsHistoryScreen()
        }

        composable(
            "fruitDetail/{fruitName}",
            arguments = listOf(navArgument("fruitName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fruitName = backStackEntry.arguments?.getString("fruitName") ?: "Unknown"
            FruitDetailScreen(fruitName = fruitName)
        }

        // ✅ Add result-quality destinations here INSIDE NavHost
        composable("good_quality/{fruitName}") { backStackEntry ->
            val fruitName = backStackEntry.arguments?.getString("fruitName") ?: "This fruit"
            GoodQualityScreen(navController, fruitName)
        }
        composable("bad_quality/{fruitName}") { backStackEntry ->
            val fruitName = backStackEntry.arguments?.getString("fruitName") ?: "This fruit"
            BadQualityScreen(navController, fruitName)
        }
        composable("mixed_quality/{fruitName}") { backStackEntry ->
            val fruitName = backStackEntry.arguments?.getString("fruitName") ?: "This fruit"
            MixedQualityScreen(navController, fruitName)
        }

    }
}
