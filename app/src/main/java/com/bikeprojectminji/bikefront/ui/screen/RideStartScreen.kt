package com.bikeprojectminji.bikefront.ui.screen

import android.util.Log
import com.bikeprojectminji.bikefront.BuildConfig
import com.bikeprojectminji.bikefront.address.AddressCandidateUiModel
import com.bikeprojectminji.bikefront.address.AddressSearchStatusUi
import com.bikeprojectminji.bikefront.address.AddressSearchUiModel
import com.bikeprojectminji.bikefront.address.HttpAddressSearchGateway
import com.bikeprojectminji.bikefront.airoute.AiRouteEvidenceBadgeUiModel
import com.bikeprojectminji.bikefront.airoute.AiRouteEvidenceStatusUi
import com.bikeprojectminji.bikefront.airoute.AiRoutePlanPresentation
import com.bikeprojectminji.bikefront.airoute.AiRoutePlanRequest
import com.bikeprojectminji.bikefront.airoute.AiRoutePlanUiModel
import com.bikeprojectminji.bikefront.airoute.AiRouteWebSocketGateway
import com.bikeprojectminji.bikefront.analytics.AnalyticsTracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bikeprojectminji.bikefront.auth.AuthLoginGateway
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.auth.HttpAuthLoginGateway
import com.bikeprojectminji.bikefront.ui.theme.GajaCardTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
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
    val aiRouteGateway = remember { AiRouteWebSocketGateway() }
    val addressSearchGateway = remember { HttpAddressSearchGateway() }
    val analyticsTracker = remember(context) { AnalyticsTracker(context) }
    var refreshKey by remember { mutableStateOf(0) }

    var activitySummaryState by remember { mutableStateOf<RideStartActivitySummaryState>(RideStartActivitySummaryState.Loading) }
    var featuredState by remember { mutableStateOf<SectionState<List<CourseCardUiModel>>>(SectionState.Loading) }
    var listState by remember { mutableStateOf<SectionState<CoursesPageUiModel>>(SectionState.Loading) }
    var aiRouteState by remember { mutableStateOf<AiRouteUiState>(AiRouteUiState.Idle) }
    var addressQuery by remember { mutableStateOf("") }
    var addressSearchState by remember { mutableStateOf<AddressSearchUiState>(AddressSearchUiState.Idle) }
    var selectedDestination by remember { mutableStateOf<AddressCandidateUiModel?>(null) }

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
                    debugError("featured load failed", it)
                    SectionState.Error("추천 로드 실패")
                },
            )
        }
        launch {
            val result = runCatching { withContext(Dispatchers.IO) { repository.fetchAllCourses(limit = 10) } }
            listState = result.fold(
                { SectionState.Success(it) },
                {
                    debugError("course list load failed", it)
                    SectionState.Error("목록 로드 실패")
                },
            )
        }
    }

    Scaffold(
        topBar = { GajaBrandTopBar(title = "홈", onProfileClick = onOpenMyInfo) },
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
            Box(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                ActivitySummaryDashboard(activitySummaryState)
            }

            Box(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                AiRoutePlannerPanel(
                    state = aiRouteState,
                    addressQuery = addressQuery,
                    addressSearchState = addressSearchState,
                    selectedDestination = selectedDestination,
                    onAddressQueryChange = {
                        addressQuery = it
                        if (selectedDestination != null) selectedDestination = null
                    },
                    onSearchAddress = {
                        addressSearchState = AddressSearchUiState.Loading
                        addressSearchGateway.search(
                            query = addressQuery,
                            accessToken = authSessionStore.getAccessToken(),
                            onSuccess = { addressSearchState = AddressSearchUiState.Success(it) },
                            onFailure = { addressSearchState = AddressSearchUiState.Error(it) },
                        )
                    },
                    onDestinationSelected = {
                        selectedDestination = it
                        addressQuery = it.label
                    },
                    onRequestPlan = {
                        val destination = selectedDestination
                        debugMessage("ai route request clicked")
                        aiRouteState = AiRouteUiState.Loading
                        aiRouteGateway.requestPlan(
                            request = AiRoutePlanRequest(
                                lat = 37.48,
                                lon = 126.95,
                                destinationLat = destination?.lat,
                                destinationLon = destination?.lon,
                                destinationLabel = destination?.label ?: "관악 순환",
                                rideStyle = if (destination == null) "balanced" else "SCENERY_FIRST",
                            ),
                            onSuccess = {
                                debugMessage("ai route request succeeded")
                                aiRouteState = AiRouteUiState.Success(it)
                            },
                            onFailure = {
                                debugMessage("ai route request failed: $it")
                                aiRouteState = AiRouteUiState.Error(it)
                            },
                        )
                    },
                )
            }

            Column {
                SectionHeader(
                    title = "추천 코스",
                    subtitle = "지금 바로 출발하기 좋은 코스를 골라보세요",
                    modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding),
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

            Box(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                CompactFreeRidePanel(onStartFreeRide = onStartFreeRide)
            }

            Column(modifier = Modifier.padding(horizontal = GajaSpacing.ScreenPadding)) {
                SectionHeader(title = "근처에서 시작하기 좋은 코스", subtitle = "길게 고르지 않고 바로 출발할 수 있게 정리했어요")

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
private fun AiRoutePlannerPanel(
    state: AiRouteUiState,
    addressQuery: String,
    addressSearchState: AddressSearchUiState,
    selectedDestination: AddressCandidateUiModel?,
    onAddressQueryChange: (String) -> Unit,
    onSearchAddress: () -> Unit,
    onDestinationSelected: (AddressCandidateUiModel) -> Unit,
    onRequestPlan: () -> Unit,
) {
    GajaSectionCard(
        containerColor = GajaColors.Ink,
        contentColor = GajaColors.White,
        borderColor = GajaColors.White.copy(alpha = 0.08f),
        contentPadding = PaddingValues(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
                    GajaStatusBadge(
                        text = "Route Coach",
                        containerColor = GajaColors.RouteBlue.copy(alpha = 0.18f),
                        contentColor = GajaColors.RouteBlueSoft,
                    )
                    Text(
                        text = "출발 판단을 먼저 계산해요",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GajaColors.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "바람, 날씨, 통제, 노면을 한 번에 묶어 지금 탈 만한 길만 남깁니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GajaColors.White.copy(alpha = 0.72f),
                    )
                }
                RouteReadinessBadge(
                    value = when (state) {
                        is AiRouteUiState.Success -> state.plan.recommendationScore.total.takeIf { it > 0 }?.toString()
                            ?: if (state.plan.confidence == "high") "92" else "82"
                        AiRouteUiState.Loading -> "--"
                        is AiRouteUiState.Error -> "!"
                        AiRouteUiState.Idle -> "82"
                    },
                    label = "준비도",
                )
            }

            RouteCoachSignalStrip()

            AddressSearchPanel(
                query = addressQuery,
                state = addressSearchState,
                selectedDestination = selectedDestination,
                onQueryChange = onAddressQueryChange,
                onSearch = onSearchAddress,
                onDestinationSelected = onDestinationSelected,
            )

            when (state) {
                AiRouteUiState.Idle -> {
                    CoachBrief()
                    GajaPrimaryButton(
                        text = "조건 기반 경로 받기",
                        onClick = onRequestPlan,
                        modifier = Modifier.testTag("ai-route-plan-button"),
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        containerColor = GajaColors.RouteBlue,
                    )
                }
                AiRouteUiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = GajaSpacing.Tiny),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 3.dp,
                            color = GajaColors.RouteBlueSoft,
                        )
                        Text(
                            text = "실시간 조건을 모으는 중",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GajaColors.White.copy(alpha = 0.82f),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                is AiRouteUiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GajaColors.Warning,
                        fontWeight = FontWeight.SemiBold,
                    )
                    GajaPrimaryButton(
                        text = "다시 요청",
                        onClick = onRequestPlan,
                        containerColor = GajaColors.RouteBlue,
                    )
                }
                is AiRouteUiState.Success -> AiRoutePlanResult(state.plan, onRequestPlan)
            }
        }
    }
}

