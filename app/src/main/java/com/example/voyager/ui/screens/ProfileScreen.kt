package com.example.voyager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyager.ui.theme.VoyagerColors

/**
 * @param userCity      live city string from MainActivity — updates as GPS moves.
 * @param tripCount     number of trips the user has taken (pass from ViewModel when ready).
 * @param countriesCount number of distinct countries visited.
 * @param savedCount    number of saved destinations.
 */
@Composable
fun ProfileScreen(
    userCity: String = "Kolkata",
    tripCount: Int = 12,
    countriesCount: Int = 5,
    savedCount: Int = 8
) {
    // ---------------------------------------------------------------------------
    // Single scroll state shared by the entire screen.
    // verticalScroll is applied to the outermost Column so EVERYTHING scrolls
    // together — header, stats, and menu — as one continuous list.
    // ---------------------------------------------------------------------------
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)          // ← makes the whole page scrollable
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VoyagerColors.CreamBackground,
                        VoyagerColors.LightBeige
                    )
                )
            )
    ) {
        // --------------- Header -----------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VoyagerColors.CreamBackground)
                .padding(top = 24.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(VoyagerColors.SunsetGold.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector   = Icons.Filled.Person,
                        contentDescription = "Profile Avatar",
                        tint          = VoyagerColors.DarkCharcoal,
                        modifier      = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text       = "Voyager User",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = VoyagerColors.DarkCharcoal
                )

                // ── DYNAMIC: shows the real current city from GPS ──
                Text(
                    text     = "$userCity, India",
                    fontSize = 14.sp,
                    color    = VoyagerColors.MediumGray
                )
            }
        }

        // --------------- Stats Row --------------------------------------------
        // ── DYNAMIC: counts come in as parameters, update when ViewModel changes ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(label = "Trips",     value = tripCount.toString(),     modifier = Modifier.weight(1f))
            ProfileStatCard(label = "Countries", value = countriesCount.toString(), modifier = Modifier.weight(1f))
            ProfileStatCard(label = "Saved",     value = savedCount.toString(),     modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --------------- Menu Items -------------------------------------------
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            ProfileMenuRow(label = "My Trips")
            ProfileMenuRow(label = "Saved Destinations")
            ProfileMenuRow(label = "Emergency Contacts")
            ProfileMenuRow(label = "Privacy Policy")
            ProfileMenuRow(label = "About Voyager")
            ProfileMenuRow(label = "Sign Out")
        }

        // Bottom breathing room so the last card isn't flush against the edge
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun ProfileStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = VoyagerColors.WarmIvory),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value,  fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = VoyagerColors.DarkCharcoal)
            Text(text = label,  fontSize = 12.sp, color = VoyagerColors.MediumGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuRow(label: String, onClick: () -> Unit = {}) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = VoyagerColors.WarmIvory),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick   = onClick
    ) {
        Row(
            modifier             = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment    = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = VoyagerColors.DarkCharcoal)
            Text(text = "›",   fontSize = 20.sp, color = VoyagerColors.MediumGray)
        }
    }
}