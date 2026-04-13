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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Schedule motivational notifications
        MotivationalNotifications.schedule(this, 10, 0)
        setContent {
            WorkoutTheme {
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
        "bodyweight", "charts", "editlog", "settings", "heatmap"
    )
    val showBottomBar = currentRoute in mainScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GlassBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") {
                                inclusive = (route == "home")
                            }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing))
            }
        ) {
            composable("home") {
                HomeScreen(
                    onStartWorkout = { id -> navController.navigate("active/$id") },
                    onViewWorkout = { id -> navController.navigate("detail/$id") },
                    onNavigateToStats = {
                        navController.navigate("stats") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToSchedule = {
                        navController.navigate("schedule") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToBodyweight = { navController.navigate("bodyweight") },
                    onNavigateToCharts = { navController.navigate("charts") },
                    onNavigateToEditLog = { navController.navigate("editlog") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToAchievements = {
                        navController.navigate("achievements") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToHeatMap = { navController.navigate("heatmap") }
                )
            }

            composable(
                route = "detail/{id}",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 4 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth / 4 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 200))
                }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                WorkoutDetailScreen(
                    workoutId = id,
                    onBack = { navController.popBackStack() },
                    onStart = { navController.navigate("active/$id") }
                )
            }

            composable(
                route = "active/{id}",
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 3 },
                        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 450))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight / 3 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 200))
                }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                ActiveWorkoutScreen(
                    workoutId = id,
                    onFinish = { navController.popBackStack("home", inclusive = false) }
                )
            }

            composable("schedule") {
                ScheduleScreen(onBack = { navController.popBackStack() })
            }

            composable("stats") {
                StatsScreen(onBack = { navController.popBackStack() })
            }

            composable("bodyweight") {
                BodyweightScreen(onBack = { navController.popBackStack() })
            }

            composable("charts") {
                ChartsScreen(onBack = { navController.popBackStack() })
            }

            composable("editlog") {
                EditLogScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = "settings",
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 3 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400))
                },
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight / 3 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 200))
                }
            ) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable("achievements") {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }

            composable("heatmap") {
                HeatMapScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
