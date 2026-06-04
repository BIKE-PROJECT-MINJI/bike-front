package com.bikeprojectminji.bikefront.free

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
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
import com.bikeprojectminji.bikefront.ui.screen.MapDisplayMode
import com.bikeprojectminji.bikefront.ui.screen.MapViewportActions
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaHudTokens
import com.bikeprojectminji.bikefront.ui.theme.GajaIconSizes
import com.bikeprojectminji.bikefront.ui.theme.GajaIconTokens
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
    var showUnsavedExitDialog by remember { mutableStateOf(false) }
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

    val trackingController = remember { FreeRideTrackingController() }

    DisposableEffect(locationGranted, courseId, locationManager) {
        if (!locationGranted || locationManager == null) {
            onDispose { }
        } else {
            val listener = LocationListener { location ->
                val accepted = trackingController.onLocationChanged(location)
                if (!accepted) {
                    return@LocationListener
                }

                val now = SystemClock.elapsedRealtime()
                if (trackingController.shouldRefreshWeather(now)) {
                    trackingController.markWeatherRequested(now)
                    refreshWeather(location, weatherGateway, trackingController::updateWeatherState)
                }
                if (courseId != null && trackingController.shouldRefreshPolicy(now)) {
                    trackingController.markPolicyRequested(now)
                    refreshRidePolicy(
                        courseId = courseId,
                        location = location,
                        trackedPoints = trackingController.trackedPoints,
                        ridePolicyGateway = ridePolicyGateway,
                        ridePolicyUiMapper = ridePolicyUiMapper,
                        updateState = trackingController::updateActivePolicyResult,
                        updateFailureState = trackingController::updatePolicyFailure,
                    )
                }
            }

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 5f, listener)
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { listener.onLocationChanged(it) }
            } catch (_: SecurityException) {
            }

            onDispose { locationManager.removeUpdates(listener) }
        }
    }

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

    fun hasMeaningfulUnsavedRideProgress(): Boolean {
        val trackedPoints = trackingController.trackedPoints
        val activeDurationSec = (trackingController.activeElapsedMillis() / 1000L).toInt()
        return trackedPoints.isNotEmpty() || activeDurationSec > 0
    }

    fun requestExit() {
        if (inFlightSave || processingRideRecordId != null) {
            return
        }
        showUnsavedExitDialog = true
    }

    BackHandler {
        requestExit()
    }

    LaunchedEffect(pendingSaveAfterAuth, inFlightSave) {
        if (pendingSaveAfterAuth && !inFlightSave) {
            pendingSaveAfterAuth = false
            saveAndOpenEditor()
        }
    }

    val currentPolicyState = trackingController.policyState
    val tempValue = trackingController.weatherState.temperatureText.ifBlank { "--" }
    val policyLabel = currentPolicyState?.stateLabel ?: "확인 중"
    val completionEligible = courseId != null && currentPolicyState?.isCompletionEligible() == true
    val completionDialogMessage = currentPolicyState?.getCompletionDialogMessage()
    val distanceText = remember(distanceMeters) { formatHudDistance(distanceMeters) }
    val activeElapsedMillis = trackingController.activeElapsedMillis()
    val elapsedText = remember(activeElapsedMillis) {
        formatHudDuration(activeElapsedMillis)
    }
    val isLocationHealthy = locationGranted && currentLocation != null && (!currentLocation.hasAccuracy() || currentLocation.accuracy <= 50f)
    val compactLocationText = remember(locationState.value) {
        compactHudText(locationState.value, fallback = "위치 확인 중")
    }
    val compactPolicyText = remember(policyLabel) {
        compactHudText(policyLabel, fallback = "정책 확인 중")
    }
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

    Box(modifier = Modifier.fillMaxSize().background(GajaColors.Carbon)) {
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
                .background(GajaColors.Background.copy(alpha = 0.14f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to GajaColors.Carbon.copy(alpha = 0.36f),
                        0.18f to Color.Transparent,
                        0.64f to Color.Transparent,
                        1f to GajaColors.Carbon.copy(alpha = 0.28f),
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = GajaHudTokens.OverlayMargin, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    RideSecondaryInfoCard(
                        modifier = Modifier.weight(1f),
                        locationText = compactLocationText,
                        policyText = compactPolicyText,
                        distanceText = distanceText,
                        temperatureText = tempValue,
                    )
                    RideHudTopBar(
                        modifier = Modifier.padding(start = 16.dp),
                        onClose = ::requestExit,
                        onRecenter = { viewportActions?.recenter?.invoke() },
                        onZoomIn = { viewportActions?.zoomIn?.invoke() },
                        onZoomOut = { viewportActions?.zoomOut?.invoke() },
                    )
                }
                PrimaryRideMetric(
                    modifier = Modifier.padding(top = 12.dp),
                    value = speedState.speedText.removeSuffix("km/h").ifBlank { "--" },
                    unit = "km/h",
                    footerText = if (trackingController.isTrackingActive) "실시간 속도" else "주행 일시정지",
                )
            }

            RideControlDock(
                statusText = statusMessage,
                elapsedText = elapsedText,
                isLocationHealthy = isLocationHealthy,
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
                onStop = ::requestExit,
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
        if (showUnsavedExitDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedExitDialog = false },
                title = { Text("저장하지 않고 종료할까요?") },
                text = {
                    Text("지금 종료하면 이번 주행 기록은 저장되지 않습니다. 저장 후 이어서 정리하거나, 저장 없이 종료할 수 있습니다.")
                },
                confirmButton = {
                    Button(onClick = {
                        showUnsavedExitDialog = false
                        if (!inFlightSave) {
                            saveAndOpenEditor()
                        }
                    }) {
                        Text("저장하기")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            showUnsavedExitDialog = false
                            onFinish()
                        }) {
                            Text("저장 없이 종료")
                        }
                        TextButton(onClick = { showUnsavedExitDialog = false }) {
                            Text("취소")
                        }
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
    onClose: () -> Unit,
    onRecenter: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MapControlButton(icon = Icons.Default.MyLocation, onClick = onRecenter)
        MapControlButton(icon = Icons.Default.Add, onClick = onZoomIn)
        MapControlButton(icon = Icons.Default.Remove, onClick = onZoomOut)
        HudControlButton(
            icon = Icons.Default.Close,
            containerColor = GajaColors.Carbon.copy(alpha = 0.84f),
            size = GajaHudTokens.MapControlSize,
            iconSize = GajaIconSizes.Medium,
            borderColor = Color.White.copy(alpha = 0.12f),
            contentDescription = "자유 주행 닫기",
            onClick = onClose,
        )
    }
}

