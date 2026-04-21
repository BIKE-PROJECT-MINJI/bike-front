package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CoursePreRideScreen(
    innerPadding: PaddingValues,
    course: CourseCardUiModel,
    onBack: () -> Unit,
    onStartRide: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember(context) { CoursesRepository(context) }
    val detailResult by produceState<Result<CourseCardUiModel>?>(initialValue = null, key1 = course.id) {
        value = runCatching {
            withContext(Dispatchers.IO) { repository.fetchCourseDetail(course.id) }
        }
    }
    val resolvedCourse = detailResult?.getOrNull() ?: course
    val loading = detailResult == null
    val error = detailResult?.exceptionOrNull() != null

    Box(modifier = Modifier.fillMaxSize().background(GajaColors.Background)) {
        // Map Placeholder (Full Screen Background for Premium Mobility Look)
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E5EA))) {
            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(64.dp), tint = Color.LightGray)
        }

        // Overlay Content (Bottom Sheet Style)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            GajaBrandTopBar(title = "경로 미리보기", onProfileClick = {})

            // Focal Course Info
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                Surface(
                    color = GajaColors.Surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.Border),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Surface(color = GajaColors.PrimaryContainer, shape = RoundedCornerShape(4.dp)) {
                            Text("CURATED COURSE", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color=GajaColors.Primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(resolvedCourse.title, style = MaterialTheme.typography.headlineLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricChip(label = "Distance", value = formatDistance(resolvedCourse.distanceKm), modifier = Modifier.weight(1f))
                            MetricChip(label = "Estimated", value = "${resolvedCourse.estimatedDurationMin}m", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Bottom Actions
                if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GajaColors.Primary)
                
                GajaPrimaryButton(
                    text = "주행 시작하기",
                    onClick = onStartRide,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    enabled = !loading && !error
                )
                SecondaryActionButton(text = "뒤로가기", onClick = onBack)
            }
        }
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}m" else "%.1fkm".format(distanceKm)
}
