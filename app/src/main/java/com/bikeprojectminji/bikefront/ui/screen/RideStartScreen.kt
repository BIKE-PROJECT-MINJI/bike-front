package com.bikeprojectminji.bikefront.ui.screen

import android.util.Log
import com.bikeprojectminji.bikefront.analytics.AnalyticsTracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bikeprojectminji.bikefront.auth.AuthLoginGateway
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.auth.HttpAuthLoginGateway
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
    onOpenMyInfo: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember(context) { CoursesRepository(context) }
    val authSessionStore = remember(context) { AuthSessionStore(context) }
    val activitySummaryGateway = remember { HttpAuthLoginGateway() }
    val analyticsTracker = remember(context) { AnalyticsTracker(context) }
    var refreshKey by remember { mutableStateOf(0) }

    var activitySummaryState by remember { mutableStateOf<RideStartActivitySummaryState>(RideStartActivitySummaryState.Loading) }
    var featuredState by remember { mutableStateOf<SectionState<List<CourseCardUiModel>>>(SectionState.Loading) }
    var listState by remember { mutableStateOf<SectionState<CoursesPageUiModel>>(SectionState.Loading) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        analyticsTracker.track("course_list_viewed", "course_list", mapOf("source" to "home"))
    }

    LaunchedEffect(refreshKey) {
        activitySummaryState = RideStartActivitySummaryState.Loading
        if (featuredState !is SectionState.Success) featuredState = SectionState.Loading
        if (listState !is SectionState.Success) listState = SectionState.Loading

        loadActivitySummary(authSessionStore, activitySummaryGateway) { result ->
            activitySummaryState = when (result) {
                is ActivitySummaryLoadResult.Success -> RideStartActivitySummaryState.from(result.summary)
                ActivitySummaryLoadResult.SignedOut -> RideStartActivitySummaryState.fromSignedOut()
                is ActivitySummaryLoadResult.Failure -> RideStartActivitySummaryState.fromFailure(result.message)
            }
        }

        launch {
            val result = runCatching { withContext(Dispatchers.IO) { repository.fetchFeaturedCourses() } }
            featuredState = result.fold(
                { SectionState.Success(it) },
                {
                    Log.e("RideStartScreen", "featured load failed", it)
                    SectionState.Error("추천 로드 실패")
                },
            )
        }
        launch {
            val result = runCatching { withContext(Dispatchers.IO) { repository.fetchAllCourses(limit = 10) } }
            listState = result.fold(
                { SectionState.Success(it) },
                {
                    Log.e("RideStartScreen", "course list load failed", it)
                    SectionState.Error("목록 로드 실패")
                },
            )
        }
    }

    Scaffold(
        topBar = { GajaBrandTopBar(title = "탐색", onProfileClick = onOpenMyInfo) },
        containerColor = GajaColors.Background
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. 요약 대시보드
            Box(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                ActivitySummaryDashboard(activitySummaryState)
            }

            // 2. 가로 스크롤 추천 섹션
            Column {
                SectionHeader(
                    title = "인기 라이딩 경로",
                    subtitle = "많이 찾는 코스부터 가볍게 골라보세요",
                    modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding),
                    action = {
                        TextButton(onClick = { }) {
                            Text("전체 보기", color = GajaColors.Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = GajaSpacing.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (val state = featuredState) {
                        is SectionState.Loading -> LoadingStateView("추천 코스를 불러오는 중")
                        is SectionState.Success -> {
                            state.data.forEach { course ->
                                FeaturedCourseGridItem(course = course, onClick = { onOpenCourse(course) })
                            }
                        }
                        is SectionState.Error -> Text("데이터를 불러올 수 없습니다", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // 3. 중앙 액션 버튼 (한글화 및 강조)
            Box(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                CompactFreeRidePanel(onStartFreeRide = onStartFreeRide)
            }

            // 4. 리스트형 주변 코스
            Column(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                SectionHeader(title = "내 주변 코스", subtitle = "지금 위치에서 시작하기 좋은 코스를 모았어요")

                when (val state = listState) {
                    is SectionState.Loading -> LoadingStateView("주변 코스를 찾는 중")
                    is SectionState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            state.data.items.forEach { course ->
                                CourseCard(course = course, onClick = { onOpenCourse(course) })
                            }
                        }
                    }
                    is SectionState.Error -> Text("목록 로드 실패")
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ActivitySummaryDashboard(state: RideStartActivitySummaryState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GajaColors.Surface,
        border = BorderStroke(1.dp, GajaColors.Border)
    ) {
        when (state) {
            RideStartActivitySummaryState.Loading -> LoadingStateView("이번 주 기록을 불러오는 중")
            is RideStartActivitySummaryState.SignedOut -> SummaryMessageContent(
                title = "이번 주 나의 활동",
                message = state.message,
            )
            is RideStartActivitySummaryState.Error -> SummaryMessageContent(
                title = "이번 주 나의 활동",
                message = state.message,
            )
            is RideStartActivitySummaryState.Ready -> {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("이번 주 활동", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    state.primaryDistanceText,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = GajaColors.TextPrimary,
                                    fontWeight = FontWeight.Black,
                                )
                                Text(
                                    "km",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GajaColors.TextTertiary,
                                    modifier = Modifier.padding(bottom = 3.dp, start = 4.dp),
                                )
                            }
                        }

                        Surface(shape = RoundedCornerShape(999.dp), color = GajaColors.PrimaryContainer) {
                            Text(
                                text = state.rideCountText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = GajaColors.Primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Text(state.helperText, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ActivityMetricBlock(label = "주행", value = state.rideCountText, icon = Icons.AutoMirrored.Filled.DirectionsBike, modifier = Modifier.weight(1f))
                        ActivityMetricBlock(label = "시간", value = state.durationText, icon = Icons.Default.History, modifier = Modifier.weight(1f))
                        ActivityMetricBlock(label = "코스", value = state.savedCourseText, icon = Icons.Default.Map, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMessageContent(title: String, message: String) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActivityMetricBlock(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = GajaColors.Background,
        border = BorderStroke(1.dp, GajaColors.Border),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, contentDescription = null, tint = GajaColors.Primary, modifier = Modifier.size(14.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = GajaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun CompactFreeRidePanel(
    onStartFreeRide: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GajaColors.Carbon,
        border = BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.05f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(shape = RoundedCornerShape(999.dp), color = GajaColors.Primary.copy(alpha = 0.16f)) {
                        Text(
                            text = "자유 주행",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = GajaColors.Primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "코스 없이 바로 출발",
                        style = MaterialTheme.typography.titleLarge,
                        color = GajaColors.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "현재 위치에서 HUD를 켜고 바로 기록을 시작할 수 있어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GajaColors.White.copy(alpha = 0.74f),
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                    contentDescription = null,
                    tint = GajaColors.White.copy(alpha = 0.78f),
                    modifier = Modifier.size(22.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CompactStatusPill(text = "즉시 시작", modifier = Modifier.weight(1f))
                CompactStatusPill(text = "기록 저장", modifier = Modifier.weight(1f))
                CompactStatusPill(text = "HUD 진입", modifier = Modifier.weight(1f))
            }

            GajaPrimaryButton(
                text = "자유 주행 시작",
                onClick = onStartFreeRide,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
            )
        }
    }
}

@Composable
private fun CompactStatusPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GajaColors.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.06f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelSmall,
            color = GajaColors.White.copy(alpha = 0.84f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun FeaturedCourseGridItem(course: CourseCardUiModel, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(214.dp)
            .height(142.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = GajaColors.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.Border),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Surface(color = GajaColors.PrimaryContainer, shape = RoundedCornerShape(999.dp)) {
                    Text("많이 찾는 코스", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = GajaColors.Primary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Text(course.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(
                    text = "탭해서 코스 미리보기",
                    style = MaterialTheme.typography.bodySmall,
                    color = GajaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeaturedCourseMeta(Icons.Default.Map, "${course.distanceKm}km")
                    FeaturedCourseMeta(Icons.Default.History, "${course.estimatedDurationMin}분")
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = GajaColors.TextTertiary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun FeaturedCourseMeta(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = GajaColors.TextSecondary, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary, fontWeight = FontWeight.Medium)
    }
}

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}