@Composable
fun PrimaryRideMetric(
    value: String,
    unit: String,
    footerText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(GajaHudTokens.SpeedCardWidth)
            .heightIn(min = GajaHudTokens.SpeedCardMinHeight)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedHudText(
            text = "현재 속도",
            style = MaterialTheme.typography.labelMedium.copy(
                shadow = Shadow(
                    color = GajaColors.Carbon.copy(alpha = 0.7f),
                    blurRadius = 10f,
                )
            ),
            color = Color.White.copy(alpha = 0.66f),
            fontWeight = FontWeight.SemiBold,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            OutlinedHudText(
                text = value,
                style = TextStyle(
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = GajaColors.Carbon.copy(alpha = 0.74f),
                        blurRadius = 22f,
                    ),
                ),
                edgeOffset = 2.dp,
                edgeBlurRadius = 6f,
            )
            OutlinedHudText(
                text = unit,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    shadow = Shadow(
                        color = GajaColors.Carbon.copy(alpha = 0.65f),
                        blurRadius = 12f,
                    )
                ),
                color = Color.White.copy(alpha = 0.82f),
                fontWeight = FontWeight.Bold,
            )
        }
        OutlinedHudText(
            text = footerText,
            style = MaterialTheme.typography.labelMedium.copy(
                shadow = Shadow(
                    color = GajaColors.Carbon.copy(alpha = 0.62f),
                    blurRadius = 10f,
                )
            ),
            color = Color.White.copy(alpha = 0.74f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RideSecondaryInfoCard(
    locationText: String,
    policyText: String,
    distanceText: String,
    temperatureText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        SecondaryInfoLine(label = "위치", value = locationText)
        SecondaryInfoLine(label = "정책", value = policyText)
        SecondaryInfoLine(label = "거리", value = distanceText)
        SecondaryInfoLine(label = "기온", value = temperatureText)
    }
}

@Composable
fun RideControlDock(
    statusText: String,
    elapsedText: String,
    isLocationHealthy: Boolean,
    inFlightSave: Boolean,
    isTrackingActive: Boolean,
    onToggleTracking: () -> Unit,
    onSave: () -> Unit,
    onStop: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GajaHudTokens.FloatingGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RideStatusBadge(isTrackingActive = isTrackingActive)
                    OutlinedHudText(
                        text = if (isTrackingActive) "실시간 주행 중" else "주행 일시정지",
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(
                                color = GajaColors.Carbon.copy(alpha = 0.72f),
                                blurRadius = 12f,
                            )
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RideStateTag(
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp),
                    isTrackingActive = isTrackingActive,
                    isLocationHealthy = isLocationHealthy,
                )
            }
            OutlinedHudText(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = GajaColors.Carbon.copy(alpha = 0.76f),
                        blurRadius = 10f,
                    )
                ),
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top,
            ) {
                HudMetricPanel(label = "경과", value = elapsedText)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            HudFloatingAction(
                label = if (inFlightSave) "저장중" else "저장",
                icon = GajaIconTokens.Saved,
                containerColor = GajaColors.White.copy(alpha = 0.96f),
                contentColor = if (inFlightSave) GajaColors.TextTertiary else GajaColors.TextPrimary,
                borderColor = GajaColors.Border.copy(alpha = 0.72f),
                size = GajaHudTokens.SecondaryControlSize,
                enabled = !inFlightSave,
                contentDescription = if (inFlightSave) "기록 저장 중" else "기록 저장",
                onClick = onSave,
            )
            HudFloatingAction(
                label = if (isTrackingActive) "일시정지" else "재개",
                icon = if (isTrackingActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                containerColor = GajaColors.Primary,
                contentColor = GajaColors.White,
                size = GajaHudTokens.PrimaryControlSize,
                iconSize = GajaIconSizes.PrimaryControl,
                contentDescription = if (isTrackingActive) "주행 일시정지" else "주행 재개",
                onClick = onToggleTracking,
            )
            HudFloatingAction(
                label = "종료",
                icon = Icons.Default.Close,
                containerColor = GajaColors.White.copy(alpha = 0.96f),
                contentColor = GajaColors.Error,
                borderColor = GajaColors.Border.copy(alpha = 0.72f),
                size = GajaHudTokens.SecondaryControlSize,
                contentDescription = "주행 종료",
                onClick = onStop,
            )
        }
    }
}

