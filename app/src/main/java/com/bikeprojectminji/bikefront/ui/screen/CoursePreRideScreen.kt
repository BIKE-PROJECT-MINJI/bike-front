package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val detailLoading = detailResult == null
    val detailError = detailResult?.exceptionOrNull() != null

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            GajaBrandTopBar(
                title = "코스 따라가기",
                subtitle = "코스 개요를 확인하고 바로 주행을 시작합니다",
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    )
                    .padding(
                        horizontal = GajaSpacing.ScreenPadding,
                        vertical = GajaSpacing.Large,
                    ),
                verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large),
            ) {
                CourseLaunchHero(course = resolvedCourse)

                BikeSurfaceCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(GajaSpacing.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
                    ) {
                        SectionHeader(title = "주행 개요")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricChip(label = "거리", value = formatDistance(resolvedCourse.distanceKm), modifier = Modifier.weight(1f))
                            MetricChip(label = "예상시간", value = "${resolvedCourse.estimatedDurationMin}분", modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (detailLoading) {
                    LoadingStateView(message = "코스 정보를 확인하는 중입니다.")
                } else if (detailError) {
                    ErrorStateView(
                        title = "시작할 수 없습니다",
                        message = "코스 정보를 불러오지 못했습니다. 다시 시도해 주세요.",
                    )
                }

                PrimaryActionButton(
                    text = "이 코스로 라이딩 시작",
                    enabled = !detailLoading && !detailError,
                    onClick = onStartRide,
                )

                SecondaryActionButton(
                    text = "코스 탐색으로 돌아가기",
                    onClick = onBack,
                )
                Spacer(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()))
            }
        }
    }
}

@Composable
private fun CourseLaunchHero(course: CourseCardUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inverseSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.inverseSurface,
                            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        ),
                    ),
                )
                .padding(GajaSpacing.CardPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium),
        ) {
            StatusBadge(text = if (course.isRecorded) "기록 코스" else "추천 코스", isActive = true)
            Text(
                text = course.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
                MetricChip(label = "거리", value = formatDistance(course.distanceKm), modifier = Modifier.weight(1f))
                MetricChip(label = "예상시간", value = "${course.estimatedDurationMin}분", modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
        "${(distanceKm * 1000).toInt()}m"
    } else {
        "%.1fkm".format(distanceKm)
    }
}
