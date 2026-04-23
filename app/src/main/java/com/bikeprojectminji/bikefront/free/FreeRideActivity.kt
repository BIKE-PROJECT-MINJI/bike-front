package com.bikeprojectminji.bikefront.free

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bikeprojectminji.bikefront.BuildConfig
import com.bikeprojectminji.bikefront.analytics.AnalyticsTracker
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.auth.AuthProfileActivity
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.ride.HttpRideRecordGateway
import com.bikeprojectminji.bikefront.ride.RideRecordGateway
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import com.bikeprojectminji.bikefront.ridepolicy.HttpRidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiMapper
import com.bikeprojectminji.bikefront.speed.RideSpeedFormatter
import com.bikeprojectminji.bikefront.speed.RideSpeedUiState
import com.bikeprojectminji.bikefront.ui.screen.GajaMapPreview
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.MapDisplayMode
import com.bikeprojectminji.bikefront.ui.screen.MapViewportActions
import com.bikeprojectminji.bikefront.ui.screen.SecondaryActionButton
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme
import com.bikeprojectminji.bikefront.weather.HttpCurrentWeatherGateway

class FreeRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val courseId = intent.getLongExtra("extra_course_id", -1L).takeIf { it > 0L }
        setContent {
            GajaTheme {
                FreeRideHudScreen(courseId = courseId, onFinish = { finish() })
            }
        }
    }
}

