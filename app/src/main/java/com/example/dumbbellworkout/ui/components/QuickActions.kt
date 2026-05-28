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
        QuickActionButton(icon = AppIcons.Bodyweight, label = "Вес", onClick = onNavigateToBodyweight, modifier = Modifier.weight(1f))
        QuickActionButton(icon = AppIcons.Charts, label = "Графики", onClick = onNavigateToCharts, modifier = Modifier.weight(1f))
        QuickActionButton(icon = AppIcons.HeatMap, label = "Карта", onClick = onNavigateToHeatMap, modifier = Modifier.weight(1f))
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Второй ряд — 3 кнопки
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(icon = AppIcons.History, label = "История", onClick = onNavigateToHistory, modifier = Modifier.weight(1f))
        QuickActionButton(icon = AppIcons.EditLog, label = "Записи", onClick = onNavigateToEditLog, modifier = Modifier.weight(1f))
        QuickActionButton(icon = AppIcons.Settings, label = "Ещё", onClick = onNavigateToSettings, modifier = Modifier.weight(1f))
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,   // ← было String
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ...
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
    }
}
