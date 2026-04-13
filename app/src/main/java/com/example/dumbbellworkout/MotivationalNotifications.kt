package com.example.dumbbellworkout

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class MotivationalReceiver : BroadcastReceiver() {

    private val messages = listOf(
        "💪 Время GRIND! Сегодня ты станешь сильнее.",
        "🔥 Не пропускай тренировку — серия на кону!",
        "🏆 Каждый подход приближает тебя к цели.",
        "⚡ Твоё тело ждёт нагрузку. Погнали!",
        "🎯 Помни: дисциплина бьёт мотивацию.",
        "💥 Вчерашний ты будет гордиться сегодняшним.",
        "🦾 Ты уже не тот, что месяц назад. Продолжай!",
        "🔱 Легенды не пропускают тренировки.",
        "🚀 Один час тренировки = 24 часа хорошего настроения.",
        "👑 Ты сильнее, чем думаешь. Докажи это сегодня."
    )

    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "grind_motivation"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Мотивация GRIND", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Мотивационные напоминания о тренировках"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val todayWorkout = getTodayWorkout()
        val workoutInfo = if (todayWorkout.id != "rest") {
            "Сегодня: ${todayWorkout.name}"
        } else {
            "Сегодня день отдыха. Восстанавливайся!"
        }

        val streak = StreakManager.getCurrentStreak(context)
        val streakText = if (streak > 0) " | 🔥 Серия: $streak дн." else ""

        val randomMessage = messages.random()

        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(randomMessage)
            .setContentText("$workoutInfo$streakText")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$workoutInfo$streakText"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

object MotivationalNotifications {

    fun schedule(context: Context, hour: Int = 10, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MotivationalReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MotivationalReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
