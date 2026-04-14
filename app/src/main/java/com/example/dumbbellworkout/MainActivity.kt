package com.example.dumbbellworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dumbbellworkout.data.repository.WorkoutRepository
import com.example.dumbbellworkout.ui.screens.HistoryScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MotivationalNotifications.schedule(this, 10, 0)
        setContent {
            WorkoutTheme {
                // Миграция данных из SharedPreferences в Room
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    scope.launch(Dispatchers.IO) {
                        WorkoutRepository(applicationContext).migrateFromSharedPrefs(applicationContext)
                    }
                }

                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val mainScreens = listOf(
        "home", "schedule", "stats", "achievements",
        "bodyweight", "charts", "editlog", "settings", "heatmap", "history"
    )
    val showBottomBar = currentRoute in mainScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GlassBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { inclusive = (route == "home") }
                            launchSingleTop = true
                        }
                    },
                    onStartWorkout = {
                        val todayWorkout = getTodayWorkout()
                        if (todayWorkout.id != "rest") {
                            navController.navigate("active/${todayWorkout.id}")
                        }
                    }
                )
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToStats = { navController.navigate("stats") },
                    onNavigateToSchedule = { navController.navigate("schedule") },
                    onNavigateToBodyweight = { navController.navigate("bodyweight") },
                    onNavigateToCharts = { navController.navigate("charts") },
                    onNavigateToEditLog = { navController.navigate("editlog") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToAchievements = { navController.navigate("achievements") },
                    onNavigateToHeatMap = { navController.navigate("heatmap") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onStartWorkout = { workoutId -> navController.navigate("active/$workoutId") },
                    onViewWorkout = { workoutId -> navController.navigate("detail/$workoutId") }
                )
            }

            composable(
                "detail/{id}",
                enterTransition = { slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(tween(350)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(250)) }
            ) {
                val id = it.arguments?.getString("id") ?: return@composable
                WorkoutDetailScreen(workoutId = id, onBack = { navController.popBackStack() }, onStart = { navController.navigate("active/$id") })
            }

            composable(
                "active/{id}",
                enterTransition = { slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500, easing = FastOutSlowInEasing)) + fadeIn(tween(400)) },
                popExitTransition = { slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeOut(tween(300)) }
            ) {
                val id = it.arguments?.getString("id") ?: return@composable
                ActiveWorkoutScreen(workoutId = id, onFinish = { navController.popBackStack("home", false) })
            }

            composable("schedule") { ScheduleScreen(onBack = { navController.popBackStack() }) }
            composable("stats") { StatsScreen(onBack = { navController.popBackStack() }) }
            composable("bodyweight") { BodyweightScreen(onBack = { navController.popBackStack() }) }
            composable("charts") { ChartsScreen(onBack = { navController.popBackStack() }) }
            composable("editlog") { EditLogScreen(onBack = { navController.popBackStack() }) }
            composable("achievements") { AchievementsScreen(onBack = { navController.popBackStack() }) }
            composable("heatmap") { HeatMapScreen(onBack = { navController.popBackStack() }) }

            // ── НОВЫЙ ЭКРАН: История ──
            composable("history") { HistoryScreen(onBack = { navController.popBackStack() }) }

            composable(
                "settings",
                enterTransition = { slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(tween(350)) },
                popExitTransition = { slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(250)) }
            ) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
