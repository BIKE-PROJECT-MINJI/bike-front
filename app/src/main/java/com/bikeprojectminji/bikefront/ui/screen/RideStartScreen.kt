package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        topBar = { GajaBrandTopBar(title = "Explore") },
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

            // Main CTA: Free Ride
            HeroCard(
                title = "자유 주행 시작",
                description = "코스 없이 마음 가는 대로 라이딩을 기록하세요.",
                buttonText = "기록 시작하기",
                onClick = onStartFreeRide,
                icon = "Quick Start"
            )

            // Featured Section
            SectionHeader(
                title = "추천 코스",
                subtitle = "오늘 달리기 좋은 최적의 경로",
                action = {
                    Text("전체보기", style = MaterialTheme.typography.labelLarge, color = GajaColors.Primary)
                }
            )
            
            when (val state = featuredState) {
                is SectionState.Loading -> LoadingStateView("추천 코스 분석 중...")
                is SectionState.Error -> ErrorStateView("오류 발생", state.message) { refreshKey++ }
                is SectionState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
                        state.data.take(3).forEach { course ->
                            CourseCard(course = course, onClick = { onOpenCourse(course) })
                        }
                    }
                }
            }

            // All Courses Section
            SectionHeader(title = "주변 코스", subtitle = "내 주변 라이더들이 선호하는 코스")
            
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
                                                    nextCursor = newData.nextCursor
                                                )
                                            )
                                        }
                                        loadingMore = false
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}
