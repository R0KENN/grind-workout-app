package com.example.dumbbellworkout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.dumbbellworkout.ui.AppIcons

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,   // ← было String
    val label: String,
    val isCenter: Boolean = false
)

@Composable
fun GlassBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onStartWorkout: () -> Unit
) {
    val (haptic, view) = rememberHaptics()
    val items = listOf(
        BottomNavItem("home", AppIcons.Home, "Главная"),
        BottomNavItem("schedule", AppIcons.Schedule, "Расписание"),
        BottomNavItem("workout", AppIcons.Workout, "Старт", isCenter = true),
        BottomNavItem("stats", AppIcons.Stats, "Статистика"),
        BottomNavItem("achievements", AppIcons.Achievements, "Ачивки")
    )

    val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = shape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { item ->
                if (item.isCenter) {
                    // Center button - no pulse, clean circle
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = (-16).dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Purple, Purple.copy(alpha = 0.7f))
                                    )
                                )
                                .border(
                                    2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.3f),
                                            Color.White.copy(alpha = 0.1f)
                                        )
                                    ),
                                    CircleShape
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    Haptics.heavy(view, haptic)
                                    onStartWorkout()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.dumbbellworkout.ui.AppIcon(
                                icon = item.icon,
                                contentDescription = item.label,
                                size = 26.dp,
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    val isSelected = currentRoute == item.route

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = 0.6f,
                            stiffness = 300f
                        ),
                        label = "nav_scale"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Purple else Color.White.copy(alpha = 0.4f),
                        animationSpec = tween(300),
                        label = "nav_color"
                    )

                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "indicator"
                    )

                    val bgAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 0.08f else 0f,
                        animationSpec = tween(300),
                        label = "nav_bg"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple.copy(alpha = bgAlpha))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                Haptics.click(haptic)
                                onNavigate(item.route)
                            }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = textColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(animatedScale)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(indicatorWidth)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Purple)
                        )
                    }
                }
            }
        }
    }
}
