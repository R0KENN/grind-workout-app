package com.example.dumbbellworkout

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(NotificationHelper.isEnabled(context)) }
    var hour by remember { mutableIntStateOf(NotificationHelper.getSavedHour(context)) }
    var minute by remember { mutableIntStateOf(NotificationHelper.getSavedMinute(context)) }
    var testSent by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isEnabled = true
            NotificationHelper.scheduleDailyReminder(context, hour, minute)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Настройки", fontWeight = FontWeight.Bold) },
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
            // Notification toggle
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("🔔 Напоминания", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Ежедневное уведомление", fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        isEnabled = true
                                        NotificationHelper.scheduleDailyReminder(context, hour, minute)
                                    }
                                } else {
                                    isEnabled = false
                                    NotificationHelper.cancelReminder(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Purple,
                                checkedTrackColor = Purple.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // Time picker
            if (isEnabled) {
                item {
                    GlassCard {
                        Text("⏰ Время напоминания", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                GlassButton(
                                    text = "▲",
                                    onClick = { hour = (hour + 1) % 24; NotificationHelper.scheduleDailyReminder(context, hour, minute) },
                                    modifier = Modifier.width(60.dp),
                                    height = 36.dp,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(String.format("%02d", hour), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Purple)
                                Spacer(modifier = Modifier.height(4.dp))
                                GlassButton(
                                    text = "▼",
                                    onClick = { hour = if (hour > 0) hour - 1 else 23; NotificationHelper.scheduleDailyReminder(context, hour, minute) },
                                    modifier = Modifier.width(60.dp),
                                    height = 36.dp,
                                    fontSize = 14.sp
                                )
                            }

                            Text(" : ", fontSize = 36.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp))

                            // Minute
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                GlassButton(
                                    text = "▲",
                                    onClick = { minute = (minute + 5) % 60; NotificationHelper.scheduleDailyReminder(context, hour, minute) },
                                    modifier = Modifier.width(60.dp),
                                    height = 36.dp,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(String.format("%02d", minute), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Purple)
                                Spacer(modifier = Modifier.height(4.dp))
                                GlassButton(
                                    text = "▼",
                                    onClick = { minute = if (minute >= 5) minute - 5 else 55; NotificationHelper.scheduleDailyReminder(context, hour, minute) },
                                    modifier = Modifier.width(60.dp),
                                    height = 36.dp,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Test notification
            item {
                GlassButton(
                    text = if (testSent) "✅ Отправлено!" else "🔔 Тестовое уведомление",
                    onClick = {
                        NotificationHelper.showTestNotification(context)
                        testSent = true
                    },
                    accentColor = if (testSent) Green else Purple,
                    height = 48.dp
                )
                LaunchedEffect(testSent) {
                    if (testSent) {
                        kotlinx.coroutines.delay(2000)
                        testSent = false
                    }
                }
            }

            // About
            item {
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("GRIND v1.0", color = Color.White.copy(alpha = 0.5f))
                        Text("Тренировки с гантелями дома", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