@Composable
private fun AddressSearchPanel(
    query: String,
    state: AddressSearchUiState,
    selectedDestination: AddressCandidateUiModel?,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDestinationSelected: (AddressCandidateUiModel) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
        Text(
            text = "목적지 검색",
            style = MaterialTheme.typography.labelLarge,
            color = GajaColors.White.copy(alpha = 0.82f),
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = {
                    Text("주소 또는 장소명", color = GajaColors.White.copy(alpha = 0.42f))
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = GajaColors.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GajaColors.RouteBlueSoft,
                    unfocusedBorderColor = GajaColors.White.copy(alpha = 0.18f),
                    cursorColor = GajaColors.RouteBlueSoft,
                    focusedContainerColor = GajaColors.White.copy(alpha = 0.06f),
                    unfocusedContainerColor = GajaColors.White.copy(alpha = 0.04f),
                ),
            )
            FilledTonalButton(
                onClick = onSearch,
                enabled = state !is AddressSearchUiState.Loading,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GajaColors.White.copy(alpha = 0.12f),
                    contentColor = GajaColors.White,
                ),
            ) {
                Text("검색")
            }
        }

        selectedDestination?.let {
            GajaInfoPill(
                text = "선택됨 ${it.label}",
                icon = Icons.Default.CheckCircle,
                containerColor = GajaColors.RouteBlue.copy(alpha = 0.18f),
                contentColor = GajaColors.RouteBlueSoft,
            )
        }

        when (state) {
            AddressSearchUiState.Idle -> Text(
                text = "주소를 고르면 추천 경로의 목적지로 바로 사용합니다.",
                style = MaterialTheme.typography.bodySmall,
                color = GajaColors.White.copy(alpha = 0.58f),
            )
            AddressSearchUiState.Loading -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = GajaColors.RouteBlueSoft,
                )
                Text("주소 후보를 찾는 중", style = MaterialTheme.typography.bodySmall, color = GajaColors.White.copy(alpha = 0.72f))
            }
            is AddressSearchUiState.Error -> Text(
                text = state.message,
                style = MaterialTheme.typography.bodySmall,
                color = GajaColors.Warning,
                fontWeight = FontWeight.SemiBold,
            )
            is AddressSearchUiState.Success -> AddressSearchResultList(
                result = state.result,
                onDestinationSelected = onDestinationSelected,
            )
        }
    }
}

