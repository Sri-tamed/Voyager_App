package com.example.voyager.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyager.ui.theme.*

@Composable
fun PermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CautionYellow.copy(alpha = 0.15f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = CautionYellow
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Location Access",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Enable location for better recommendations",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoyagerYellow
                )
            ) {
                Text("Enable")
            }
        }
    }
}