package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        topBar = { GajaBrandTopBar(title = "Course Detail") },
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Brush.verticalGradient(listOf(GajaColors.SurfaceContainerHigh, GajaColors.SurfaceContainerLow)), MaterialTheme.shapes.extraLarge)
                    .padding(20.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallBadge(if (resolvedCourse.isRecorded) "기록 코스" else "공식 루트", primary = true)
                        SmallBadge("중급자")
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.86f), MaterialTheme.shapes.large)
                            .padding(18.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            HeroMetric(label = "거리", value = formatDistance(resolvedCourse.distanceKm), unit = null)
                            HeroMetric(label = "예상 시간", value = "${resolvedCourse.estimatedDurationMin}", unit = "분")
                            HeroMetric(label = "획득 고도", value = if (resolvedCourse.distanceKm > 20) "320" else "180", unit = "m")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = resolvedCourse.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = GajaColors.TextPrimary,
                )
                Text(
                    text = "바람을 가르며 달리는 코스입니다. 평탄한 구간과 가벼운 업힐이 섞여 있어 초중급 라이더가 즐기기 좋습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GajaColors.TextSecondary,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                InfoTile(icon = Icons.Default.Route, title = "노면 상태", value = "100% 포장도로", modifier = Modifier.weight(1f))
                InfoTile(icon = Icons.Default.Speed, title = "권장 평균 속도", value = "22 - 25 km/h", modifier = Modifier.weight(1f))
            }

            SectionHeader(title = "주행 데이터", subtitle = "이 코스의 예상 주행 지표입니다")
            Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing), modifier = Modifier.fillMaxWidth()) {
                MetricChip(label = "총 거리", value = formatDistance(resolvedCourse.distanceKm), modifier = Modifier.weight(1f))
                MetricChip(label = "예상 시간", value = "${resolvedCourse.estimatedDurationMin}분", modifier = Modifier.weight(1f))
            }

            if (loading) {
                LoadingStateView("코스 상세 분석 중...")
            } else if (error) {
                ErrorStateView("로드 실패", "코스 데이터를 가져오지 못했습니다.") { }
            }

            SectionHeader(title = "라이더 리뷰", subtitle = "실제 주행자들의 한 줄 후기")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewCard(name = "스피드매니아", daysAgo = "2일 전", body = "노면이 아주 깔끔해서 속도 내기 좋습니다. 중간에 쉬기 좋은 포인트도 적당합니다.")
                ReviewCard(name = "밤바람러너", daysAgo = "1주일 전", body = "야경이 멋지고 루트가 직관적입니다. 주말에는 보행자가 많아 속도 조절이 필요합니다.")
            }

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                GajaPrimaryButton(
                    text = "이 코스로 시작하기",
                    enabled = !loading && !error,
                    onClick = onStartRide,
                )
                SecondaryActionButton(text = "돌아가기", onClick = onBack)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SmallBadge(text: String, primary: Boolean = false) {
    Surface(
        color = if (primary) GajaColors.TertiaryContainer.copy(alpha = 0.16f) else GajaColors.SurfaceContainerHigh,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (primary) GajaColors.Tertiary else GajaColors.TextSecondary,
        )
    }
}

@Composable
private fun HeroMetric(label: String, value: String, unit: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Black)
            if (unit != null) {
                Text(text = unit, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun InfoTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = GajaColors.TextSecondary, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            }
            Text(value, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReviewCard(name: String, daysAgo: String, body: String) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(name, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text("★★★★★", style = MaterialTheme.typography.bodySmall, color = GajaColors.PrimaryContainer)
                }
                Text(daysAgo, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary)
            }
            Text(body, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
        }
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}m" else "%.1fkm".format(distanceKm)
}
