package com.example.dumbbellworkout.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dumbbellworkout.*
import kotlinx.coroutines.delay

@Composable
fun FinishSummary(
    elapsedSeconds: Int,
    calories: Int,
    sessionTonnage: Float,
    totalSets: Int,
    sessionRecords: Int,
    streak: Int,
    earnedXP: Int,
    levelNumber: Int,
    prevTonnage: Float,
    onFinish: () -> Unit
) {
    val tonnageDiff = sessionTonnage - prevTonnage
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); showContent = true }

    fun formatTime(s: Int): String {
        val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, sec) else String.format("%02d:%02d", m, sec)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        ConfettiEffect(isActive = true)

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .semantics { contentDescription = "Тренировка завершена. Время: ${formatTime(elapsedSeconds)}, тоннаж: ${sessionTonnage.toInt()} кг, подходов: $totalSets" },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(40.dp)) }

                item {
                    val scale by animateFloatAsState(
                        targetValue = if (showContent) 1f else 0f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f), label = "trophy"
                    )
                    Text("🏆", fontSize = 72.sp, modifier = Modifier.scale(scale))
                }

                item { Text("Тренировка завершена!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Purple) }

                item {
                    val shape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(shape)
                            .background(Brush.horizontalGradient(listOf(Purple.copy(alpha = 0.2f), Purple.copy(alpha = 0.05f))))
                            .border(1.dp, Purple.copy(alpha = 0.2f), shape).padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text("⭐", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+$earnedXP XP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Purple)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Уровень $levelNumber", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        val isUp = tonnageDiff >= 0
                        val diffColor = if (isUp) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                        val diffText = if (isUp) "+${tonnageDiff.toInt()} кг" else "${tonnageDiff.toInt()} кг"
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(diffColor.copy(alpha = 0.08f))
                                .border(1.dp, diffColor.copy(alpha = 0.15f), shape).padding(14.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${if (isUp) "📈" else "📉"} Сравнение с прошлой", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
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
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.15f), shape).padding(14.dp),
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
                            .border(1.dp, Color(0xFFFF6B35).copy(alpha = 0.15f), shape).padding(14.dp),
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
                            .padding(vertical = 14.dp)
                            .semantics { contentDescription = "Вернуться на главную" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("На главную", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
