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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RideStartScreen(
    innerPadding: PaddingValues,
    onOpenFreeRide: () -> Unit,
    onOpenCourse: (CourseCardUiModel) -> Unit,
    onOpenCourses: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { CoursesRepository(context) }
    var refreshKey by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val featuredCourses = produceState<List<CourseCardUiModel>>(initialValue = emptyList(), key1 = refreshKey) {
        value = runCatching {
            withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() }
        }.getOrDefault(emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // GAJA Hero Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        ),
                    ),
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "GAJA",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "오늘도 안전하게 라이딩하세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Free Ride Primary Card
            GajaPrimaryCard(
                title = "자유 주행",
                description = "코스 없이 바로 기록을 시작하는 모드입니다. 현재 위치에서 바로 시작하세요.",
                buttonText = "자유 주행 시작",
                onClick = onOpenFreeRide,
            )

            // Recommended Courses Section
            SectionTitle(
                title = "추천 코스",
                subtitle = "빠르게 바로 탈 수 있는 코스를 먼저 보여줍니다.",
            )

            if (featuredCourses.value.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = "추천 코스를 불러오는 중이거나 아직 준비되지 않았습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    featuredCourses.value.forEach { course ->
                        CourseCard(course = course, onClick = { onOpenCourse(course) })
                    }
                }
            }

            // Secondary Actions
            GajaSecondaryButton(
                text = "전체 코스 보러 가기",
                onClick = onOpenCourses,
            )

            GajaOutlinedButton(
                text = "내 정보 / 로그인",
                onClick = onOpenProfile,
            )
        }
    }
}