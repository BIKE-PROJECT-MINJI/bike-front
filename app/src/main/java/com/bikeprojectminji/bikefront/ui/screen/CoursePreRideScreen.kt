package com.bikeprojectminji.bikefront.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bikeprojectminji.bikefront.ridepolicy.HttpRidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiMapper
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiModel
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CoursePreRideScreen(
    innerPadding: PaddingValues,
    course: CourseCardUiModel,
    onBack: () -> Unit,
    onStartRide: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember(context) { CoursesRepository(context) }
    val ridePolicyGateway = remember { HttpRidePolicyEvaluationGateway() }
    val ridePolicyUiMapper = remember { RidePolicyUiMapper() }
    val locationManager = remember(context) { context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }
    val detailResult by produceState<Result<CourseCardUiModel>?>(initialValue = null, key1 = course.id) {
        value = runCatching {
            withContext(Dispatchers.IO) { repository.fetchCourseDetail(course.id) }
        }
    }
    val resolvedCourse = detailResult?.getOrNull() ?: course
    val loading = detailResult == null
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var currentLocation by remember(resolvedCourse.id) { mutableStateOf<Location?>(null) }
    var policyState by remember(resolvedCourse.id) { mutableStateOf<RidePolicyUiModel?>(null) }
    var startGateStatus by remember(resolvedCourse.id) { mutableStateOf<String?>(null) }
    var policyLoading by remember(resolvedCourse.id) { mutableStateOf(false) }
    var lastPolicyRequestedAt by remember(resolvedCourse.id) { mutableStateOf(0L) }
    var routePreviewState by remember(resolvedCourse.id) {
        mutableStateOf<CoursePreRideMapPreviewUiState>(CoursePreRideMapPreviewUiState.Loading)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        locationGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!locationGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(locationGranted, resolvedCourse.id, locationManager) {
        if (!locationGranted || locationManager == null) {
            currentLocation = null
            policyLoading = false
            startGateStatus = null
            policyState = null
            onDispose { }
        } else {
            val listener = LocationListener { location ->
                currentLocation = location

                val now = SystemClock.elapsedRealtime()
                if (lastPolicyRequestedAt != 0L && now - lastPolicyRequestedAt < 5_000L) {
                    return@LocationListener
                }

                lastPolicyRequestedAt = now
                policyLoading = true
                ridePolicyGateway.evaluate(
                    resolvedCourse.id,
                    PRE_START_PHASE,
                    location,
                    object : RidePolicyEvaluationGateway.Callback {
                        override fun onSuccess(result: RidePolicyEvaluationGateway.EvaluationResult) {
                            startGateStatus = result.startGate.status
                            policyState = ridePolicyUiMapper.map(result)
                            policyLoading = false
                        }

                        override fun onFailure(message: String) {
                            startGateStatus = null
                            policyState = RidePolicyUiModel(
                                "판단 보류",
                                message,
                                "",
                                false,
                                "",
                                0,
                                0,
                                0,
                            )
                            policyLoading = false
                        }
                    },
                )
            }

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000L, 5f, listener)
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let(listener::onLocationChanged)
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let(listener::onLocationChanged)
            } catch (_: SecurityException) {
                currentLocation = null
                policyLoading = false
                startGateStatus = null
                policyState = RidePolicyUiModel(
                    "위치 권한 필요",
                    "현재 위치 권한을 허용하면 출발 가능 여부를 확인할 수 있습니다.",
                    "",
                    false,
                    "",
                    0,
                    0,
                    0,
                )
            }

            onDispose { locationManager.removeUpdates(listener) }
        }
    }

    val preRideStatusCard = remember(locationGranted, currentLocation, policyLoading, policyState, routePreviewState) {
        resolveCoursePreRideStatusCard(
            locationGranted = locationGranted,
            currentLocation = currentLocation,
            policyLoading = policyLoading,
            policyState = policyState,
            routePreviewState = routePreviewState,
        )
    }
    val startEnabled = !loading && startGateStatus == START_GATE_ELIGIBLE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GajaColors.Background)
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = GajaSpacing.ScreenPadding, vertical = GajaSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(GajaSpacing.ItemSpacing),
    ) {
        GajaBrandTopBar(title = "경로 미리보기")

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GajaColors.Surface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, GajaColors.Border),
            shadowElevation = 4.dp,
        ) {
            GajaMapPreview(
                modifier = Modifier.fillMaxWidth(),
                heightDp = 232,
                courseId = resolvedCourse.id,
                mode = MapDisplayMode.PREVIEW,
                onRoutePointsLoaded = { points ->
                    routePreviewState = CoursePreRideMapPreviewStateReducer.onRoutePointsLoaded(
                        currentState = routePreviewState,
                        points = points,
                    )
                },
                onRouteLoadFailed = { message ->
                    routePreviewState = CoursePreRideMapPreviewStateReducer.onRouteLoadFailed(message)
                },
            )
        }

        CoursePreRidePreviewStatusCard(status = preRideStatusCard)

        Surface(
            color = GajaColors.Surface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, GajaColors.Border),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(color = GajaColors.PrimaryContainer, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                "추천 코스",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = GajaColors.Primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            resolvedCourse.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = GajaColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = GajaColors.Background,
                        border = BorderStroke(1.dp, GajaColors.Border),
                    ) {
                        Text(
                            text = if (startEnabled) "출발 가능" else "상태 확인 중",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (startEnabled) GajaColors.Primary else GajaColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CourseMetricBlock(label = "거리", value = formatDistance(resolvedCourse.distanceKm), modifier = Modifier.weight(1f))
                    CourseMetricBlock(label = "예상 시간", value = "${resolvedCourse.estimatedDurationMin}분", modifier = Modifier.weight(1f))
                    CourseMetricBlock(
                        label = if (!policyState?.detailCaption.isNullOrBlank()) "시작점" else "경로",
                        value = policyState?.detailCaption ?: if (loading) "동기화" else "준비됨",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GajaColors.Primary)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(18.dp),
            color = GajaColors.Carbon.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, GajaColors.White.copy(alpha = 0.06f)),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "퍼포먼스 시작 준비",
                        style = MaterialTheme.typography.labelSmall,
                        color = GajaColors.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "출발 전에 상태를 확인하고 바로 주행으로 이어집니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.74f),
                    )
                }

                GajaPrimaryButton(
                    text = "이 코스로 시작",
                    onClick = onStartRide,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    enabled = startEnabled,
                )
                SecondaryActionButton(text = "다른 코스 보기", onClick = onBack)
            }
        }
    }
}

