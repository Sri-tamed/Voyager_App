package com.example.voyager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.voyager.data.model.Experience
import com.example.voyager.ui.theme.*

@Composable
fun ExperienceCard(
    experience: Experience,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image
            AsyncImage(
                model = experience.imageUrl,
                contentDescription = experience.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = experience.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )

                Text(
                    text = experience.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = VoyagerYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = experience.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = experience.price,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VoyagerYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}