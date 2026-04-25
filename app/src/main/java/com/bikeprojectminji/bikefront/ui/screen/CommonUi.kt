package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.ui.theme.GajaButtonTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaCardTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaControlTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaIconTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaIconSizes
import com.bikeprojectminji.bikefront.ui.theme.GajaRadius
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GajaBrandTopBar(
    title: String,
    onProfileClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = GajaColors.TextPrimary
            )
        },
        actions = {
            when {
                actions != null -> actions()
                onProfileClick != null -> {
                    Surface(
                        shape = CircleShape,
                        color = GajaColors.SurfaceMuted,
                        border = BorderStroke(1.dp, GajaColors.Border),
                    ) {
                        IconButton(onClick = onProfileClick, modifier = Modifier.size(GajaControlTokens.TopBarAction)) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "내 정보",
                                tint = GajaColors.Accent,
                                modifier = Modifier.size(GajaIconSizes.Large)
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = GajaColors.TextPrimary
        )
    )
}

@Composable
fun GajaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = GajaColors.Primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(GajaButtonTokens.Height),
        shape = RoundedCornerShape(GajaRadius.Large),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = GajaColors.White,
            disabledContainerColor = GajaColors.Divider,
            disabledContentColor = GajaColors.TextTertiary
        ),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp,
        ),
        interactionSource = interactionSource,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (icon != null) {
                Spacer(Modifier.width(GajaSpacing.Tiny))
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(GajaIconSizes.Medium))
            }
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(GajaButtonTokens.Height),
        shape = RoundedCornerShape(GajaRadius.Medium),
        border = BorderStroke(1.dp, GajaColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = GajaColors.TextPrimary),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = GajaSpacing.Tiny)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
            action?.invoke()
        }
        if (subtitle != null) {
            Spacer(Modifier.height(GajaSpacing.Micro))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
        }
    }
}

@Composable
fun GajaSectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color = GajaColors.Surface,
    contentColor: Color = GajaColors.TextPrimary,
    borderColor: Color = GajaColors.Border,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(GajaRadius.Medium),
    tonalElevation: androidx.compose.ui.unit.Dp = 0.dp,
    shadowElevation: androidx.compose.ui.unit.Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(GajaCardTokens.DefaultPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(GajaCardTokens.BorderWidth, borderColor),
        tonalElevation = tonalElevation,
        shadowElevation = if (shadowElevation == 0.dp) GajaCardTokens.SubtleElevation else shadowElevation,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
            content = content,
        )
    }
}

@Composable
fun GajaStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = GajaColors.PrimaryContainer,
    contentColor: Color = GajaColors.Primary,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GajaRadius.Pill),
        color = containerColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = GajaControlTokens.BadgeHorizontalPadding,
                vertical = GajaControlTokens.BadgeVerticalPadding,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun GajaMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emphasized: Boolean = false,
    supportingText: String? = null,
    containerColor: Color = if (emphasized) GajaColors.PrimaryContainer else GajaColors.SurfaceMuted,
    contentColor: Color = GajaColors.TextPrimary,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GajaRadius.Small),
        color = containerColor,
        border = BorderStroke(GajaCardTokens.BorderWidth, GajaColors.Border),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GajaCardTokens.CompactPadding, vertical = GajaSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Micro),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Micro),
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (emphasized) GajaColors.Primary else GajaColors.TextSecondary,
                        modifier = Modifier.size(GajaIconSizes.Small),
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = GajaColors.TextSecondary,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            supportingText?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = GajaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun GajaInfoPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    containerColor: Color = GajaColors.Background,
    contentColor: Color = GajaColors.TextSecondary,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GajaRadius.Small),
        color = containerColor,
        border = BorderStroke(GajaCardTokens.BorderWidth, GajaColors.Border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Micro),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(GajaIconSizes.Small),
                    tint = contentColor,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
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
    gradientColors: List<Color> = GajaColors.HeroGradient,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GajaRadius.Large),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(GajaCardTokens.HeroPadding)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)) {
                if (icon != null) {
                    GajaStatusBadge(
                        text = icon,
                        containerColor = GajaColors.White.copy(alpha = 0.8f),
                        contentColor = GajaColors.Accent,
                    )
                }
                
                Column {
                    Text(title, style = MaterialTheme.typography.headlineMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(GajaSpacing.Micro))
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
                }

                GajaPrimaryButton(
                    text = buttonText,
                    onClick = onClick,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    containerColor = GajaColors.Accent,
                )
            }
        }
    }
}

@Composable
fun CourseCard(
    course: com.bikeprojectminji.bikefront.ui.screen.CourseCardUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(GajaRadius.Medium),
        color = GajaColors.Surface,
        border = BorderStroke(1.dp, GajaColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(GajaCardTokens.DefaultPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
        ) {
            Box(
                modifier = Modifier
                    .size(GajaControlTokens.LargeListLeading)
                    .clip(RoundedCornerShape(GajaRadius.Small))
                    .background(GajaColors.PrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = GajaIconTokens.Course, 
                    contentDescription = null, 
                    tint = GajaColors.Accent, 
                    modifier = Modifier.size(GajaIconSizes.PrimaryControl)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny), verticalAlignment = Alignment.CenterVertically) {
                    Text(course.title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                    if (course.isRecorded) {
                        GajaStatusBadge(text = "내 저장")
                    }
                }
                Spacer(Modifier.height(GajaSpacing.Micro))
                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
                    MetricLabel(GajaIconTokens.Distance, "%.1fkm".format(course.distanceKm))
                    MetricLabel(GajaIconTokens.Duration, "${course.estimatedDurationMin}m")
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                contentDescription = null, 
                tint = GajaColors.TextTertiary, 
                modifier = Modifier.size(GajaIconSizes.Medium)
            )
        }
    }
}

@Composable
private fun MetricLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Micro)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(GajaIconSizes.Small), tint = GajaColors.TextSecondary)
        Text(text, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MetricChip(label: String, value: String, modifier: Modifier = Modifier) {
    GajaMetricCard(
        label = label.uppercase(),
        value = value,
        modifier = modifier,
    )
}

@Composable
fun BikeSurfaceCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(GajaCardTokens.DefaultPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    GajaSectionCard(
        modifier = modifier,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun LoadingStateView(message: String) {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GajaColors.Primary, strokeWidth = 5.dp)
            Spacer(Modifier.height(GajaSpacing.Medium))
            Text(message, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextSecondary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ErrorStateView(title: String, message: String, onRetry: () -> Unit) {
    GajaSectionCard(
        containerColor = GajaColors.Error.copy(alpha = 0.05f),
        borderColor = GajaColors.Error.copy(alpha = 0.2f),
        shape = RoundedCornerShape(GajaRadius.XLarge),
        contentPadding = PaddingValues(28.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = GajaColors.Error, fontWeight = FontWeight.Black)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextPrimary)
        TextButton(onClick = onRetry, contentPadding = PaddingValues(0.dp)) {
            Text("다시 시도", color = GajaColors.Error, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyStateView(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
    ) {
        Icon(imageVector = GajaIconTokens.Course, contentDescription = null, modifier = Modifier.size(72.dp), tint = GajaColors.Border)
        Text(title, style = MaterialTheme.typography.headlineSmall, color = GajaColors.TextPrimary, fontWeight = FontWeight.Black)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextSecondary, textAlign = TextAlign.Center)
    }
}
