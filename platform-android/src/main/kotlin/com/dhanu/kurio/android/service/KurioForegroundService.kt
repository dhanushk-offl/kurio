package com.dhanu.kurio.android.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.dhanu.kurio.android.notification.KurioNotificationManager
import io.github.aakira.napier.Napier

class KurioForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "com.dhanu.kurio.service.START"
        const val ACTION_STOP = "com.dhanu.kurio.service.STOP"

        fun start(context: Context) {
            val intent = Intent(context, KurioForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "ForegroundService") { "Failed to start foreground service" }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, KurioForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "ForegroundService") { "Failed to stop foreground service" }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundService()
            ACTION_STOP -> stopForegroundService()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        Napier.d(tag = "ForegroundService") { "Starting foreground service with microphone type" }
        val notification = KurioNotificationManager.createForegroundNotification(
            context = this,
            title = "Kurio Active",
            text = "Speech-to-text recording is running"
        )
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "ForegroundService") { "Error in startForeground" }
        }
    }

    private fun stopForegroundService() {
        Napier.d(tag = "ForegroundService") { "Stopping foreground service" }
        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "ForegroundService") { "Error stopping service" }
        }
    }
}
