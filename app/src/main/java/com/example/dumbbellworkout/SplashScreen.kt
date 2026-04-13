package com.example.dumbbellworkout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.ImageLoader
import android.os.Build
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }

    // Phase 0: nothing (0ms)
    // Phase 1: glow appears (200ms)
    // Phase 2: icon appears (500ms)
    // Phase 3: text appears (900ms)
    // Phase 4: subtitle appears (1200ms)
    // Phase 5: navigate (2500ms)

    LaunchedEffect(Unit) {
        delay(200); phase = 1
        delay(300); phase = 2
        delay(400); phase = 3
        delay(300); phase = 4
        delay(1300); phase = 5
        onFinished()
    }

    // Glow animation
    val glowAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 0.3f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "glow"
    )

    // Icon animation
    val iconScale by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "icon_alpha"
    )

    // Title animation
    val titleAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "title_alpha"
    )

    val titleOffset by animateFloatAsState(
        targetValue = if (phase >= 3) 0f else 30f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "title_offset"
    )


    // Breathing glow
    val infiniteTransition = rememberInfiniteTransition(label = "glow_breathe")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Glow behind icon
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(glowScale)
                .alpha(glowAlpha)
                .blur(60.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Purple.copy(alpha = 0.6f),
                            Purple.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(iconScale)
                    .alpha(iconAlpha)
            ) {
                val context = LocalContext.current
                val imageLoader = remember {
                    ImageLoader.Builder(context)
                        .components {
                            if (Build.VERSION.SDK_INT >= 28) {
                                add(ImageDecoderDecoder.Factory())
                            } else {
                                add(GifDecoder.Factory())
                            }
                        }
                        .build()
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("android.resource://${context.packageName}/${R.mipmap.ic_launcher_foreground}")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Logo",
                    imageLoader = imageLoader,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title "GRIND"
            Text(
                text = "GRIND",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(titleAlpha)
                    .offset(y = titleOffset.dp)
            )

        }
    }
}
