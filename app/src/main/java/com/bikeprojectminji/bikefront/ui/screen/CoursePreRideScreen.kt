package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
        topBar = { GajaBrandTopBar(title = "코스 상세") },
        containerColor = GajaColors.Background,
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GajaSpacing.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large),
        ) {
            Spacer(Modifier.height(GajaSpacing.Small))

            GajaMapPreview(modifier = Modifier.fillMaxWidth(), heightDp = 260)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = resolvedCourse.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = GajaColors.TextPrimary,
                )
                Text(
                    text = "현재 화면에서는 코스 제목, 거리, 예상 시간까지 확인할 수 있습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GajaColors.TextSecondary,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing), modifier = Modifier.fillMaxWidth()) {
                MetricChip(label = "거리", value = formatDistance(resolvedCourse.distanceKm), modifier = Modifier.weight(1f))
                MetricChip(label = "예상 시간", value = "${resolvedCourse.estimatedDurationMin}분", modifier = Modifier.weight(1f))
            }

            if (loading) {
                LoadingStateView("코스 상세 불러오는 중...")
            } else if (error) {
                ErrorStateView("로드 실패", "코스 데이터를 가져오지 못했습니다.") { }
            }

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                GajaPrimaryButton(
                    text = "주행 화면 열기",
                    enabled = !loading && !error,
                    onClick = onStartRide,
                )
                SecondaryActionButton(text = "돌아가기", onClick = onBack)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}m" else "%.1fkm".format(distanceKm)
}