@Composable
private fun AddressSearchResultList(
    result: AddressSearchUiModel,
    onDestinationSelected: (AddressCandidateUiModel) -> Unit,
) {
    when {
        result.status == AddressSearchStatusUi.Empty -> Text(
            text = result.message.ifBlank { "검색 결과가 없습니다." },
            style = MaterialTheme.typography.bodySmall,
            color = GajaColors.White.copy(alpha = 0.66f),
        )
        result.isFailure -> Text(
            text = result.message.ifBlank { "주소 검색을 완료하지 못했습니다." },
            style = MaterialTheme.typography.bodySmall,
            color = GajaColors.Warning,
            fontWeight = FontWeight.SemiBold,
        )
        else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = result.status.displayText,
                style = MaterialTheme.typography.labelSmall,
                color = GajaColors.White.copy(alpha = 0.58f),
            )
            result.candidates.take(3).forEach { candidate ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDestinationSelected(candidate) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Small),
                    color = GajaColors.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.08f)),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = candidate.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GajaColors.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = candidate.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = GajaColors.White.copy(alpha = 0.62f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteReadinessBadge(value: String, label: String) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Medium),
        color = GajaColors.White.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.12f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = GajaColors.White, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = GajaColors.White.copy(alpha = 0.62f))
        }
    }
}

@Composable
private fun RouteCoachSignalStrip() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
    ) {
        CoachSignalPill(Icons.Default.Speed, "바람", "동 5km/h", Modifier.weight(1f))
        CoachSignalPill(Icons.Default.Shield, "노면", "검토", Modifier.weight(1f))
        CoachSignalPill(Icons.Default.WarningAmber, "통제", "회피", Modifier.weight(1f))
    }
}

