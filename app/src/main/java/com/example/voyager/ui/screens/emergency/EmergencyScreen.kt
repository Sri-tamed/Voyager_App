package com.example.voyager.ui.screens.emergency

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voyager.emergency.EmergencyManager
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.data.model.EmergencyContact
import com.example.voyager.data.model.EmergencyEvent
import com.example.voyager.ui.theme.VoyagerColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * INTEGRATED Emergency Screen - Fixed for compilation
 * Combines your beautiful UI with system-level reliability
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EmergencyScreen(
    emergencyManager: EmergencyManager, // Direct injection
    onCancel: () -> Unit,
    viewModel: EmergencyViewModel = viewModel(
        factory = EmergencyViewModelFactory(emergencyManager)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showContacts by remember { mutableStateOf(false) }

    // Permission handling
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
        )
    )

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        viewModel.updatePermissionsState(permissionsState.allPermissionsGranted)
    }

    // Bug 1 fix: Collect one-shot UI events from the ViewModel and launch intents.
    // Uses LocalContext.current (Compose best practice) and includes try/catch + println logs.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            try {
                when (event) {
                    is EmergencyUiEvent.OpenSmsApp -> {
                        println("EmergencyScreen: OpenSmsApp phones=${event.phoneNumbers.size}")
                        val smsUri = if (event.phoneNumbers.isNotEmpty()) {
                            // ACTION_SENDTO + sms:NUMBER is the most reliable way to open SMS app with recipients.
                            Uri.parse("smsto:${event.phoneNumbers.joinToString(separator = ";")}")
                        } else {
                            Uri.parse("smsto:")
                        }

                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = smsUri
                            putExtra("sms_body", event.message)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        ContextCompat.startActivity(context, intent, null)
                    }

                    is EmergencyUiEvent.OpenDialer -> {
                        println("EmergencyScreen: OpenDialer number=${event.phoneNumber}")
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${event.phoneNumber}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        ContextCompat.startActivity(context, intent, null)
                    }
                }
            } catch (e: Exception) {
                // Keep UI intact; just log for debugging.
                println("EmergencyScreen: failed to handle event=$event error=${e.message}")
            }
        }
    }

    // If user is managing contacts from within emergency mode, show that screen.
    if (showContacts) {
        EmergencyContactsScreen(
            emergencyManager = emergencyManager,
            onBack = { showContacts = false }
        )
        return
    }

    // Show permission request if needed for main emergency mode
    if (!permissionsState.allPermissionsGranted) {
        PermissionRequestScreen(
            onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() },
            onCancel = onCancel
        )
        return
    }

    // Main Emergency UI
    if (uiState.contacts.isEmpty()) {
        NoContactsScreen(
            // Bug: previously did nothing. Now opens inline contacts manager.
            onAddContacts = { showContacts = true },
            onCancel = onCancel
        )
    } else {
        EmergencyModeScreenIntegrated(
            uiState = uiState,
            onSosClick = { viewModel.triggerSos(DangerLevel.HIGH) },
            // Bug 1 fix: Wire buttons to real ViewModel handlers.
            onShareLocation = { viewModel.shareLocation() },
            onCallPrimaryContact = { contact -> viewModel.callContact(contact) },
            onManageContacts = { showContacts = true },
            onCancel = {
                viewModel.stopEmergencyMode()
                onCancel()
            }
        )
    }
}

