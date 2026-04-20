package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = { GajaBrandTopBar(title = "Course Detail") },
        containerColor = GajaColors.Background
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))

            // Course Hero Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = GajaColors.TextPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(GajaSpacing.Large),
                    verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
                ) {
                    Surface(
                        color = GajaColors.Accent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            (if (resolvedCourse.isRecorded) "Recorded" else "Curated").uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = GajaColors.TextPrimary
                        )
                    }
                    Text(
                        text = resolvedCourse.title,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }
            }

            SectionHeader(title = "주행 데이터", subtitle = "이 코스의 예상 주행 지표입니다")
            
            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                MetricChip(label = "총 거리", value = formatDistance(resolvedCourse.distanceKm), modifier = Modifier.weight(1f))
                MetricChip(label = "예상 시간", value = "${resolvedCourse.estimatedDurationMin}분", modifier = Modifier.weight(1f))
            }

            if (loading) {
                LoadingStateView("코스 상세 분석 중...")
            } else if (error) {
                ErrorStateView("로드 실패", "코스 데이터를 가져오지 못했습니다.") { /* Retry logic */ }
            }

            Spacer(Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                GajaPrimaryButton(
                    text = "라이딩 시작",
                    enabled = !loading && !error,
                    onClick = onStartRide
                )
                SecondaryActionButton(
                    text = "돌아가기",
                    onClick = onBack
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}m" else "%.1fkm".format(distanceKm)
}
