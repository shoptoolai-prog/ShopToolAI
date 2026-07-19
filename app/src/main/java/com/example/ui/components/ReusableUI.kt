package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.Product

@Composable
fun PremiumLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LogoAnimation")
    
    val rotation by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val pulseScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Pulse"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(size * 0.8f * pulseScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Geometric Canvas Logo
        Canvas(
            modifier = Modifier
                .size(size)
                .testTag("shoptool_logo_canvas")
        ) {
            val width = this.size.width
            val height = this.size.height
            val center = Offset(width / 2, height / 2)
            val radius = (width.coerceAtMost(height) / 2) * 0.7f

            // 1. Draw outer stylized tech circle
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        secondaryColor,
                        primaryColor.copy(alpha = 0.2f),
                        primaryColor
                    ),
                    center = center
                ),
                radius = radius,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Draw outer pulse orbit nodes
            val orbitAngleRad = Math.toRadians(rotation.toDouble())
            val nodeX = center.x + radius * Math.cos(orbitAngleRad).toFloat()
            val nodeY = center.y + radius * Math.sin(orbitAngleRad).toFloat()
            drawCircle(
                color = primaryColor,
                radius = 7.dp.toPx(),
                center = Offset(nodeX, nodeY)
            )

            // 3. Draw inner stylized shopping bag combined with scanning lens
            val bagWidth = radius * 0.8f
            val bagHeight = radius * 0.9f
            val bagLeft = center.x - bagWidth / 2
            val bagTop = center.y - bagHeight * 0.4f

            // Bag body
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor),
                    start = Offset(bagLeft, bagTop),
                    end = Offset(bagLeft + bagWidth, bagTop + bagHeight)
                ),
                topLeft = Offset(bagLeft, bagTop),
                size = Size(bagWidth, bagHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            // Bag handle arch
            val handleRadius = bagWidth * 0.3f
            drawArc(
                brush = Brush.linearGradient(colors = listOf(primaryColor, secondaryColor)),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - handleRadius, bagTop - handleRadius),
                size = Size(handleRadius * 2, handleRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Center Scanner Ring representing AI
            drawCircle(
                color = secondaryColor.copy(alpha = 0.15f),
                radius = radius * 0.4f * pulseScale,
                center = center
            )
            drawCircle(
                color = primaryColor,
                radius = 3.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
fun ScanningAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Scanning")
    
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val waveRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveRadius"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height
        val center = Offset(width / 2, height / 2)
        val maxRadius = width.coerceAtMost(height) / 2

        // Outer bounds
        drawCircle(
            color = primaryColor.copy(alpha = 0.1f),
            radius = maxRadius,
            style = Stroke(width = 1.dp.toPx())
        )

        // Pulsing radar ripple waves
        drawCircle(
            color = primaryColor.copy(alpha = (1f - (waveRadius / 100f)).coerceAtLeast(0f) * 0.3f),
            radius = maxRadius * (waveRadius / 100f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Rotating scanner sweep gradient
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.5f),
                    secondaryColor.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = center
            ),
            startAngle = sweepAngle,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
            size = Size(maxRadius * 2, maxRadius * 2)
        )

        // Active target reticles (4 corners)
        val cornerLen = 15.dp.toPx()
        val padding = 5.dp.toPx()
        val boxSize = maxRadius * 1.4f

        // Top Left
        drawLine(primaryColor, Offset(center.x - boxSize/2, center.y - boxSize/2 + cornerLen), Offset(center.x - boxSize/2, center.y - boxSize/2), strokeWidth = 3.dp.toPx())
        drawLine(primaryColor, Offset(center.x - boxSize/2, center.y - boxSize/2), Offset(center.x - boxSize/2 + cornerLen, center.y - boxSize/2), strokeWidth = 3.dp.toPx())

        // Top Right
        drawLine(primaryColor, Offset(center.x + boxSize/2, center.y - boxSize/2 + cornerLen), Offset(center.x + boxSize/2, center.y - boxSize/2), strokeWidth = 3.dp.toPx())
        drawLine(primaryColor, Offset(center.x + boxSize/2, center.y - boxSize/2), Offset(center.x + boxSize/2 - cornerLen, center.y - boxSize/2), strokeWidth = 3.dp.toPx())

        // Bottom Left
        drawLine(primaryColor, Offset(center.x - boxSize/2, center.y + boxSize/2 - cornerLen), Offset(center.x - boxSize/2, center.y + boxSize/2), strokeWidth = 3.dp.toPx())
        drawLine(primaryColor, Offset(center.x - boxSize/2, center.y + boxSize/2), Offset(center.x - boxSize/2 + cornerLen, center.y + boxSize/2), strokeWidth = 3.dp.toPx())

        // Bottom Right
        drawLine(primaryColor, Offset(center.x + boxSize/2, center.y + boxSize/2 - cornerLen), Offset(center.x + boxSize/2, center.y + boxSize/2), strokeWidth = 3.dp.toPx())
        drawLine(primaryColor, Offset(center.x + boxSize/2, center.y + boxSize/2), Offset(center.x + boxSize/2 - cornerLen, center.y + boxSize/2), strokeWidth = 3.dp.toPx())
    }
}

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Product Image + Basic Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            // High-quality decorative vector gradient fallback
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    )

                    // Match Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.matchPercentage}%",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Title, Brand & Price
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = product.brand,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified Brand Match",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = product.lowestPrice,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Best Price Found",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Insight Summaries: Pros & Cons in double columns or neat rows
            Text(
                text = "AI Product Insight",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Positives (Green card)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Pros",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = product.positiveReviewSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }

                // Negatives (Red card)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cons",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = product.negativeReviewSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Comparison Accordeon / Drawer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Compare Stores (${product.priceComparison.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (isExpanded) {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        product.priceComparison.forEach { priceSource ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = priceSource.sourceName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (priceSource.isLowest) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "LOWEST",
                                                color = MaterialTheme.colorScheme.tertiary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = priceSource.price,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (priceSource.isLowest) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: Buy Now
            Button(
                onClick = { uriHandler.openUri(product.buyUrl) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("buy_now_button_${product.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowOutward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buy Now At ${product.brand}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