@Composable
fun FreeRideHudScreen(courseId: Long?, onFinish: () -> Unit) {
    val context = LocalContext.current
    val authSessionStore = remember { AuthSessionStore(context) }
    val analyticsTracker = remember(context) { AnalyticsTracker(context) }
    val rideRecordGateway = remember { HttpRideRecordGateway() }
    val weatherGateway = remember { HttpCurrentWeatherGateway() }
    val ridePolicyGateway = remember { HttpRidePolicyEvaluationGateway() }
    val ridePolicyUiMapper = remember { RidePolicyUiMapper() }
    val speedFormatter = remember { RideSpeedFormatter() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val locationManager = remember(context) { context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }

    var locationGranted by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<CourseRoutePointsGateway.RoutePoint>>(emptyList()) }
    var viewportActions by remember { mutableStateOf<MapViewportActions?>(null) }
    var inFlightSave by remember { mutableStateOf(false) }
    var pendingSaveAfterAuth by remember { mutableStateOf(false) }
    var lastSavePreparation by remember { mutableStateOf<FreeRideSavePreparation.Ready?>(null) }
    var processingRideRecordId by remember { mutableStateOf<Long?>(null) }
    var processingRideRecordMessage by remember { mutableStateOf(context.getString(R.string.ride_finish_processing_message)) }
    var processingRideRecordFailed by remember { mutableStateOf(false) }
    var saveFailureState by remember { mutableStateOf<FreeRideSaveFailureUiState>(FreeRideSaveFailureUiState.None) }
    var showCourseCompletionDialog by remember(courseId) { mutableStateOf(false) }
    var courseCompletionDialogConsumed by remember(courseId) { mutableStateOf(false) }
    val startedAtMillis = remember { System.currentTimeMillis() }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        locationGranted = granted
    }
    val authLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && authSessionStore.isSignedIn()) {
            pendingSaveAfterAuth = true
        }
    }

    LaunchedEffect(Unit) {
        analyticsTracker.track("ride_started", "ride_hud", mapOf("courseId" to courseId, "startedFrom" to if (courseId != null) "course_detail" else "free_ride"))
        if (!locationGranted) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val trackingController = rememberFreeRideTrackingState(
        courseId = courseId,
        locationGranted = locationGranted,
        locationManager = locationManager,
        weatherGateway = weatherGateway,
        ridePolicyGateway = ridePolicyGateway,
        ridePolicyUiMapper = ridePolicyUiMapper,
    )

    val currentLocation = trackingController.currentLocation
    val previousLocation = trackingController.previousLocation
    val speedState: RideSpeedUiState = speedFormatter.format(currentLocation, previousLocation)
    val locationState = remember(locationGranted, currentLocation, speedState.message) {
        RideLocationHudStateResolver.resolve(
            locationGranted,
            currentLocation != null,
            currentLocation?.hasAccuracy() == true && currentLocation.accuracy > 50f,
            "위치 권한이 필요합니다.",
            "현재 위치 신호를 기다리는 중입니다.",
            speedState.message,
        )
    }
    val distanceMeters = remember(trackingController.trackedPoints.size) {
        if (trackingController.trackedPoints.size < 2) 0 else trackingController.trackedPoints.zipWithNext().sumOf { (a, b) ->
            val results = FloatArray(1)
            android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results)
            results[0].toInt()
        }
    }
    val statusMessage = remember(trackingController.isTrackingActive, trackingController.policyState?.message, speedState.message, trackingController.weatherState.temperatureMessage) {
        if (!trackingController.isTrackingActive) {
            "일시정지됨. 다시 누르면 주행을 이어갑니다."
        } else {
            RideStatusMessageResolver.resolve(
                "주행 상태를 확인하는 중입니다.",
                sanitizeHudMessage(trackingController.policyState?.message),
                "현재 위치 신호를 기다리는 중입니다.",
                sanitizeHudMessage(speedState.message),
                "현재 위치 신호를 기다리는 중입니다.",
                sanitizeHudMessage(trackingController.weatherState.temperatureMessage),
                "날씨를 확인하는 중입니다.",
            )
        }
    }

    fun openCourseEditor(preparation: FreeRideSavePreparation.Ready, rideRecordId: Long) {
        lastSavePreparation = null
        processingRideRecordId = null
        processingRideRecordFailed = false
        processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
        saveFailureState = FreeRideSaveFailureUiState.None
        context.startActivity(
            FreeRideSaveCoordinator.createEditorIntent(
                context = context,
                rideRecordId = rideRecordId,
                distanceKm = preparation.distanceKm,
                durationMinutes = preparation.durationMinutes,
            )
        )
    }

    fun pollRideRecordFinalization(preparation: FreeRideSavePreparation.Ready, rideRecordId: Long) {
        rideRecordGateway.getRideRecordStatus(preparation.accessToken, rideRecordId, object : RideRecordGateway.StatusCallback {
            override fun onSuccess(result: RideRecordGateway.RideRecordFinalizationStatusResult) {
                when (result.status) {
                    "READY" -> openCourseEditor(preparation, result.rideRecordId)
                    "FAILED" -> {
                        when (val failureState = FreeRideSaveFailureResolver.resolve(
                            message = result.errorMessage,
                            fallbackMessage = context.getString(R.string.ride_finish_processing_failed_message)
                        )) {
                            is FreeRideSaveFailureUiState.ShortRide -> {
                                processingRideRecordId = null
                                processingRideRecordFailed = false
                                processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                                saveFailureState = failureState
                            }
                            is FreeRideSaveFailureUiState.Generic -> {
                                processingRideRecordId = result.rideRecordId
                                processingRideRecordFailed = true
                                processingRideRecordMessage = failureState.message
                                saveFailureState = FreeRideSaveFailureUiState.None
                            }
                            FreeRideSaveFailureUiState.None -> Unit
                        }
                    }
                    else -> {
                        processingRideRecordId = result.rideRecordId
                        processingRideRecordFailed = false
                        processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                        saveFailureState = FreeRideSaveFailureUiState.None
                        mainHandler.postDelayed({ pollRideRecordFinalization(preparation, rideRecordId) }, 2000L)
                    }
                }
            }

            override fun onFailure(message: String) {
                when (val failureState = FreeRideSaveFailureResolver.resolve(
                    message = message,
                    fallbackMessage = context.getString(R.string.ride_finish_processing_failed_message)
                )) {
                    is FreeRideSaveFailureUiState.ShortRide -> {
                        processingRideRecordId = null
                        processingRideRecordFailed = false
                        processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                        saveFailureState = failureState
                    }
                    is FreeRideSaveFailureUiState.Generic -> {
                        processingRideRecordId = rideRecordId
                        processingRideRecordFailed = true
                        processingRideRecordMessage = failureState.message
                        saveFailureState = FreeRideSaveFailureUiState.None
                    }
                    FreeRideSaveFailureUiState.None -> Unit
                }
            }
        })
    }

    fun regenerateRideRecord() {
        val rideRecordId = processingRideRecordId ?: return
        val endedAtMillis = System.currentTimeMillis()
        val preparation = FreeRideSaveCoordinator.prepare(
            accessToken = authSessionStore.accessToken,
            trackedPoints = trackingController.trackedPoints,
            routePoints = routePoints,
            distanceMeters = distanceMeters,
            startedAtMillis = startedAtMillis,
            endedAtMillis = endedAtMillis,
            activeDurationMillis = trackingController.activeElapsedMillis(),
        )
        if (preparation !is FreeRideSavePreparation.Ready) {
            return
        }
        processingRideRecordFailed = false
        processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
        saveFailureState = FreeRideSaveFailureUiState.None
        rideRecordGateway.regenerateRideRecord(preparation.accessToken, rideRecordId, object : RideRecordGateway.StatusCallback {
            override fun onSuccess(result: RideRecordGateway.RideRecordFinalizationStatusResult) {
                processingRideRecordId = result.rideRecordId
                pollRideRecordFinalization(preparation, result.rideRecordId)
            }

            override fun onFailure(message: String) {
                when (val failureState = FreeRideSaveFailureResolver.resolve(
                    message = message,
                    fallbackMessage = context.getString(R.string.ride_finish_processing_failed_message)
                )) {
                    is FreeRideSaveFailureUiState.ShortRide -> {
                        processingRideRecordId = null
                        processingRideRecordFailed = false
                        processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                        saveFailureState = failureState
                    }
                    is FreeRideSaveFailureUiState.Generic -> {
                        processingRideRecordFailed = true
                        processingRideRecordMessage = failureState.message
                        saveFailureState = FreeRideSaveFailureUiState.None
                    }
                    FreeRideSaveFailureUiState.None -> Unit
                }
            }
        })
    }

    fun retrySavePreparedDraft(preparation: FreeRideSavePreparation.Ready) {
        inFlightSave = true
        saveFailureState = FreeRideSaveFailureUiState.None
        rideRecordGateway.saveRideRecord(preparation.accessToken, preparation.draft, object : RideRecordGateway.Callback {
            override fun onSuccess(result: RideRecordGateway.RideRecordSaveResult) {
                inFlightSave = false
                processingRideRecordId = result.rideRecordId
                processingRideRecordFailed = false
                processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                if (result.getFinalizationStatus() == "READY") {
                    openCourseEditor(preparation, result.getRideRecordId())
                } else {
                    pollRideRecordFinalization(preparation, result.getRideRecordId())
                }
            }

            override fun onFailure(message: String) {
                inFlightSave = false
                when (val failureState = FreeRideSaveFailureResolver.resolve(
                    message = message,
                    fallbackMessage = context.getString(R.string.ride_finish_processing_failed_message)
                )) {
                    is FreeRideSaveFailureUiState.ShortRide -> {
                        processingRideRecordId = null
                        processingRideRecordFailed = false
                        processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                        saveFailureState = failureState
                        lastSavePreparation = null
                    }
                    is FreeRideSaveFailureUiState.Generic -> {
                        processingRideRecordId = -1L
                        processingRideRecordFailed = true
                        processingRideRecordMessage = failureState.message
                        saveFailureState = FreeRideSaveFailureUiState.None
                    }
                    FreeRideSaveFailureUiState.None -> Unit
                }
            }
        })
    }

    fun saveAndOpenEditor() {
        val endedAtMillis = System.currentTimeMillis()
        when (val preparation = FreeRideSaveCoordinator.prepare(
            accessToken = authSessionStore.accessToken,
            trackedPoints = trackingController.trackedPoints,
            routePoints = routePoints,
            distanceMeters = distanceMeters,
            startedAtMillis = startedAtMillis,
            endedAtMillis = endedAtMillis,
            activeDurationMillis = trackingController.activeElapsedMillis(),
        )) {
            is FreeRideSavePreparation.Blocked -> {
                if (preparation.requiresAuth) {
                    authLauncher.launch(AuthProfileActivity.createIntent(context, returnAfterSave = true))
                }
            }
            is FreeRideSavePreparation.Ready -> {
                if (FreeRideSaveCoordinator.isShortRideDuration(preparation.draft.durationSec)) {
                    saveFailureState = FreeRideSaveFailureUiState.ShortRide(
                        "주행 시작 후 10초 미만 기록은 저장되지 않습니다."
                    )
                    lastSavePreparation = null
                    return
                }
                lastSavePreparation = preparation
                inFlightSave = true
                saveFailureState = FreeRideSaveFailureUiState.None
                rideRecordGateway.saveRideRecord(preparation.accessToken, preparation.draft, object : RideRecordGateway.Callback {
                    override fun onSuccess(result: RideRecordGateway.RideRecordSaveResult) {
                        inFlightSave = false
                        processingRideRecordId = result.rideRecordId
                        processingRideRecordFailed = false
                        processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                        if (result.finalizationStatus == "READY") {
                            openCourseEditor(preparation, result.rideRecordId)
                        } else {
                            pollRideRecordFinalization(preparation, result.rideRecordId)
                        }
                    }

                    override fun onFailure(message: String) {
                        inFlightSave = false
                        when (val failureState = FreeRideSaveFailureResolver.resolve(
                            message = message,
                            fallbackMessage = context.getString(R.string.ride_finish_processing_failed_message)
                        )) {
                            is FreeRideSaveFailureUiState.ShortRide -> {
                                lastSavePreparation = null
                                processingRideRecordId = null
                                processingRideRecordFailed = false
                                processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                                saveFailureState = failureState
                            }
                            is FreeRideSaveFailureUiState.Generic -> {
                                processingRideRecordId = -1L
                                processingRideRecordFailed = true
                                processingRideRecordMessage = failureState.message
                                saveFailureState = FreeRideSaveFailureUiState.None
                            }
                            FreeRideSaveFailureUiState.None -> Unit
                        }
                    }
                })
            }
        }
    }

    LaunchedEffect(pendingSaveAfterAuth, inFlightSave) {
        if (pendingSaveAfterAuth && !inFlightSave) {
            pendingSaveAfterAuth = false
            saveAndOpenEditor()
        }
    }

    val windValue = trackingController.weatherState.windText.ifBlank { "--" }
    val tempValue = trackingController.weatherState.temperatureText.ifBlank { "--" }
    val policyLabel = trackingController.policyState?.stateLabel ?: "확인 중"
    val bannerMessage = sanitizeHudMessage(trackingController.policyState?.takeIf { it.isShowBanner && it.bannerMessage.isNotBlank() }?.bannerMessage)
    val completionEligible = courseId != null && trackingController.policyState?.isCompletionEligible == true
    val completionDialogMessage = trackingController.policyState?.completionDialogMessage

    LaunchedEffect(courseId, completionEligible, completionDialogMessage, inFlightSave, processingRideRecordId, saveFailureState) {
        val shouldBlockCompletionDialog = courseId == null || inFlightSave || processingRideRecordId != null || saveFailureState !is FreeRideSaveFailureUiState.None
        if (shouldBlockCompletionDialog) {
            showCourseCompletionDialog = false
            return@LaunchedEffect
        }

        if (completionEligible && !completionDialogMessage.isNullOrBlank()) {
            if (!courseCompletionDialogConsumed) {
                showCourseCompletionDialog = true
            }
        } else {
            showCourseCompletionDialog = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        GajaMapPreview(
            modifier = Modifier.fillMaxSize(),
            courseId = courseId,
            mode = MapDisplayMode.RIDE,
            locationPermissionGranted = locationGranted,
            onRoutePointsLoaded = { routePoints = it },
            onViewportActionsReady = { viewportActions = it },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.36f),
                        0.18f to Color.Transparent,
                        0.76f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.42f),
                    )
                )
        )

        RideHudTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            isTrackingActive = trackingController.isTrackingActive,
            locationText = locationState.value,
            policyText = policyLabel,
            bannerMessage = bannerMessage,
            tempValue = tempValue,
            windValue = windValue,
            onClose = onFinish,
            onRecenter = { viewportActions?.recenter?.invoke() },
            onZoomIn = { viewportActions?.zoomIn?.invoke() },
            onZoomOut = { viewportActions?.zoomOut?.invoke() },
        )

        PrimaryRideMetric(
            value = speedState.speedText.removeSuffix("km/h"),
            unit = "km/h",
            distanceText = "",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 88.dp),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            RideControlDock(
                statusText = statusMessage,
                inFlightSave = inFlightSave,
                isTrackingActive = trackingController.isTrackingActive,
                onToggleTracking = {
                    if (trackingController.isTrackingActive) {
                        trackingController.pauseTracking()
                    } else {
                        trackingController.resumeTracking()
                    }
                },
                onSave = { if (!inFlightSave) saveAndOpenEditor() },
                onStop = onFinish,
            )
        }

        if (processingRideRecordId != null) {
            val canRetryGeneration = processingRideRecordId != null && processingRideRecordId!! > 0L
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(if (processingRideRecordFailed) "코스 생성 실패" else "코스 생성 중")
                },
                text = {
                    Text(processingRideRecordMessage)
                },
                confirmButton = {
                    if (processingRideRecordFailed) {
                        if (canRetryGeneration) {
                            Button(onClick = { regenerateRideRecord() }) {
                                Text(context.getString(R.string.ride_finish_processing_retry_button))
                            }
                        } else if (lastSavePreparation != null) {
                            Button(onClick = { retrySavePreparedDraft(lastSavePreparation!!) }) {
                                Text("다시 저장")
                            }
                        } else {
                            TextButton(onClick = {
                                processingRideRecordId = null
                                processingRideRecordFailed = false
                                processingRideRecordMessage = context.getString(R.string.ride_finish_processing_message)
                            }) {
                                Text("확인")
                            }
                        }
                    }
                },
                dismissButton = {
                    if (!processingRideRecordFailed) {
                        TextButton(onClick = {}) {
                            Text(context.getString(R.string.ride_finish_processing_wait_button))
                        }
                    }
                }
            )
        }
        if (showCourseCompletionDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("코스 완주 안내") },
                text = {
                    Text(
                        completionDialogMessage ?: "완주 조건을 충족했습니다. 지금 기록을 저장하고 주행을 마칠까요?",
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        showCourseCompletionDialog = false
                        courseCompletionDialogConsumed = true
                        if (!inFlightSave && processingRideRecordId == null) {
                            saveAndOpenEditor()
                        }
                    }) {
                        Text("기록 저장 후 종료")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCourseCompletionDialog = false
                        courseCompletionDialogConsumed = true
                    }) {
                        Text("계속 주행")
                    }
                },
            )
        }
        if (saveFailureState is FreeRideSaveFailureUiState.ShortRide) {
            val shortRideState = saveFailureState as FreeRideSaveFailureUiState.ShortRide
            AlertDialog(
                onDismissRequest = {},
                title = { Text("기록 저장 불가") },
                text = { Text(shortRideState.message) },
                confirmButton = {
                    Button(onClick = {
                        saveFailureState = FreeRideSaveFailureUiState.None
                        onFinish()
                    }) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

@Composable
fun RideHudTopBar(
    modifier: Modifier = Modifier,
    isTrackingActive: Boolean,
    locationText: String,
    policyText: String,
    bannerMessage: String?,
    tempValue: String,
    windValue: String,
    onClose: () -> Unit,
    onRecenter: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RideStatusBadge(
                        text = if (isTrackingActive) "LIVE" else "PAUSED",
                        containerColor = if (isTrackingActive) GajaColors.Primary else GajaColors.Warning,
                    )
                    RideStatusBadge(text = locationText, containerColor = GajaColors.Carbon.copy(alpha = 0.82f))
                    RideStatusBadge(text = policyText, containerColor = GajaColors.Carbon.copy(alpha = 0.82f))
                }
                SecondaryConditionsRow(tempValue = tempValue, windValue = windValue)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MapControlButton(icon = Icons.Default.MyLocation, onClick = onRecenter)
                MapControlButton(icon = Icons.Default.Add, onClick = onZoomIn)
                MapControlButton(icon = Icons.Default.Remove, onClick = onZoomOut)
                HudControlButton(
                    icon = Icons.Default.Close,
                    containerColor = GajaColors.Carbon.copy(alpha = 0.82f),
                    size = 40.dp,
                    onClick = onClose,
                )
            }
        }
        if (!bannerMessage.isNullOrBlank()) {
            Surface(
                color = GajaColors.Error.copy(alpha = 0.82f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.wrapContentHeight(),
            ) {
                Text(
                    text = bannerMessage,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun RideStatusBadge(text: String, containerColor: Color) {
    Surface(
        color = containerColor,
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun PrimaryRideMetric(
    value: String,
    unit: String,
    distanceText: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-3).sp,
                color = Color.White,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.72f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                    blurRadius = 18f,
                ),
            ),
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.titleMedium,
            color = GajaColors.White.copy(alpha = 0.86f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
        if (distanceText.isNotBlank()) {
            RideStatusBadge(text = distanceText, containerColor = GajaColors.Carbon.copy(alpha = 0.78f))
        }
    }
}

@Composable
fun SecondaryConditionsRow(
    tempValue: String,
    windValue: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = GajaColors.Carbon.copy(alpha = 0.78f),
            shape = CircleShape,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CompactMetricChip(label = "기온", value = tempValue)
                CompactMetricChip(label = "바람", value = windValue)
            }
        }
    }
}

@Composable
fun RideControlDock(
    statusText: String,
    inFlightSave: Boolean,
    isTrackingActive: Boolean,
    onToggleTracking: () -> Unit,
    onSave: () -> Unit,
    onStop: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!isTrackingActive) {
            Surface(
                shape = CircleShape,
                color = GajaColors.Warning.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
            ) {
                Text(
                    text = "일시정지됨",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = GajaColors.Carbon,
                    fontWeight = FontWeight.Black,
                )
            }
        }
        if (statusText.isNotBlank()) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            HudControlButton(
                icon = if (isTrackingActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                containerColor = if (isTrackingActive) GajaColors.Carbon.copy(alpha = 0.80f) else GajaColors.Primary,
                onClick = onToggleTracking,
            )
            GajaPrimaryButton(
                text = if (inFlightSave) "저장 중..." else "기록 저장",
                onClick = onSave,
                enabled = !inFlightSave,
                modifier = Modifier.widthIn(min = 180.dp),
            )
            HudControlButton(
                icon = Icons.Default.Close,
                containerColor = GajaColors.Error.copy(alpha = 0.82f),
                onClick = onStop,
            )
        }
    }
}

@Composable
fun CompactMetricChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.52f),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
fun MapControlButton(icon: ImageVector, onClick: () -> Unit) {
    HudControlButton(
        icon = icon,
        containerColor = GajaColors.Carbon.copy(alpha = 0.80f),
        size = 40.dp,
        onClick = onClick,
    )
}

@Composable
fun HudControlButton(
    icon: ImageVector,
    containerColor: Color,
    size: androidx.compose.ui.unit.Dp = 54.dp,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(size),
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor, contentColor = Color.White),
        shape = CircleShape,
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(size * 0.46f))
    }
}

private fun sanitizeHudMessage(message: String?): String {
    if (message.isNullOrBlank()) return ""
    if (!BuildConfig.DEBUG && (message.contains("127.0.0.1") || message.contains("localhost") || message.contains(":8080"))) {
        return "연결 상태를 다시 확인해 주세요."
    }
    return message
}
