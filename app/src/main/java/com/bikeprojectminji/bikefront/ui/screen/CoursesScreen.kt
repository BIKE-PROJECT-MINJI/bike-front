package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bikeprojectminji.bikefront.ui.screen.CourseCard
import com.bikeprojectminji.bikefront.ui.screen.EmptyStateView
import com.bikeprojectminji.bikefront.ui.screen.ErrorStateView
import com.bikeprojectminji.bikefront.ui.screen.GajaBrandTopBar
import com.bikeprojectminji.bikefront.ui.screen.GajaSectionCard
import com.bikeprojectminji.bikefront.ui.screen.LoadingStateView
import com.bikeprojectminji.bikefront.ui.screen.SectionHeader
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class CourseTab(val label: String, val description: String) {
    RECOMMENDED("추천", "엄선된 인기 코스"),
    ALL("전체", "공개 코스 둘러보기"),
}

private sealed class CoursesLoadState {
    data object Loading : CoursesLoadState()
    data class Success(val courses: List<CourseCardUiModel>) : CoursesLoadState()
    data class Error(val message: String) : CoursesLoadState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    innerPadding: PaddingValues,
    onOpenCourse: (CourseCardUiModel) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { CoursesRepository(context) }
    var selectedTab by remember { mutableStateOf(CourseTab.RECOMMENDED) }
    var refreshKey by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val featuredState = produceState<CoursesLoadState>(initialValue = CoursesLoadState.Loading, key1 = refreshKey) {
        value = CoursesLoadState.Loading
        value = runCatching {
            val courses = withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() }
            CoursesLoadState.Success(courses)
        }.getOrElse { CoursesLoadState.Error(it.message ?: "코스를 불러오지 못했습니다") }
    }

    val allCoursesState = produceState<CoursesLoadState>(initialValue = CoursesLoadState.Loading, key1 = refreshKey) {
        value = CoursesLoadState.Loading
        value = runCatching {
            val page = withContext(Dispatchers.IO) { repository.fetchAllCourses() }
            CoursesLoadState.Success(page.items)
        }.getOrElse { CoursesLoadState.Error(it.message ?: "코스를 불러오지 못했습니다") }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = GajaColors.Background,
        topBar = { GajaBrandTopBar(title = "코스") },
    ) { scaffoldPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                refreshKey++
                isRefreshing = false
            },
            modifier = Modifier.padding(scaffoldPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = GajaSpacing.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(GajaSpacing.SectionGap)
            ) {
                Spacer(Modifier.height(GajaSpacing.Small))
                
                SectionHeader(
                    title = "코스 탐색",
                    subtitle = "거리와 시간 정보를 한눈에 비교하세요"
                )

                CourseTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                )

                Text(
                    text = "추천에서는 엄선된 코스를, 전체에서는 공개된 코스를 함께 볼 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GajaColors.TextSecondary,
                )

                val loadState = when (selectedTab) {
                    CourseTab.RECOMMENDED -> featuredState.value
                    CourseTab.ALL -> allCoursesState.value
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
                    exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(),
                    modifier = Modifier.weight(1f)
                ) {
                    when (loadState) {
                        is CoursesLoadState.Loading -> LoadingStateView("코스 탐색 중...")
                        is CoursesLoadState.Error -> ErrorStateView("오류", loadState.message) { refreshKey++ }
                        is CoursesLoadState.Success -> {
                            if (loadState.courses.isEmpty()) {
                                EmptyStateView("결과 없음", "표시할 코스가 없습니다.")
                            } else {
                                CourseListContent(loadState.courses, onOpenCourse)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseTabSelector(selectedTab: CourseTab, onTabSelected: (CourseTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
        CourseTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            GajaSectionCard(
                modifier = Modifier.weight(1f),
                containerColor = if (isSelected) GajaColors.Primary else GajaColors.White,
                borderColor = if (isSelected) GajaColors.Primary else GajaColors.Border,
                contentColor = if (isSelected) GajaColors.White else GajaColors.TextPrimary,
                contentPadding = PaddingValues(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Medium),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .wrapContentHeight()
                        .clickable { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(GajaSpacing.Micro),
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) GajaColors.White else GajaColors.TextPrimary
                    )
                    Text(
                        text = tab.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) GajaColors.White.copy(alpha = 0.82f) else GajaColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseListContent(
    courses: List<CourseCardUiModel>,
    onOpenCourse: (CourseCardUiModel) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
        items(courses, key = { it.id }) { course ->
            CourseCard(course = course, onClick = { onOpenCourse(course) })
        }
        item { Spacer(Modifier.height(40.dp)) }
    }
}
