package com.example.voyager.ui.screens


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyager.ui.theme.VoyagerColors

@Composable
fun EmergencyModeScreen(
    onCancel: () -> Unit
) {
    // Pulsing animation for SOS button
    val infiniteTransition = rememberInfiniteTransition(label = "sosButton")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sosScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VoyagerColors.CreamBackground,
                        VoyagerColors.LightBeige
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Emergency Header
            EmergencyHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Status Card
            EmergencyStatusCard()

            Spacer(modifier = Modifier.weight(1f))

            // SOS Button
            SOSButton(
                scale = scale,
                onClick = { /* Trigger SOS */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Actions
            EmergencyActionsRow()

            Spacer(modifier = Modifier.height(32.dp))

            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VoyagerColors.DarkCharcoal
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            VoyagerColors.MediumGray,
                            VoyagerColors.MediumGray
                        )
                    )
                )
            ) {
                Text(
                    text = "Cancel Emergency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmergencyHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warning icon with subtle pulsing
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(VoyagerColors.EmergencyRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Emergency",
                tint = VoyagerColors.EmergencyRed,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Emergency Mode Active",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = VoyagerColors.DarkCharcoal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your emergency contacts have been notified",
            style = MaterialTheme.typography.bodyLarge,
            color = VoyagerColors.MediumGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmergencyStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = VoyagerColors.WarmIvory
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Danger Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = VoyagerColors.MediumGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DANGER",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VoyagerColors.EmergencyRed
                    )
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(VoyagerColors.EmergencyRed)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Divider(
                color = VoyagerColors.MediumGray.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = VoyagerColors.DarkCharcoal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Last Known Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = VoyagerColors.MediumGray
                    )
                    Text(
                        text = "Downtown Area, City Center",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = VoyagerColors.DarkCharcoal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sharing status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VoyagerColors.SafeGreen.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(VoyagerColors.SafeGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Location sharing active",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = VoyagerColors.SafeGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun SOSButton(
    scale: Float,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(180.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = VoyagerColors.EmergencyRed.copy(alpha = 0.4f),
                spotColor = VoyagerColors.EmergencyRed.copy(alpha = 0.4f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = VoyagerColors.EmergencyRed
        ),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "SOS",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SOS",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color.White
            )
            Text(
                text = "Call Emergency",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun EmergencyActionsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmergencyActionCard(
            title = "Share Location",
            icon = "📍",
            onClick = { /* Share location */ },
            modifier = Modifier.weight(1f)
        )

        EmergencyActionCard(
            title = "Call Contact",
            icon = "📞",
            onClick = { /* Call emergency contact */ },
            modifier = Modifier.weight(1f)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyActionCard(
    title: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = VoyagerColors.WarmIvory
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = VoyagerColors.DarkCharcoal,
                textAlign = TextAlign.Center
            )
        }
    }
}