@Composable
private fun CoachSignalPill(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Small),
        color = GajaColors.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, contentDescription = null, tint = GajaColors.RouteBlueSoft, modifier = Modifier.size(14.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = GajaColors.White.copy(alpha = 0.58f))
            }
            Text(value, style = MaterialTheme.typography.titleSmall, color = GajaColors.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CoachBrief() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
    ) {
        RouteCoachMiniMetric("추천", "관악 순환", Modifier.weight(1f))
        RouteCoachMiniMetric("방식", "안전 우선", Modifier.weight(1f))
    }
}

@Composable
private fun RouteCoachMiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GajaColors.White.copy(alpha = 0.52f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = GajaColors.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AiRoutePlanResult(plan: AiRoutePlanUiModel, onRequestPlan: () -> Unit) {
    val presentation = remember(plan) { AiRoutePlanPresentation.from(plan) }
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GajaColors.RouteBlueSoft, modifier = Modifier.size(18.dp))
            Text(
                text = "오늘의 추천 경로",
                style = MaterialTheme.typography.labelLarge,
                color = GajaColors.RouteBlueSoft,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = presentation.headline,
            style = MaterialTheme.typography.bodyLarge,
            color = GajaColors.White,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = presentation.reason,
            style = MaterialTheme.typography.bodySmall,
            color = GajaColors.White.copy(alpha = 0.72f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        RouteScoreBreakdown(presentation)
        EvidenceBadgeFlow(plan.evidenceBadges)
        Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            plan.routePoints.take(3).forEachIndexed { index, point ->
                GajaInfoPill(
                    text = "${index + 1}. ${point.label}",
                    icon = if (index == 0) Icons.Default.NearMe else Icons.Default.Map,
                    containerColor = GajaColors.White.copy(alpha = 0.08f),
                    contentColor = GajaColors.White.copy(alpha = 0.82f),
                )
            }
        }
        val primaryRisk = plan.risks.firstOrNull()
        if (presentation.cautionText.isNotBlank()) {
            Text(
                text = presentation.cautionText,
                style = MaterialTheme.typography.bodySmall,
                color = GajaColors.Warning,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        } else if (primaryRisk != null) {
            Text(
                text = "${primaryRisk.label}: ${primaryRisk.summary}",
                style = MaterialTheme.typography.bodySmall,
                color = GajaColors.White.copy(alpha = 0.68f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = presentation.provenanceLabel,
                style = MaterialTheme.typography.labelSmall,
                color = GajaColors.White.copy(alpha = 0.52f),
            )
            TextButton(onClick = onRequestPlan, contentPadding = PaddingValues(0.dp)) {
                Text("조건 다시 계산", color = GajaColors.RouteBlueSoft, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun EvidenceBadgeFlow(badges: List<AiRouteEvidenceBadgeUiModel>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
        verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
    ) {
        badges.take(4).forEach { badge ->
            EvidenceBadgePill(badge)
        }
    }
}

@Composable
private fun RouteScoreBreakdown(presentation: AiRoutePlanPresentation) {
    Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        GajaInfoPill(
            text = presentation.scoreLabel,
            icon = Icons.Default.Speed,
            containerColor = GajaColors.RouteBlue.copy(alpha = 0.18f),
            contentColor = GajaColors.RouteBlueSoft,
        )
        presentation.scoreChips.forEach { chip ->
            GajaInfoPill(
                text = chip,
                icon = Icons.Default.CheckCircle,
                containerColor = GajaColors.White.copy(alpha = 0.08f),
                contentColor = GajaColors.White.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
private fun EvidenceBadgePill(badge: AiRouteEvidenceBadgeUiModel) {
    val contentColor = when (badge.status) {
        AiRouteEvidenceStatusUi.Verified -> GajaColors.RouteBlueSoft
        AiRouteEvidenceStatusUi.Warning -> GajaColors.Warning
        AiRouteEvidenceStatusUi.Failed -> GajaColors.Warning
        AiRouteEvidenceStatusUi.Unknown -> GajaColors.White.copy(alpha = 0.68f)
    }
    GajaInfoPill(
        text = "${badge.label} ${badge.statusLabel}",
        icon = when (badge.status) {
            AiRouteEvidenceStatusUi.Verified -> Icons.Default.CheckCircle
            AiRouteEvidenceStatusUi.Warning -> Icons.Default.WarningAmber
            AiRouteEvidenceStatusUi.Failed -> Icons.Default.WarningAmber
            AiRouteEvidenceStatusUi.Unknown -> Icons.Default.Shield
        },
        containerColor = GajaColors.White.copy(alpha = 0.08f),
        contentColor = contentColor,
    )
}

@Composable
fun ActivitySummaryDashboard(state: RideStartActivitySummaryState) {
    GajaSectionCard(
        containerColor = GajaColors.SurfaceElevated,
        borderColor = GajaColors.White,
        contentPadding = PaddingValues(22.dp),
    ) {
        when (state) {
            RideStartActivitySummaryState.Loading -> LoadingStateView("이번 주 기록을 불러오는 중")
            is RideStartActivitySummaryState.SignedOut -> DailyReadinessBoard(
                title = "오늘 탈 준비",
                message = state.message,
                badge = "로그인 필요",
            )
            is RideStartActivitySummaryState.Error -> DailyReadinessBoard(
                title = "오늘 탈 준비",
                message = state.message,
                badge = "동기화 확인",
                accentColor = GajaColors.Warning,
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                GajaStatusBadge(
                                    text = if (state.isEmptyWeek) "오늘 시작" else "주간 리듬",
                                    containerColor = if (state.isEmptyWeek) GajaColors.EnergySoft else GajaColors.PaceVioletSoft,
                                    contentColor = if (state.isEmptyWeek) GajaColors.Energy else GajaColors.PaceViolet,
                                )
                                Text("이번 주 라이딩", style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
                            }
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

                        WeeklyReadinessScore(isEmptyWeek = state.isEmptyWeek)
                    }

                    Text(state.helperText, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)

                    ReadinessPulseBar(isEmptyWeek = state.isEmptyWeek)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
                    ) {
                        GajaMetricCard(
                            label = "주행",
                            value = state.rideCountText,
                            icon = Icons.AutoMirrored.Filled.DirectionsBike,
                            modifier = Modifier.weight(1f),
                            emphasized = true,
                        )
                        GajaMetricCard(
                            label = "시간",
                            value = state.durationText,
                            icon = Icons.Default.History,
                            modifier = Modifier.weight(1f),
                            containerColor = GajaColors.EnergySoft,
                            contentColor = GajaColors.TextPrimary,
                        )
                        GajaMetricCard(
                            label = "코스",
                            value = state.savedCourseText,
                            icon = Icons.Default.Map,
                            modifier = Modifier.weight(1f),
                            containerColor = GajaColors.PaceVioletSoft,
                            contentColor = GajaColors.TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyReadinessBoard(
    title: String,
    message: String,
    badge: String,
    accentColor: androidx.compose.ui.graphics.Color = GajaColors.Energy,
) {
    Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(GajaSpacing.Micro)) {
                GajaStatusBadge(
                    text = badge,
                    containerColor = accentColor.copy(alpha = 0.14f),
                    contentColor = accentColor,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = GajaColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(message, style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextSecondary)
            }
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Medium),
                color = GajaColors.Ink,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text("AI", style = MaterialTheme.typography.titleSmall, color = GajaColors.Volt, fontWeight = FontWeight.Black)
                    Text("대기", style = MaterialTheme.typography.labelSmall, color = GajaColors.White.copy(alpha = 0.68f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny), modifier = Modifier.fillMaxWidth()) {
            PreviewSignalPill("위치", "확인 예정", Modifier.weight(1f), GajaColors.PrimarySoft)
            PreviewSignalPill("날씨", "연동 예정", Modifier.weight(1f), GajaColors.EnergySoft)
            PreviewSignalPill("경로", "추천 대기", Modifier.weight(1f), GajaColors.PaceVioletSoft)
        }
    }
}

@Composable
private fun WeeklyReadinessScore(isEmptyWeek: Boolean) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Medium),
        color = GajaColors.Ink,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(if (isEmptyWeek) "65" else "88", style = MaterialTheme.typography.headlineSmall, color = GajaColors.Volt, fontWeight = FontWeight.Black)
            Text("오늘 점수", style = MaterialTheme.typography.labelSmall, color = GajaColors.White.copy(alpha = 0.66f))
        }
    }
}

@Composable
private fun ReadinessPulseBar(isEmptyWeek: Boolean) {
    val colors = if (isEmptyWeek) {
        listOf(GajaColors.Energy, GajaColors.Border, GajaColors.Border)
    } else {
        listOf(GajaColors.Primary, GajaColors.Energy, GajaColors.PaceViolet)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        colors.forEachIndexed { index, color ->
            Surface(
                modifier = Modifier
                    .weight(if (index == 0) 1.6f else 1f)
                    .height(7.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Pill),
                color = color,
            ) {}
        }
    }
}

@Composable
private fun PreviewSignalPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Small),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.White),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.titleSmall, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
        }
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
                        text = "바로 출발",
                        containerColor = GajaColors.PrimaryContainer,
                        contentColor = GajaColors.Accent,
                    )
                    Text(
                        text = "코스 없이 바로 기록",
                        style = MaterialTheme.typography.titleLarge,
                        color = GajaColors.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "현재 위치에서 속도, 시간, 날씨 맥락을 켜고 주행을 남깁니다.",
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
                QuickRideSignal(label = "START", value = "즉시", modifier = Modifier.weight(1f))
                QuickRideSignal(label = "HUD", value = "속도", modifier = Modifier.weight(1f))
                QuickRideSignal(label = "LOG", value = "저장", modifier = Modifier.weight(1f))
            }

            GajaPrimaryButton(
                text = "자유 주행 시작",
                onClick = onStartFreeRide,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = GajaColors.Energy,
            )
        }
    }
}

