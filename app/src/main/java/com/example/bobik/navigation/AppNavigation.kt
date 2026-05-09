package com.example.bobik.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.bobik.ui.screens.*
import com.example.bobik.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Chat : Screen("chat", "对话", Icons.Default.Chat)
    data object Brain : Screen("brain", "脑图", Icons.Default.Psychology)
    data object Memories : Screen("memories", "记忆", Icons.Default.Memory)
    data object Status : Screen("status", "状态", Icons.Default.Monitor)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

val bottomNavScreens = listOf(Screen.Chat, Screen.Brain, Screen.Memories, Screen.Status, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkSurface) {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = { navController.navigate(screen.route) { popUpTo(Screen.Chat.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = GrayLight,
                            unselectedTextColor = GrayLight,
                            indicatorColor = ElectricViolet.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Chat.route) { ChatScreen(viewModel) }
            composable(Screen.Brain.route) { BrainScreen(viewModel) }
            composable(Screen.Memories.route) { MemoriesScreen(viewModel) }
            composable(Screen.Status.route) { StatusScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}

// 复用 bobi 的配色
val DarkSurface = androidx.compose.ui.graphics.Color(0xFF1A1A2E)
val NeonCyan = androidx.compose.ui.graphics.Color(0xFF00F5FF)
val ElectricViolet = androidx.compose.ui.graphics.Color(0xFF7B2FFF)
val GrayLight = androidx.compose.ui.graphics.Color(0xFF9E9E9E)
