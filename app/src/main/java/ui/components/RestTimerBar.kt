package com.example.dumbbellworkout.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dumbbellworkout.Purple

@Composable
fun RestTimerBar(
    restSecondsLeft: Int,
    totalRestSeconds: Int,
    onSkipRest: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Purple.copy(alpha = 0.08f))
            .border(1.dp, Purple.copy(alpha = 0.15f), shape)
            .padding(20.dp)
            .semantics {
                contentDescription = "Таймер отдыха: ${restSecondsLeft / 60} минут ${restSecondsLeft % 60} секунд"
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Отдых", fontSize = 13.sp, color = Purple, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                String.format("%d:%02d", restSecondsLeft / 60, restSecondsLeft % 60),
                fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (totalRestSeconds > 0) restSecondsLeft.toFloat() / totalRestSeconds.toFloat() else 0f },
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                color = Purple, trackColor = Color.White.copy(alpha = 0.08f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .clickable { onSkipRest() }
                    .padding(vertical = 10.dp)
                    .semantics { contentDescription = "Пропустить отдых" },
                contentAlignment = Alignment.Center
            ) {
                Text("Пропустить", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}
