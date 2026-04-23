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
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
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

internal enum class FreeRideTrackingStatus {
    ACTIVE,
    PAUSED,
}

internal data class FreeRideLocationSample(
    val latitude: Double,
    val longitude: Double,
    val location: Location? = null,
)

private data class CourseCoordinate(
    val latitude: Double,
    val longitude: Double,
)

internal data class CourseCompletionCheckResult(
    val targetLabel: String,
    val distanceMeters: Int,
    val eligible: Boolean,
)

internal class FreeRideTrackingController(
    startedAtElapsedRealtimeMillis: Long = SystemClock.elapsedRealtime(),
) {
    var currentLocation by mutableStateOf<Location?>(null)
        private set

    var previousLocation by mutableStateOf<Location?>(null)
        private set

    var weatherState by mutableStateOf(WeatherUiState())
        private set

    var policyState by mutableStateOf<RidePolicyUiModel?>(null)
        private set

    var trackingStatus by mutableStateOf(FreeRideTrackingStatus.ACTIVE)
        private set

    private val mutableTrackedPoints = mutableStateListOf<RideRecordGateway.RideRecordPoint>()
    private var lastWeatherRequestAt by mutableStateOf(0L)
    private var lastPolicyRequestAt by mutableStateOf(0L)
    private var activeSegmentStartedAtElapsedRealtimeMillis = startedAtElapsedRealtimeMillis
    private var accumulatedActiveElapsedMillis = 0L
    private var hasLeftLoopStartZone = false
    private var activePolicyMatchQualitySatisfied = false
    private var currentCoordinate: CourseCoordinate? = null

    val trackedPoints: List<RideRecordGateway.RideRecordPoint>
        get() = mutableTrackedPoints

    val isTrackingActive: Boolean
        get() = trackingStatus == FreeRideTrackingStatus.ACTIVE

    fun onLocationChanged(location: Location): Boolean {
        return onLocationSample(
            FreeRideLocationSample(
                latitude = location.latitude,
                longitude = location.longitude,
                location = location,
            ),
        )
    }

    fun onLocationSample(sample: FreeRideLocationSample): Boolean {
        if (!isTrackingActive) {
            return false
        }

        previousLocation = if (sample.location != null) currentLocation else previousLocation
        currentLocation = sample.location ?: currentLocation
        currentCoordinate = CourseCoordinate(sample.latitude, sample.longitude)
        mutableTrackedPoints.add(
            RideRecordGateway.RideRecordPoint(
                mutableTrackedPoints.size + 1,
                sample.latitude,
                sample.longitude,
            ),
        )
        return true
    }

    fun pauseTracking(nowElapsedRealtimeMillis: Long = SystemClock.elapsedRealtime()) {
        if (!isTrackingActive) {
            return
        }

        accumulatedActiveElapsedMillis += (nowElapsedRealtimeMillis - activeSegmentStartedAtElapsedRealtimeMillis).coerceAtLeast(0L)
        trackingStatus = FreeRideTrackingStatus.PAUSED
        previousLocation = null
    }

    fun resumeTracking(nowElapsedRealtimeMillis: Long = SystemClock.elapsedRealtime()) {
        if (isTrackingActive) {
            return
        }

        activeSegmentStartedAtElapsedRealtimeMillis = nowElapsedRealtimeMillis
        lastWeatherRequestAt = 0L
        lastPolicyRequestAt = 0L
        currentLocation = null
        currentCoordinate = null
        previousLocation = null
        trackingStatus = FreeRideTrackingStatus.ACTIVE
    }

    fun activeElapsedMillis(nowElapsedRealtimeMillis: Long = SystemClock.elapsedRealtime()): Long {
        val activeSegmentMillis = if (isTrackingActive) {
            (nowElapsedRealtimeMillis - activeSegmentStartedAtElapsedRealtimeMillis).coerceAtLeast(0L)
        } else {
            0L
        }
        return accumulatedActiveElapsedMillis + activeSegmentMillis
    }

    fun shouldRefreshWeather(nowElapsedRealtimeMillis: Long): Boolean {
        return shouldRefresh(nowElapsedRealtimeMillis, lastWeatherRequestAt)
    }

    fun markWeatherRequested(nowElapsedRealtimeMillis: Long) {
        lastWeatherRequestAt = nowElapsedRealtimeMillis
    }

    fun shouldRefreshPolicy(nowElapsedRealtimeMillis: Long): Boolean {
        return shouldRefresh(nowElapsedRealtimeMillis, lastPolicyRequestAt)
    }

    fun markPolicyRequested(nowElapsedRealtimeMillis: Long) {
        lastPolicyRequestAt = nowElapsedRealtimeMillis
    }

    fun updateWeatherState(newState: WeatherUiState) {
        weatherState = newState
    }

    fun updatePolicyState(newState: RidePolicyUiModel?) {
        policyState = newState
    }

    fun updateActivePolicyResult(
        result: RidePolicyEvaluationGateway.EvaluationResult,
        uiState: RidePolicyUiModel,
    ) {
        activePolicyMatchQualitySatisfied = isActivePolicyMatched(result)
        policyState = uiState
    }

    fun updatePolicyFailure(newState: RidePolicyUiModel) {
        activePolicyMatchQualitySatisfied = false
        policyState = newState
    }

    fun distanceToCourseDestinationMeters(routePoints: List<CourseRoutePointsGateway.RoutePoint>): Int? {
        val coordinate = currentCoordinate ?: return null
        val destination = orderedRoutePoints(routePoints).lastOrNull() ?: return null
        return distanceBetween(coordinate.latitude, coordinate.longitude, destination.latitude, destination.longitude)
    }

    fun courseCompletionCheck(routePoints: List<CourseRoutePointsGateway.RoutePoint>): CourseCompletionCheckResult? {
        val coordinate = currentCoordinate ?: return null
        val orderedRoutePoints = orderedRoutePoints(routePoints)
        if (orderedRoutePoints.isEmpty()) {
            return null
        }

        return if (isLoopCourse(orderedRoutePoints)) {
            val startPoint = orderedRoutePoints.first()
            val distanceToStart = distanceBetween(coordinate.latitude, coordinate.longitude, startPoint.latitude, startPoint.longitude)
            if (distanceToStart > LOOP_START_EXIT_THRESHOLD_METERS) {
                hasLeftLoopStartZone = true
            }
            CourseCompletionCheckResult(
                targetLabel = "출발 지점",
                distanceMeters = distanceToStart,
                eligible = hasLeftLoopStartZone && activePolicyMatchQualitySatisfied && distanceToStart <= COURSE_COMPLETION_DIALOG_THRESHOLD_METERS,
            )
        } else {
            hasLeftLoopStartZone = false
            val destination = orderedRoutePoints.last()
            val distanceToDestination = distanceBetween(coordinate.latitude, coordinate.longitude, destination.latitude, destination.longitude)
            CourseCompletionCheckResult(
                targetLabel = "도착 지점",
                distanceMeters = distanceToDestination,
                eligible = distanceToDestination <= COURSE_COMPLETION_DIALOG_THRESHOLD_METERS,
            )
        }
    }

    private fun isActivePolicyMatched(result: RidePolicyEvaluationGateway.EvaluationResult): Boolean {
        val status = result.offRoute.status
        val overallState = result.overallState
        return overallState != "UNDETERMINED" && (status == "ON_ROUTE" || status == "WARNING")
    }

    private fun orderedRoutePoints(routePoints: List<CourseRoutePointsGateway.RoutePoint>): List<CourseRoutePointsGateway.RoutePoint> {
        return routePoints.sortedBy { it.pointOrder }
    }

    private fun isLoopCourse(routePoints: List<CourseRoutePointsGateway.RoutePoint>): Boolean {
        if (routePoints.size < 3) {
            return false
        }
        val first = routePoints.first()
        val last = routePoints.last()
        return distanceBetween(first, last) <= LOOP_ROUTE_CLOSE_DISTANCE_METERS
    }

    private fun distanceBetween(
        first: CourseRoutePointsGateway.RoutePoint,
        second: CourseRoutePointsGateway.RoutePoint,
    ): Int {
        return distanceBetween(
            first.latitude,
            first.longitude,
            second.latitude,
            second.longitude,
        )
    }

    private fun distanceBetween(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
    ): Int {
        val latitudeDelta = Math.toRadians(endLatitude - startLatitude)
        val longitudeDelta = Math.toRadians(endLongitude - startLongitude)
        val startLatitudeRadians = Math.toRadians(startLatitude)
        val endLatitudeRadians = Math.toRadians(endLatitude)

        val haversine = Math.sin(latitudeDelta / 2).let { it * it } +
            Math.cos(startLatitudeRadians) * Math.cos(endLatitudeRadians) *
            Math.sin(longitudeDelta / 2).let { it * it }
        val angularDistance = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))
        return (EARTH_RADIUS_METERS * angularDistance).toInt()
    }
}

