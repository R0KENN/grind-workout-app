package com.example.dumbbellworkout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dumbbellworkout.data.repository.OverloadSuggestion
import com.example.dumbbellworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(workoutId: String, onFinish: () -> Unit) {
    val context = LocalContext.current
    val workout = ALL_WORKOUTS[workoutId] ?: return
    val coroutineScope = rememberCoroutineScope()
    val workoutRepository = remember(context) { WorkoutRepository(context) }
    val (haptic, view) = rememberHaptics()

    var exerciseIndex by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var currentSet by remember { mutableIntStateOf(1) }
    var isResting by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var restSecondsLeft by remember { mutableIntStateOf(0) }
    var showRecord by remember { mutableStateOf(false) }
    var recordOldWeight by remember { mutableFloatStateOf(0f) }
    var weightSuggestion by remember { mutableStateOf<WorkoutLog.WeightSuggestion?>(null) }
    val autosuggestEnabled = remember { UserPreferences.isAutosuggestEnabled(context) }
    val progressionStep = remember { UserPreferences.getProgressionStep(context) }
    var editingSet by remember { mutableStateOf<LoggedSet?>(null) }
    var completedSetsToday by remember { mutableStateOf(listOf<LoggedSet>()) }
    var totalSets by remember { mutableIntStateOf(0) }
    var logRevision by remember { mutableIntStateOf(0) }

// Перезагружаем список выполненных подходов текущего упражнения при смене упражнения и при изменении журнала
    LaunchedEffect(exerciseIndex, totalSets, logRevision) {
        val ex = workout.exercises.getOrNull(exerciseIndex) ?: return@LaunchedEffect
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = sdf.format(java.util.Date())
        completedSetsToday = WorkoutLog.loadAllLogs(context)[today]?.sets
            ?.filter { it.exerciseName == ex.name }
            ?: emptyList()
    }

    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(true) }
    var sessionTonnage by remember { mutableFloatStateOf(0f) }
    var sessionRecords by remember { mutableIntStateOf(0) }


    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    var exerciseVisible by remember { mutableStateOf(true) }

    val currentExercise = workout.exercises.getOrNull(exerciseIndex)

    LaunchedEffect(exerciseIndex) {
        val ex = workout.exercises.getOrNull(exerciseIndex)
        if (ex != null) {
            noteText = NotesManager.getNote(context, ex.name)

            // Подсказка веса для первого подхода нового упражнения
            if (currentSet == 1 && weightInput.isEmpty()) {
                weightSuggestion = if (autosuggestEnabled) {
                    WorkoutLog.suggestWeight(context, ex.name, ex.reps, progressionStep)
                } else null

                weightSuggestion?.suggestedWeight?.let { w ->
                    val s = "%.1f".format(w)
                    weightInput = if (s.endsWith(".0")) s.dropLast(2) else s
                }
            }
        }
        exerciseVisible = true
    }

    LaunchedEffect(timerRunning) {
        while (timerRunning) { delay(1000L); elapsedSeconds++ }
    }

    LaunchedEffect(isResting, restSecondsLeft) {
        if (isResting && restSecondsLeft > 0) {
            delay(1000L); restSecondsLeft--
        } else if (isResting && restSecondsLeft == 0) {
            isResting = false
            VibrationHelper.vibrateRestEnd(context)
        }
    }

    fun formatTime(s: Int): String {
        val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, sec) else String.format("%02d:%02d", m, sec)
    }

    fun calculateCalories(tonnage: Float, minutes: Int): Int =
        (tonnage * 0.05f + minutes * 5.5f).toInt()

    fun animateToNextExercise(nextIndex: Int, withRest: Boolean = false, restSec: Int = 0) {
        coroutineScope.launch {
            exerciseVisible = false
            delay(300)
            exerciseIndex = nextIndex
            currentSet = 1
            val nextEx = workout.exercises[nextIndex]
            weightSuggestion = if (autosuggestEnabled) {
                WorkoutLog.suggestWeight(context, nextEx.name, nextEx.reps, progressionStep)
            } else null

            weightInput = weightSuggestion?.suggestedWeight?.let { w ->
                // Округляем до 1 знака после запятой и убираем хвостовой ".0"
                val s = "%.1f".format(w)
                if (s.endsWith(".0")) s.dropLast(2) else s
            } ?: WorkoutLog.getLastWeight(context, nextEx.name)?.toString() ?: ""
            repsInput = ""
            if (withRest) {
                restSecondsLeft = restSec
                isResting = true
            } else {
                isResting = false
            }
            delay(100)
            exerciseVisible = true
        }
    }

    fun saveSet() {
        val w = weightInput.replace(",", ".").toFloatOrNull() ?: return
        val r = repsInput.toIntOrNull() ?: return
        val ex = currentExercise ?: return

        Haptics.confirm(haptic)

        val oldMax = WorkoutLog.getMaxWeight(context, ex.name)
        if (w > oldMax && oldMax > 0) {
            showRecord = true; recordOldWeight = oldMax; sessionRecords++
            VibrationHelper.vibrateRecord(context)
            Haptics.heavy(view, haptic)
        }

        WorkoutLog.addSet(context, LoggedSet(ex.name, currentSet, w, r))
        coroutineScope.launch {
            workoutRepository.addSet(
                exerciseName = ex.name,
                setNumber = currentSet,
                weight = w,
                reps = r
            )
        }
        sessionTonnage += w * r
        totalSets++
        weightInput = ""; repsInput = ""
        weightSuggestion = null  // показывали один раз — больше не надо

        if (currentSet < ex.sets) {
            currentSet++
            restSecondsLeft = ex.restSeconds
            isResting = true
        } else if (exerciseIndex < workout.exercises.size - 1) {
            animateToNextExercise(exerciseIndex + 1, withRest = true, restSec = ex.restSeconds)
        } else {
            isFinished = true; timerRunning = false
        }
    }

    fun skipExercise() {
        Haptics.click(haptic)
        if (exerciseIndex < workout.exercises.size - 1) {
            animateToNextExercise(exerciseIndex + 1)
        } else {
            isFinished = true; timerRunning = false
        }
    }

    if (showNoteDialog && currentExercise != null) {
        var tempNote by remember(currentExercise.name) { mutableStateOf(noteText) }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("📝 Заметка", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text(currentExercise.name, fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempNote,
                        onValueChange = { tempNote = it },
                        placeholder = { Text("Ощущения, боль, заметки...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    NotesManager.saveNote(context, currentExercise.name, tempNote)
                    noteText = tempNote
                    showNoteDialog = false
                }) { Text("Сохранить", color = Purple) }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Отмена", color = Color.White.copy(alpha = 0.5f))
                }
            },
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Диалог редактирования подхода ──
    editingSet?.let { setToEdit ->
        var newWeight by remember(setToEdit) { mutableStateOf(setToEdit.weight.toString()) }
        var newReps by remember(setToEdit) { mutableStateOf(setToEdit.reps.toString()) }

        AlertDialog(
            onDismissRequest = { editingSet = null },
            title = { Text("Редактировать подход ${setToEdit.setNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("Вес, кг") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newReps,
                        onValueChange = { newReps = it },
                        label = { Text("Повторений") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = newWeight.replace(",", ".").toFloatOrNull()
                    val r = newReps.toIntOrNull()
                    if (w != null && r != null) {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val today = sdf.format(java.util.Date())
                        WorkoutLog.updateSet(context, today, setToEdit, w, r)
                        coroutineScope.launch {
                            workoutRepository.updateMatchingSet(
                                date = today,
                                exerciseName = setToEdit.exerciseName,
                                setNumber = setToEdit.setNumber,
                                oldWeight = setToEdit.weight,
                                oldReps = setToEdit.reps,
                                newWeight = w,
                                newReps = r
                            )
                        }
                        // Пересчитываем сессионную статистику
                        sessionTonnage += (w * r) - (setToEdit.weight * setToEdit.reps)
                        Haptics.confirm(haptic)
                        editingSet = null
                        logRevision++
                    }
                }) { Text("Сохранить", color = Purple) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val today = sdf.format(java.util.Date())
                        WorkoutLog.deleteSet(context, today, setToEdit)
                        coroutineScope.launch {
                            workoutRepository.deleteMatchingSet(
                                date = today,
                                exerciseName = setToEdit.exerciseName,
                                setNumber = setToEdit.setNumber,
                                weight = setToEdit.weight,
                                reps = setToEdit.reps
                            )
                        }
                        sessionTonnage -= setToEdit.weight * setToEdit.reps
                        totalSets -= 1
                        if (currentSet > 1) currentSet -= 1
                        Haptics.reject(view, haptic)
                        editingSet = null
                        logRevision++
                    }) { Text("Удалить", color = Color(0xFFE57373)) }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { editingSet = null }) {
                        Text("Отмена", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            },
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ===== FINISH SCREEN =====
    if (isFinished) {
        LaunchedEffect(Unit) {
            val missedDate = StreakManager.getMissedTrainingDay(context)
            if (missedDate != null && StreakManager.getMissedWorkoutId(missedDate) == workoutId) {
                StreakManager.recordRecovery(context, missedDate)
            }
            StreakManager.recordWorkout(context)
            LevelManager.addXP(context, LevelManager.xpForWorkout())
            val streak = StreakManager.getCurrentStreak(context)
            LevelManager.addXP(context, LevelManager.xpForStreak(streak))
        }

        val calories = calculateCalories(sessionTonnage, elapsedSeconds / 60)
        val level = LevelManager.getUserLevel(context)
        val streak = StreakManager.getCurrentStreak(context)

        val prevTonnage = remember {
            val logs = WorkoutLog.loadAllLogs(context)
            val sortedDates = logs.keys.sorted()
            if (sortedDates.size >= 2) {
                val prevDate = sortedDates[sortedDates.size - 2]
                logs[prevDate]?.sets?.sumOf { (it.weight * it.reps).toDouble() }?.toFloat() ?: 0f
            } else 0f
        }
        val tonnageDiff = sessionTonnage - prevTonnage

        var showContent by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { delay(300); showContent = true }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            ConfettiEffect(isActive = true)

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(40.dp)) }

                    item {
                        val scale by animateFloatAsState(
                            targetValue = if (showContent) 1f else 0f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f),
                            label = "trophy"
                        )
                        Text("🏆", fontSize = 72.sp, modifier = Modifier.scale(scale))
                    }

                    item {
                        Text("Тренировка завершена!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Purple)
                    }

                    item {
                        val earnedXP = LevelManager.xpForWorkout() + LevelManager.xpForStreak(streak)
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(Brush.horizontalGradient(listOf(Purple.copy(alpha = 0.2f), Purple.copy(alpha = 0.05f))))
                                .border(1.dp, Purple.copy(alpha = 0.2f), shape)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text("⭐", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("+$earnedXP XP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Purple)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Уровень ${level.level}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("⏱", formatTime(elapsedSeconds), "Время"),
                                Triple("🔥", "$calories", "Калории"),
                                Triple("🏋️", "${sessionTonnage.toInt()} кг", "Тоннаж"),
                                Triple("💪", "$totalSets", "Подходы")
                            ).forEach { (icon, value, label) ->
                                Box(modifier = Modifier.weight(1f)) {
                                    val shape = RoundedCornerShape(14.dp)
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(shape)
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(icon, fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (prevTonnage > 0) {
                        item {
                            val shape = RoundedCornerShape(14.dp)
                            val isUp = tonnageDiff >= 0
                            val diffColor = if (isUp) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                            val diffIcon = if (isUp) "📈" else "📉"
                            val diffText = if (isUp) "+${tonnageDiff.toInt()} кг" else "${tonnageDiff.toInt()} кг"

                            Box(
                                modifier = Modifier.fillMaxWidth().clip(shape)
                                    .background(diffColor.copy(alpha = 0.08f))
                                    .border(1.dp, diffColor.copy(alpha = 0.15f), shape)
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$diffIcon Сравнение с прошлой", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(diffText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = diffColor)
                                }
                            }
                        }
                    }

                    if (sessionRecords > 0) {
                        item {
                            val shape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(shape)
                                    .background(Color(0xFFFFD700).copy(alpha = 0.08f))
                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.15f), shape)
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥇 Новых рекордов: $sessionRecords", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            }
                        }
                    }

                    item {
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(Color(0xFFFF6B35).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFFFF6B35).copy(alpha = 0.15f), shape)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥 Серия: $streak дн.", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(Brush.horizontalGradient(listOf(Purple, Purple.copy(alpha = 0.7f))))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
                                .clickable { onFinish() }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("На главную", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
        return
    }

    // ── Диалог подтверждения выхода ──
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Завершить тренировку?", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column {
                    Text(
                        "У тебя уже $totalSets ${pluralSets(totalSets)} в этой сессии.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Прогресс уже сохранён. Выйти сейчас или показать итоги?",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    Haptics.heavy(view, haptic)
                    showExitDialog = false
                    isFinished = true       // запускаем экран итогов
                    timerRunning = false
                }) { Text("Показать итоги", color = Purple, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        Haptics.reject(view, haptic)
                        showExitDialog = false
                        onFinish()
                    }) { Text("Выйти без итогов", color = Color.White.copy(alpha = 0.6f)) }
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Отмена", color = Color.White.copy(alpha = 0.4f))
                    }
                }
            },
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (currentExercise == null) return

    val isTimedExercise = currentExercise.reps.contains("сек", ignoreCase = true)
    val progress = (exerciseIndex + currentSet.toFloat() / currentExercise.sets) / workout.exercises.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("${exerciseIndex + 1}/${workout.exercises.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(formatTime(elapsedSeconds), fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (noteText.isNotBlank()) Purple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                .clickable { showNoteDialog = true }
                                .padding(8.dp)
                        ) {
                            Text("📝", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${sessionTonnage.toInt()} кг", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Haptics.click(haptic)
                        if (totalSets > 0) {
                            showExitDialog = true
                        } else {
                            onFinish()
                        }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (exerciseVisible) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item(key = "progress") {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                        color = Purple,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }

                item(key = "gif") {
                    if (currentExercise.gifRes != 0) {
                        GifImage(gifRes = currentExercise.gifRes, exerciseName = currentExercise.name, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                item(key = "name") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentExercise.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.White)
                        Text(currentExercise.target, fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f), textAlign = TextAlign.Center)
                    }
                }

                if (noteText.isNotBlank()) {
                    item(key = "note_preview") {
                        val shape = RoundedCornerShape(10.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(Color(0xFF2A2A40).copy(alpha = 0.5f))
                                .border(1.dp, Purple.copy(alpha = 0.1f), shape)
                                .clickable { showNoteDialog = true }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📝", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(noteText, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 2)
                            }
                        }
                    }
                }

                if (completedSetsToday.isNotEmpty()) {
                    item(key = "completed_sets") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Сделано: ${completedSetsToday.size}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                            completedSetsToday.forEach { set ->
                                val shape = RoundedCornerShape(8.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape)
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
                                        .clickable {
                                            Haptics.click(haptic)
                                            editingSet = set
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "#${set.setNumber}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "${set.weight} кг × ${set.reps}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("✏️", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item(key = "sets_reps") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val shape = RoundedCornerShape(12.dp)
                        Box(modifier = Modifier.weight(1f).clip(shape).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.06f), shape).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Подход", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                                Text("$currentSet/${currentExercise.sets}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple)
                            }
                        }
                        Box(modifier = Modifier.weight(1f).clip(shape).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.06f), shape).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isTimedExercise) "Время" else "Повторения", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                                Text(currentExercise.reps, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B6B))
                            }
                        }
                    }
                }

                item(key = "weight_suggestion") {
                    weightSuggestion?.takeIf { it.delta != 0f }?.let { sug ->
                        val shape = RoundedCornerShape(10.dp)
                        val accent = if (sug.delta > 0) Color(0xFF4CAF50) else Color(0xFFFFA726)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(accent.copy(alpha = 0.12f))
                                .border(1.dp, accent.copy(alpha = 0.3f), shape)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (sug.delta > 0) "📈" else "📉", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${if (sug.delta > 0) "+" else ""}${"%.1f".format(sug.delta)} кг",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                                Text(sug.reason, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                item(key = "last_weight") {
                    val lastWeight = WorkoutLog.getLastWeight(context, currentExercise.name)
                    if (lastWeight != null) {
                        Text("Прошлый раз: $lastWeight кг", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
                    }
                }

                item(key = "overload_suggestion") {
                    val suggestion by produceState<OverloadSuggestion?>(
                        initialValue = null,
                        key1 = currentExercise.name,
                        key2 = currentExercise.reps
                    ) {
                        if (!currentExercise.reps.contains("сек", ignoreCase = true)) {
                            val targetReps = Regex("""\d+""")
                                .findAll(currentExercise.reps)
                                .map { it.value.toInt() }
                                .lastOrNull()
                                ?: 12

                            value = workoutRepository.getProgressiveOverloadSuggestion(
                                currentExercise.name,
                                targetReps
                            )
                        }
                    }
                    if (suggestion != null) {
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(Color(0xFF4CAF50).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.15f), shape)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    "📈 Прогрессивная перегрузка",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Рекомендуем: ${suggestion.suggestedWeight} кг (было ${suggestion.currentWeight} кг)",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    suggestion.reason,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }
                }

                item(key = "history") {
                    val history = remember(exerciseIndex) { WorkoutLog.getExerciseHistory(context, currentExercise.name) }
                    if (history.isNotEmpty()) {
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("📋 Прошлая тренировка", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Purple.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.height(6.dp))
                                history.forEach { set ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Подход ${set.setNumber}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.35f))
                                        Text("${set.weight} кг × ${set.reps}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "record") {
                    AnimatedVisibility(visible = showRecord) {
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🥇 Новый рекорд! Было: $recordOldWeight кг", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        LaunchedEffect(Unit) { delay(3000); showRecord = false }
                    }
                }

                if (isResting) {
                    item(key = "rest") {
                        val shape = RoundedCornerShape(14.dp)
                        Box(modifier = Modifier.fillMaxWidth().clip(shape).background(Purple.copy(alpha = 0.08f)).border(1.dp, Purple.copy(alpha = 0.15f), shape).padding(20.dp)) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Отдых", fontSize = 13.sp, color = Purple, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(String.format("%d:%02d", restSecondsLeft / 60, restSecondsLeft % 60), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { restSecondsLeft.toFloat() / currentExercise.restSeconds.toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                                    color = Purple, trackColor = Color.White.copy(alpha = 0.08f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp)).clickable { isResting = false; restSecondsLeft = 0 }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                    Text("Пропустить", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }


                if (!isResting) {
                    item(key = "weight_input") {
                        OutlinedTextField(
                            value = weightInput, onValueChange = { weightInput = it },
                            label = { Text("Вес (кг)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                        )
                    }
                    item(key = "reps_input") {
                        OutlinedTextField(
                            value = repsInput, onValueChange = { repsInput = it },
                            label = { Text(if (isTimedExercise) "Время (сек)" else "Повторения", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = Color.White.copy(alpha = 0.1f))
                        )
                    }
                    item(key = "save_btn") {
                        val enabled = weightInput.isNotBlank() && repsInput.isNotBlank()
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(if (enabled) Brush.horizontalGradient(listOf(Purple, Purple.copy(alpha = 0.7f))) else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f))))
                                .border(1.dp, if (enabled) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), shape)
                                .clickable(enabled = enabled) { saveSet() }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Записать подход", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f))
                        }
                    }
                    item(key = "skip_btn") {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { skipExercise() }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text("Пропустить упражнение", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f))
                        }
                    }
                }

                item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}
private fun pluralSets(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "подход"
        mod10 in 2..4 && mod100 !in 12..14 -> "подхода"
        else -> "подходов"
    }
}
