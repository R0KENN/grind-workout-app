package com.example.dumbbellworkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyweightScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf(WorkoutLog.loadBodyweight(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚖️ Вес тела", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassCard {
                    Text("Записать вес", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it; saved = false },
                        label = { Text("Вес (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(
                        text = if (saved) "✅ Сохранено!" else "Сохранить",
                        onClick = {
                            val w = weightInput.replace(",", ".").toFloatOrNull()
                            if (w != null) {
                                WorkoutLog.saveBodyweight(context, w)
                                saved = true
                                data = WorkoutLog.loadBodyweight(context)
                            }
                        },
                        enabled = weightInput.isNotBlank() && !saved,
                        accentColor = if (saved) Green else Purple,
                        height = 48.dp
                    )
                }
            }

            if (data.isNotEmpty()) {
                item {
                    Text("📋 История", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                val sortedEntries = data.entries.sortedByDescending { it.key }
                val firstWeight = sortedEntries.lastOrNull()?.value
                items(sortedEntries) { (date, weight) ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(date, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$weight кг", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (firstWeight != null && firstWeight != weight) {
                                    val diff = weight - firstWeight
                                    val diffColor = if (diff < 0) Green else Red
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val badgeShape = RoundedCornerShape(6.dp)
                                    Box(
                                        modifier = Modifier
                                            .clip(badgeShape)
                                            .background(diffColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${if (diff > 0) "+" else ""}${String.format("%.1f", diff)}",
                                            fontSize = 12.sp, color = diffColor, fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    GlassCard {
                        Text(
                            "Пока нет записей. Добавьте свой вес! 📝",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
