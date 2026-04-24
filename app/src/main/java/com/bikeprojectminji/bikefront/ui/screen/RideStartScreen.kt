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
import com.bikeprojectminji.bikefront.ui.theme.GajaCardTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaControlTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaIconSizes
import com.bikeprojectminji.bikefront.ui.theme.GajaRadius
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

        requestRideStartActivitySummary(authSessionStore, activitySummaryGateway) { result ->
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
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.SectionGap)
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
                    horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small)
                ) {
                    when (val state = featuredState) {
                        is SectionState.Loading -> LoadingStateView("추천 코스를 불러오는 중")
                        is SectionState.Success -> {
                            state.data.forEach { course ->
                                FeaturedCourseGridItem(course = course, onClick = { onOpenCourse(course) })
                            }
                        }
                        is SectionState.Error -> Text("데이터를 불러올 수 없습니다", modifier = Modifier.padding(GajaSpacing.Medium))
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
    GajaSectionCard {
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
                    verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small)
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

                        GajaStatusBadge(text = state.rideCountText)
                    }

                    Text(state.helperText, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
                    ) {
                        GajaMetricCard(label = "주행", value = state.rideCountText, icon = Icons.AutoMirrored.Filled.DirectionsBike, modifier = Modifier.weight(1f), emphasized = true)
                        GajaMetricCard(label = "시간", value = state.durationText, icon = Icons.Default.History, modifier = Modifier.weight(1f))
                        GajaMetricCard(label = "코스", value = state.savedCourseText, icon = Icons.Default.Map, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMessageContent(title: String, message: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CompactFreeRidePanel(
    onStartFreeRide: () -> Unit,
) {
    GajaSectionCard(
        containerColor = GajaColors.Carbon,
        borderColor = GajaColors.White.copy(alpha = 0.05f),
        contentPadding = PaddingValues(GajaCardTokens.ElevatedPadding),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(GajaSpacing.Micro),
                ) {
                    GajaStatusBadge(
                        text = "자유 주행",
                        containerColor = GajaColors.Primary.copy(alpha = 0.16f),
                        contentColor = GajaColors.Primary,
                    )
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
                    modifier = Modifier.size(GajaIconSizes.Control),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
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
    GajaInfoPill(
        text = text,
        modifier = modifier,
        containerColor = GajaColors.White.copy(alpha = 0.08f),
        contentColor = GajaColors.White.copy(alpha = 0.84f),
    )
}

@Composable
fun FeaturedCourseGridItem(course: CourseCardUiModel, onClick: () -> Unit) {
    GajaSectionCard(
        modifier = Modifier
            .width(214.dp)
            .height(152.dp)
            .clickable { onClick() },
        contentPadding = PaddingValues(GajaCardTokens.DefaultPadding),
        shadowElevation = GajaCardTokens.SubtleElevation,
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                GajaStatusBadge(text = "많이 찾는 코스")
                Spacer(Modifier.height(GajaSpacing.Tiny))
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
                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
                    FeaturedCourseMeta(Icons.Default.Map, "${course.distanceKm}km")
                    FeaturedCourseMeta(Icons.Default.History, "${course.estimatedDurationMin}분")
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = GajaColors.TextTertiary,
                    modifier = Modifier.size(GajaIconSizes.Medium),
                )
            }
        }
    }
}

@Composable
private fun FeaturedCourseMeta(icon: ImageVector, text: String) {
    GajaInfoPill(text = text, icon = icon)
}

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}

private fun requestRideStartActivitySummary(
    authSessionStore: AuthSessionStore,
    gateway: AuthLoginGateway,
    onResult: (ActivitySummaryLoadResult) -> Unit,
) {
    if (!authSessionStore.isSignedIn) {
        onResult(ActivitySummaryLoadResult.SignedOut)
        return
    }

    val accessToken = authSessionStore.accessToken
    if (accessToken.isBlank()) {
        onResult(ActivitySummaryLoadResult.Failure("로그인 정보가 필요합니다."))
        return
    }

    gateway.getMyActivitySummary(accessToken, object : AuthLoginGateway.ActivitySummaryCallback {
        override fun onSuccess(result: AuthLoginGateway.ActivitySummaryResult) {
            onResult(ActivitySummaryLoadResult.Success(result))
        }

        override fun onFailure(message: String) {
            onResult(ActivitySummaryLoadResult.Failure(message.ifBlank { "활동 요약을 확인하지 못했습니다." }))
        }
    })
}
