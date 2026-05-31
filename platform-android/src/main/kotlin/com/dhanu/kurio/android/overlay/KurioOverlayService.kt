package com.dhanu.kurio.android.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import io.github.aakira.napier.Napier

class KurioOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout
    private var isExpanded = false
    private var isListening = false
    private var params: WindowManager.LayoutParams? = null

    companion object {
        const val ACTION_START = "com.dhanu.kurio.ACTION_START"
        const val ACTION_STOP = "com.dhanu.kurio.ACTION_STOP"
        const val ACTION_TOGGLE = "com.dhanu.kurio.ACTION_TOGGLE"
        const val EXTRA_STATE = "state"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> showOverlay()
            ACTION_STOP -> hideOverlay()
            ACTION_TOGGLE -> toggleOverlay()
        }
        return START_STICKY
    }

    private fun showOverlay() {
        if (::overlayView.isInitialized) return

        overlayView = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        try {
            windowManager.addView(overlayView, params)
            Napier.d("Overlay") { "Overlay shown" }
        } catch (e: Exception) {
            Napier.e("Overlay", throwable = e) { "Failed to show overlay" }
        }
    }

    private fun hideOverlay() {
        if (::overlayView.isInitialized) {
            try {
                windowManager.removeView(overlayView)
            } catch (_: Exception) { }
        }
    }

    private fun toggleOverlay() {
        if (::overlayView.isInitialized) {
            hideOverlay()
        } else {
            showOverlay()
        }
    }

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }
}
