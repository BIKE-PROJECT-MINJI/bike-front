package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GajaBrandTopBar(
    title: String,
    onProfileClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Default.AccountCircle, 
                contentDescription = "Profile", 
                tint = GajaColors.TextPrimary, 
                modifier = Modifier.size(32.dp)
            )
        }
    }
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
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

@Composable
fun GajaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = GajaColors.Primary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = GajaColors.White,
            disabledContainerColor = GajaColors.Divider,
            disabledContentColor = GajaColors.TextTertiary
        ),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (icon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
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
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GajaColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = GajaColors.TextPrimary),
        enabled = enabled
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
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
            action?.invoke()
        }
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
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
    gradientColors: List<Color> = listOf(GajaColors.Carbon, GajaColors.Accent)
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (icon != null) {
                    Surface(
                        color = GajaColors.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = icon.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GajaColors.White
                        )
                    }
                }
                
                Column {
                    Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GajaColors.Primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(buttonText, color = GajaColors.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                        contentDescription = null, 
                        modifier = Modifier.size(18.dp), 
                        tint = GajaColors.White
                    )
                }
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
        shape = RoundedCornerShape(16.dp),
        color = GajaColors.White,
        border = BorderStroke(1.dp, GajaColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(GajaColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = GajaIconTokens.Course, 
                    contentDescription = null, 
                    tint = GajaColors.TextPrimary, 
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(course.title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricLabel(GajaIconTokens.Distance, "%.1fkm".format(course.distanceKm))
                    MetricLabel(GajaIconTokens.Duration, "${course.estimatedDurationMin}m")
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                contentDescription = null, 
                tint = GajaColors.TextTertiary, 
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MetricLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = GajaColors.TextSecondary)
        Text(text, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MetricChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GajaColors.Background,
        border = BorderStroke(1.dp, GajaColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BikeSurfaceCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GajaColors.Surface,
        border = BorderStroke(1.dp, GajaColors.Border),
        content = { content() }
    )
}

@Composable
fun LoadingStateView(message: String) {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = GajaColors.Primary, strokeWidth = 5.dp)
            Spacer(Modifier.height(20.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextSecondary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ErrorStateView(title: String, message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = GajaColors.Error.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, GajaColors.Error.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = GajaColors.Error, fontWeight = FontWeight.Black)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextPrimary)
            TextButton(onClick = onRetry, contentPadding = PaddingValues(0.dp)) {
                Text("RETRY", color = GajaColors.Error, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = GajaIconTokens.Course, contentDescription = null, modifier = Modifier.size(72.dp), tint = GajaColors.Border)
        Text(title, style = MaterialTheme.typography.headlineSmall, color = GajaColors.TextPrimary, fontWeight = FontWeight.Black)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
