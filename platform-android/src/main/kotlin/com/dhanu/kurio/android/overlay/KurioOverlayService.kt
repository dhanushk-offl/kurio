package com.dhanu.kurio.android.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.android.accessibility.KurioAccessibilityService
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.theme.KurioTheme
import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.domain.repository.SettingsRepository
import com.dhanu.kurio.domain.repository.TranscriptionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KurioOverlayService : Service(), KoinComponent {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout
    private lateinit var composeView: ComposeView
    private var params: WindowManager.LayoutParams? = null

    private val transcriptionRepository: TranscriptionRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Internal overlay expansion state
    private val isExpandedFlow = MutableStateFlow(false)

    companion object {
        const val ACTION_START = "com.dhanu.kurio.ACTION_START"
        const val ACTION_STOP = "com.dhanu.kurio.ACTION_STOP"
        const val ACTION_TOGGLE = "com.dhanu.kurio.ACTION_TOGGLE"

        fun start(context: Context) {
            val intent = Intent(context, KurioOverlayService::class.java).apply {
                action = ACTION_START
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, KurioOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
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
        if (::overlayView.isInitialized && overlayView.parent != null) return

        overlayView = FrameLayout(this)

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
            y = 400
        }

        composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                KurioTheme {
                    OverlayContent()
                }
            }
        }

        overlayView.addView(composeView)
        setupDragListener()

        try {
            windowManager.addView(overlayView, params)
            Napier.d(tag = "Overlay") { "Overlay added to window manager" }
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "Overlay") { "Failed to show overlay" }
        }
    }

    private fun hideOverlay() {
        if (::overlayView.isInitialized && overlayView.parent != null) {
            try {
                windowManager.removeView(overlayView)
                Napier.d(tag = "Overlay") { "Overlay removed from window manager" }
            } catch (_: Exception) { }
        }
    }

    private fun toggleOverlay() {
        if (::overlayView.isInitialized && overlayView.parent != null) {
            hideOverlay()
        } else {
            showOverlay()
        }
    }

    private fun setupDragListener() {
        var isDragging = false
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        composeView.setOnTouchListener { view, event ->
            val layoutParams = params ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.abs(dx) > 15 || Math.abs(dy) > 15) {
                        isDragging = true
                    }
                    if (isDragging) {
                        layoutParams.x = initialX + dx.toInt()
                        layoutParams.y = initialY + dy.toInt()
                        try {
                            windowManager.updateViewLayout(overlayView, layoutParams)
                        } catch (_: Exception) { }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Small tap triggers toggle expansion
                        isExpandedFlow.value = !isExpandedFlow.value
                    } else {
                        // Snap smoothly to nearest screen side
                        val screenWidth = resources.displayMetrics.widthPixels
                        val viewWidth = view.width
                        val middle = screenWidth / 2
                        layoutParams.x = if (layoutParams.x + viewWidth / 2 < middle) 0 else screenWidth - viewWidth
                        try {
                            windowManager.updateViewLayout(overlayView, layoutParams)
                        } catch (_: Exception) { }
                    }
                    true
                }
                else -> false
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun OverlayContent() {
        val isExpanded by isExpandedFlow.collectAsState()
        val transcriptionState by transcriptionRepository.observeTranscriptionState().collectAsState(TranscriptionState.IDLE)
        val currentResult by transcriptionRepository.observeCurrentResult().collectAsState(null)

        Card(
            shape = if (isExpanded) RoundedCornerShape(16.dp) else CircleShape,
            colors = CardDefaults.cardColors(containerColor = KurioColors.Surface),
            modifier = Modifier
                .wrapContentSize()
                .shadow(elevation = 6.dp, shape = if (isExpanded) RoundedCornerShape(16.dp) else CircleShape)
                .clip(if (isExpanded) RoundedCornerShape(16.dp) else CircleShape)
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) with fadeOut(animationSpec = tween(150))
                },
                label = "OverlayAnimation"
            ) { expanded ->
                if (expanded) {
                    ExpandedPanelUI(
                        state = transcriptionState,
                        result = currentResult,
                        onCollapse = { isExpandedFlow.value = false },
                        onStartRecord = {
                            serviceScope.launch { transcriptionRepository.startRecording() }
                        },
                        onStopRecord = {
                            serviceScope.launch { transcriptionRepository.stopRecording() }
                        },
                        onCancelRecord = {
                            serviceScope.launch { transcriptionRepository.cancelRecording() }
                        }
                    )
                } else {
                    CollapsedBubbleUI(state = transcriptionState)
                }
            }
        }
    }

    @Composable
    fun CollapsedBubbleUI(state: TranscriptionState) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by if (state == TranscriptionState.LISTENING) {
            transition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
        } else {
            remember { mutableStateOf(1f) }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = when (state) {
                        TranscriptionState.LISTENING -> KurioColors.Error
                        TranscriptionState.PROCESSING -> KurioColors.Primary
                        else -> KurioColors.Primary
                    }
                )
        ) {
            // Pulse circle under mic icon
            if (state == TranscriptionState.LISTENING) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .scale(pulseScale)
                        .background(KurioColors.Error.copy(alpha = 0.3f), CircleShape)
                )
            }

            Text(
                text = "🎙️",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun ExpandedPanelUI(
        state: TranscriptionState,
        result: TranscriptionResult?,
        onCollapse: () -> Unit,
        onStartRecord: () -> Unit,
        onStopRecord: () -> Unit,
        onCancelRecord: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kurio Offline",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = KurioColors.PrimaryText,
                    modifier = Modifier.weight(1f)
                )

                // Drag indicator / close
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(KurioColors.SurfaceVariant, CircleShape)
                        .clickable { onCollapse() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        fontSize = 12.sp,
                        color = KurioColors.SecondaryText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Body depending on transcription state
            when (state) {
                TranscriptionState.IDLE -> {
                    Text(
                        text = "Tap Speak to transcribe and auto-inject text offline.",
                        fontSize = 13.sp,
                        color = KurioColors.SecondaryText,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onStartRecord,
                        colors = ButtonDefaults.buttonColors(containerColor = KurioColors.Primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Speak Now", color = Color.White)
                    }
                }

                TranscriptionState.LISTENING,
                TranscriptionState.VAD_DETECTING,
                TranscriptionState.VAD_SPEECH -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        WaveformAnimation()
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = when (state) {
                                TranscriptionState.VAD_DETECTING -> "Detecting speech..."
                                TranscriptionState.VAD_SPEECH -> "Speech detected"
                                else -> "Listening..."
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KurioColors.Error
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onCancelRecord,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KurioColors.SecondaryText),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = onStopRecord,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KurioColors.Error),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Finish", color = Color.White)
                        }
                    }
                }

                TranscriptionState.PROCESSING,
                TranscriptionState.POST_PROCESSING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = KurioColors.Primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Processing Speech Offline...",
                            fontSize = 13.sp,
                            color = KurioColors.PrimaryText
                        )
                    }
                }

                TranscriptionState.COMPLETED -> {
                    if (result != null && result.text.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .background(KurioColors.SurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = result.text,
                                fontSize = 13.sp,
                                color = KurioColors.PrimaryText,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Kurio Text", result.text)
                                    clipboard.setPrimaryClip(clip)
                                    onCollapse()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KurioColors.Primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Copy", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val accessibilityService = KurioAccessibilityService.getInstance()
                                    if (accessibilityService != null) {
                                        accessibilityService.injectText(result.text)
                                    } else {
                                        // Clipboard fallback if service not enabled
                                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Kurio Text", result.text)
                                        clipboard.setPrimaryClip(clip)
                                    }
                                    onCollapse()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KurioColors.Primary),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text("Inject", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "No speech detected. Please try again.",
                            fontSize = 13.sp,
                            color = KurioColors.SecondaryText
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onStartRecord,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KurioColors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }

                TranscriptionState.ERROR -> {
                    Text(
                        text = "An error occurred during transcription.",
                        fontSize = 13.sp,
                        color = KurioColors.Error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onStartRecord,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KurioColors.Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        }
    }

    @Composable
    fun WaveformAnimation() {
        val transition = rememberInfiniteTransition(label = "waveform")
        val heights = listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f).mapIndexed { index, base ->
            transition.animateFloat(
                initialValue = 0.2f,
                targetValue = base,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400 + index * 100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(24.dp)
        ) {
            heights.forEach { heightVal ->
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight(heightVal.value)
                        .background(KurioColors.Error, RoundedCornerShape(1.5.dp))
                )
            }
        }
    }


    override fun onDestroy() {
        hideOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }
}
