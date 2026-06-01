package com.dhanu.kurio.android.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import io.github.aakira.napier.Napier

object TextInjectionManager {

    /**
     * Injects text into the currently focused EditText.
     * Respects security rules (never injects into password fields).
     * Tries ACTION_SET_TEXT first, then falls back to copying to clipboard and performing ACTION_PASTE.
     */
    fun inject(context: Context, service: AccessibilityService, text: String): Boolean {
        val rootNode = service.rootInActiveWindow
        if (rootNode == null) {
            Napier.w(tag = "TextInjectionManager") { "Root node in active window is null. Cannot inject." }
            return false
        }

        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focusedNode == null) {
            Napier.w(tag = "TextInjectionManager") { "No active focused input field detected." }
            rootNode.recycle()
            return false
        }

        // Security check: Ignore password fields
        if (focusedNode.isPassword) {
            Napier.w(tag = "TextInjectionManager") { "Text injection cancelled: target is a password field." }
            focusedNode.recycle()
            rootNode.recycle()
            return false
        }

        var success = false

        // Method 1: ACTION_SET_TEXT
        try {
            val bundle = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
            if (success) {
                Napier.d(tag = "TextInjectionManager") { "Successfully injected text via ACTION_SET_TEXT" }
            }
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "TextInjectionManager") { "Error during ACTION_SET_TEXT execution" }
        }

        // Method 2: Clipboard & Paste Fallback
        if (!success) {
            Napier.d(tag = "TextInjectionManager") { "ACTION_SET_TEXT failed, attempting clipboard paste fallback" }
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Kurio Transcription", text)
                clipboard.setPrimaryClip(clip)

                success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                if (success) {
                    Napier.d(tag = "TextInjectionManager") { "Successfully pasted text via clipboard fallback" }
                } else {
                    Napier.w(tag = "TextInjectionManager") { "Clipboard paste action returned failure status" }
                }
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "TextInjectionManager") { "Failed to paste via clipboard fallback" }
            }
        }

        focusedNode.recycle()
        rootNode.recycle()
        return success
    }
}
