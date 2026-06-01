package com.dhanu.kurio.android.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.github.aakira.napier.Napier

class KurioAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: KurioAccessibilityService? = null

        fun getInstance(): KurioAccessibilityService? {
            return instance
        }

        fun isServiceRunning(): Boolean {
            return instance != null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Napier.d(tag = "AccessibilityService") { "KurioAccessibilityService Connected" }

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op. We dynamically query the focused node via findFocus(FOCUS_INPUT) 
        // at the time of text injection to ensure correctness and avoid memory leaks.
    }

    override fun onInterrupt() {
        Napier.d(tag = "AccessibilityService") { "KurioAccessibilityService Interrupted" }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        Napier.d(tag = "AccessibilityService") { "KurioAccessibilityService Unbound" }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Injects text into the currently focused EditText node.
     * Delegates logic directly to TextInjectionManager.
     */
    fun injectText(text: String): Boolean {
        return TextInjectionManager.inject(this, this, text)
    }
}
