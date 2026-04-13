package com.example.dumbbellworkout

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiPiece(
    val startX: Float,
    val speed: Float,
    val amplitude: Float,
    val frequency: Float,
    val size: Float,
    val color: Color,
    val delay: Float
)

@Composable
fun ConfettiEffect(isActive: Boolean) {
    if (!isActive) return

    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }

    val colors = listOf(Purple, Red, Green, Orange, Color(0xFFFFD700), Color(0xFF00BCD4))

    // 25 вместо 60
    val pieces = remember {
        List(25) {
            ConfettiPiece(
                startX = Random.nextFloat() * screenWidth,
                speed = 0.3f + Random.nextFloat() * 0.7f,
                amplitude = 20f + Random.nextFloat() * 40f,
                frequency = 1f + Random.nextFloat() * 3f,
                size = 4f + Random.nextFloat() * 6f,
                color = colors[Random.nextInt(colors.size)],
                delay = Random.nextFloat() * 0.5f
            )
        }
    }

    // Одиночная анимация вместо бесконечной — 3 секунды и стоп
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(3000, easing = LinearEasing),
        label = "confetti"
    )

    LaunchedEffect(Unit) { progress = 1f }

    if (animatedProgress >= 0.99f) return // прекращаем рисовать

    Canvas(modifier = Modifier.fillMaxSize()) {
        pieces.forEach { piece ->
            val adjustedProgress = ((animatedProgress + piece.delay) % 1f)
            val y = adjustedProgress * (screenHeight + 100f) - 50f
            val x = piece.startX + sin((adjustedProgress * piece.frequency * Math.PI * 2).toDouble()).toFloat() * piece.amplitude

            drawCircle(
                color = piece.color.copy(alpha = (1f - adjustedProgress).coerceIn(0f, 1f)),
                radius = piece.size,
                center = Offset(x, y)
            )
        }
    }
}
