package com.example.dumbbellworkout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dumbbellworkout.Purple
import com.example.dumbbellworkout.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("История", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Фильтр по упражнению",
                            tint = if (state.selectedExerciseFilter != null) Purple else Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (state.selectedDate != null) {
            // Detail view for a date
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.filterByExercise(state.selectedExerciseFilter)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("← ", fontSize = 16.sp, color = Purple)
                        Text("Назад к списку", fontSize = 14.sp, color = Purple)
                    }
                }

                item {
                    Text(state.selectedDate!!, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    val tonnage = state.setsForDate.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                    Text(
                        "${state.setsForDate.size} подходов · ${String.format("%.0f", tonnage)} кг тоннаж",
                        fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f)
                    )
                }

                // Группируем по упражнению
                val grouped = state.setsForDate.groupBy { it.exerciseName }
                grouped.forEach { (exerciseName, sets) ->
                    item(key = "header_$exerciseName") {
                        Text(exerciseName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Purple, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(sets, key = { it.id }) { set ->
                        val shape = RoundedCornerShape(10.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
                                .padding(12.dp)
                                .semantics { contentDescription = "${set.exerciseName}, подход ${set.setNumber}: ${set.weight} кг на ${set.reps} повторений" }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Подход ${set.setNumber}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
                                Text("${set.weight} кг × ${set.reps}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // Date list
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (state.selectedExerciseFilter != null) {
                    item {
                        val shape = RoundedCornerShape(8.dp)
                        Row(
                            modifier = Modifier
                                .clip(shape)
                                .background(Purple.copy(alpha = 0.15f))
                                .border(1.dp, Purple.copy(alpha = 0.2f), shape)
                                .clickable { viewModel.filterByExercise(null) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(state.selectedExerciseFilter!!, fontSize = 13.sp, color = Purple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✕", fontSize = 14.sp, color = Purple.copy(alpha = 0.7f))
                        }
                    }
                }

                if (state.filteredDates.isEmpty() && !state.isLoading) {
                    item {
                        Text(
                            "Нет записей${if (state.selectedExerciseFilter != null) " для этого упражнения" else ""}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp
                        )
                    }
                }

                items(state.filteredDates, key = { "date_$it" }) { date ->
                    val shape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))))
                            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
                            .clickable { viewModel.selectDate(date) }
                            .padding(14.dp)
                            .semantics { contentDescription = "Тренировка $date" }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("📋", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(date, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                            Text("›", fontSize = 20.sp, color = Color.White.copy(alpha = 0.2f))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color(0xFF1A1A2E)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Фильтр по упражнению", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                // "Все" option
                val shape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(if (state.selectedExerciseFilter == null) Purple.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { viewModel.filterByExercise(null); showFilterSheet = false }
                        .padding(12.dp)
                ) {
                    Text("Все упражнения", color = if (state.selectedExerciseFilter == null) Purple else Color.White.copy(alpha = 0.7f))
                }

                state.exerciseNames.forEach { name ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (state.selectedExerciseFilter == name) Purple.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.filterByExercise(name); showFilterSheet = false }
                            .padding(12.dp)
                    ) {
                        Text(name, color = if (state.selectedExerciseFilter == name) Purple else Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