@Composable
private fun EmergencyModeScreenIntegrated(
    uiState: EmergencyUiState,
    onSosClick: () -> Unit,
    onShareLocation: () -> Unit,
    onCallPrimaryContact: (EmergencyContact) -> Unit,
    onManageContacts: () -> Unit,
    onCancel: () -> Unit
) {
    // Bug 2 fix: Debug log to verify emergency-mode state is correct.
    LaunchedEffect(uiState.isEmergencyModeActive) {
        println("EmergencyModeScreenIntegrated: isEmergencyModeActive=${uiState.isEmergencyModeActive}")
    }

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
            EmergencyHeader(isActive = uiState.isEmergencyModeActive)

            Spacer(modifier = Modifier.height(32.dp))

            // Status Card with real data
            EmergencyStatusCard(
                dangerLevel = uiState.currentDangerLevel,
                contactsCount = uiState.contacts.size,
                lastEvent = uiState.recentEvents.firstOrNull()
            )

            Spacer(modifier = Modifier.weight(1f))

            // SOS Button - NOW ACTUALLY WORKS!
            SOSButton(
                scale = scale,
                onClick = onSosClick,
                isInProgress = uiState.isSosInProgress,
                enabled = !uiState.isSosInProgress
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Actions
            EmergencyActionsRow(
                contacts = uiState.contacts,
                onShareLocation = onShareLocation,
                onCallPrimaryContact = onCallPrimaryContact,
                onManageContacts = onManageContacts
            )

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
private fun EmergencyHeader(isActive: Boolean) {
    // Bug 2 fix: log state changes to verify correct UI binding.
    LaunchedEffect(isActive) {
        println("EmergencyHeader: isActive=$isActive")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warning icon with pulsing
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
            text = if (isActive) "Emergency Mode Active" else "Emergency Mode",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = VoyagerColors.DarkCharcoal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isActive)
                "Your emergency contacts will be notified"
            else
                "Press SOS to alert emergency contacts",
            style = MaterialTheme.typography.bodyLarge,
            color = VoyagerColors.MediumGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmergencyStatusCard(
    dangerLevel: DangerLevel,
    contactsCount: Int,
    lastEvent: EmergencyEvent?
) {
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
                        text = dangerLevel.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when(dangerLevel) {
                            DangerLevel.SAFE -> VoyagerColors.SafeGreen
                            DangerLevel.MODERATE -> Color(0xFFFF9800)
                            DangerLevel.HIGH -> VoyagerColors.EmergencyRed
                            DangerLevel.CRITICAL -> Color(0xFF8B0000)
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when(dangerLevel) {
                                DangerLevel.SAFE -> VoyagerColors.SafeGreen
                                DangerLevel.MODERATE -> Color(0xFFFF9800)
                                DangerLevel.HIGH -> VoyagerColors.EmergencyRed
                                DangerLevel.CRITICAL -> Color(0xFF8B0000)
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                color = VoyagerColors.MediumGray.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Emergency Contacts Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Emergency Contacts",
                    style = MaterialTheme.typography.labelMedium,
                    color = VoyagerColors.MediumGray
                )
                Text(
                    text = "$contactsCount/5 configured",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = VoyagerColors.DarkCharcoal
                )
            }

            lastEvent?.let { event ->
                Spacer(modifier = Modifier.height(16.dp))

                // Location from last event
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
                            text = "${event.latitude}, ${event.longitude}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = VoyagerColors.DarkCharcoal
                        )
                        Text(
                            text = "Source: ${event.locationSource.name} • ${event.locationAccuracy}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = VoyagerColors.MediumGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System status
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
                        text = "📡 SMS delivery active (works offline)",
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
    onClick: () -> Unit,
    isInProgress: Boolean,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(180.dp)
            .scale(if (enabled) scale else 1f)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = VoyagerColors.EmergencyRed.copy(alpha = 0.4f),
                spotColor = VoyagerColors.EmergencyRed.copy(alpha = 0.4f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = VoyagerColors.EmergencyRed,
            disabledContainerColor = VoyagerColors.MediumGray
        ),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SENDING",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            } else {
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
                    text = "Alert Contacts",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun EmergencyActionsRow(
    contacts: List<EmergencyContact>,
    onShareLocation: () -> Unit,
    onCallPrimaryContact: (EmergencyContact) -> Unit,
    onManageContacts: () -> Unit
) {
    val primary = contacts
        .sortedWith(compareByDescending<EmergencyContact> { it.isPrimary }.thenBy { it.position })
        .firstOrNull()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmergencyActionCard(
            // Bug 1 fix: Share Location button now actually does something meaningful.
            title = "Share Location",
            icon = "📍",
            subtitle = "Send SMS",
            onClick = onShareLocation,
            modifier = Modifier.weight(1f)
        )

        if (primary != null) {
            EmergencyActionCard(
                // Bug 1 fix: Call Contact button now opens dialer with the primary contact number.
                title = "Call ${primary.name}",
                icon = "📞",
                subtitle = "Primary contact",
                onClick = { onCallPrimaryContact(primary) },
                modifier = Modifier.weight(1f)
            )
        } else {
            EmergencyActionCard(
                title = "Add Contacts",
                icon = "➕",
                subtitle = "Get started",
                onClick = { onManageContacts() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyActionCard(
    title: String,
    icon: String,
    subtitle: String = "",
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
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = VoyagerColors.DarkCharcoal,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = VoyagerColors.MediumGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(
    onRequestPermissions: () -> Unit,
    onCancel: () -> Unit
) {
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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = VoyagerColors.WarmIvory
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = VoyagerColors.EmergencyRed,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Permissions Required",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Emergency Mode requires:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoyagerColors.MediumGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                PermissionItem("📍 Location", "For sending your GPS coordinates")
                Spacer(modifier = Modifier.height(8.dp))
                PermissionItem("💬 SMS", "For offline emergency alerts")

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoyagerColors.EmergencyRed
                    )
                ) {
                    Text("Grant Permissions")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(title, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = VoyagerColors.MediumGray
            )
        }
    }
}

@Composable
private fun NoContactsScreen(
    onAddContacts: () -> Unit,
    onCancel: () -> Unit
) {
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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = VoyagerColors.WarmIvory
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👥", fontSize = 64.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "No Emergency Contacts",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Add up to 5 emergency contacts to enable SOS alerts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoyagerColors.MediumGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onAddContacts,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Emergency Contacts")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}