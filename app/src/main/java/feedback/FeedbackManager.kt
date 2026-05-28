package com.example.dumbbellworkout.feedback

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class FeedbackType {
    Tick,        // короткий клик (выбор)
    Success,     // подход завершён
    Heavy,       // тренировка завершена / достижение
    Warning,     // конец отдыха
    Error
}

@Singleton
class FeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun perform(type: FeedbackType) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        val effect = when (type) {
            FeedbackType.Tick    -> VibrationEffect.createOneShot(20, 80)
            FeedbackType.Success -> VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1)
            FeedbackType.Heavy   -> VibrationEffect.createOneShot(120, 255)
            FeedbackType.Warning -> VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 60, 80, 60), -1)
            FeedbackType.Error   -> VibrationEffect.createOneShot(200, 200)
        }
        v.vibrate(effect)
    }
}
