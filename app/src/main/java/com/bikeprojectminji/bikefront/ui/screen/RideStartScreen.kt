package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    var loadMoreError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(refreshKey) {
        featuredState = SectionState.Loading
        listState = SectionState.Loading
        loadingMore = false
        loadMoreError = null

        launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() }
            }
            featuredState = result.fold(
                onSuccess = { SectionState.Success(it) },
                onFailure = { SectionState.Error("추천 코스를 불러오지 못했습니다.") },
            )
        }

        launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { repository.fetchAllCourses(limit = 10) }
            }
            listState = result.fold(
                onSuccess = { SectionState.Success(it) },
                onFailure = { SectionState.Error("전체 코스를 불러오지 못했습니다.") },
            )
        }
    }

    val featuredError = featuredState is SectionState.Error
    val listError = listState is SectionState.Error

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            GajaBrandTopBar(title = "홈")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = GajaSpacing.ScreenPadding,
                        vertical = GajaSpacing.Large,
                    ),
                verticalArrangement = Arrangement.spacedBy(GajaSpacing.ScreenPadding),
            ) {
                if (featuredError && listError) {
                    ErrorStateView(
                        title = "코스 정보를 불러오지 못했습니다",
                        message = "잠시 후 다시 시도해 주세요.",
                        onRetry = { refreshKey++ },
                    )
                } else {
                    HeroCard(
                        title = "바로 출발",
                        description = "지금 위치에서 바로 기록을 시작하고, 필요할 때 코스로 전환할 수 있습니다.",
                        buttonText = "자유 주행 열기",
                        onClick = onStartFreeRide,
                        icon = "자유 주행",
                    )

                    SectionHeader(
                        title = "추천 코스",
                        subtitle = "빠르게 출발하기 좋은 코스",
                    )
                    when (val currentFeaturedState = featuredState) {
                        SectionState.Loading -> LoadingStateView(message = "추천 코스를 불러오는 중입니다.")
                        is SectionState.Error -> ErrorStateView(
                            title = "추천 코스를 불러오지 못했습니다",
                            message = currentFeaturedState.message,
                            onRetry = { refreshKey++ },
                        )
                        is SectionState.Success -> {
                            val featuredCourses = currentFeaturedState.data
                            if (featuredCourses.isEmpty()) {
                                EmptyStateView(
                                    title = "추천 코스를 준비 중입니다",
                                    message = "지금은 추천 코스가 없습니다.",
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FeaturedCourseHeroCard(
                                        course = featuredCourses.first(),
                                        onClick = { onOpenCourse(featuredCourses.first()) },
                                    )
                                    featuredCourses.drop(1).take(2).forEach { course ->
                                        CourseCard(course = course, onClick = { onOpenCourse(course) })
                                    }
                                }
                            }
                        }
                    }

                    SectionHeader(
                        title = "전체 코스 목록",
                        subtitle = "거리와 시간 중심으로 바로 비교하는 목록",
                    )
                    when (val currentListState = listState) {
                        SectionState.Loading -> LoadingStateView(message = "전체 코스를 불러오는 중입니다.")
                        is SectionState.Error -> ErrorStateView(
                            title = "전체 코스를 불러오지 못했습니다",
                            message = currentListState.message,
                            onRetry = { refreshKey++ },
                        )
                        is SectionState.Success -> {
                            val page = currentListState.data
                            if (page.items.isEmpty()) {
                                EmptyStateView(
                                    title = "전체 코스를 준비 중입니다",
                                    message = "현재 노출할 코스가 없습니다.",
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    page.items.forEach { course ->
                                        CourseCard(course = course, onClick = { onOpenCourse(course) })
                                    }
                                }
                            }

                            if (page.hasNext) {
                                SecondaryActionButton(
                                    text = if (loadingMore) "불러오는 중..." else "더보기",
                                    enabled = !loadingMore,
                                    onClick = {
                                        val nextCursor = page.nextCursor ?: return@SecondaryActionButton
                                        coroutineScope.launch {
                                            loadingMore = true
                                            loadMoreError = null
                                            val nextPage = runCatching {
                                                withContext(Dispatchers.IO) {
                                                    repository.fetchAllCourses(cursor = nextCursor, limit = 10)
                                                }
                                            }
                                            listState = nextPage.fold(
                                                onSuccess = {
                                                    SectionState.Success(
                                                        page.copy(
                                                            items = page.items + it.items,
                                                            hasNext = it.hasNext,
                                                            nextCursor = it.nextCursor,
                                                        ),
                                                    )
                                                },
                                                onFailure = {
                                                    loadMoreError = "추가 코스를 불러오지 못했습니다."
                                                    currentListState
                                                },
                                            )
                                            loadingMore = false
                                        }
                                    },
                                )
                            }

                            if (!loadMoreError.isNullOrBlank()) {
                                BikeSurfaceCard {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = loadMoreError.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                )
            }
        }
    }
}

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}

@Composable
private fun FeaturedCourseHeroCard(
    course: CourseCardUiModel,
    onClick: () -> Unit,
) {
    HeroCard(
        title = course.title,
        description = "추천 루트로 바로 진입해 라이딩 흐름을 시작합니다.",
        buttonText = "이 코스 시작",
        onClick = onClick,
        gradientColors = listOf(
            MaterialTheme.colorScheme.inverseSurface,
            MaterialTheme.colorScheme.secondaryContainer,
        ),
        icon = "추천 코스",
    )

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricChip(
            label = "거리",
            value = formatDistance(course.distanceKm),
            modifier = Modifier.weight(1f),
        )
        MetricChip(
            label = "예상 시간",
            value = "${course.estimatedDurationMin}분",
            modifier = Modifier.weight(1f),
        )
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
        "${(distanceKm * 1000).toInt()}m"
    } else {
        "%.1f km".format(distanceKm)
    }
}
