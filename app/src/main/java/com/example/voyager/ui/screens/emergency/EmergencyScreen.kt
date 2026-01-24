package com.example.voyager.ui.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.voyager.data.model.DangerLevel
import com.example.voyager.ui.components.*
import com.example.voyager.ui.theme.*

@Composable
fun EmergencyScreen(
    viewModel: EmergencyViewModel,
    onManageContactsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        when (uiState.dangerLevel) {
                            DangerLevel.SAFE -> SafeGreen.copy(alpha = 0.1f)
                            DangerLevel.CAUTION -> CautionYellow.copy(alpha = 0.15f)
                            DangerLevel.DANGER -> DangerRed.copy(alpha = 0.1f)
                        },
                        BackgroundPrimary
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 40.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Safety",
                        modifier = Modifier.size(48.dp),
                        tint = when (uiState.dangerLevel) {
                            DangerLevel.SAFE -> SafeGreen
                            DangerLevel.CAUTION -> CautionYellow
                            DangerLevel.DANGER -> DangerRed
                        }
                    )

                    Text(
                        text = "Emergency Center",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Works even offline",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VoyagerYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // OFFLINE STATUS INDICATOR
            if (!uiState.isOnline) {
                item {
                    OfflineWarningBanner(
                        hasLastKnownLocation = uiState.hasLastKnownLocation,
                        hasSmsCapability = uiState.hasSmsPermission
                    )
                }
            }

            item {
                // Danger level indicator
                DangerLevelCard(
                    dangerLevel = uiState.dangerLevel,
                    locationName = uiState.currentLocationName,
                    isLocationCached = uiState.isLocationCached
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // SOS Button with offline capability indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SOSButton(
                        onSOSTriggered = { viewModel.triggerOfflineSOS() },
                        isActive = uiState.isSOSActive,
                        requireHoldToActivate = true
                    )

                    // Status messages
                    when {
                        uiState.isSOSActive -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = EmergencyCritical.copy(alpha = 0.1f)
                                ),
                                shape = CustomShapes.medium
                            ) {
                                Text(
                                    text = "🚨 SOS ACTIVE\n${uiState.sosStatusMessage}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = EmergencyCritical,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        uiState.sosQueuedCount > 0 -> {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = CautionYellow.copy(alpha = 0.1f)
                                ),
                                shape = CustomShapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Queued",
                                        tint = CautionYellow
                                    )
                                    Text(
                                        text = "${uiState.sosQueuedCount} SOS alert(s) queued\nWill send when online",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Offline capability badge
                    if (!uiState.isOnline) {
                        Surface(
                            shape = CustomShapes.small,
                            color = SafeGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellular4Bar,
                                    contentDescription = "Offline capable",
                                    tint = SafeGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (uiState.hasSmsPermission)
                                        "SMS available offline"
                                    else
                                        "Call available offline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SafeGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Offline badge
                    if (!uiState.isOnline) {
                        Surface(
                            shape = CustomShapes.extraSmall,
                            color = DangerRed.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                                    contentDescription = "Offline",
                                    tint = DangerRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Phone,
                        label = "Call Local\nEmergency",
                        onClick = { viewModel.callLocalEmergency() },
                        modifier = Modifier.weight(1f),
                        isOfflineCapable = true
                    )

                    QuickActionCard(
                        icon = Icons.Default.Share,
                        label = "Share\nLocation",
                        onClick = { viewModel.shareLocation() },
                        modifier = Modifier.weight(1f),
                        isOfflineCapable = !uiState.isOnline && uiState.hasSmsPermission
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Contacts,
                        label = "Emergency\nContacts",
                        onClick = onManageContactsClick,
                        modifier = Modifier.weight(1f),
                        isOfflineCapable = true
                    )

                    QuickActionCard(
                        icon = Icons.Default.VolumeUp,
                        label = "Sound\nAlarm",
                        onClick = { viewModel.triggerLocalAlarm() },
                        modifier = Modifier.weight(1f),
                        isOfflineCapable = true
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                // Emergency contacts preview
                EmergencyContactsPreview(
                    contacts = uiState.emergencyContacts,
                    onManageClick = onManageContactsClick,
                    onCallContact = { contact -> viewModel.callContact(contact) }
                )
            }
        }
    }
}

@Composable
fun OfflineWarningBanner(
    hasLastKnownLocation: Boolean,
    hasSmsCapability: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CautionYellow.copy(alpha = 0.15f)
        ),
        shape = CustomShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                    contentDescription = "Offline",
                    tint = CautionYellow,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "You're Offline",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = when {
                    hasSmsCapability && hasLastKnownLocation ->
                        "✓ SMS emergency alerts will work\n✓ Last known location available\n✓ Local alarm functional"
                    hasLastKnownLocation ->
                        "✓ Last known location available\n✓ Emergency calls will work\n⚠ Enable SMS permission for alerts"
                    else ->
                        "⚠ Limited functionality\n✓ Emergency calls will work\n⚠ Location data unavailable"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun DangerLevelCard(
    dangerLevel: DangerLevel,
    locationName: String?,
    isLocationCached: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (color, label, icon) = when (dangerLevel) {
        DangerLevel.SAFE -> Triple(SafeGreen, "Safe Area", Icons.Default.CheckCircle)
        DangerLevel.CAUTION -> Triple(CautionYellow, "Caution Required", Icons.Default.Warning)
        DangerLevel.DANGER -> Triple(DangerRed, "Danger Zone", Icons.Default.Error)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        shape = CustomShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )

                if (locationName != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        if (isLocationCached) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Cached location",
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOfflineCapable: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = CustomShapes.medium
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = VoyagerYellow,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }

            // Offline capable badge
            if (isOfflineCapable) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = CustomShapes.extraSmall,
                    color = SafeGreen
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellular4Bar,
                        contentDescription = "Works offline",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyContactsPreview(
    contacts: List<com.example.voyager.data.model.EmergencyContact>,
    onManageClick: () -> Unit,
    onCallContact: (com.example.voyager.data.model.EmergencyContact) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emergency Contacts",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            TextButton(onClick = onManageClick) {
                Text("Manage")
            }
        }

        if (contacts.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = DangerRed.copy(alpha = 0.1f)
                ),
                shape = CustomShapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = DangerRed
                    )
                    Text(
                        text = "No emergency contacts set",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DangerRed,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Add contacts to enable SOS alerts",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            contacts.take(3).forEach { contact ->
                EmergencyContactCard(
                    contact = contact,
                    onCall = { onCallContact(contact) }
                )
            }

            if (contacts.size > 3) {
                TextButton(
                    onClick = onManageClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View all ${contacts.size} contacts")
                }
            }
        }
    }
}

@Composable
fun EmergencyContactCard(
    contact: com.example.voyager.data.model.EmergencyContact,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        shape = CustomShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CustomShapes.SOSButton,
                    color = VoyagerYellow.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = contact.name,
                        tint = VoyagerYellow,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = contact.relationship,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            IconButton(onClick = onCall) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call ${contact.name}",
                    tint = SafeGreen
                )
            }
        }
    }
}