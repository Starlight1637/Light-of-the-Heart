package com.mindful.companion.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mindful.companion.data.repository.AuthRepository
import com.mindful.companion.ui.screens.home.HomeScreen
import com.mindful.companion.ui.screens.create.CreatePostScreen
import com.mindful.companion.ui.screens.profile.ProfileScreen
import com.mindful.companion.ui.screens.resources.ResourcesScreen
import com.mindful.companion.ui.screens.energy.EnergyStationScreen
import com.mindful.companion.ui.theme.*
import dagger.hilt.android.EntryPointAccessors
import com.mindful.companion.MindfulCompanionApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulCompanionNavigation(
    modifier: Modifier = Modifier,
    authRepository: AuthRepository? = null
) {
    val context = LocalContext.current
    val authRepo = authRepository ?: remember {
        val appContext = context.applicationContext as MindfulCompanionApplication
        EntryPointAccessors.fromApplication(
            appContext,
            AuthRepositoryEntryPoint::class.java
        ).authRepository()
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var isAdmin by remember { mutableStateOf(authRepo.isAdmin()) }

    LaunchedEffect(currentDestination) {
        isAdmin = authRepo.isAdmin()
    }

    if (isAdmin && currentDestination?.route != "splash" && currentDestination?.route != "login") {
        AdminNavigation(modifier = modifier)
        return
    }

    val showBottomBar = currentDestination?.route != "splash" && currentDestination?.route != "login"

    val bottomNavItems = listOf(
        BottomNavItem(route = "home",    title = "今日",   icon = Icons.Default.WbSunny),
        BottomNavItem(route = "chat",    title = "心记",   icon = Icons.Default.Book),
        BottomNavItem(route = "energy",  title = "能量站", icon = Icons.Default.BatteryChargingFull),
        BottomNavItem(route = "profile", title = "我的",   icon = Icons.Default.Person)
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                StitchBottomNavBar(
                    items = bottomNavItems,
                    currentRoute = currentDestination?.route,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = if (showBottomBar)
                Modifier.padding(innerPadding).padding(bottom = 80.dp)
            else
                Modifier,
            enterTransition = {
                fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                        slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it / 10 }
            },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(280, easing = FastOutSlowInEasing)) },
            popExitTransition = {
                fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 10 }
            }
        ) {
            composable("splash") {
                com.mindful.companion.ui.screens.splash.SplashScreen(
                    onTimeout = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("login") {
                com.mindful.companion.ui.screens.login.LoginScreen(navController = navController)
            }
            composable("chat") {
                com.mindful.companion.ui.screens.chat.ChatScreen(navController = navController)
            }
            composable("energy") {
                EnergyStationScreen(navController = navController)
            }
            composable("profile") {
                ProfileScreen(navController = navController)
            }
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("create") {
                CreatePostScreen(navController = navController)
            }
            composable("resources") {
                ResourcesScreen(navController = navController)
            }
            composable("breathing") {
                com.mindful.companion.ui.screens.breathing.BreathingScreen(navController = navController)
            }
            composable("whitenoise") {
                com.mindful.companion.ui.screens.whitenoise.WhiteNoiseScreen(navController = navController)
            }
            composable("quotes") {
                com.mindful.companion.ui.screens.quotes.QuotesScreen(navController = navController)
            }
            composable("settings") {
                com.mindful.companion.ui.screens.settings.SettingsScreen(navController = navController)
            }
            composable("change_password") {
                com.mindful.companion.ui.screens.settings.ChangePasswordScreen(navController = navController)
            }
            composable("feedback_submit") {
                com.mindful.companion.ui.screens.feedback.FeedbackSubmitScreen(navController = navController)
            }
            composable("my_posts") {
                com.mindful.companion.ui.screens.history.MyPostsScreen(navController = navController)
            }
            composable("report") {
                com.mindful.companion.ui.screens.report.ReportScreen(navController = navController)
            }
            composable("weekly_report") {
                com.mindful.companion.ui.screens.report.WeeklyReportScreen(navController = navController)
            }
        }
    }
}

@Composable
private fun StitchBottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
            .background(StitchNavBarBg)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                if (selected) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(StitchNavActiveBg)
                            .clickable { onItemClick(item) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = StitchNavActiveText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StitchNavActiveText
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .clickable { onItemClick(item) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = StitchOnSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            color = StitchOnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface AuthRepositoryEntryPoint {
    fun authRepository(): AuthRepository
}
