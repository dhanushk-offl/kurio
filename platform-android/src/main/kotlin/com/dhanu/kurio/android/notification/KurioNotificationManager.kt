package com.dhanu.kurio.android.notification

import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dhanu.kurio.android.R

object KurioNotificationManager {
    private const val CHANNEL_ID = "kurio_transcription"
    private const val CHANNEL_NAME = "Transcription"
    private const val FOREGROUND_NOTIFICATION_ID = 1001
    private const val UPDATE_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Transcription service notifications"
                setShowBadge(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun createForegroundNotification(
        context: Context,
        title: String,
        text: String
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
