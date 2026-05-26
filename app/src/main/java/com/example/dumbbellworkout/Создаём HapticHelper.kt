package com.example.dumbbellworkout

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView

/**
 * Обёртка над тактильной отдачей.
 * Использует Compose HapticFeedback API для лёгких импульсов
 * и системные HapticFeedbackConstants для более выразительных эффектов (API 30+).
 *
 * Для "сильных" событий (рекорд, конец отдыха) продолжаем использовать VibrationHelper —
 * там нам нужен полноценный паттерн вибрации, а не короткий тактильный импульс.
 */
object Haptics {

    /** Лёгкий "тик" — для тапа по обычной кнопке. */
    fun click(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /** Более выраженный отклик — для подтверждения действия (отметка подхода готовым). */
    fun confirm(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /**
     * Жёсткий "тик" через системный API — для значимых действий (старт тренировки, завершение).
     * Доступен на API 30+, на старых API делаем fallback на LongPress через Compose.
     */
    fun heavy(view: View, haptic: HapticFeedback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Отрицательный отклик — для ошибки/отмены/удаления подхода. */
    fun reject(view: View, haptic: HapticFeedback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

/**
 * Удобный аксессор для использования внутри Composable.
 * Возвращает пару (haptic, view) — оба нужны, потому что часть эффектов
 * работает только через нативный View.performHapticFeedback (API 30+).
 *
 * Использование:
 *   val (haptic, view) = rememberHaptics()
 *   onClick = { Haptics.click(haptic); ... }
 */
@Composable
fun rememberHaptics(): Pair<HapticFeedback, View> {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val view = LocalView.current
    return haptic to view
}
