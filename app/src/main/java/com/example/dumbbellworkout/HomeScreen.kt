package com.example.dumbbellworkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dumbbellworkout.ui.components.*
import com.example.dumbbellworkout.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartWorkout: (String) -> Unit,
    onViewWorkout: (String) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToBodyweight: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToEditLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToHeatMap: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GRIND", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Уровень + Серия + Неделя ──
            item(key = "top_stats") {
                AnimatedListItem(index = 0) {
                    TopStatsRow(
                        userLevel = state.userLevel,
                        currentStreak = state.currentStreak,
                        canRecover = state.canRecover,
                        completedDays = state.completedDays,
                        totalDays = state.totalDays,
                        weeklyProgress = state.weeklyProgress
                    )
                }
            }

            // ── Баннер восстановления ──
            if (state.missedDate != null) {
                item(key = "recovery") {
                    AnimatedListItem(index = 1) {
                        RecoveryBanner(
                            missedWorkoutName = state.missedWorkoutName,
                            missedWorkoutId = state.missedWorkoutId,
                            onStartWorkout = onStartWorkout
                        )
                    }
                }
            }

            // ── Совет дня ──
            item(key = "advice") {
                AnimatedListItem(index = 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple.copy(alpha = 0.08f))
                            .border(1.dp, Purple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .semantics { contentDescription = "Совет дня: ${state.advice}" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧠", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(state.advice, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), lineHeight = 18.sp)
                    }
                }
            }

            // ── Сегодняшняя тренировка ──
            item(key = "today") {
                AnimatedListItem(index = 3) {
                    TodayWorkoutCard(
                        workout = state.todayWorkout,
                        isRestDay = state.isRestDay,
                        onStartWorkout = onStartWorkout
                    )
                }
            }

            // ── Челленджи ──
            item(key = "challenges") {
                val challenges = state.challenges
                val shape = RoundedCornerShape(14.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
                        .padding(14.dp)
                        .semantics { contentDescription = "Еженедельные челленджи: ${challenges.count { it.isCompleted }} из ${challenges.size} выполнено" },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("⚔️ Челленджи", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${challenges.count { it.isCompleted }}/${challenges.size}", fontSize = 13.sp, color = Purple)
                    }
                    challenges.forEach { ch ->
                        val prog = if (ch.targetValue > 0) ch.currentValue.toFloat() / ch.targetValue else 0f
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ch.icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(ch.title, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                        color = if (ch.isCompleted) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f))
                                    Text(if (ch.isCompleted) "✅" else "${ch.currentValue}/${ch.targetValue}",
                                        fontSize = 11.sp, color = if (ch.isCompleted) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.4f))
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.08f))) {
                                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(prog.coerceIn(0f, 1f)).clip(RoundedCornerShape(2.dp))
                                        .background(if (ch.isCompleted) Color(0xFF4CAF50) else Purple))
                                }
                            }
                        }
                    }
                }
            }

            // ── Цитата ──
            item(key = "quote") {
                Text(
                    "\"${state.todayQuote}\"", fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.35f), textAlign = TextAlign.Center,
                    lineHeight = 18.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // ── Быстрые действия ──
            item(key = "actions") {
                QuickActions(
                    onNavigateToBodyweight = onNavigateToBodyweight,
                    onNavigateToCharts = onNavigateToCharts,
                    onNavigateToHeatMap = onNavigateToHeatMap,
                    onNavigateToEditLog = onNavigateToEditLog,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToHistory = onNavigateToHistory
                )
            }

            // ── Программы ──
            item(key = "programs_title") {
                Text("Программы", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
            }

            val programs = ALL_WORKOUTS.values.filter { it.id != "rest" }.toList()
            itemsIndexed(programs, key = { _, p -> p.id }) { _, program ->
                ProgramItem(program = program, onClick = { onViewWorkout(program.id) })
            }

            item(key = "spacer") { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