@Composable
private fun CoursePreRidePreviewStatusCard(status: CoursePreRideStatusCardUiState) {
    Surface(
        color = status.containerColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GajaColors.Border.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = status.title,
                style = MaterialTheme.typography.titleSmall,
                color = status.titleColor,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = status.message,
                style = MaterialTheme.typography.bodyMedium,
                color = status.messageColor,
            )
            status.detailCaption?.takeIf { it.isNotBlank() }?.let { detailCaption ->
                Text(
                    text = detailCaption,
                    style = MaterialTheme.typography.labelMedium,
                    color = status.messageColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) "${(distanceKm * 1000).toInt()}m" else "%.1fkm".format(distanceKm)
}

@Composable
private fun CourseMetricBlock(
    label: String,
    value: String,
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = GajaColors.TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

private const val PRE_START_PHASE = "PRE_START"
private const val START_GATE_ELIGIBLE = "ELIGIBLE"

private data class CoursePreRideStatusCardUiState(
    val title: String,
    val message: String,
    val detailCaption: String? = null,
    val containerColor: Color,
    val titleColor: Color = GajaColors.TextPrimary,
    val messageColor: Color = GajaColors.TextSecondary,
)

private fun resolveCoursePreRideStatusCard(
    locationGranted: Boolean,
    currentLocation: Location?,
    policyLoading: Boolean,
    policyState: RidePolicyUiModel?,
    routePreviewState: CoursePreRideMapPreviewUiState,
): CoursePreRideStatusCardUiState {
    if (!locationGranted) {
        return CoursePreRideStatusCardUiState(
            title = "출발 준비가 필요해요",
            message = "현재 위치 권한을 허용하면 코스 출발 가능 여부를 바로 확인할 수 있어요.",
            containerColor = GajaColors.Warning.copy(alpha = 0.20f),
        )
    }

    if (currentLocation == null) {
        return CoursePreRideStatusCardUiState(
            title = "현재 위치 확인 중",
            message = "위치를 찾는 중이에요. 확인되면 바로 출발 준비 상태를 보여드릴게요.",
            containerColor = GajaColors.White.copy(alpha = 0.88f),
        )
    }

    if (policyLoading && policyState == null) {
        return CoursePreRideStatusCardUiState(
            title = "출발 상태 확인 중",
            message = "현재 위치를 기준으로 이 코스를 바로 시작할 수 있는지 확인하고 있어요.",
            containerColor = GajaColors.White.copy(alpha = 0.88f),
        )
    }

    if (policyState != null) {
        val containerColor = when (policyState.stateLabel) {
            "주행 가능" -> GajaColors.Success.copy(alpha = 0.18f)
            "시작 위치 확인 필요" -> GajaColors.Error.copy(alpha = 0.18f)
            else -> GajaColors.Warning.copy(alpha = 0.20f)
        }

        return CoursePreRideStatusCardUiState(
            title = policyState.stateLabel,
            message = policyState.message,
            detailCaption = policyState.detailCaption,
            containerColor = containerColor,
        )
    }

    return when (routePreviewState) {
        CoursePreRideMapPreviewUiState.Loading -> CoursePreRideStatusCardUiState(
            title = "코스 준비 중",
            message = routePreviewState.message,
            containerColor = GajaColors.White.copy(alpha = 0.88f),
        )

        CoursePreRideMapPreviewUiState.Ready -> CoursePreRideStatusCardUiState(
            title = routePreviewState.title,
            message = "경로를 불러왔어요. 현재 위치 기준 출발 상태를 계속 확인합니다.",
            containerColor = GajaColors.Success.copy(alpha = 0.18f),
        )

        CoursePreRideMapPreviewUiState.Empty -> CoursePreRideStatusCardUiState(
            title = routePreviewState.title,
            message = routePreviewState.message,
            containerColor = GajaColors.Warning.copy(alpha = 0.20f),
        )

        is CoursePreRideMapPreviewUiState.Error -> CoursePreRideStatusCardUiState(
            title = routePreviewState.title,
            message = routePreviewState.message,
            containerColor = GajaColors.Error.copy(alpha = 0.18f),
        )
    }
}
