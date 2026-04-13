package com.example.dumbbellworkout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val unlocked = remember { AchievementsManager.getUnlocked(context) }
    val progress = remember { AchievementsManager.getProgress(context) }
    val unlockedCount = unlocked.size
    val totalCount = AchievementsManager.ALL_ACHIEVEMENTS.size

    // Check for newly unlocked
    var newlyUnlocked by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var showBadgeAnimation by remember { mutableStateOf(false) }
    var animatingAchievement by remember { mutableStateOf<Achievement?>(null) }

    LaunchedEffect(Unit) {
        val newOnes = AchievementsManager.checkAndUnlock(context)
        if (newOnes.isNotEmpty()) {
            newlyUnlocked = newOnes
            // Animate first one
            animatingAchievement = newOnes.first()
            showBadgeAnimation = true
        }
    }

    // Badge unlock overlay
    if (showBadgeAnimation && animatingAchievement != null) {
        BadgeUnlockOverlay(
            achievement = animatingAchievement!!,
            onDismiss = {
                showBadgeAnimation = false
                // Show next if available
                val remaining = newlyUnlocked.drop(1)
                newlyUnlocked = remaining
                if (remaining.isNotEmpty()) {
                    animatingAchievement = remaining.first()
                    showBadgeAnimation = true
                } else {
                    animatingAchievement = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Достижения", fontWeight = FontWeight.Bold) },
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
            item(key = "summary") {
                GlassCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$unlockedCount / $totalCount", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Purple)
                        Text("достижений разблокировано", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { unlockedCount.toFloat() / totalCount.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Purple, trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }
            }

            val unlockedAchievements = AchievementsManager.ALL_ACHIEVEMENTS.filter { it.id in unlocked }
            val lockedAchievements = AchievementsManager.ALL_ACHIEVEMENTS.filter { it.id !in unlocked }

            if (unlockedAchievements.isNotEmpty()) {
                item(key = "unlocked_header") {
                    Text("✅ Разблокированные", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Green)
                }
                items(unlockedAchievements, key = { "unlocked_${it.id}" }) { achievement ->
                    AnimatedAchievementCard(achievement = achievement, isUnlocked = true, progress = null)
                }
            }

            if (lockedAchievements.isNotEmpty()) {
                item(key = "locked_header") {
                    Text("🔒 В процессе", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White.copy(alpha = 0.5f))
                }
                items(lockedAchievements, key = { "locked_${it.id}" }) { achievement ->
                    val prog = progress[achievement.id]
                    AnimatedAchievementCard(achievement = achievement, isUnlocked = false, progress = prog)
                }
            }
        }
    }
}

@Composable
fun AnimatedAchievementCard(achievement: Achievement, isUnlocked: Boolean, progress: Pair<Int, Int>?) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "card_scale"
    )

    val shape = RoundedCornerShape(16.dp)
    val accentColor = if (isUnlocked) Purple else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = if (isUnlocked) 0.12f else 0.04f),
                        accentColor.copy(alpha = if (isUnlocked) 0.04f else 0.02f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = if (isUnlocked) 0.3f else 0.08f),
                        accentColor.copy(alpha = if (isUnlocked) 0.1f else 0.03f)
                    )
                ),
                shape
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Animated icon
            val iconScale by animateFloatAsState(
                targetValue = if (visible && isUnlocked) 1f else 0.7f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 200f),
                label = "icon_scale"
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .scale(iconScale)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Purple.copy(alpha = 0.15f)
                        else Color.White.copy(alpha = 0.03f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(achievement.icon, fontSize = 28.sp, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                )
                Text(
                    achievement.description,
                    fontSize = 12.sp,
                    color = if (isUnlocked) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f)
                )
                if (!isUnlocked && progress != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { (progress.first.toFloat() / progress.second.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.weight(1f).height(4.dp),
                            color = Purple, trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${progress.first}/${progress.second}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                }
                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✅ Выполнено!", fontSize = 12.sp, color = Green)
                }
            }
        }
    }
}

// ===== BADGE UNLOCK OVERLAY =====
@Composable
fun BadgeUnlockOverlay(achievement: Achievement, onDismiss: () -> Unit) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        show = true
        delay(3500)
        show = false
        delay(400)
        onDismiss()
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            // Particle effect
            ParticleExplosion()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Animated icon
                val iconScale by animateFloatAsState(
                    targetValue = if (show) 1f else 0f,
                    animationSpec = spring(dampingRatio = 0.3f, stiffness = 150f),
                    label = "badge_icon"
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Purple.copy(alpha = 0.3f), Purple.copy(alpha = 0.05f))
                            )
                        )
                        .border(2.dp, Purple.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(achievement.icon, fontSize = 56.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("🎉 Достижение разблокировано!", fontSize = 14.sp, color = Purple, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(achievement.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(achievement.description, fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun ParticleExplosion() {
    data class Particle(val angle: Float, val speed: Float, val size: Float, val color: Color, val delay: Long)

    val particles = remember {
        List(40) {
            Particle(
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 300f + 100f,
                size = Random.nextFloat() * 8f + 3f,
                color = listOf(Purple, Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFF4CAF50), Color.White).random(),
                delay = Random.nextLong(0, 300)
            )
        }
    }

    val animProgress by rememberInfiniteTransition(label = "particles").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "particle_progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val dist = p.speed * animProgress
            val x = center.x + (cos(rad) * dist).toFloat()
            val y = center.y + (sin(rad) * dist).toFloat()
            val alpha = (1f - animProgress).coerceIn(0f, 1f)

            drawCircle(
                color = p.color.copy(alpha = alpha * 0.8f),
                radius = p.size * (1f - animProgress * 0.5f),
                center = Offset(x, y)
            )
        }
    }
}
