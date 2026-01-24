package com.example.voyager.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyager.ui.components.GlassCard

@Composable
fun ExploreHomeScreen(
    userCity: String = "Kolkata",
    onSearchClick: () -> Unit = {},
    onCountryClick: () -> Unit = {},
    onPermissionsClick: () -> Unit = {},
    onExperienceClick: (String) -> Unit = {}
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

        // ✅ HERO TOP (Theme like 3rd pic)
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

                // Top row: Title + crown icon placeholder
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

                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.55f)
                    ) {}
                }

                // ✅ Glass Search Bar
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "search",
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

        // ✅ Content feed (SmartGuide layout)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {

            Spacer(modifier = Modifier.height(14.dp))

            // Current location small card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onCountryClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "location",
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
                            text = "Tap to change destination",
                            fontSize = 12.sp,
                            color = Color(0xFF1A1A1A).copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Permission banner
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAD9FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPermissionsClick() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "warning",
                        tint = Color(0xFF6B2EF8)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Please allow permissions, so we can guide you.",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4B2AA6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Nearby countries
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
                NearbyCountryCard("Nepal")
                NearbyCountryCard("Myanmar")
                NearbyCountryCard("Bhutan")
                NearbyCountryCard("Sri Lanka")
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Experiences
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
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExperienceCard(
                title = "Kolkata: City Highlights",
                price = "From ₹7374",
                rating = "4.7"
            ) { onExperienceClick("Kolkata: City Highlights") }

            Spacer(modifier = Modifier.height(12.dp))

            ExperienceCard(
                title = "Kolkata: Spirituality & Temples",
                price = "From ₹7592",
                rating = "4.4"
            ) { onExperienceClick("Kolkata: Spirituality & Temples") }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NearbyCountryCard(title: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .width(180.dp)
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFAC31C).copy(alpha = 0.25f))
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExperienceCard(
    title: String,
    price: String,
    rating: String,
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
                    .background(Color(0xFFFDEBB9))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = price,
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "★ $rating",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            }
        }
    }
}
