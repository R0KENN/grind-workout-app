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
fun ProgramItem(program: Workout, onClick: () -> Unit) {
    val emoji = when {
        program.name.contains("PUSH", true) -> "💪"
        program.name.contains("PULL", true) -> "🏋️"
        program.name.contains("SQUAD", true) || program.name.contains("LEG", true) -> "🦵"
        program.name.contains("POWER", true) -> "⚡"
        else -> "🏋️"
    }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))))
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
            .clickable { onClick() }
            .padding(14.dp)
            .semantics { contentDescription = "Программа ${program.name}, ${program.exercises.size} упражнений" }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(Purple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(program.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${program.exercises.size} упр. · ${program.time}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f))
            }
            Text("›", fontSize = 20.sp, color = Color.White.copy(alpha = 0.2f))
        }
    }
}
