package com.example.dumbbellworkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp,
    height: Dp = 56.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val shape = RoundedCornerShape(16.dp)
    val alpha = if (enabled) 1f else 0.4f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.25f * alpha),
                        accentColor.copy(alpha = 0.10f * alpha)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.20f * alpha),
                        Color.White.copy(alpha = 0.05f * alpha)
                    )
                ),
                shape = shape
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = alpha),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlassQuickAction(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    // Разделяем emoji и текст
    val emoji = title.takeWhile { !it.isLetter() || it.code > 0x2000 }.trim()
    val label = title.dropWhile { !it.isLetter() || it.code > 0x2000 }.trim()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = shape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji.ifEmpty { "📌" },
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label.ifEmpty { title },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
