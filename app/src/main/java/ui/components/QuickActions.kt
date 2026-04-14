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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickActions(
    onNavigateToBodyweight: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToHeatMap: () -> Unit,
    onNavigateToEditLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    // Первый ряд — 3 кнопки
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            icon = "⚖️",
            label = "Вес",
            onClick = onNavigateToBodyweight,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = "📈",
            label = "Графики",
            onClick = onNavigateToCharts,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = "🗓️",
            label = "Карта",
            onClick = onNavigateToHeatMap,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Второй ряд — 3 кнопки
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            icon = "📋",
            label = "История",
            onClick = onNavigateToHistory,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = "✏️",
            label = "Записи",
            onClick = onNavigateToEditLog,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = "⚙️",
            label = "Ещё",
            onClick = onNavigateToSettings,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
            .clickable { onClick() }
            .padding(vertical = 10.dp)
            .semantics { contentDescription = "$label, кнопка" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
        }
    }
}
