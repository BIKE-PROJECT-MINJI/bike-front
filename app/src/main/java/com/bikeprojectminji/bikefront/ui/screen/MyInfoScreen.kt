package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.auth.AuthLoginGateway
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.auth.HttpAuthLoginGateway
import com.bikeprojectminji.bikefront.ui.theme.GajaCardTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaIconTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaIconSizes
import com.bikeprojectminji.bikefront.ui.theme.GajaRadius
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Loaded(
        val displayName: String,
        val activitySummary: AuthLoginGateway.ActivitySummaryResult? = null,
        val summaryErrorMessage: String? = null,
    ) : ProfileState()
    data object NotLoggedIn : ProfileState()
}

val ProfileState.Loaded.totalDistance: String
    get() = activitySummary?.let { "${formatGroupedOneDecimal(it.overallSummary.totalDistanceKm)} km" } ?: "--"

val ProfileState.Loaded.totalElevation: String
    get() = activitySummary?.let { "${it.overallSummary.totalElevationM.toInt()} m" } ?: "--"

val ProfileState.Loaded.totalRides: String
    get() = activitySummary?.let { it.overallSummary.totalRides.toString() } ?: "--"

val ProfileState.Loaded.avgSpeed: String
    get() = activitySummary?.let { "${formatOneDecimal(it.overallSummary.avgSpeedKmh)} km/h" } ?: "--"

