package com.example.dumbbellworkout

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (NotificationHelper.isEnabled(context)) {
                val h = NotificationHelper.getSavedHour(context)
                val m = NotificationHelper.getSavedMinute(context)
                NotificationHelper.scheduleDailyReminder(context, h, m)
                MotivationalNotifications.schedule(context, h, m)
            }
        }
    }
}