@Composable
internal fun rememberFreeRideTrackingState(
    courseId: Long?,
    locationGranted: Boolean,
    locationManager: LocationManager?,
    weatherGateway: CurrentWeatherGateway,
    ridePolicyGateway: RidePolicyEvaluationGateway,
    ridePolicyUiMapper: RidePolicyUiMapper,
): FreeRideTrackingController {
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

    return trackingController
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
    updateState: (RidePolicyEvaluationGateway.EvaluationResult, RidePolicyUiModel) -> Unit,
    updateFailureState: (RidePolicyUiModel) -> Unit,
) {
    ridePolicyGateway.evaluate(courseId, "ACTIVE", location, object : RidePolicyEvaluationGateway.Callback {
        override fun onSuccess(result: RidePolicyEvaluationGateway.EvaluationResult) {
            updateState(result, ridePolicyUiMapper.map(result))
        }

        override fun onFailure(message: String) {
            updateFailureState(RidePolicyUiModel("판단 보류", message, "", false, "", 0, 0, 0))
        }
    })
}

private fun shouldRefresh(now: Long, lastRequestedAt: Long): Boolean {
    if (lastRequestedAt == 0L) return true
    return now - lastRequestedAt >= 5_000L
}

private const val LOOP_ROUTE_CLOSE_DISTANCE_METERS = 80
private const val LOOP_START_EXIT_THRESHOLD_METERS = 250
private const val COURSE_COMPLETION_DIALOG_THRESHOLD_METERS = 150
private const val EARTH_RADIUS_METERS = 6_371_000.0
