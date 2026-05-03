package com.mindful.companion.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindful.companion.ui.screens.batch.BatchAccountScreen
import com.mindful.companion.ui.screens.feedback.FeedbackScreen
import com.mindful.companion.ui.screens.profile.AdminProfileScreen
import com.mindful.companion.ui.screens.report.AdminReportDetailScreen
import com.mindful.companion.ui.screens.report.AdminReportsScreen
import com.mindful.companion.ui.screens.watchlist.WatchListDetailScreen
import com.mindful.companion.ui.screens.watchlist.WatchListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        AdminBottomNavItem(
            route = "admin_watchlist",
            title = "关注对象",
            icon = Icons.Default.Warning
        ),
        AdminBottomNavItem(
            route = "admin_reports",
            title = "查看报告",
            icon = Icons.Default.Assessment
        ),
        AdminBottomNavItem(
            route = "admin_profile",
            title = "我的",
            icon = Icons.Default.Person
        )
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "admin_watchlist",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                com.mindful.companion.ui.screens.login.LoginScreen(navController = navController)
            }
            composable("admin_watchlist") {
                WatchListScreen(navController = navController)
            }
            composable(
                route = "watchlist_detail/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) {
                WatchListDetailScreen(navController = navController)
            }
            composable("admin_reports") {
                AdminReportsScreen(navController = navController)
            }
            composable(
                route = "admin_report_detail/{reportId}",
                arguments = listOf(navArgument("reportId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                AdminReportDetailScreen(navController = navController, reportId = reportId)
            }
            composable("admin_profile") {
                AdminProfileScreen(navController = navController)
            }
            composable("settings") {
                com.mindful.companion.ui.screens.settings.SettingsScreen(navController = navController)
            }
            composable("change_password") {
                com.mindful.companion.ui.screens.settings.ChangePasswordScreen(navController = navController)
            }
            composable("batch_accounts") {
                BatchAccountScreen(navController = navController)
            }
            composable("feedback") {
                FeedbackScreen(navController = navController)
            }
        }
    }
}

data class AdminBottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)
