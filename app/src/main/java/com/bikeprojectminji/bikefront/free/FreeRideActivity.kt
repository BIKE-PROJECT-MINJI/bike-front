package com.bikeprojectminji.bikefront.free

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.course.CourseEditorActivity
import com.bikeprojectminji.bikefront.free.RideLocationHudStateResolver
import com.bikeprojectminji.bikefront.free.RideStatusMessageResolver
import com.bikeprojectminji.bikefront.ride.HttpRideRecordGateway
import com.bikeprojectminji.bikefront.ride.RideRecordGateway
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import com.bikeprojectminji.bikefront.ridepolicy.HttpRidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiMapper
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiModel
import com.bikeprojectminji.bikefront.speed.RideSpeedFormatter
import com.bikeprojectminji.bikefront.speed.RideSpeedUiState
import com.bikeprojectminji.bikefront.ui.screen.GajaMapPreview
import com.bikeprojectminji.bikefront.ui.screen.GajaPrimaryButton
import com.bikeprojectminji.bikefront.ui.screen.MapDisplayMode
import com.bikeprojectminji.bikefront.ui.theme.GajaColors
import com.bikeprojectminji.bikefront.ui.theme.GajaTheme
import com.bikeprojectminji.bikefront.weather.CurrentWeatherGateway
import com.bikeprojectminji.bikefront.weather.HttpCurrentWeatherGateway
import com.bikeprojectminji.bikefront.weather.WeatherHudValueFormatter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.roundToInt

class FreeRideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L).takeIf { it > 0L }
        setContent {
            GajaTheme {
                FreeRideMainScreen(
                    courseId = courseId,
                    onFinish = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_COURSE_ID = "course_id"
    }
}

private data class WeatherUiState(
    val temperatureText: String = "--",
    val temperatureMessage: String = "날씨를 확인하는 중입니다.",
    val windText: String = "--",
    val windMessage: String = "바람 정보를 확인하는 중입니다.",
)

private data class LocationUiState(
    val value: String = "확인 중",
    val message: String? = "현재 위치를 확인하는 중입니다.",
)

@Composable
fun FreeRideMainScreen(courseId: Long?, onFinish: () -> Unit) {
    val context = LocalContext.current
    val authSessionStore = remember { AuthSessionStore(context) }
    val rideRecordGateway = remember { HttpRideRecordGateway() }
    val weatherGateway = remember { HttpCurrentWeatherGateway() }
    val ridePolicyGateway = remember { HttpRidePolicyEvaluationGateway() }
    val ridePolicyUiMapper = remember { RidePolicyUiMapper() }
    val speedFormatter = remember { RideSpeedFormatter() }
    val locationManager = remember { context.getSystemService(LocationManager::class.java) }

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var previousLocation by remember { mutableStateOf<Location?>(null) }
    var weatherState by remember { mutableStateOf(WeatherUiState()) }
    var policyState by remember { mutableStateOf<RidePolicyUiModel?>(null) }
    var routePoints by remember { mutableStateOf<List<CourseRoutePointsGateway.RoutePoint>>(emptyList()) }
    val trackedPoints = remember { mutableStateListOf<RideRecordGateway.RideRecordPoint>() }
    var inFlightSave by remember { mutableStateOf(false) }
    var startedAtMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        locationGranted = granted
    }

    LaunchedEffect(Unit) {
        if (!locationGranted) permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(locationGranted) {
        if (!locationGranted || locationManager == null) onDispose { }
        else {
            val listener = LocationListener { location ->
                previousLocation = currentLocation
                currentLocation = location
                trackedPoints.add(
                    RideRecordGateway.RideRecordPoint(
                        trackedPoints.size,
                        location.latitude,
                        location.longitude,
                    ),
                )
                weatherGateway.loadCurrent(location.latitude, location.longitude, object : CurrentWeatherGateway.Callback {
                    override fun onSuccess(result: CurrentWeatherGateway.WeatherResult) {
                        weatherState = WeatherUiState(
                            temperatureText = WeatherHudValueFormatter.formatTemperature(result.temperatureC),
                            temperatureMessage = if (result.isStale) "날씨 갱신이 지연되고 있습니다." else "현재 기온 정보입니다.",
                            windText = WeatherHudValueFormatter.formatWind(result.windDirectionText, result.windSpeedKmh),
                            windMessage = if (result.isStale) "바람 정보 갱신이 지연되고 있습니다." else "현재 바람 정보입니다.",
                        )
                    }
                    override fun onEmpty() {
                        weatherState = WeatherUiState(
                            temperatureText = "--",
                            temperatureMessage = "날씨 정보 없음",
                            windText = "--",
                            windMessage = "바람 정보 없음",
                        )
                    }
                    override fun onFailure(message: String) {
                        weatherState = WeatherUiState(
                            temperatureText = "--",
                            temperatureMessage = message,
                            windText = "--",
                            windMessage = message,
                        )
                    }
                })
                if (courseId != null) {
                    ridePolicyGateway.evaluate(courseId, "ACTIVE", location, object : com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway.Callback {
                        override fun onSuccess(result: com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway.EvaluationResult) {
                            policyState = ridePolicyUiMapper.map(result)
                        }
                        override fun onFailure(message: String) {
                            policyState = RidePolicyUiModel("판단 보류", message, false, "", 0, 0, 0)
                        }
                    })
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

    val speedState: RideSpeedUiState = speedFormatter.format(currentLocation, previousLocation)
    val locationUiState = RideLocationHudStateResolver.resolve(
        locationGranted,
        currentLocation != null,
        currentLocation?.hasAccuracy() == true && currentLocation!!.accuracy > 50f,
        "위치 권한이 필요합니다.",
        "현재 위치를 확인하는 중입니다.",
        "현재 위치 정보가 불안정합니다.",
    )
    val distanceMeters = remember(trackedPoints.size) {
        if (trackedPoints.size < 2) 0
        else trackedPoints.zipWithNext().sumOf { (a, b) ->
            val result = FloatArray(1)
            Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result)
            result[0].roundToInt()
        }
    }
    val statusMessage = RideStatusMessageResolver.resolve(
        "주행 중 정보를 갱신하고 있습니다.",
        policyState?.message,
        "",
        speedState.message,
        "",
        weatherState.temperatureMessage,
        "",
    )

    fun saveAndOpenEditor() {
        val token = authSessionStore.accessToken
        if (token.isBlank()) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (trackedPoints.isEmpty() && routePoints.isEmpty()) {
            Toast.makeText(context, "저장할 주행 기록이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val sourcePoints = if (trackedPoints.isNotEmpty()) trackedPoints else routePoints.map {
            RideRecordGateway.RideRecordPoint(it.pointOrder, it.latitude, it.longitude)
        }
        inFlightSave = true
        val endedAtMillis = System.currentTimeMillis()
        rideRecordGateway.saveRideRecord(
            token,
            RideRecordGateway.RideRecordDraft(
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(startedAtMillis), ZoneId.systemDefault()),
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(endedAtMillis), ZoneId.systemDefault()),
                distanceMeters,
                ((endedAtMillis - startedAtMillis) / 1000L).toInt(),
                sourcePoints,
            ),
            object : RideRecordGateway.Callback {
                override fun onSuccess(result: RideRecordGateway.RideRecordSaveResult) {
                    inFlightSave = false
                    val intent = Intent(context, CourseEditorActivity::class.java).apply {
                        putExtra(CourseEditorActivity.EXTRA_RIDE_RECORD_ID, result.rideRecordId)
                        putExtra(CourseEditorActivity.EXTRA_DISTANCE_KM, distanceMeters / 1000.0)
                        putExtra(CourseEditorActivity.EXTRA_DURATION_MIN, ((endedAtMillis - startedAtMillis) / 60000L).toInt())
                        putExtra(CourseEditorActivity.EXTRA_SOURCE_SUMMARY, "주행 기록에서 코스 초안을 이어서 정리합니다.")
                    }
                    context.startActivity(intent)
                }
                override fun onFailure(message: String) {
                    inFlightSave = false
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    Scaffold(containerColor = GajaColors.Background) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            GajaMapPreview(
                modifier = Modifier.fillMaxSize(),
                mode = MapDisplayMode.RIDE,
                courseId = courseId,
                locationPermissionGranted = locationGranted,
                onRoutePointsLoaded = { routePoints = it },
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    policyState?.takeIf { it.isShowBanner && it.bannerMessage.isNotBlank() }?.let { banner ->
                        Surface(color = GajaColors.ErrorContainer.copy(alpha = 0.92f), shape = MaterialTheme.shapes.medium) {
                            Text(banner.bannerMessage, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), color = GajaColors.Error, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        HudCard("속도", speedState.speedText, speedState.message, Modifier.weight(1f))
                        HudCard("거리", if (distanceMeters > 0) "${distanceMeters / 1000.0}km" else "--", "지금까지 진행한 거리", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        HudCard("위치", locationUiState.value, locationUiState.message ?: "위치 상태", Modifier.weight(1f))
                        HudCard("주행 정책", policyState?.stateLabel ?: "판단 보류", policyState?.message ?: "현재 위치를 기다리는 중입니다.", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        HudCard("기온", weatherState.temperatureText, weatherState.temperatureMessage, Modifier.weight(1f))
                        HudCard("풍향/풍속", weatherState.windText, weatherState.windMessage, Modifier.weight(1f))
                    }
                    Surface(color = GajaColors.White.copy(alpha = 0.92f), shape = MaterialTheme.shapes.large) {
                        Text(statusMessage, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.bodyMedium, color = GajaColors.TextPrimary)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GajaPrimaryButton(text = if (inFlightSave) "기록 저장 중..." else "기록 저장 후 코스 만들기", onClick = { if (!inFlightSave) saveAndOpenEditor() }, enabled = !inFlightSave)
                    GajaPrimaryButton(text = "화면 종료", onClick = onFinish)
                }
            }
        }
    }
}

@Composable
private fun HudCard(title: String, value: String, message: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = GajaColors.White.copy(alpha = 0.92f)), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = GajaColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = GajaColors.TextPrimary, fontWeight = FontWeight.Black)
            Text(message, style = MaterialTheme.typography.bodySmall, color = GajaColors.TextSecondary)
        }
    }
}
