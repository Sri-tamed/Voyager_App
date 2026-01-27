package com.example.voyager.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyager.data.model.DangerLevel  // Import from data model
import com.google.android.gms.maps.model.LatLng

// REMOVED the local DangerLevel enum - use the imported one from data.model

@Composable
fun HomeDashboard(
    userLocation: LatLng,
    userCity: String = "Kolkata", // Made this a parameter instead of hardcoded
    dangerLevel: DangerLevel = DangerLevel.SAFE, // Now using data model DangerLevel
    hasLocationPermission: Boolean = true, // Added parameter for permission state
    onEmergencyClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onLocationChangeClick: () -> Unit = {},
    onPermissionClick: () -> Unit = {},
    onSafetyInfoClick: () -> Unit = {},
    onCountryClick: (String) -> Unit = {},
    onExperienceClick: (String) -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    val bgCream = Color(0xFFFDF1CE)
    val heroGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFAC31C),
            Color(0xFFFDEBB9),
            bgCream
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgCream)
    ) {
        // ✅ HERO TOP SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(heroGradient)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Title + Emergency button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Voyager",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111111)
                        )
                        Text(
                            text = "Find destinations worldwide",
                            fontSize = 13.sp,
                            color = Color.Black.copy(alpha = 0.65f)
                        )
                    }

                    // Emergency button with danger level indicator
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onEmergencyClick() },
                        shape = CircleShape,
                        color = when (dangerLevel) {
                            DangerLevel.SAFE -> Color.Green.copy(alpha = 0.3f)
                            DangerLevel.MODERATE -> Color.Yellow.copy(alpha = 0.5f)  // Changed from CAUTION to MODERATE
                            DangerLevel.HIGH -> Color(0xFFFF6B35).copy(alpha = 0.5f)  // Changed from DANGER to HIGH
                            DangerLevel.CRITICAL -> Color.Red.copy(alpha = 0.6f)
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "🚨",
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                // ✅ Glass Search Bar
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onSearchClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search places, cities, experiences",
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A).copy(alpha = 0.65f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ✅ SCROLLABLE CONTENT SECTION
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Made scrollable
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Current location card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onLocationChangeClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = userCity,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "Lat: ${String.format("%.4f", userLocation.latitude)}, " +
                                    "Lng: ${String.format("%.4f", userLocation.longitude)}",
                            fontSize = 11.sp,
                            color = Color(0xFF1A1A1A).copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Change",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B2EF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Safety status banner (conditional)
            if (dangerLevel != DangerLevel.SAFE) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (dangerLevel) {
                            DangerLevel.MODERATE -> Color(0xFFFFF4E6)      // Changed from CAUTION
                            DangerLevel.HIGH -> Color(0xFFFFE5E5)         // Changed from DANGER
                            DangerLevel.CRITICAL -> Color(0xFFFFCDD2)
                            else -> Color(0xFFEAD9FF)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSafetyInfoClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = when (dangerLevel) {
                                DangerLevel.MODERATE -> Color(0xFFFF9800)      // Changed from CAUTION
                                DangerLevel.HIGH -> Color(0xFFE53935)          // Changed from DANGER
                                DangerLevel.CRITICAL -> Color(0xFFB71C1C)
                                else -> Color(0xFF6B2EF8)
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (dangerLevel) {
                                    DangerLevel.MODERATE -> "Exercise Caution"      // Changed from CAUTION
                                    DangerLevel.HIGH -> "Danger Alert"              // Changed from DANGER
                                    DangerLevel.CRITICAL -> "Critical Alert"
                                    else -> "Safe Area"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = when (dangerLevel) {
                                    DangerLevel.MODERATE -> Color(0xFFE65100)       // Changed from CAUTION
                                    DangerLevel.HIGH -> Color(0xFFC62828)           // Changed from DANGER
                                    DangerLevel.CRITICAL -> Color(0xFF8B0000)
                                    else -> Color(0xFF4B2AA6)
                                }
                            )
                            Text(
                                text = when (dangerLevel) {
                                    DangerLevel.MODERATE -> "Stay aware of your surroundings"     // Changed from CAUTION
                                    DangerLevel.HIGH -> "Avoid this area if possible"             // Changed from DANGER
                                    DangerLevel.CRITICAL -> "Immediate evacuation recommended"
                                    else -> "You're in a safe area"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Permission banner (conditional - only show if no permission)
            if (!hasLocationPermission) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAD9FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPermissionClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFF6B2EF8)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Enable location permissions for better guidance",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF4B2AA6)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Nearby countries section
            Text(
                text = "Nearby countries",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111111)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                NearbyCountryCard("Nepal", "🇳🇵") { onCountryClick("Nepal") }
                NearbyCountryCard("Myanmar", "🇲🇲") { onCountryClick("Myanmar") }
                NearbyCountryCard("Bhutan", "🇧🇹") { onCountryClick("Bhutan") }
                NearbyCountryCard("Sri Lanka", "🇱🇰") { onCountryClick("Sri Lanka") }
                NearbyCountryCard("Bangladesh", "🇧🇩") { onCountryClick("Bangladesh") }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Experiences section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experiences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111111)
                )
                Text(
                    text = "See all",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111111).copy(alpha = 0.65f),
                    modifier = Modifier.clickable { onSeeAllClick() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExperienceCard(
                title = "Kolkata: City Highlights",
                price = "From ₹7,374",
                rating = "4.7",
                reviews = "142"
            ) { onExperienceClick("City Highlights") }

            Spacer(modifier = Modifier.height(12.dp))

            ExperienceCard(
                title = "Kolkata: Spirituality & Temples",
                price = "From ₹7,592",
                rating = "4.4",
                reviews = "98"
            ) { onExperienceClick("Spirituality & Temples") }

            Spacer(modifier = Modifier.height(12.dp))

            ExperienceCard(
                title = "Heritage Walking Tour",
                price = "From ₹3,500",
                rating = "4.8",
                reviews = "256"
            ) { onExperienceClick("Heritage Walking Tour") }

            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation
        }
    }
}

// ✅ GlassCard Component (you'll need to add this)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.4f),
        shadowElevation = 0.dp
    ) {
        content()
    }
}

@Composable
private fun NearbyCountryCard(
    title: String,
    flag: String = "🏳️",
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .width(180.dp)
            .height(120.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFAC31C).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = flag,
                    fontSize = 24.sp
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
                Text(
                    text = "Explore",
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B)
                )
            }
        }
    }
}

@Composable
private fun ExperienceCard(
    title: String,
    price: String,
    rating: String,
    reviews: String = "0",
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFAC31C),
                                Color(0xFFFDEBB9)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏛️",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    color = Color(0xFF111111)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = price,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B6B6B)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐",
                        fontSize = 14.sp
                    )
                    Text(
                        text = " $rating",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                }
                Text(
                    text = "($reviews)",
                    fontSize = 11.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}