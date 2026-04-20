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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RideStartScreen(
    innerPadding: PaddingValues,
    onStartFreeRide: () -> Unit,
    onOpenCourse: (CourseCardUiModel) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { CoursesRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var refreshKey by remember { mutableStateOf(0) }

    var featuredState by remember { mutableStateOf<SectionState<List<CourseCardUiModel>>>(SectionState.Loading) }
    var listState by remember { mutableStateOf<SectionState<CoursesPageUiModel>>(SectionState.Loading) }
    var loadingMore by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refreshKey) {
        featuredState = SectionState.Loading
        listState = SectionState.Loading

        launch {
            val result = runCatching { withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() } }
            featuredState = result.fold({ SectionState.Success(it) }, { SectionState.Error("추천 코스 로드 실패") })
        }
        launch {
            val result = runCatching { withContext(Dispatchers.IO) { repository.fetchAllCourses(limit = 10) } }
            listState = result.fold({ SectionState.Success(it) }, { SectionState.Error("전체 목록 로드 실패") })
        }
    }

    Scaffold(
        topBar = { GajaBrandTopBar(title = "Home") },
        bottomBar = { HomeBottomBar() },
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

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "안녕하세요, 라이더님!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GajaColors.TextPrimary,
                )
                Text(
                    text = "오늘도 힘차게 달려볼까요?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GajaColors.TextSecondary,
                )
            }

            HeroCard(
                title = "자유 주행 시작",
                description = "코스 없이도 지금 바로 라이딩을 시작하고, 기록은 자동으로 남겨보세요.",
                buttonText = "기록 시작하기",
                onClick = onStartFreeRide,
                icon = "premium",
            )

            SectionHeader(
                title = "추천 코스",
                subtitle = "오늘 달리기 좋은 프리미엄 루트",
                action = {
                    Text(
                        text = "FEATURED",
                        style = MaterialTheme.typography.labelSmall,
                        color = GajaColors.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )

            when (val state = featuredState) {
                is SectionState.Loading -> LoadingStateView("추천 코스 분석 중...")
                is SectionState.Error -> ErrorStateView("오류 발생", state.message) { refreshKey++ }
                is SectionState.Success -> FeaturedCoursesSection(
                    courses = state.data,
                    onOpenCourse = onOpenCourse,
                )
            }

            SectionHeader(
                title = "전체 목록",
                subtitle = "거리와 난이도로 비교해 고르세요",
                action = {
                    Text(
                        text = "더보기",
                        style = MaterialTheme.typography.labelLarge,
                        color = GajaColors.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )

            when (val state = listState) {
                is SectionState.Loading -> LoadingStateView("코스 목록 검색 중...")
                is SectionState.Error -> ErrorStateView("오류 발생", state.message) { refreshKey++ }
                is SectionState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                        state.data.items.forEach { course ->
                            CourseCard(course = course, onClick = { onOpenCourse(course) })
                        }

                        if (state.data.hasNext) {
                            SecondaryActionButton(
                                text = if (loadingMore) "로딩 중..." else "코스 더 보기",
                                enabled = !loadingMore,
                                onClick = {
                                    val nextCursor = state.data.nextCursor ?: return@SecondaryActionButton
                                    coroutineScope.launch {
                                        loadingMore = true
                                        val next = runCatching {
                                            withContext(Dispatchers.IO) { repository.fetchAllCourses(cursor = nextCursor) }
                                        }
                                        next.onSuccess { newData ->
                                            listState = SectionState.Success(
                                                state.data.copy(
                                                    items = state.data.items + newData.items,
                                                    hasNext = newData.hasNext,
                                                    nextCursor = newData.nextCursor,
                                                ),
                                            )
                                        }
                                        loadingMore = false
                                    }
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeaturedCoursesSection(
    courses: List<CourseCardUiModel>,
    onOpenCourse: (CourseCardUiModel) -> Unit,
) {
    if (courses.isEmpty()) {
        EmptyStateView("추천 코스 없음", "지금은 보여드릴 추천 코스가 없습니다.")
        return
    }

    val primary = courses.first()
    val secondary = courses.drop(1).take(2)

    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
        FeaturedPrimaryCard(course = primary, onClick = { onOpenCourse(primary) })
        secondary.forEachIndexed { index, course ->
            FeaturedCompactCard(
                course = course,
                badge = if (index == 0) "도전 코스" else "힐링 라이딩",
                onClick = { onOpenCourse(course) },
            )
        }
    }
}

@Composable
private fun FeaturedPrimaryCard(course: CourseCardUiModel, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(GajaColors.BrandGradient))
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TinyOverlayChip("초보자 추천")
                    TinyOverlayChip("평지 위주")
                }
                Spacer(Modifier.height(92.dp))
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = GajaColors.White,
                    fontWeight = FontWeight.Black,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    WhiteMeta("거리", formatDistance(course.distanceKm))
                    WhiteMeta("예상", "${course.estimatedDurationMin}분")
                }
            }
        }
    }
}

@Composable
private fun FeaturedCompactCard(course: CourseCardUiModel, badge: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = GajaColors.SurfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = Brush.verticalGradient(listOf(GajaColors.PrimaryContainer.copy(alpha = 0.9f), GajaColors.Primary)),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.DirectionsBike, contentDescription = null, tint = GajaColors.White)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = badge, style = MaterialTheme.typography.labelSmall, color = GajaColors.Primary)
                Text(text = course.title, style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    text = "${if (course.distanceKm > 12) "고도 포함" else "자연 풍경"} • ${formatDistance(course.distanceKm)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GajaColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun TinyOverlayChip(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.18f),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = GajaColors.White,
        )
    }
}

@Composable
private fun WhiteMeta(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = GajaColors.White.copy(alpha = 0.8f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = GajaColors.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeBottomBar() {
    Surface(
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 12.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeNavItem("홈", Icons.Default.Home, active = true)
            HomeNavItem("탐색", Icons.Default.Explore)
            HomeNavItem("라이딩", Icons.Default.DirectionsBike)
            HomeNavItem("그룹", Icons.Default.Group)
            HomeNavItem("내 정보", Icons.Default.Person)
        }
    }
}

@Composable
private fun HomeNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean = false) {
    val color = if (active) GajaColors.PrimaryContainer else GajaColors.TextSecondary.copy(alpha = 0.7f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}m" else "%.1fkm".format(distanceKm)
}