@Composable
private fun QuickRideSignal(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(GajaRadius.Small),
        color = GajaColors.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.08f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = GajaSpacing.Small, vertical = GajaSpacing.Tiny),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = GajaColors.White.copy(alpha = 0.48f))
            Text(value, style = MaterialTheme.typography.titleSmall, color = GajaColors.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeaturedCourseGridItem(course: CourseCardUiModel, onClick: () -> Unit) {
    GajaSectionCard(
        modifier = Modifier
            .width(236.dp)
            .height(176.dp)
            .clickable { onClick() },
        containerColor = GajaColors.SurfaceElevated,
        borderColor = GajaColors.White,
        contentPadding = PaddingValues(18.dp),
        shadowElevation = GajaCardTokens.SubtleElevation,
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GajaStatusBadge(
                        text = "검토된 루트",
                        containerColor = GajaColors.PrimarySoft,
                        contentColor = GajaColors.Primary,
                    )
                    Text(
                        text = course.featuredRank?.let { "#$it" } ?: "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = GajaColors.TextTertiary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(GajaSpacing.Tiny))
                Text(course.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(
                    text = "날씨와 길 상태를 보고 바로 비교",
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
                    FeaturedCourseMeta(Icons.Default.Map, "${course.distanceKm}km", GajaColors.EnergySoft, GajaColors.Energy)
                    FeaturedCourseMeta(Icons.Default.History, "${course.estimatedDurationMin}분", GajaColors.PaceVioletSoft, GajaColors.PaceViolet)
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
private fun FeaturedCourseMeta(
    icon: ImageVector,
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    GajaInfoPill(text = text, icon = icon, containerColor = containerColor, contentColor = contentColor)
}

private sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>
    data class Success<T>(val data: T) : SectionState<T>
    data class Error(val message: String) : SectionState<Nothing>
}

private sealed interface AiRouteUiState {
    data object Idle : AiRouteUiState
    data object Loading : AiRouteUiState
    data class Success(val plan: AiRoutePlanUiModel) : AiRouteUiState
    data class Error(val message: String) : AiRouteUiState
}

private sealed interface AddressSearchUiState {
    data object Idle : AddressSearchUiState
    data object Loading : AddressSearchUiState
    data class Success(val result: AddressSearchUiModel) : AddressSearchUiState
    data class Error(val message: String) : AddressSearchUiState
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

private fun debugError(message: String, throwable: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e("RideStartScreen", message, throwable)
    }
}

private fun debugMessage(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("RideStartScreen", message)
    }
}
