package com.dhanu.kurio.android.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object KurioPermissions {
    fun hasMicrophonePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasOverlayPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return Settings.canDrawOverlays(context)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

@Composable
fun rememberMicrophonePermissionLauncher(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) onGranted() else onDenied()
}.apply {
    val context = LocalContext.current
    if (!KurioPermissions.hasMicrophonePermission(context)) {
        launch(Manifest.permission.RECORD_AUDIO)
    }
}

@Composable
fun MicrophonePermissionEffect(
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onResult(isGranted)
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!KurioPermissions.hasMicrophonePermission(context)) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            onResult(true)
        }
    }
}

@Composable
fun OverlayPermissionEffect(
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onResult(KurioPermissions.hasOverlayPermission(context))
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!KurioPermissions.hasOverlayPermission(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            launcher.launch(intent)
        } else {
            onResult(true)
        }
    }
}

@Composable
fun RequestMicrophonePermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) onGranted() else onDenied()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!KurioPermissions.hasMicrophonePermission(context)) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            onGranted()
        }
    }
}
