package com.example.dumbbellworkout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedListItem(
    index: Int,
    delayPerItem: Long = 80L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * delayPerItem)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 30.dp,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .alpha(alpha)
            .offset(y = offsetY)
    ) {
        content()
    }
}

@Composable
fun AnimatedScale(
    delay: Long = 0L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay)
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
    }
}

@Composable
fun PulseEffect(
    pulseFraction: Float = 1.05f,
    duration: Int = 1500,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseFraction,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

@Composable
fun AnimatedCounter(
    targetValue: Int,
    duration: Int = 1000
): Int {
    var oldValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(duration, easing = FastOutSlowInEasing),
        label = "counter"
    )

    LaunchedEffect(targetValue) {
        oldValue = targetValue
    }

    return animatedValue
}

@Composable
fun AnimatedFloatCounter(
    targetValue: Float,
    duration: Int = 1000
): Float {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(duration, easing = FastOutSlowInEasing),
        label = "float_counter"
    )
    return animatedValue
}
