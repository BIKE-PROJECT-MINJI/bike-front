package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaIconTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

// ============================================================================
// GAJA MODERN FOREST COMPONENT LIBRARY
// Sophisticated Green, High Readability
// ============================================================================

/**
 * Modern, clean TopBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GajaBrandTopBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { }) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = GajaColors.TextPrimary)
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = GajaColors.TextPrimary)
        }
    }
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GajaColors.TextPrimary
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = GajaColors.TextPrimary
        )
    )
}

/**
 * Primary action button - Forest Green.
 */
@Composable
fun GajaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = GajaColors.Primary,
            contentColor = GajaColors.White,
            disabledContainerColor = GajaColors.Border,
            disabledContentColor = GajaColors.TextTertiary
        ),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (icon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/**
 * Secondary button - Outlined with Forest Green.
 */
@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.5.dp, GajaColors.Primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = GajaColors.Primary
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Data-rich Section Header.
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = GajaSpacing.Small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = GajaColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            action?.invoke()
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = GajaColors.TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Hero Card with Fresh Forest Gradient.
 */
@Composable
fun HeroCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    gradientColors: List<Color> = GajaColors.BrandGradient
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(GajaSpacing.Large)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)) {
                if (icon != null) {
                    Surface(
                        color = GajaColors.LimeAccent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            icon.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = GajaColors.TextPrimary
                        )
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = GajaColors.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GajaColors.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GajaColors.White),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(buttonText, color = GajaColors.Primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = GajaColors.Primary)
                }
            }
        }
    }
}

/**
 * Compact Course Card for lists.
 */
@Composable
fun CourseCard(
    course: com.bikeprojectminji.bikefront.ui.screen.CourseCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = GajaColors.White),
        border = BorderStroke(1.dp, GajaColors.Border)
    ) {
        Row(
            modifier = Modifier
                .padding(GajaSpacing.Medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(GajaColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(GajaIconTokens.Course, contentDescription = null, tint = GajaColors.Primary)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GajaColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    MetricLabel(GajaIconTokens.Distance, "%.1fkm".format(course.distanceKm))
                    MetricLabel(GajaIconTokens.Duration, "${course.estimatedDurationMin}m")
                }
            }
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = GajaColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MetricLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = GajaColors.TextSecondary
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = GajaColors.TextSecondary
        )
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = GajaColors.PrimaryContainer),
        border = BorderStroke(1.dp, GajaColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(GajaSpacing.Medium),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = GajaColors.TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = GajaColors.TextPrimary,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun BikeSurfaceCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = GajaColors.PrimaryContainer),
        content = { content() }
    )
}

@Composable
fun LoadingStateView(message: String) {
    Box(Modifier.fillMaxWidth().padding(GajaSpacing.Large), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GajaColors.Primary)
            Spacer(Modifier.height(GajaSpacing.Medium))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
        }
    }
}

@Composable
fun ErrorStateView(title: String, message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GajaColors.Error.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, GajaColors.Error)
    ) {
        Column(Modifier.padding(GajaSpacing.Medium), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.Error)
            Text(message, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextPrimary)
            TextButton(onClick = onRetry) {
                Text("다시 시도", color = GajaColors.Error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(GajaSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(GajaIconTokens.Course, contentDescription = null, modifier = Modifier.size(48.dp), tint = GajaColors.TextTertiary)
        Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
