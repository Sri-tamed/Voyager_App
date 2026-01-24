package com.example.voyager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.voyager.ui.theme.CustomShapes
import com.example.voyager.ui.theme.GlassWhite
import com.example.voyager.ui.theme.ShadowLight

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    glassOpacity: Float = 0.85f,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = CustomShapes.GlassCard,
        colors = CardDefaults.cardColors(
            containerColor = GlassWhite.copy(alpha = glassOpacity)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        Color(0xFFFFC107),
        Color(0xFFFFB300)
    ),
    elevation: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = CustomShapes.DestinationCard,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(gradientColors)
                )
                .padding(contentPadding),
            content = content
        )
    }
}