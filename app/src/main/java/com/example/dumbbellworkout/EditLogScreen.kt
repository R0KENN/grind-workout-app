package com.example.dumbbellworkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var allLogs by remember { mutableStateOf(WorkoutLog.loadAllLogs(context)) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var editingSet by remember { mutableStateOf<LoggedSet?>(null) }
    var editWeight by remember { mutableStateOf("") }
    var editReps by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Pair<String, LoggedSet>?>(null) }

    // Диалог редактирования
    if (editingSet != null) {
        AlertDialog(
            onDismissRequest = { editingSet = null },
            title = { Text("✏️ Редактировать подход") },
            text = {
                Column {
                    Text(editingSet!!.exerciseName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Подход #${editingSet!!.setNumber}", color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editWeight, onValueChange = { editWeight = it },
                        label = { Text("Вес (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editReps, onValueChange = { editReps = it },
                        label = { Text("Повторения") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val w = editWeight.replace(",", ".").toFloatOrNull()
                    val r = editReps.toIntOrNull()
                    if (w != null && r != null && selectedDate != null) {
                        WorkoutLog.updateSet(context, selectedDate!!, editingSet!!, w, r)
                        allLogs = WorkoutLog.loadAllLogs(context)
                        editingSet = null
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { editingSet = null }) { Text("Отмена") }
            }
        )
    }

    // Диалог удаления
    if (showDeleteDialog && deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("🗑 Удалить подход?") },
            text = {
                val (date, set) = deleteTarget!!
                Text("${set.exerciseName}\nПодход #${set.setNumber}: ${set.weight} кг × ${set.reps}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (date, set) = deleteTarget!!
                        WorkoutLog.deleteSet(context, date, set)
                        allLogs = WorkoutLog.loadAllLogs(context)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedDate != null) "📅 $selectedDate" else "✏️ Редактирование", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedDate != null) selectedDate = null else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (selectedDate != null) {
            // ─── ПОДХОДЫ ЗА ВЫБРАННЫЙ ДЕНЬ ───
            val dayLog = allLogs[selectedDate]
            if (dayLog == null || dayLog.sets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Нет записей за этот день", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Группируем по упражнению
                    val grouped = dayLog.sets.groupBy { it.exerciseName }
                    for ((exName, sets) in grouped) {
                        item {
                            Text(exName, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                modifier = Modifier.padding(top = 8.dp))
                        }
                        items(sets) { set ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Подход #${set.setNumber}: ${set.weight} кг × ${set.reps}",
                                            fontSize = 14.sp
                                        )
                                    }
                                    // Кнопка редактирования
                                    IconButton(onClick = {
                                        editingSet = set
                                        editWeight = set.weight.toString()
                                        editReps = set.reps.toString()
                                    }) {
                                        Icon(Icons.Default.Edit, "Редактировать", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    // Кнопка удаления
                                    IconButton(onClick = {
                                        deleteTarget = Pair(selectedDate!!, set)
                                        showDeleteDialog = true
                                    }) {
                                        Icon(Icons.Default.Delete, "Удалить", tint = Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ─── СПИСОК ДНЕЙ ───
            val sortedDates = allLogs.keys.sortedDescending()
            if (sortedDates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Нет записей", color = TextSecondary, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedDates) { date ->
                        val log = allLogs[date] ?: return@items
                        val t = WorkoutLog.calculateTonnage(context, date)
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedDate = date },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("📅 $date", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("${log.sets.size} подходов • ${String.format("%.0f", t)} кг", fontSize = 13.sp, color = TextSecondary)
                                }
                                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
