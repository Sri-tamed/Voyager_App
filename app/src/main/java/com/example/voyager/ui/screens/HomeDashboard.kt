package com.example.voyager.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyager.data.model.*
import com.example.voyager.ui.theme.getSafetyColor

@Composable
fun HomeDashboard(
    userLocation: UserLocation?,
    dangerLevel: DangerLevel,
    onEmergencyClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C3E50))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = userLocation?.city ?: "Locating...",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (userLocation != null) {
                    "${String.format("%.4f", userLocation.latitude)}, ${String.format("%.4f", userLocation.longitude)}"
                } else {
                    "Acquiring GPS signal..."
                },
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            SafetyStatusCard(dangerLevel)

            Spacer(modifier = Modifier.weight(1f))

            EmergencyButton(onClick = onEmergencyClick)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SafetyStatusCard(dangerLevel: DangerLevel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CircleShape),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B2838)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(getSafetyColor(dangerLevel), CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Safety Status",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = dangerLevel.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = getSafetyColor(dangerLevel)
                )
            }
        }
    }
}

@Composable
fun EmergencyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .shadow(12.dp, CircleShape),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE74C3C)
        )
    ) {
        Text(
            text = "SOS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
