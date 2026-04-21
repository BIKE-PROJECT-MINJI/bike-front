package com.bikeprojectminji.bikefront.free

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bikeprojectminji.bikefront.ride.RideRecordGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiMapper
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiModel
import com.bikeprojectminji.bikefront.weather.CurrentWeatherGateway
import com.bikeprojectminji.bikefront.weather.WeatherHudValueFormatter

internal data class WeatherUiState(
    val temperatureText: String = "--",
    val temperatureMessage: String = "날씨를 확인하는 중입니다.",
    val windText: String = "--",
    val windMessage: String = "바람 정보를 확인하는 중입니다.",
)

internal data class FreeRideTrackingState(
    val currentLocation: Location?,
    val previousLocation: Location?,
    val weatherState: WeatherUiState,
    val policyState: RidePolicyUiModel?,
    val trackedPoints: List<RideRecordGateway.RideRecordPoint>,
)

@Composable
internal fun rememberFreeRideTrackingState(
    courseId: Long?,
    locationGranted: Boolean,
    locationManager: LocationManager?,
    weatherGateway: CurrentWeatherGateway,
    ridePolicyGateway: RidePolicyEvaluationGateway,
    ridePolicyUiMapper: RidePolicyUiMapper,
): FreeRideTrackingState {
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var previousLocation by remember { mutableStateOf<Location?>(null) }
    var weatherState by remember { mutableStateOf(WeatherUiState()) }
    var policyState by remember { mutableStateOf<RidePolicyUiModel?>(null) }
    var lastWeatherRequestAt by remember { mutableStateOf(0L) }
    var lastPolicyRequestAt by remember { mutableStateOf(0L) }
    val trackedPoints = remember { mutableStateListOf<RideRecordGateway.RideRecordPoint>() }

    DisposableEffect(locationGranted, courseId, locationManager) {
        if (!locationGranted || locationManager == null) {
            onDispose { }
        } else {
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
                val now = SystemClock.elapsedRealtime()
                if (shouldRefresh(now, lastWeatherRequestAt)) {
                    lastWeatherRequestAt = now
                    refreshWeather(location, weatherGateway) { weatherState = it }
                }
                if (courseId != null && shouldRefresh(now, lastPolicyRequestAt)) {
                    lastPolicyRequestAt = now
                    refreshRidePolicy(
                        courseId = courseId,
                        location = location,
                        ridePolicyGateway = ridePolicyGateway,
                        ridePolicyUiMapper = ridePolicyUiMapper,
                    ) { policyState = it }
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

    return FreeRideTrackingState(
        currentLocation = currentLocation,
        previousLocation = previousLocation,
        weatherState = weatherState,
        policyState = policyState,
        trackedPoints = trackedPoints,
    )
}

private fun refreshWeather(
    location: Location,
    weatherGateway: CurrentWeatherGateway,
    updateState: (WeatherUiState) -> Unit,
) {
    weatherGateway.loadCurrent(location.latitude, location.longitude, object : CurrentWeatherGateway.Callback {
        override fun onSuccess(result: CurrentWeatherGateway.WeatherResult) {
            updateState(
                WeatherUiState(
                    temperatureText = WeatherHudValueFormatter.formatTemperature(result.temperatureC),
                    temperatureMessage = if (result.isStale) "날씨 갱신이 지연되고 있습니다." else "현재 기온 정보입니다.",
                    windText = WeatherHudValueFormatter.formatWind(result.windDirectionText, result.windSpeedKmh),
                    windMessage = if (result.isStale) "바람 정보 갱신이 지연되고 있습니다." else "현재 바람 정보입니다.",
                ),
            )
        }

        override fun onEmpty() {
            updateState(
                WeatherUiState(
                    temperatureText = "--",
                    temperatureMessage = "날씨 정보 없음",
                    windText = "--",
                    windMessage = "바람 정보 없음",
                ),
            )
        }

        override fun onFailure(message: String) {
            updateState(
                WeatherUiState(
                    temperatureText = "--",
                    temperatureMessage = message,
                    windText = "--",
                    windMessage = message,
                ),
            )
        }
    })
}

private fun refreshRidePolicy(
    courseId: Long,
    location: Location,
    ridePolicyGateway: RidePolicyEvaluationGateway,
    ridePolicyUiMapper: RidePolicyUiMapper,
    updateState: (RidePolicyUiModel?) -> Unit,
) {
    ridePolicyGateway.evaluate(courseId, "ACTIVE", location, object : RidePolicyEvaluationGateway.Callback {
        override fun onSuccess(result: RidePolicyEvaluationGateway.EvaluationResult) {
            updateState(ridePolicyUiMapper.map(result))
        }

        override fun onFailure(message: String) {
            updateState(RidePolicyUiModel("판단 보류", message, false, "", 0, 0, 0))
        }
    })
}

private fun shouldRefresh(now: Long, lastRequestedAt: Long): Boolean {
    if (lastRequestedAt == 0L) return true
    return now - lastRequestedAt >= 5_000L
}
