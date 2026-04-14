package com.example.dumbbellworkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dumbbellworkout.Purple
import com.example.dumbbellworkout.Workout

@Composable
fun TodayWorkoutCard(
    workout: Workout,
    isRestDay: Boolean,
    onStartWorkout: (String) -> Unit
) {
    val bannerColor = if (isRestDay) Color(0xFF4CAF50) else Purple
    val desc = if (isRestDay) "День отдыха" else "Сегодня: ${workout.name}, ${workout.exercises.size} упражнений"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(bannerColor.copy(alpha = 0.35f), bannerColor.copy(alpha = 0.1f))))
            .border(1.dp, bannerColor.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(16.dp)
            .semantics { contentDescription = desc }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isRestDay) "😴 День отдыха" else "📋 Сегодня",
                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    if (isRestDay) "Восстановление" else workout.name,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                if (!isRestDay) {
                    Text(
                        "${workout.exercises.size} упр. · ~${workout.exercises.size * 5} мин",
                        fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
            if (!isRestDay) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(bannerColor.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onStartWorkout(workout.id) }
                        .semantics { contentDescription = "Начать тренировку ${workout.name}" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }
}
