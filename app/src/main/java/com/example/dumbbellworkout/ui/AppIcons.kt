package com.example.dumbbellworkout.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*

/**
 * Централизованный реестр иконок приложения.
 * Заменяет эмодзи на vector-иконки Lucide.
 */
object AppIcons {
    // Bottom navigation
    val Home: ImageVector       = Lucide.House
    val Schedule: ImageVector   = Lucide.CalendarDays
    val Workout: ImageVector    = Lucide.Dumbbell
    val Stats: ImageVector      = Lucide.ChartColumn
    val Achievements: ImageVector = Lucide.Trophy

    // Quick actions
    val Bodyweight: ImageVector = Lucide.Scale
    val Charts: ImageVector     = Lucide.TrendingUp
    val HeatMap: ImageVector    = Lucide.LayoutGrid
    val History: ImageVector    = Lucide.ClipboardList
    val EditLog: ImageVector    = Lucide.Pencil
    val Settings: ImageVector   = Lucide.Settings

    // Home screen
    val Brain: ImageVector      = Lucide.Brain         // совет дня (🧠)
    val Quote: ImageVector      = Lucide.Quote
    val Play: ImageVector       = Lucide.Play
    val Rest: ImageVector       = Lucide.Moon          // день отдыха (😴)
    val Today: ImageVector      = Lucide.ClipboardCheck

    // Stats / streaks
    val Fire: ImageVector       = Lucide.Flame         // 🔥
    val Warning: ImageVector    = Lucide.TriangleAlert // ⚠️
    val Bolt: ImageVector       = Lucide.Zap           // ⚡
    val Crown: ImageVector      = Lucide.Crown         // 👑
    val Medal: ImageVector      = Lucide.Medal

    // Achievements
    val Check: ImageVector      = Lucide.Check         // ✅
    val Lock: ImageVector       = Lucide.Lock          // 🔒
    val Star: ImageVector       = Lucide.Star
    val Target: ImageVector     = Lucide.Target
    val Bell: ImageVector       = Lucide.Bell          // 🔔
    val Clock: ImageVector      = Lucide.Clock         // ⏰

    // Navigation
    val Back: ImageVector       = Lucide.ArrowLeft
    val ArrowUp: ImageVector    = Lucide.ChevronUp
    val ArrowDown: ImageVector  = Lucide.ChevronDown
}
