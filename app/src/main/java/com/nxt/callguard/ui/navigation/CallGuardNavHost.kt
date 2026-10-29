package com.nxt.callguard.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nxt.callguard.ui.CallGuardViewModel
import com.nxt.callguard.ui.screens.BlocklistScreen
import com.nxt.callguard.ui.screens.DashboardScreen
import com.nxt.callguard.ui.screens.HistoryScreen
import com.nxt.callguard.ui.screens.ReportScreen
import com.nxt.callguard.ui.screens.SettingsScreen

private sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Dest("dashboard", "Tổng quan", Icons.Filled.Dashboard)
    data object Blocklist : Dest("blocklist", "Danh sách chặn", Icons.Filled.Block)
    data object History : Dest("history", "Lịch sử", Icons.Filled.History)
    data object Report : Dest("report", "Báo cáo", Icons.Filled.Report)
    data object Settings : Dest("settings", "Cài đặt", Icons.Filled.Settings)
}

private val destinations = listOf(Dest.Dashboard, Dest.Blocklist, Dest.History, Dest.Report, Dest.Settings)

@Composable
fun CallGuardNavHost(
    viewModel: CallGuardViewModel,
    onRequestRuntimePermissions: () -> Unit,
    onRequestCallScreeningRole: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                destinations.forEach { dest ->
                    NavigationBarItem(
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Dashboard.route) { DashboardScreen(viewModel) }
            composable(Dest.Blocklist.route) { BlocklistScreen(viewModel) }
            composable(Dest.History.route) { HistoryScreen(viewModel) }
            composable(Dest.Report.route) { ReportScreen(viewModel) }
            composable(Dest.Settings.route) {
                SettingsScreen(onRequestRuntimePermissions, onRequestCallScreeningRole)
            }
        }
    }
}
