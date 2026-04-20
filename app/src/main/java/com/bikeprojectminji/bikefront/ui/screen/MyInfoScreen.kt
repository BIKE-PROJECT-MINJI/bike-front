package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaIconTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private sealed class ProfileState {
    data object Loading : ProfileState()
    data class Loaded(
        val displayName: String,
        val totalDistance: String = "1,248 km",
        val totalElevation: String = "8,520 m",
        val totalRides: String = "42",
        val avgSpeed: String = "18.5 km/h",
    ) : ProfileState()
    data object NotLoggedIn : ProfileState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInfoScreen(
    innerPadding: PaddingValues,
    onOpenProfile: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authSessionStore = remember { AuthSessionStore(context) }
    var profileState by remember { mutableStateOf<ProfileState>(ProfileState.Loading) }
    var isRefreshing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, authSessionStore) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileState = ProfileState.Loading
                val displayName = authSessionStore.displayName?.takeIf { it.isNotBlank() }
                profileState = if (displayName != null) {
                    ProfileState.Loaded(displayName = displayName)
                } else {
                    ProfileState.NotLoggedIn
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = { GajaBrandTopBar(title = "Profile") },
        containerColor = GajaColors.Background
    ) { scaffoldPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                val displayName = authSessionStore.displayName?.takeIf { it.isNotBlank() }
                profileState = if (displayName != null) ProfileState.Loaded(displayName = displayName) else ProfileState.NotLoggedIn
                isRefreshing = false
            },
            modifier = Modifier.padding(scaffoldPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = GajaSpacing.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)
            ) {
                Spacer(Modifier.height(GajaSpacing.Small))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
                    exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(),
                ) {
                    when (val state = profileState) {
                        is ProfileState.Loading -> LoadingStateView("프로필 분석 중...")
                        is ProfileState.NotLoggedIn -> NotLoggedInContent(onOpenProfile = onOpenProfile)
                        is ProfileState.Loaded -> ProfileContent(state, onOpenProfile)
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun NotLoggedInContent(onOpenProfile: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)) {
        HeroCard(
            title = "성장을 기록하세요",
            description = "로그인하면 모든 라이딩 데이터가 통계로 요약됩니다.",
            buttonText = "로그인 / 회원가입",
            onClick = onOpenProfile,
            icon = "Guest Mode"
        )

        SectionHeader(title = "주요 기능", subtitle = "회원가입 시 제공되는 프로 서비스")
        
        Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
            listOf("주행 경로 저장 및 공유", "상세 주행 통계 분석", "나만의 코스 만들기", "라이딩 이력 관리").forEach { text ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = GajaColors.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.Border)
                ) {
                    Row(
                        modifier = Modifier.padding(GajaSpacing.Medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(GajaIconTokens.Success, contentDescription = null, tint = GajaColors.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(text, style = MaterialTheme.typography.bodyLarge, color = GajaColors.TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(state: ProfileState.Loaded, onOpenProfile: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Large)) {
        // High Impact Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = GajaColors.TextPrimary)
        ) {
            Row(
                modifier = Modifier.padding(GajaSpacing.Large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
            ) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(GajaColors.Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.displayName.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, color = GajaColors.TextPrimary)
                }
                Column {
                    Text(state.displayName, style = MaterialTheme.typography.headlineLarge, color = GajaColors.White)
                    Text("Pro Rider", style = MaterialTheme.typography.labelMedium, color = GajaColors.Accent)
                }
            }
        }

        SectionHeader(title = "종합 통계", subtitle = "나의 라이딩 성과 요약")
        
        Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
            MetricChip("전체 거리", state.totalDistance, Modifier.weight(1f))
            MetricChip("획득 고도", state.totalElevation, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
            MetricChip("주행 횟수", state.totalRides, Modifier.weight(1f))
            MetricChip("평균 속도", state.avgSpeed, Modifier.weight(1f))
        }

        SectionHeader(title = "활동", subtitle = "데이터 관리")
        Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing)) {
            ActivityRow(GajaIconTokens.Saved, "저장된 코스", "내가 보관한 주행 경로")
            ActivityRow(GajaIconTokens.Stats, "내 기록실", "월간/연간 주행 데이터 분석")
        }

        SectionHeader(title = "설정")
        GajaPrimaryButton("프로필 수정", onClick = onOpenProfile)
        SecondaryActionButton("로그아웃", onClick = { /* TODO */ })
    }
}

@Composable
private fun ActivityRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = GajaColors.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(GajaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)
        ) {
            Icon(icon, contentDescription = null, tint = GajaColors.Primary, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary)
            }
            Icon(GajaIconTokens.Direction, contentDescription = null, tint = GajaColors.TextTertiary, modifier = Modifier.size(20.dp))
        }
    }
}