@Composable
fun SecondaryInfoLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedHudText(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                shadow = Shadow(
                    color = GajaColors.Carbon.copy(alpha = 0.62f),
                    blurRadius = 8f,
                )
            ),
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedHudText(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = GajaColors.Carbon.copy(alpha = 0.68f),
                    blurRadius = 10f,
                )
            ),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun MapControlButton(icon: ImageVector, onClick: () -> Unit) {
    val contentDescription = when (icon) {
        Icons.Default.MyLocation -> "내 위치로 이동"
        Icons.Default.Add -> "지도 확대"
        Icons.Default.Remove -> "지도 축소"
        else -> null
    }
    HudControlButton(
        icon = icon,
        containerColor = GajaColors.Carbon.copy(alpha = 0.82f),
        size = GajaHudTokens.MapControlSize,
        iconSize = GajaIconSizes.Medium,
        borderColor = Color.White.copy(alpha = 0.12f),
        contentDescription = contentDescription,
        onClick = onClick,
    )
}

@Composable
fun HudControlButton(
    icon: ImageVector,
    containerColor: Color,
    size: androidx.compose.ui.unit.Dp = GajaHudTokens.SecondaryControlSize,
    iconSize: androidx.compose.ui.unit.Dp = GajaIconSizes.Control,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    borderColor: Color = Color.Transparent,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(size),
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.68f),
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 10.dp,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            enabled = enabled,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
fun HudFloatingAction(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = GajaIconSizes.Control,
    borderColor: Color = Color.Transparent,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HudControlButton(
            icon = icon,
            containerColor = containerColor,
            size = size,
            iconSize = iconSize,
            contentColor = contentColor,
            borderColor = borderColor,
            enabled = enabled,
            contentDescription = contentDescription,
            onClick = onClick,
        )
        OutlinedHudText(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RideStatusBadge(isTrackingActive: Boolean) {
    OutlinedHudText(
        text = if (isTrackingActive) "LIVE" else "PAUSED",
        style = MaterialTheme.typography.labelLarge.copy(
            shadow = Shadow(
                color = GajaColors.Carbon.copy(alpha = 0.52f),
                blurRadius = 6f,
            )
        ),
        color = if (isTrackingActive) GajaColors.PrimaryContainer else GajaColors.Warning,
        fontWeight = FontWeight.Black,
        edgeColor = GajaColors.Carbon.copy(alpha = 0.48f),
        edgeOffset = 0.8.dp,
        edgeBlurRadius = 2.5f,
    )
}

@Composable
private fun RideStateTag(
    modifier: Modifier = Modifier,
    isTrackingActive: Boolean,
    isLocationHealthy: Boolean,
) {
    OutlinedHudText(
        text = when {
            !isTrackingActive -> "정지됨"
            isLocationHealthy -> "GPS 양호"
            else -> "GPS 확인 중"
        },
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium.copy(
            shadow = Shadow(
                color = GajaColors.Carbon.copy(alpha = 0.56f),
                blurRadius = 7f,
            )
        ),
        color = when {
            !isTrackingActive -> GajaColors.Warning
            isLocationHealthy -> GajaColors.PrimaryContainer
            else -> GajaColors.Warning
        },
        fontWeight = FontWeight.Bold,
        edgeColor = GajaColors.Carbon.copy(alpha = 0.42f),
        edgeOffset = 0.8.dp,
        edgeBlurRadius = 2.5f,
    )
}

@Composable
private fun HudMetricPanel(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedHudText(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = Shadow(
                    color = GajaColors.Carbon.copy(alpha = 0.72f),
                    blurRadius = 8f,
                )
            ),
            color = Color.White.copy(alpha = 0.62f),
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedHudText(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                shadow = Shadow(
                    color = GajaColors.Carbon.copy(alpha = 0.82f),
                    blurRadius = 12f,
                )
            ),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OutlinedHudText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    edgeColor: Color = GajaColors.Carbon.copy(alpha = 0.94f),
    edgeOffset: androidx.compose.ui.unit.Dp = 1.5.dp,
    edgeBlurRadius: Float = 4f,
) {
    val outlineStyle = style.copy(
        shadow = Shadow(
            color = edgeColor,
            blurRadius = edgeBlurRadius,
        )
    )

    Box(modifier = modifier) {
        listOf(
            Pair(-edgeOffset, 0.dp),
            Pair(edgeOffset, 0.dp),
            Pair(0.dp, -edgeOffset),
            Pair(0.dp, edgeOffset),
        ).forEach { (offsetX, offsetY) ->
            Text(
                text = text,
                modifier = Modifier.offset(x = offsetX, y = offsetY),
                style = outlineStyle,
                color = edgeColor,
                fontWeight = fontWeight,
                textAlign = textAlign,
                maxLines = maxLines,
                overflow = overflow,
            )
        }

        Text(
            text = text,
            style = style,
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}

private fun compactHudText(value: String, fallback: String): String {
    val sanitized = value.trim().ifBlank { fallback }
    return if (sanitized.length <= 28) sanitized else sanitized.take(27) + "…"
}

private fun formatHudDistance(distanceMeters: Int): String {
    return if (distanceMeters >= 1000) {
        String.format("%.1fkm", distanceMeters / 1000.0)
    } else {
        "${distanceMeters}m"
    }
}

private fun formatHudDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 1000L / 60L).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}시간 ${minutes}분"
        totalMinutes > 0 -> "${totalMinutes}분"
        else -> "1분 미만"
    }
}

private fun sanitizeHudMessage(message: String?): String {
    if (message.isNullOrBlank()) return ""
    if (!BuildConfig.DEBUG && (message.contains("127.0.0.1") || message.contains("localhost") || message.contains(":8080"))) {
        return "연결 상태를 다시 확인해 주세요."
    }
    return message
}
