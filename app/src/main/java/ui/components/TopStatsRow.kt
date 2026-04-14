package com.example.dumbbellworkout.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dumbbellworkout.Purple
import com.example.dumbbellworkout.UserLevel

@Composable
fun TopStatsRow(
    userLevel: UserLevel,
    currentStreak: Int,
    canRecover: Boolean,
    completedDays: Int,
    totalDays: Int,
    weeklyProgress: Float
) {
    val animatedWeeklyProgress by animateFloatAsState(
        targetValue = weeklyProgress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "weekly"
    )
    val animatedXP by animateFloatAsState(
        targetValue = userLevel.currentXP.toFloat() / userLevel.xpForNextLevel.toFloat().coerceAtLeast(1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "xp"
    )
    val levelColor = when {
        userLevel.level <= 3 -> Color(0xFF4CAF50)
        userLevel.level <= 7 -> Color(0xFF2196F3)
        userLevel.level <= 12 -> Purple
        userLevel.level <= 17 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Уровень
        GlassStatBox(
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Уровень ${userLevel.level}, ${userLevel.title}" }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(Color.White.copy(alpha = 0.1f), -90f, 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(levelColor, -90f, animatedXP * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text("${userLevel.level}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = levelColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(userLevel.title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
            }
        }

        // Серия
        GlassStatBox(
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = if (canRecover) "Серия $currentStreak дней, под угрозой" else "Серия $currentStreak дней"
                },
            borderColor = if (canRecover) Color(0xFFFF9800).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
            gradientTop = if (canRecover) Color(0xFFFF9800).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.07f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (canRecover) "⚠️" else "🔥", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$currentStreak", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = if (canRecover) Color(0xFFFF9800) else Color.White
                )
                Text(
                    if (canRecover) "спаси!" else "дней", fontSize = 11.sp,
                    color = if (canRecover) Color(0xFFFF9800).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // Неделя
        GlassStatBox(
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Прогресс недели: $completedDays из $totalDays дней" }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(Color.White.copy(alpha = 0.1f), -90f, 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(Purple, -90f, animatedWeeklyProgress * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text("${(animatedWeeklyProgress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Purple)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("$completedDays/$totalDays", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun GlassStatBox(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    gradientTop: Color = Color.White.copy(alpha = 0.07f),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(gradientTop, Color.White.copy(alpha = 0.02f))))
            .border(1.dp, borderColor, shape)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
