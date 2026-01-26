package com.example.voyager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyager.ui.theme.EmergencyCritical
import kotlinx.coroutines.delay

@Composable
fun SOSButton(
    onSOSTriggered: () -> Unit,
    isActive: Boolean,
    requireHoldToActivate: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    val holdDuration = 2000L // 2 seconds to activate

    // Pulsing animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed && requireHoldToActivate) {
            val startTime = System.currentTimeMillis()
            while (isPressed && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed.toFloat() / holdDuration).coerceIn(0f, 1f)

                if (holdProgress >= 1f) {
                    onSOSTriggered()
                    holdProgress = 0f
                }
                delay(16) // ~60fps
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (!isPressed) {
            // Reset progress when released
            holdProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (requireHoldToActivate) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (!requireHoldToActivate) {
                            onSOSTriggered()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer ring (progress indicator when holding)
        if (requireHoldToActivate && isPressed) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                drawCircle(
                    color = EmergencyCritical.copy(alpha = 0.3f),
                    radius = size.minDimension / 2 - strokeWidth / 2,
                    style = Stroke(width = strokeWidth)
                )

                drawArc(
                    color = EmergencyCritical,
                    startAngle = -90f,
                    sweepAngle = 360f * holdProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - strokeWidth,
                        size.height - strokeWidth
                    )
                )
            }
        }

        // Main SOS button
        Canvas(
            modifier = Modifier
                .size(if (isActive) 180.dp * pulseScale else 180.dp)
        ) {
            // Outer glow when active
            if (isActive) {
                drawCircle(
                    color = EmergencyCritical.copy(alpha = 0.3f),
                    radius = size.minDimension / 2
                )
            }

            // Main circle
            drawCircle(
                color = if (isActive || isPressed) EmergencyCritical else EmergencyCritical.copy(alpha = 0.9f),
                radius = size.minDimension / 2 - 10.dp.toPx()
            )

            // Inner circle
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = size.minDimension / 2 - 30.dp.toPx()
            )
        }

        // Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "SOS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (requireHoldToActivate && !isActive) {
                Text(
                    text = if (isPressed) "Hold..." else "Hold to activate",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            } else if (isActive) {
                Text(
                    text = "ACTIVE",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}