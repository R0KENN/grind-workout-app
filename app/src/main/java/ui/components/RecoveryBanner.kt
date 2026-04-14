package com.example.dumbbellworkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun RecoveryBanner(
    missedWorkoutName: String,
    missedWorkoutId: String,
    onStartWorkout: (String) -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xFFFF9800).copy(alpha = 0.2f), Color(0xFFFF9800).copy(alpha = 0.05f))))
            .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.25f), shape)
            .padding(14.dp)
            .semantics { contentDescription = "Серия под угрозой. Нажмите чтобы пройти $missedWorkoutName" }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("⚠️ Серия под угрозой!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Пройди $missedWorkoutName чтобы восстановить", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFF9800).copy(alpha = 0.3f))
                    .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable { onStartWorkout(missedWorkoutId) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .semantics { contentDescription = "Начать восстановительную тренировку" }
            ) {
                Text("▶", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
