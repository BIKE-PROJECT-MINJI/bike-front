package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.ui.theme.GajaIconTokens
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// === Profile State ===
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
    val authSessionStore = remember(context) { AuthSessionStore(context) }
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {},
    ) { scaffoldPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                val displayName = authSessionStore.displayName?.takeIf { it.isNotBlank() }
                profileState = if (displayName != null) {
                    ProfileState.Loaded(displayName = displayName)
                } else {
                    ProfileState.NotLoggedIn
                }
                isRefreshing = false
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    )
                    .verticalScroll(rememberScrollState()),
            ) {
                GajaBrandTopBar(title = "내 정보", subtitle = "주행 기록과 계정을 확인하세요", showSettings = true)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
                    exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(),
                ) {
                    when (val state = profileState) {
                        is ProfileState.Loading -> {
                            ProfileLoadingState()
                        }
                        is ProfileState.NotLoggedIn -> {
                            NotLoggedInContent(onOpenProfile = onOpenProfile)
                        }
                        is ProfileState.Loaded -> {
                            ProfileContent(
                                displayName = state.displayName,
                                totalDistance = state.totalDistance,
                                totalElevation = state.totalElevation,
                                totalRides = state.totalRides,
                                avgSpeed = state.avgSpeed,
                                onOpenProfile = onOpenProfile,
                            )
                        }
                    }
                }
            }
        }
    }
}

// === Loading State ===
@Composable
private fun ProfileLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )
            Text(
                text = "프로필을 불러오는 중...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// === Not Logged In Content ===
@Composable
private fun NotLoggedInContent(onOpenProfile: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.inverseSurface,
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ),
                    )
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                ) {
                    Text(
                        text = "게스트 모드",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = "로그인하면 라이딩 기록이 쌓입니다",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                )
                Text(
                    text = "주행 기록 저장, 코스 보관, 계정 기반 동기화까지 한 흐름으로 이어집니다. 지금은 둘러보기만 가능하고 데이터는 남지 않습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.84f),
                )
                PrimaryActionButton(
                    text = "로그인 / 회원가입",
                    onClick = onOpenProfile,
                )
            }
        }

        SectionHeader(
            title = "로그인 후 이용 가능",
            subtitle = "계정이 생기면 아래 기능이 실제 자산으로 누적됩니다",
        )

        GajaFeatureCard(
            features = listOf(
                FeatureItem(icon = "", text = "주행 경로 저장 및 공유"),
                FeatureItem(icon = "", text = "상세 주행 통계 분석"),
                FeatureItem(icon = "", text = "나만의 코스 만들기"),
                FeatureItem(icon = "", text = "라이딩 이력 관리"),
            ),
        )
    }
}

// === Profile Content (Logged In) ===
@Composable
private fun ProfileContent(
    displayName: String,
    totalDistance: String,
    totalElevation: String,
    totalRides: String,
    avgSpeed: String,
    onOpenProfile: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // === Profile Hero Card ===
        EnhancedProfileHeroCard(
            displayName = displayName,
            onProfileClick = onOpenProfile,
        )

        // === Stats Grid ===
        StatsGridSection(
            totalDistance = totalDistance,
            totalElevation = totalElevation,
            totalRides = totalRides,
            avgSpeed = avgSpeed,
        )

        // === Quick Actions ===
        SectionHeader(
            title = "활동",
            subtitle = "주행 기록과 저장한 코스를 관리하세요",
        )

        QuickActionsSection(onOpenProfile = onOpenProfile)

        // === Account Section ===
        SectionHeader(
            title = "계정",
            subtitle = "프로필 및 설정을 관리합니다",
        )

        AccountSection(onOpenProfile = onOpenProfile)
    }
}

// === Enhanced Profile Hero Card ===
@Composable
private fun EnhancedProfileHeroCard(
    displayName: String,
    onProfileClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        onClick = onProfileClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // === Gradient Header ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.inverseSurface,
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ),
                    )
                    .padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // === Avatar ===
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = displayName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }

                    // === Name and Status ===
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        ) {
                            Text(
                                text = "탭하여 프로필 관리",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // === Quick Stats Row ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickStatItem(
                    label = "누적",
                    value = "1,248 km",
                    modifier = Modifier.weight(1f),
                )
                QuickStatItem(
                    label = "고도",
                    value = "8,520 m",
                    modifier = Modifier.weight(1f),
                )
                QuickStatItem(
                    label = "주행",
                    value = "42회",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// === Quick Stat Item ===
@Composable
private fun QuickStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// === Stats Grid Section ===
@Composable
private fun StatsGridSection(
    totalDistance: String,
    totalElevation: String,
    totalRides: String,
    avgSpeed: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatMetricCard(
                title = "총 거리",
                value = totalDistance,
                subtitle = "누적 주행",
                modifier = Modifier.weight(1f),
            )
            StatMetricCard(
                title = "획득 고도",
                value = totalElevation,
                subtitle = "총 상승",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatMetricCard(
                title = "주행 횟수",
                value = totalRides,
                subtitle = "완료한 라이딩",
                modifier = Modifier.weight(1f),
            )
            StatMetricCard(
                title = "평균 속도",
                value = avgSpeed,
                subtitle = "전체 평균",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// === Stat Metric Card ===
@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

// === Quick Actions Section ===
@Composable
private fun QuickActionsSection(onOpenProfile: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ActivityRow(
            icon = GajaIconTokens.Saved,
            title = "기록한 코스",
            description = "저장한 코스를 확인하고 관리합니다",
        )
        ActivityRow(
            icon = GajaIconTokens.Stats,
            title = "내 기록",
            description = "상세 주행 통계를 확인합니다",
        )
    }
}

@Composable
private fun ActivityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(GajaIconTokens.Direction, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// === Account Section ===
@Composable
private fun AccountSection(onOpenProfile: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GajaSecondaryButton(
            text = "프로필 관리",
            onClick = onOpenProfile,
        )

        GajaOutlinedButton(
            text = "로그아웃",
            onClick = { /* TODO: Implement logout */ },
            enabled = false,
        )
    }
}
