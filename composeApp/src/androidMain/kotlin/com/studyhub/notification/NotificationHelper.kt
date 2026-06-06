package com.studyhub.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.studyhub.MainActivity
import com.studyhub.R

object NotificationChannels {
    const val CHANNEL_REMINDER = "studyhub_reminder"
    const val CHANNEL_REMINDER_NAME = "Smart Reminder"
}

object NotificationIds {
    const val BASE_REMINDER = 1000
}

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            NotificationChannels.CHANNEL_REMINDER,
            NotificationChannels.CHANNEL_REMINDER_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat tugas dari AI StudyHub"
            enableLights(true)
            lightColor = Color.parseColor("#8B7355")
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250)
            setShowBadge(true)
        }

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun buildReminderNotification(
    context: Context,
    taskId: String,
    taskTitle: String,
    taskSubject: String,
    aiReason: String
): Notification {
    // Intent to open TaskDetail when tapped
    val openIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("taskId", taskId)
        putExtra("openScreen", "task_detail")
    }
    val openPendingIntent = PendingIntent.getActivity(
        context, taskId.hashCode(), openIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or
        PendingIntent.FLAG_IMMUTABLE
    )

    // Action: Mark as done
    val doneIntent = Intent(
        context, ReminderBroadcastReceiver::class.java
    ).apply {
        action = "ACTION_MARK_DONE"
        putExtra("taskId", taskId)
    }
    val donePendingIntent = PendingIntent.getBroadcast(
        context, taskId.hashCode() + 1, doneIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or
        PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(
        context, NotificationChannels.CHANNEL_REMINDER
    )
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("📚 $taskTitle")
        .setContentText("$taskSubject · $aiReason")
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText("Mata kuliah: $taskSubject\n" +
                "AI: $aiReason")
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setContentIntent(openPendingIntent)
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_check,
            "Tandai Selesai",
            donePendingIntent
        )
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .build()
}
