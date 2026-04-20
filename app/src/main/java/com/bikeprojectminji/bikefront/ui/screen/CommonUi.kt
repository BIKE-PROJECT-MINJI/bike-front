package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaIconTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GajaBrandTopBar(
    title: String,
    onProfileClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { }) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = GajaColors.Primary)
        }
    },
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GajaColors.SurfaceContainerHigh)
                        .clickable(enabled = onProfileClick != null) { onProfileClick?.invoke() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = GajaColors.TextSecondary)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "gaja",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GajaColors.PrimaryContainer,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = GajaColors.TextSecondary,
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GajaColors.Background,
            titleContentColor = GajaColors.TextPrimary,
        ),
    )
}

@Composable
fun GajaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GajaColors.PrimaryContainer,
            contentColor = GajaColors.White,
            disabledContainerColor = GajaColors.SurfaceContainerHigh,
            disabledContentColor = GajaColors.TextTertiary,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        enabled = enabled,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (icon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, GajaColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GajaColors.SurfaceContainerLow,
            contentColor = GajaColors.TextPrimary,
        ),
        enabled = enabled,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = GajaSpacing.Small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = GajaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            action?.invoke()
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = GajaColors.TextSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun HeroCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    gradientColors: List<Color> = GajaColors.BrandGradient,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientColors))
                .padding(GajaSpacing.Large),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)) {
                if (icon != null) {
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = icon.uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = GajaColors.White,
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium,
                    color = GajaColors.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GajaColors.White.copy(alpha = 0.88f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Button(
                    onClick = onClick,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GajaColors.White,
                        contentColor = GajaColors.Primary,
                    ),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    course: CourseCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, GajaColors.Border.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GajaColors.Surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(GajaIconTokens.Ride, contentDescription = null, tint = GajaColors.PrimaryContainer)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GajaColors.TextPrimary,
                )
                Text(
                    text = if (course.isRecorded) {
                        "기록 코스 • ${formatCourseDistance(course.distanceKm)} • ${course.estimatedDurationMin}분"
                    } else {
                        "코스 정보 • ${formatCourseDistance(course.distanceKm)} • ${course.estimatedDurationMin}분"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = GajaColors.TextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = GajaColors.TextTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, GajaColors.Border.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = GajaColors.TextSecondary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = GajaColors.TextPrimary,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
fun BikeSurfaceCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, GajaColors.Border.copy(alpha = 0.25f)),
        content = { content() },
    )
}

@Composable
fun LoadingStateView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(GajaSpacing.Large),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GajaColors.PrimaryContainer)
            Spacer(Modifier.height(GajaSpacing.Medium))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
        }
    }
}

@Composable
fun ErrorStateView(title: String, message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GajaColors.ErrorContainer),
        border = BorderStroke(1.dp, GajaColors.Error.copy(alpha = 0.2f)),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(GajaSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(GajaSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(GajaIconTokens.Course, contentDescription = null, modifier = Modifier.size(48.dp), tint = GajaColors.TextTertiary)
        Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary, textAlign = TextAlign.Center)
    }
}

private fun formatCourseDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}m" else "%.1fkm".format(distanceKm)
}
