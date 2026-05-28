package com.example.dumbbellworkout.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Brain
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.ChartBar
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.ClipboardCheck
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Medal
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Quote
import com.composables.icons.lucide.Scale
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.TriangleAlert
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.Zap

/**
 * Централизованный реестр иконок приложения.
 * Заменяет эмодзи на vector-иконки Lucide.
 * Источник имён: https://lucide.dev/icons/
 */
object AppIcons {
    // Bottom navigation
    val Home: ImageVector         = Lucide.House
    val Schedule: ImageVector     = Lucide.CalendarDays
    val Workout: ImageVector      = Lucide.Dumbbell
    val Stats: ImageVector        = Lucide.ChartBar
    val Achievements: ImageVector = Lucide.Trophy

    // Quick actions
    val Bodyweight: ImageVector = Lucide.Scale
    val Charts: ImageVector     = Lucide.TrendingUp
    val HeatMap: ImageVector    = Lucide.LayoutGrid
    val History: ImageVector    = Lucide.ClipboardList
    val EditLog: ImageVector    = Lucide.Pencil
    val Settings: ImageVector   = Lucide.Settings

    // Home screen
    val Brain: ImageVector = Lucide.Brain         // совет дня (🧠)
    val Quote: ImageVector = Lucide.Quote
    val Play: ImageVector  = Lucide.Play
    val Rest: ImageVector  = Lucide.Moon          // день отдыха (😴)
    val Today: ImageVector = Lucide.ClipboardCheck

    // Stats / streaks
    val Fire: ImageVector    = Lucide.Flame         // 🔥
    val Warning: ImageVector = Lucide.TriangleAlert // ⚠️
    val Bolt: ImageVector    = Lucide.Zap           // ⚡
    val Crown: ImageVector   = Lucide.Crown         // 👑
    val Medal: ImageVector   = Lucide.Medal

    // Achievements
    val Check: ImageVector  = Lucide.Check          // ✅
    val Lock: ImageVector   = Lucide.Lock           // 🔒
    val Star: ImageVector   = Lucide.Star
    val Target: ImageVector = Lucide.Target
    val Bell: ImageVector   = Lucide.Bell           // 🔔
    val Clock: ImageVector  = Lucide.Clock          // ⏰

    // Navigation
    val Back: ImageVector      = Lucide.ArrowLeft
    val ArrowUp: ImageVector   = Lucide.ChevronUp
    val ArrowDown: ImageVector = Lucide.ChevronDown
}
