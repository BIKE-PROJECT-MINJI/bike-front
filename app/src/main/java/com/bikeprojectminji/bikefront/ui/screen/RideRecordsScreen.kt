package com.bikeprojectminji.bikefront.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.ride.HttpRideRecordGateway
import com.bikeprojectminji.bikefront.ride.RideRecordGateway
import com.bikeprojectminji.bikefront.ui.theme.GajaCardTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaRadius
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

private sealed interface RideRecordsLoadState {
    data object Loading : RideRecordsLoadState
    data object SignedOut : RideRecordsLoadState
    data class Success(val items: List<RideRecordGateway.RideRecordHistoryItem>) : RideRecordsLoadState
    data class Error(val message: String) : RideRecordsLoadState
}

private sealed interface RideRecordDetailLoadState {
    data object Idle : RideRecordDetailLoadState
    data object Loading : RideRecordDetailLoadState
    data class Success(val detail: RideRecordGateway.RideRecordDetailResult) : RideRecordDetailLoadState
    data class Error(val message: String) : RideRecordDetailLoadState
}

internal data class RideRecordStatusUi(
    val label: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRecordsScreen(
    innerPadding: PaddingValues,
    onOpenProfile: () -> Unit,
    onOpenCourse: (Long) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val authSessionStore = remember(context) { AuthSessionStore(context) }
    val gateway = remember { HttpRideRecordGateway() }
    var refreshKey by remember { mutableStateOf(0) }
    var expandedRideRecordId by remember { mutableStateOf<Long?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val recordsState by produceState<RideRecordsLoadState>(
        initialValue = RideRecordsLoadState.Loading,
        key1 = refreshKey,
    ) {
        val accessToken = authSessionStore.accessToken
        value = if (accessToken.isBlank()) {
            RideRecordsLoadState.SignedOut
        } else {
            fetchRideRecords(accessToken, gateway)
        }
        isRefreshing = false
    }

    val detailState by produceState<RideRecordDetailLoadState>(
        initialValue = RideRecordDetailLoadState.Idle,
        key1 = expandedRideRecordId,
        key2 = refreshKey,
    ) {
        val selectedRideRecordId = expandedRideRecordId
        val accessToken = authSessionStore.accessToken
        value = when {
            selectedRideRecordId == null -> RideRecordDetailLoadState.Idle
            accessToken.isBlank() -> RideRecordDetailLoadState.Error("로그인 후 주행 기록 상세를 확인할 수 있어요.")
            else -> {
                RideRecordDetailLoadState.Loading
                fetchRideRecordDetail(accessToken, selectedRideRecordId, gateway)
            }
        }
    }

    Scaffold(
        topBar = { GajaBrandTopBar(title = "주행 기록") },
        containerColor = GajaColors.Background,
    ) { scaffoldPadding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                refreshKey++
            },
            modifier = Modifier
                .padding(innerPadding)
                .padding(scaffoldPadding),
        ) {
            when (val state = recordsState) {
                RideRecordsLoadState.Loading -> LoadingStateView("주행 기록을 불러오는 중")
                RideRecordsLoadState.SignedOut -> NotLoggedInContent(onOpenProfile = onOpenProfile)
                is RideRecordsLoadState.Error -> ErrorStateView(
                    title = "기록을 불러오지 못했어요",
                    message = state.message,
                    onRetry = { refreshKey++ },
                )
                is RideRecordsLoadState.Success -> {
                    if (state.items.isEmpty()) {
                        EmptyStateView(
                            title = "아직 주행 기록이 없어요",
                            message = "첫 라이딩을 저장하면 여기서 처리 상태와 연결 코스를 바로 확인할 수 있어요.",
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = GajaSpacing.ScreenPadding),
                            verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing),
                        ) {
                            item {
                                Spacer(Modifier.height(GajaSpacing.Small))
                                SectionHeader(
                                    title = "내 주행 기록",
                                    subtitle = "READY / FINALIZING / FAILED 상태와 연결 코스를 한곳에서 확인하세요",
                                )
                            }
                            items(state.items, key = { it.rideRecordId }) { item ->
                                val isExpanded = expandedRideRecordId == item.rideRecordId
                                RideRecordCard(
                                    item = item,
                                    isExpanded = isExpanded,
                                    detailState = if (isExpanded) detailState else RideRecordDetailLoadState.Idle,
                                    onToggle = {
                                        expandedRideRecordId = if (isExpanded) null else item.rideRecordId
                                    },
                                    onOpenCourse = onOpenCourse,
                                )
                            }
                            item { Spacer(Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RideRecordCard(
    item: RideRecordGateway.RideRecordHistoryItem,
    isExpanded: Boolean,
    detailState: RideRecordDetailLoadState,
    onToggle: () -> Unit,
    onOpenCourse: (Long) -> Unit,
) {
    val statusUi = remember(item.finalizationStatus) { rideRecordStatusUi(item.finalizationStatus) }
    GajaSectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(GajaRadius.Large),
        containerColor = GajaColors.Surface,
        borderColor = if (isExpanded) GajaColors.Primary.copy(alpha = 0.45f) else GajaColors.Border,
        shadowElevation = if (isExpanded) 4.dp else 0.dp,
        contentPadding = PaddingValues(GajaCardTokens.DefaultPadding),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = formatRideRecordDateRange(item.startedAt, item.endedAt),
                        style = MaterialTheme.typography.titleMedium,
                        color = GajaColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${formatMeters(item.distanceM)} · ${formatDuration(item.durationSec)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GajaColors.TextSecondary,
                    )
                }
                GajaStatusBadge(text = statusUi.label, containerColor = statusUi.containerColor, contentColor = statusUi.contentColor)
            }

            Text(
                text = listStatusSummary(item),
                style = MaterialTheme.typography.bodySmall,
                color = GajaColors.TextSecondary,
            )

            if (isExpanded) {
                RideRecordExpandedDetail(
                    detailState = detailState,
                    onOpenCourse = onOpenCourse,
                )
            }
        }
    }
}

@Composable
private fun RideRecordExpandedDetail(
    detailState: RideRecordDetailLoadState,
    onOpenCourse: (Long) -> Unit,
) {
    when (detailState) {
        RideRecordDetailLoadState.Idle,
        RideRecordDetailLoadState.Loading,
        -> LoadingStateView("기록 상태를 확인하는 중")
        is RideRecordDetailLoadState.Error -> BikeSurfaceCard {
            Text(
                text = detailState.message,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = GajaColors.TextSecondary,
            )
        }
        is RideRecordDetailLoadState.Success -> {
            val detail = detailState.detail
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny)) {
                    RideRecordMetricCard("raw", detail.rawPointCount.toString(), Modifier.weight(1f))
                    RideRecordMetricCard("processed", detail.processedPointCount.toString(), Modifier.weight(1f))
                }

                BikeSurfaceCard {
                    Column(
                        modifier = Modifier.padding(GajaCardTokens.DefaultPadding),
                        verticalArrangement = Arrangement.spacedBy(GajaSpacing.Tiny),
                    ) {
                        Text(
                            text = detailStatusSummary(detail),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GajaColors.TextPrimary,
                        )
                        when {
                            detail.linkedCourseId != null -> {
                                Text(
                                    text = "연결된 코스 #${detail.linkedCourseId}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GajaColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                TextButton(onClick = { onOpenCourse(detail.linkedCourseId) }) {
                                    Text("연결된 코스 보기")
                                }
                            }
                            detail.status == "READY" -> Text(
                                text = "아직 연결된 코스는 없습니다. 코스 생성이 끝나면 여기서 바로 이동할 수 있어요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GajaColors.TextSecondary,
                            )
                            detail.status == "FINALIZING" -> Text(
                                text = "기록 보정이 끝나면 연결 코스 상태를 갱신해 보여드릴게요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GajaColors.TextSecondary,
                            )
                            else -> Text(
                                text = detail.errorMessage?.ifBlank { "기록 처리에 실패했습니다." } ?: "기록 처리에 실패했습니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GajaColors.Error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RideRecordMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    GajaMetricCard(label = label, value = value, modifier = modifier)
}

private suspend fun fetchRideRecords(
    accessToken: String,
    gateway: RideRecordGateway,
): RideRecordsLoadState = suspendCancellableCoroutine { continuation ->
    gateway.getRideRecords(accessToken, object : RideRecordGateway.HistoryCallback {
        override fun onSuccess(result: RideRecordGateway.RideRecordHistoryResult) {
            continuation.resume(RideRecordsLoadState.Success(result.items))
        }

        override fun onFailure(message: String) {
            continuation.resume(RideRecordsLoadState.Error(message))
        }
    })
}

private suspend fun fetchRideRecordDetail(
    accessToken: String,
    rideRecordId: Long,
    gateway: RideRecordGateway,
): RideRecordDetailLoadState = suspendCancellableCoroutine { continuation ->
    gateway.getRideRecordDetail(accessToken, rideRecordId, object : RideRecordGateway.DetailCallback {
        override fun onSuccess(result: RideRecordGateway.RideRecordDetailResult) {
            continuation.resume(RideRecordDetailLoadState.Success(result))
        }

        override fun onFailure(message: String) {
            continuation.resume(RideRecordDetailLoadState.Error(message))
        }
    })
}

internal fun rideRecordStatusUi(status: String): RideRecordStatusUi {
    return when (status) {
        "READY" -> RideRecordStatusUi("READY", GajaColors.Success.copy(alpha = 0.16f), GajaColors.Success)
        "FAILED" -> RideRecordStatusUi("FAILED", GajaColors.Error.copy(alpha = 0.16f), GajaColors.Error)
        else -> RideRecordStatusUi("FINALIZING", GajaColors.Warning.copy(alpha = 0.20f), GajaColors.Warning)
    }
}

private fun listStatusSummary(item: RideRecordGateway.RideRecordHistoryItem): String {
    return when (item.finalizationStatus) {
        "READY" -> if (item.linkedCourseId != null) {
            "기록 보정이 완료됐고 연결된 코스가 있습니다. 탭해서 상태를 더 확인하세요."
        } else {
            "기록 보정이 완료됐습니다. 연결 코스 상태를 탭해서 확인하세요."
        }
        "FAILED" -> "기록 처리에 실패했습니다. 탭해서 실패 사유와 연결 상태를 확인하세요."
        else -> "기록을 보정하는 중입니다. 완료되면 연결 코스 상태를 보여드릴게요."
    }
}

private fun detailStatusSummary(detail: RideRecordGateway.RideRecordDetailResult): String {
    return when (detail.status) {
        "READY" -> "보정된 경로가 준비됐습니다. processed ${detail.processedPointCount}개 포인트 기준으로 다음 흐름이 이어집니다."
        "FAILED" -> detail.errorMessage?.ifBlank { "기록 처리에 실패했습니다." } ?: "기록 처리에 실패했습니다."
        else -> "raw ${detail.rawPointCount}개 포인트를 기준으로 기록을 보정하는 중입니다."
    }
}

private fun formatRideRecordDateRange(startedAt: String, endedAt: String): String {
    val startText = formatRideRecordDate(startedAt)
    val endText = formatRideRecordDate(endedAt)
    return if (startText == endText || endText.isBlank()) startText else "$startText → $endText"
}

private fun formatRideRecordDate(raw: String): String {
    if (raw.isBlank()) return "시간 정보 없음"
    return runCatching {
        OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("M월 d일 HH:mm"))
    }.getOrElse { raw.take(16).replace('T', ' ') }
}

private fun formatMeters(distanceM: Int): String {
    return if (distanceM >= 1000) {
        String.format("%.1fkm", distanceM / 1000.0)
    } else {
        "${distanceM}m"
    }
}

private fun formatDuration(durationSec: Int): String {
    val hours = durationSec / 3600
    val minutes = (durationSec % 3600) / 60
    return when {
        hours > 0 -> "${hours}시간 ${minutes}분"
        minutes > 0 -> "${minutes}분"
        else -> "1분 미만"
    }
}