val ProfileState.Loaded.isWeeklySummaryEmpty: Boolean
    get() = activitySummary?.let {
        it.weeklySummary.distanceKm <= 0.0 && it.weeklySummary.rideCount == 0L && it.weeklySummary.durationMinutes == 0L && it.weeklySummary.savedCourseCount == 0L
    } == true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInfoScreen(
    innerPadding: PaddingValues,
    onOpenProfile: () -> Unit,
    onOpenCourses: () -> Unit,
    onOpenRideRecords: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authSessionStore = remember { AuthSessionStore(context) }
    val activitySummaryGateway = remember { HttpAuthLoginGateway() }
    var profileState by remember { mutableStateOf<ProfileState>(ProfileState.Loading) }
    var isRefreshing by remember { mutableStateOf(false) }

    fun loadProfileState() {
        val displayName = authSessionStore.displayName.takeIf { it.isNotBlank() } ?: "라이더"
        requestMyInfoActivitySummary(authSessionStore, activitySummaryGateway) { result ->
            profileState = when (result) {
                is ActivitySummaryLoadResult.Success -> ProfileState.Loaded(
                    displayName = displayName,
                    activitySummary = result.summary,
                )

                ActivitySummaryLoadResult.SignedOut -> ProfileState.NotLoggedIn

                is ActivitySummaryLoadResult.Failure -> ProfileState.Loaded(
                    displayName = displayName,
                    activitySummary = null,
                    summaryErrorMessage = result.message,
                )
            }
            isRefreshing = false
        }
    }

    DisposableEffect(lifecycleOwner, authSessionStore) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileState = ProfileState.Loading
                loadProfileState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = { GajaBrandTopBar(title = "내 정보") },
        containerColor = GajaColors.Background
    ) { scaffoldPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                loadProfileState()
            },
            modifier = Modifier
                .padding(innerPadding)
                .padding(scaffoldPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = GajaSpacing.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(GajaSpacing.SectionGap)
            ) {
                Spacer(Modifier.height(GajaSpacing.Small))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
                    exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(),
                ) {
                    when (val state = profileState) {
                        is ProfileState.Loading -> LoadingStateView("내 정보를 불러오는 중")
                        is ProfileState.NotLoggedIn -> NotLoggedInContent(onOpenProfile = onOpenProfile)
                        is ProfileState.Loaded -> ProfileContent(state, onOpenProfile, onOpenCourses, onOpenRideRecords)
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun NotLoggedInContent(onOpenProfile: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)) {
        HeroCard(
            title = "성장을 기록하세요",
            description = "로그인하면 저장한 코스와 주행 기록을 한곳에서 이어서 볼 수 있어요.",
            buttonText = "로그인 / 회원가입",
            onClick = onOpenProfile,
            icon = "계정 연결"
        )

        ActivityRow(
            icon = GajaIconTokens.Profile,
            title = "로그인 / 회원가입",
            desc = "내 기록과 저장한 코스를 같은 계정으로 이어서 관리하기",
            onClick = onOpenProfile,
        )

        SectionHeader(title = "로그인하면 바로 할 수 있는 일", subtitle = "필요한 기능만 간단히 안내해 드려요")
        
        Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
            listOf("주행 경로 저장 및 공유", "상세 주행 통계 분석", "나만의 코스 만들기", "라이딩 이력 관리").forEach { text ->
                GajaSectionCard(contentPadding = PaddingValues(GajaCardTokens.DefaultPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
                    ) {
                        Icon(GajaIconTokens.Success, contentDescription = null, tint = GajaColors.Primary, modifier = Modifier.size(GajaIconSizes.Medium))
                        Text(text, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState.Loaded,
    onOpenProfile: () -> Unit,
    onOpenCourses: () -> Unit,
    onOpenRideRecords: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)) {
        GajaSectionCard(contentPadding = PaddingValues(GajaCardTokens.ElevatedPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Medium),
                ) {
                    Box(
                        modifier = Modifier.size(58.dp).clip(CircleShape).background(GajaColors.Carbon),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(state.displayName.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = GajaColors.White)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(state.displayName, style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text("라이딩 통계 대시보드", style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary)
                    }
                    GajaStatusBadge(text = "프로필")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
                ) {
                    GajaMetricCard(label = "거리", value = state.totalDistance, modifier = Modifier.weight(1f), emphasized = true)
                    GajaMetricCard(label = "라이드", value = state.totalRides, modifier = Modifier.weight(1f))
                    GajaMetricCard(label = "속도", value = state.avgSpeed, modifier = Modifier.weight(1f))
                }
            }
        }

        SectionHeader(title = "종합 통계", subtitle = "전체 기록을 빠르게 훑을 수 있게 밀도를 높였어요")
        state.summaryErrorMessage?.let { message ->
            GajaSectionCard {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GajaColors.TextSecondary,
                )
            }
        }
        if (state.isWeeklySummaryEmpty) {
            Text(
                text = "이번 주 활동은 아직 비어 있습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = GajaColors.TextSecondary,
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
            DashboardMetricCard("전체 거리", state.totalDistance, Modifier.weight(1f), emphasized = true)
            DashboardMetricCard("획득 고도", state.totalElevation, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
            DashboardMetricCard("주행 횟수", state.totalRides, Modifier.weight(1f))
            DashboardMetricCard("평균 속도", state.avgSpeed, Modifier.weight(1f))
        }

        SectionHeader(title = "바로가기", subtitle = "기록 아래에서 바로 다음 행동으로 이어지게 정리했어요")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactShortcutRow(
                icon = GajaIconTokens.Saved,
                title = "저장된 코스",
                desc = "저장한 코스 확인",
                onClick = onOpenCourses,
            )
            CompactShortcutRow(
                icon = GajaIconTokens.Stats,
                title = "주행 기록",
                desc = "기록 상태와 연결 코스 확인",
                onClick = onOpenRideRecords,
            )
            CompactShortcutRow(
                icon = GajaIconTokens.Profile,
                title = "계정 관리",
                desc = "내 정보 정리",
                onClick = onOpenProfile,
            )
        }

        SectionHeader(title = "설정")
        GajaPrimaryButton("프로필 수정", onClick = onOpenProfile)
        SecondaryActionButton("로그아웃", onClick = { /* TODO */ })
    }
}

@Composable
private fun DashboardMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    GajaMetricCard(
        label = label,
        value = value,
        modifier = modifier,
        emphasized = emphasized,
        containerColor = GajaColors.Surface,
    )
}

@Composable
private fun CompactShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState().value
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(GajaRadius.Small),
        color = if (pressed) GajaColors.PrimaryContainer else GajaColors.Surface,
        border = BorderStroke(1.dp, GajaColors.Border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GajaCardTokens.DefaultPadding, vertical = GajaSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
        ) {
            Surface(shape = RoundedCornerShape(GajaRadius.Small), color = GajaColors.PrimaryContainer) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = GajaColors.Primary,
                    modifier = Modifier.padding(10.dp).size(GajaIconSizes.Medium),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary)
            }
            Icon(GajaIconTokens.Direction, contentDescription = null, tint = GajaColors.TextTertiary, modifier = Modifier.size(GajaIconSizes.Medium))
        }
    }
}

@Composable
private fun ActivityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    onClick: (() -> Unit)? = null,
) {
    GajaSectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentPadding = PaddingValues(GajaCardTokens.DefaultPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
        ) {
            Icon(icon, contentDescription = null, tint = GajaColors.Primary, modifier = Modifier.size(GajaIconSizes.Large))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary)
            }
            Icon(GajaIconTokens.Direction, contentDescription = null, tint = GajaColors.TextTertiary, modifier = Modifier.size(GajaIconSizes.Medium))
        }
    }
}

private fun requestMyInfoActivitySummary(
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

private fun formatOneDecimal(value: Double): String = String.format(java.util.Locale.US, "%.1f", value)

private fun formatGroupedOneDecimal(value: Double): String = String.format(java.util.Locale.US, "%,.1f", value)
