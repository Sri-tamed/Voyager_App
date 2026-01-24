package com.example.voyager.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyager.ui.theme.CustomShapes
import com.example.voyager.ui.theme.DangerRed
import com.example.voyager.ui.theme.EmergencyCritical
import kotlinx.coroutines.delay

@Composable
fun SOSButton(
    onSOSTriggered: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    requireHoldToActivate: Boolean = true,
    holdDurationMillis: Long = 2000L
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }
    val interactionSource = remember { MutableInteractionSource() }

    // Pulsing animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse scale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed && requireHoldToActivate) {
            val startTime = System.currentTimeMillis()
            while (isPressed) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed.toFloat() / holdDurationMillis).coerceIn(0f, 1f)

                if (holdProgress >= 1f) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSOSTriggered()
                    isPressed = false
                    break
                }
                delay(16) // ~60fps
            }
            holdProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .size(200.dp)
            .scale(if (isActive) pulseScale else 1f),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring (progress indicator when holding)
        if (requireHoldToActivate && isPressed) {
            CircularProgressIndicator(
                progress = { holdProgress },
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                strokeWidth = 6.dp,
                trackColor = DangerRed.copy(alpha = 0.3f)
            )
        }

        // Main SOS button
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CustomShapes.SOSButton)
                .background(if (isActive) EmergencyCritical else DangerRed)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (!requireHoldToActivate) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSOSTriggered()
                        }
                    }
                )
                .then(
                    if (requireHoldToActivate) {
                        Modifier.pressGestureHelper(
                            onPress = { isPressed = true },
                            onRelease = { isPressed = false }
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                if (requireHoldToActivate && !isPressed) {
                    Text(
                        text = "Hold to activate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// Helper modifier for press/release detection
fun Modifier.pressGestureHelper(
    onPress: () -> Unit,
    onRelease: () -> Unit
) = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            when (event.type) {
                PointerEventType.Press -> onPress()
                PointerEventType.Release -> onRelease()
            }
        }
    }
}