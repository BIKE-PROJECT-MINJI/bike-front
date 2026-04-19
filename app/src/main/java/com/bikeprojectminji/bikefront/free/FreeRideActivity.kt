package com.bikeprojectminji.bikefront.free

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.auth.AuthProfileActivity
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.config.AppConfig
import com.bikeprojectminji.bikefront.course.CourseEditorActivity
import com.bikeprojectminji.bikefront.ride.HttpRideRecordGateway
import com.bikeprojectminji.bikefront.ride.RideRecordGateway
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import com.bikeprojectminji.bikefront.ridemap.HttpCourseRoutePointsGateway
import com.bikeprojectminji.bikefront.ridepolicy.HttpRidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiMapper
import com.bikeprojectminji.bikefront.ui.model.RideMode
import com.bikeprojectminji.bikefront.ui.theme.BikeFrontTheme
import com.bikeprojectminji.bikefront.ui.theme.GajaSpacing
import com.bikeprojectminji.bikefront.weather.CurrentWeatherGateway
import com.bikeprojectminji.bikefront.weather.HttpCurrentWeatherGateway
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import java.time.OffsetDateTime
import java.util.ArrayList
import kotlin.math.roundToInt

class FreeRideActivity : ComponentActivity() {

    enum class PermissionState(@StringRes val messageRes: Int) {
        REQUESTING(R.string.free_ride_permission_loading),
        NEED_PERMISSION(R.string.free_ride_permission_required),
        DENIED(R.string.free_ride_permission_denied),
        SETTINGS_REQUIRED(R.string.free_ride_permission_settings),
        GRANTED(R.string.free_ride_status_ready),
    }

    companion object {
        private const val EXTRA_MODE = "extra_mode"
        private const val EXTRA_COURSE_ID = "extra_course_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val LOCATION_SOURCE_ID = "ride_location_source"
        private const val LOCATION_LAYER_ID = "ride_location_layer"
        private const val ROUTE_SOURCE_ID = "ride_route_source"
        private const val ROUTE_LAYER_ID = "ride_route_layer"
        private const val KEY_MODE = "key_mode"
        private const val KEY_COURSE_ID = "key_course_id"
        private const val KEY_TITLE = "key_title"
        private const val KEY_PERMISSION_STATE = "key_permission_state"
        private const val KEY_IS_RIDING = "key_is_riding"
        private const val KEY_IS_SAVING = "key_is_saving"
        private const val KEY_STATUS_MESSAGE = "key_status_message"
        private const val KEY_WEATHER_SUMMARY = "key_weather_summary"
        private const val KEY_POLICY_SUMMARY = "key_policy_summary"
        private const val KEY_POLICY_BANNER = "key_policy_banner"
        private const val KEY_STARTED_AT = "key_started_at"
        private const val KEY_LATEST_LOCATION = "key_latest_location"
        private const val KEY_RECORDED_LATITUDES = "key_recorded_latitudes"
        private const val KEY_RECORDED_LONGITUDES = "key_recorded_longitudes"
        private const val KEY_MAP_VIEW_STATE = "key_map_view_state"
        private const val LOCATION_UPDATE_INTERVAL_MS = 2_000L
        private const val LOCATION_UPDATE_DISTANCE_M = 5f
        private const val MIN_RECORD_DISTANCE_M = 5f
        private const val MAX_ACCEPTABLE_ACCURACY_M = 50f
        private const val ACTIVE_POLICY_REEVALUATION_INTERVAL_MS = 6_000L
        private const val ACTIVE_POLICY_REEVALUATION_DISTANCE_M = 15f
        private const val WEATHER_REFRESH_INTERVAL_MS = 300_000L
        private const val WEATHER_REFRESH_DISTANCE_M = 1_000f

        fun newFreeRideIntent(context: Context): Intent {
            return Intent(context, FreeRideActivity::class.java)
                .putExtra(EXTRA_MODE, RideMode.FREE_RIDE.name)
                .putExtra(EXTRA_TITLE, "자유 주행")
        }

        fun newCourseFollowIntent(context: Context, courseId: Long, title: String): Intent {
            return Intent(context, FreeRideActivity::class.java)
                .putExtra(EXTRA_MODE, RideMode.COURSE_FOLLOW.name)
                .putExtra(EXTRA_COURSE_ID, courseId)
                .putExtra(EXTRA_TITLE, title)
        }
    }

    private val authSessionStore by lazy { AuthSessionStore(this) }
    private val rideRecordGateway: RideRecordGateway by lazy { HttpRideRecordGateway() }
    private val courseRoutePointsGateway: CourseRoutePointsGateway by lazy { HttpCourseRoutePointsGateway() }
    private val ridePolicyEvaluationGateway: RidePolicyEvaluationGateway by lazy { HttpRidePolicyEvaluationGateway() }
    private val ridePolicyUiMapper = RidePolicyUiMapper()
    private val currentWeatherGateway: CurrentWeatherGateway by lazy { HttpCurrentWeatherGateway() }

    private var rideMode by mutableStateOf(RideMode.FREE_RIDE)
    private var courseId by mutableStateOf(-1L)
    private var screenTitle by mutableStateOf("자유 주행")
    private var permissionState by mutableStateOf(PermissionState.REQUESTING)
    private var latestLocation by mutableStateOf<Location?>(null)
    private var isRiding by mutableStateOf(false)
    private var isSaving by mutableStateOf(false)
    private var statusMessage by mutableStateOf("")
    private var weatherSummary by mutableStateOf("날씨 확인 전")
    private var policySummary by mutableStateOf("정책 없음")
    private var policyBanner by mutableStateOf<String?>(null)
    private var startedAt: OffsetDateTime? = null
    private var mapViewState: Bundle? = null
    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private var currentRoutePoints: List<CourseRoutePointsGateway.RoutePoint> = emptyList()
    private val recordedPoints = mutableListOf<RideRecordGateway.RideRecordPoint>()
    private var lastPolicyEvaluationLocation: Location? = null
    private var lastPolicyEvaluationAtMillis: Long = 0L
    private var lastWeatherRefreshLocation: Location? = null
    private var lastWeatherRefreshAtMillis: Long = 0L
    private var policyRequestInFlight = false
    private var weatherRequestInFlight = false

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
        if (granted) {
            statusMessage = readyStatusMessage()
            startLocationUpdatesIfPossible()
        }
    }

    private val authProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            persistRideRecordAndOpenEditor()
        } else {
            isSaving = false
            statusMessage = getString(R.string.ride_finish_login_required_message)
        }
    }

    private val locationListener = LocationListener { location ->
        if (location.hasAccuracy() && location.accuracy > MAX_ACCEPTABLE_ACCURACY_M) return@LocationListener
        latestLocation = location
        renderCurrentLocationOnMap(location)
        maybeRefreshWeather(location)
        if (isRiding) {
            recordRidePointIfNeeded(location)
            statusMessage = activeStatusMessage()
            maybeEvaluateActivePolicy(location)
        } else {
            statusMessage = readyStatusMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        rideMode = runCatching { RideMode.valueOf(intent.getStringExtra(EXTRA_MODE) ?: RideMode.FREE_RIDE.name) }
            .getOrDefault(RideMode.FREE_RIDE)
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L)
        screenTitle = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank {
            if (rideMode == RideMode.FREE_RIDE) "자유 주행" else "코스 따라가기"
        }
        statusMessage = readyStatusMessage()
        restoreState(savedInstanceState)

        setContent {
            BikeFrontTheme {
                RideScreen(
                    rideMode = rideMode,
                    screenTitle = screenTitle,
                    permissionState = permissionState,
                    latestLocation = latestLocation,
                    isRiding = isRiding,
                    isSaving = isSaving,
                    statusMessage = statusMessage,
                    weatherSummary = weatherSummary,
                    policySummary = policySummary,
                    policyBanner = policyBanner,
                    mapViewSavedState = mapViewState,
                    canSaveRecord = canSaveRecord(),
                    onBack = { finish() },
                    onRequestPermission = { requestLocationPermission() },
                    onOpenSettings = { openAppSettings() },
                    onStartRide = { startRide() },
                    onFinishRide = { finishRide() },
                    onSaveRecord = { continueToSaveFlow() },
                    onCenterMyLocation = { centerCameraToContent() },
                    onMapReady = { view -> attachMap(view) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        if (hasLocationPermission()) {
            permissionState = PermissionState.GRANTED
            startLocationUpdatesIfPossible()
        } else if (permissionState == PermissionState.REQUESTING) {
            permissionState = PermissionState.NEED_PERMISSION
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onPause() {
        stopLocationUpdates()
        mapView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_MODE, rideMode.name)
        outState.putLong(KEY_COURSE_ID, courseId)
        outState.putString(KEY_TITLE, screenTitle)
        outState.putString(KEY_PERMISSION_STATE, permissionState.name)
        outState.putBoolean(KEY_IS_RIDING, isRiding)
        outState.putBoolean(KEY_IS_SAVING, isSaving)
        outState.putString(KEY_STATUS_MESSAGE, statusMessage)
        outState.putString(KEY_WEATHER_SUMMARY, weatherSummary)
        outState.putString(KEY_POLICY_SUMMARY, policySummary)
        outState.putString(KEY_POLICY_BANNER, policyBanner)
        outState.putString(KEY_STARTED_AT, startedAt?.toString())
        latestLocation?.let { outState.putParcelable(KEY_LATEST_LOCATION, it) }
        if (recordedPoints.isNotEmpty()) {
            outState.putDoubleArray(KEY_RECORDED_LATITUDES, recordedPoints.map { it.latitude }.toDoubleArray())
            outState.putDoubleArray(KEY_RECORDED_LONGITUDES, recordedPoints.map { it.longitude }.toDoubleArray())
        }
        val savedMapState = Bundle()
        mapView?.onSaveInstanceState(savedMapState)
        outState.putBundle(KEY_MAP_VIEW_STATE, savedMapState)
    }

    override fun onDestroy() {
        stopLocationUpdates()
        mapView?.onDestroy()
        super.onDestroy()
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        rideMode = runCatching { RideMode.valueOf(savedInstanceState.getString(KEY_MODE) ?: rideMode.name) }
            .getOrDefault(rideMode)
        courseId = savedInstanceState.getLong(KEY_COURSE_ID, courseId)
        screenTitle = savedInstanceState.getString(KEY_TITLE).orEmpty().ifBlank { screenTitle }
        permissionState = runCatching {
            PermissionState.valueOf(savedInstanceState.getString(KEY_PERMISSION_STATE) ?: permissionState.name)
        }.getOrDefault(permissionState)
        isRiding = savedInstanceState.getBoolean(KEY_IS_RIDING, isRiding)
        isSaving = savedInstanceState.getBoolean(KEY_IS_SAVING, isSaving)
        statusMessage = savedInstanceState.getString(KEY_STATUS_MESSAGE).orEmpty().ifBlank { statusMessage }
        weatherSummary = savedInstanceState.getString(KEY_WEATHER_SUMMARY).orEmpty().ifBlank { weatherSummary }
        policySummary = savedInstanceState.getString(KEY_POLICY_SUMMARY).orEmpty().ifBlank { policySummary }
        policyBanner = savedInstanceState.getString(KEY_POLICY_BANNER)
        startedAt = savedInstanceState.getString(KEY_STARTED_AT)?.let { OffsetDateTime.parse(it) }
        latestLocation = savedInstanceState.readLocationCompat(KEY_LATEST_LOCATION)
        recordedPoints.clear()
        val latitudes = savedInstanceState.getDoubleArray(KEY_RECORDED_LATITUDES)
        val longitudes = savedInstanceState.getDoubleArray(KEY_RECORDED_LONGITUDES)
        if (latitudes != null && longitudes != null) {
            val size = minOf(latitudes.size, longitudes.size)
            repeat(size) { index ->
                recordedPoints.add(
                    RideRecordGateway.RideRecordPoint(
                        index + 1,
                        latitudes[index],
                        longitudes[index],
                    ),
                )
            }
        }
        mapViewState = savedInstanceState.getBundle(KEY_MAP_VIEW_STATE)
    }

    private fun attachMap(view: MapView) {
        if (mapView === view) return
        mapView = view
        view.getMapAsync { map ->
            mapLibreMap = map
            map.setStyle(AppConfig.MAP_STYLE_URL) { style ->
                initializeMapStyle(style)
                loadRoutePointsIfNeeded()
                latestLocation?.let {
                    renderCurrentLocationOnMap(it)
                    centerCameraToContent()
                }
            }
        }
    }

    private fun requestLocationPermission() {
        permissionState = PermissionState.REQUESTING
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationUpdatesIfPossible() {
        if (!hasLocationPermission()) return
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL_MS, LOCATION_UPDATE_DISTANCE_M, locationListener)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL_MS, LOCATION_UPDATE_DISTANCE_M, locationListener)
            resolveBestLastKnownLocation(locationManager)?.let {
                latestLocation = it
                renderCurrentLocationOnMap(it)
                centerCameraToContent()
                maybeRefreshWeather(it)
            }
        } catch (_: SecurityException) {
            permissionState = PermissionState.SETTINGS_REQUIRED
        } catch (_: IllegalArgumentException) {
            permissionState = PermissionState.SETTINGS_REQUIRED
        }
    }

    private fun stopLocationUpdates() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        runCatching { locationManager.removeUpdates(locationListener) }
    }

    private fun resolveBestLastKnownLocation(locationManager: LocationManager): Location? {
        return locationManager.getProviders(true)
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    private fun initializeMapStyle(style: Style) {
        if (style.getSource(ROUTE_SOURCE_ID) == null) {
            style.addSource(GeoJsonSource(ROUTE_SOURCE_ID, FeatureCollection.fromFeatures(arrayOf())))
        }
        if (style.getLayer(ROUTE_LAYER_ID) == null) {
            val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
            routeLayer.setProperties(
                PropertyFactory.lineColor(ContextCompat.getColor(this, R.color.route_line)),
                PropertyFactory.lineWidth(4f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            )
            style.addLayer(routeLayer)
        }
        if (style.getSource(LOCATION_SOURCE_ID) == null) {
            style.addSource(GeoJsonSource(LOCATION_SOURCE_ID, FeatureCollection.fromFeatures(arrayOf())))
        }
        if (style.getLayer(LOCATION_LAYER_ID) == null) {
            val locationLayer = CircleLayer(LOCATION_LAYER_ID, LOCATION_SOURCE_ID)
            locationLayer.setProperties(
                PropertyFactory.circleColor(ContextCompat.getColor(this, R.color.info_text)),
                PropertyFactory.circleRadius(7f),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleStrokeColor(ContextCompat.getColor(this, R.color.surface)),
            )
            style.addLayer(locationLayer)
        }
    }

    private fun loadRoutePointsIfNeeded() {
        if (rideMode != RideMode.COURSE_FOLLOW || courseId <= 0) return
        courseRoutePointsGateway.loadRoutePoints(courseId, object : CourseRoutePointsGateway.Callback {
            override fun onSuccess(result: CourseRoutePointsGateway.RoutePointsResult) {
                currentRoutePoints = result.points
                renderRouteOnMap()
                centerCameraToContent()
            }

            override fun onFailure(message: String) {
                currentRoutePoints = emptyList()
                policySummary = message
            }
        })
    }

    private fun renderRouteOnMap() {
        val source = mapLibreMap?.style?.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID) ?: return
        if (currentRoutePoints.isEmpty()) {
            source.setGeoJson(FeatureCollection.fromFeatures(arrayOf()))
            return
        }
        val coordinates = currentRoutePoints.map { Point.fromLngLat(it.longitude, it.latitude) }
        source.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(coordinates))))
    }

    private fun renderCurrentLocationOnMap(location: Location) {
        val source = mapLibreMap?.style?.getSourceAs<GeoJsonSource>(LOCATION_SOURCE_ID) ?: return
        source.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude))))
    }

    private fun centerCameraToContent() {
        if (rideMode == RideMode.COURSE_FOLLOW && currentRoutePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            currentRoutePoints.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
            latestLocation?.let { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
            mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 96))
            return
        }
        val location = latestLocation ?: return
        mapLibreMap?.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder().target(LatLng(location.latitude, location.longitude)).zoom(16.0).build(),
            ),
        )
    }

    private fun startRide() {
        val currentLocation = latestLocation ?: run {
            statusMessage = getString(R.string.free_ride_location_waiting)
            return
        }
        if (!hasLocationPermission()) {
            permissionState = PermissionState.NEED_PERMISSION
            return
        }
        startedAt = OffsetDateTime.now()
        if (rideMode == RideMode.COURSE_FOLLOW) {
            evaluatePreStartPolicy(currentLocation)
        } else {
            activateRide(currentLocation)
        }
    }

    private fun activateRide(location: Location) {
        isRiding = true
        recordedPoints.clear()
        recordRidePointForce(location)
        statusMessage = activeStatusMessage()
    }

    private fun finishRide() {
        if (!isRiding) return
        latestLocation?.let { recordRidePointIfNeeded(it) }
        isRiding = false
        statusMessage = completeStatusMessage()
    }

    private fun continueToSaveFlow() {
        if (!canSaveRecord()) {
            statusMessage = getString(R.string.ride_finish_save_failed_message)
            return
        }
        if (!authSessionStore.isSignedIn()) {
            val intent = Intent(this, AuthProfileActivity::class.java)
            intent.putExtra(AuthProfileActivity.EXTRA_REASON, getString(R.string.ride_finish_login_required_message))
            authProfileLauncher.launch(intent)
            return
        }
        persistRideRecordAndOpenEditor()
    }

    private fun persistRideRecordAndOpenEditor() {
        val currentStartedAt = startedAt ?: run {
            statusMessage = getString(R.string.ride_finish_save_failed_message)
            return
        }
        if (recordedPoints.isEmpty()) {
            statusMessage = getString(R.string.ride_finish_save_failed_message)
            return
        }
        isSaving = true
        statusMessage = getString(R.string.ride_finish_saving_message)
        rideRecordGateway.saveRideRecord(
            authSessionStore.getAccessToken(),
            RideRecordGateway.RideRecordDraft(
                currentStartedAt,
                OffsetDateTime.now(),
                calculateDistanceMeters(),
                calculateDurationSeconds(currentStartedAt),
                buildCanonicalPath(recordedPoints),
            ),
            object : RideRecordGateway.Callback {
                override fun onSuccess(result: RideRecordGateway.RideRecordSaveResult) {
                    isSaving = false
                    statusMessage = getString(R.string.ride_finish_saved_success_message, result.rideRecordId)
                    openCourseEditor(result.rideRecordId)
                }

                override fun onFailure(message: String) {
                    isSaving = false
                    statusMessage = message
                }
            },
        )
    }

    private fun openCourseEditor(rideRecordId: Long) {
        val currentStartedAt = startedAt ?: OffsetDateTime.now()
        val intent = Intent(this, CourseEditorActivity::class.java)
        intent.putExtra(
            CourseEditorActivity.EXTRA_SOURCE_SUMMARY,
            if (rideMode == RideMode.FREE_RIDE) getString(R.string.free_ride_editor_source_summary)
            else getString(R.string.course_follow_editor_source_summary, screenTitle),
        )
        intent.putExtra(CourseEditorActivity.EXTRA_RIDE_RECORD_ID, rideRecordId)
        intent.putExtra(CourseEditorActivity.EXTRA_DISTANCE_KM, calculateDistanceMeters() / 1000.0)
        intent.putExtra(CourseEditorActivity.EXTRA_DURATION_MIN, calculateDurationSeconds(currentStartedAt) / 60)
        startActivity(intent)
    }

    private fun evaluatePreStartPolicy(location: Location) {
        if (courseId <= 0L) {
            policySummary = getString(R.string.ride_policy_missing_course_message)
            return
        }
        policyRequestInFlight = true
        ridePolicyEvaluationGateway.evaluate(courseId, "PRE_START", location, object : RidePolicyEvaluationGateway.Callback {
            override fun onSuccess(result: RidePolicyEvaluationGateway.EvaluationResult) {
                policyRequestInFlight = false
                val ui = ridePolicyUiMapper.map(result)
                policySummary = "${ui.stateLabel}: ${ui.message}"
                policyBanner = ui.bannerMessage.takeIf { ui.isShowBanner && it.isNotBlank() }
                if (result.startGate.status == "ELIGIBLE") {
                    activateRide(location)
                }
            }

            override fun onFailure(message: String) {
                policyRequestInFlight = false
                policySummary = message
                policyBanner = message
            }
        })
    }

    private fun maybeEvaluateActivePolicy(location: Location) {
        if (rideMode != RideMode.COURSE_FOLLOW || courseId <= 0 || !isRiding || policyRequestInFlight) return
        if (!shouldReevaluateActivePolicy(location)) return
        policyRequestInFlight = true
        lastPolicyEvaluationAtMillis = System.currentTimeMillis()
        lastPolicyEvaluationLocation = Location(location)
        ridePolicyEvaluationGateway.evaluate(courseId, "ACTIVE", location, object : RidePolicyEvaluationGateway.Callback {
            override fun onSuccess(result: RidePolicyEvaluationGateway.EvaluationResult) {
                policyRequestInFlight = false
                val ui = ridePolicyUiMapper.map(result)
                policySummary = "${ui.stateLabel}: ${ui.message}"
                policyBanner = ui.bannerMessage.takeIf { ui.isShowBanner && it.isNotBlank() }
            }

            override fun onFailure(message: String) {
                policyRequestInFlight = false
                policySummary = message
            }
        })
    }

    private fun shouldReevaluateActivePolicy(location: Location): Boolean {
        if (lastPolicyEvaluationLocation == null || lastPolicyEvaluationAtMillis == 0L) return true
        val elapsedMillis = System.currentTimeMillis() - lastPolicyEvaluationAtMillis
        if (elapsedMillis >= ACTIVE_POLICY_REEVALUATION_INTERVAL_MS) return true
        val lastLocation = lastPolicyEvaluationLocation ?: return true
        return location.distanceTo(lastLocation) >= ACTIVE_POLICY_REEVALUATION_DISTANCE_M
    }

    private fun maybeRefreshWeather(location: Location) {
        if (weatherRequestInFlight) return
        if (lastWeatherRefreshLocation != null && lastWeatherRefreshAtMillis > 0L) {
            val elapsedMillis = System.currentTimeMillis() - lastWeatherRefreshAtMillis
            val lastLocation = lastWeatherRefreshLocation
            if (lastLocation != null && elapsedMillis < WEATHER_REFRESH_INTERVAL_MS && location.distanceTo(lastLocation) < WEATHER_REFRESH_DISTANCE_M) {
                return
            }
        }
        weatherRequestInFlight = true
        lastWeatherRefreshAtMillis = System.currentTimeMillis()
        lastWeatherRefreshLocation = Location(location)
        currentWeatherGateway.loadCurrent(location.latitude, location.longitude, object : CurrentWeatherGateway.Callback {
            override fun onSuccess(result: CurrentWeatherGateway.WeatherResult) {
                weatherRequestInFlight = false
                weatherSummary = buildString {
                    append(result.temperatureC ?: "--")
                    append("°C")
                    if (!result.windDirectionText.isNullOrBlank() && result.windSpeedKmh != null) {
                        append(" · ")
                        append(result.windDirectionText)
                        append(" ")
                        append(result.windSpeedKmh)
                        append("km/h")
                    }
                }
            }

            override fun onEmpty() {
                weatherRequestInFlight = false
                weatherSummary = getString(R.string.ride_weather_empty_message)
            }

            override fun onFailure(message: String) {
                weatherRequestInFlight = false
                weatherSummary = message
            }
        })
    }

    private fun calculateDistanceMeters(): Int {
        var distance = 0f
        for (i in 1 until recordedPoints.size) {
            val before = recordedPoints[i - 1]
            val after = recordedPoints[i]
            val result = FloatArray(1)
            Location.distanceBetween(before.latitude, before.longitude, after.latitude, after.longitude, result)
            distance += result[0]
        }
        return distance.roundToInt()
    }

    private fun calculateDurationSeconds(currentStartedAt: OffsetDateTime): Int {
        return java.time.Duration.between(currentStartedAt, OffsetDateTime.now()).seconds.toInt().coerceAtLeast(0)
    }

    private fun buildCanonicalPath(points: List<RideRecordGateway.RideRecordPoint>): List<RideRecordGateway.RideRecordPoint> {
        if (points.size <= 2) return points
        val canonical = ArrayList<RideRecordGateway.RideRecordPoint>()
        canonical.add(points.first())
        var lastKept = points.first()
        for (index in 1 until points.lastIndex) {
            val candidate = points[index]
            val segmentDistance = FloatArray(1)
            Location.distanceBetween(lastKept.latitude, lastKept.longitude, candidate.latitude, candidate.longitude, segmentDistance)
            if (segmentDistance[0] >= MIN_RECORD_DISTANCE_M * 1.5f) {
                canonical.add(RideRecordGateway.RideRecordPoint(canonical.size + 1, candidate.latitude, candidate.longitude))
                lastKept = candidate
            }
        }
        canonical.add(RideRecordGateway.RideRecordPoint(canonical.size + 1, points.last().latitude, points.last().longitude))
        return canonical
    }

    private fun recordRidePointForce(location: Location) {
        recordedPoints.add(RideRecordGateway.RideRecordPoint(recordedPoints.size + 1, location.latitude, location.longitude))
    }

    private fun recordRidePointIfNeeded(location: Location) {
        if (recordedPoints.isEmpty()) {
            recordRidePointForce(location)
            return
        }
        val last = recordedPoints.last()
        val distance = FloatArray(1)
        Location.distanceBetween(last.latitude, last.longitude, location.latitude, location.longitude, distance)
        if (distance[0] < MIN_RECORD_DISTANCE_M) return
        recordRidePointForce(location)
    }

    private fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", packageName, null)))
    }

    private fun readyStatusMessage(): String {
        return if (rideMode == RideMode.FREE_RIDE) getString(R.string.free_ride_status_ready) else getString(R.string.course_follow_status_ready)
    }

    private fun canSaveRecord(): Boolean {
        return !isRiding && !isSaving && startedAt != null && recordedPoints.isNotEmpty()
    }

    private fun activeStatusMessage(): String {
        return if (rideMode == RideMode.FREE_RIDE) getString(R.string.free_ride_status_active) else getString(R.string.course_follow_status_active)
    }

    private fun completeStatusMessage(): String {
        return if (rideMode == RideMode.FREE_RIDE) getString(R.string.free_ride_status_complete) else getString(R.string.course_follow_status_complete)
    }
}

@Composable
private fun RideScreen(
    rideMode: RideMode,
    screenTitle: String,
    permissionState: FreeRideActivity.PermissionState,
    latestLocation: Location?,
    isRiding: Boolean,
    isSaving: Boolean,
    statusMessage: String,
    weatherSummary: String,
    policySummary: String,
    policyBanner: String?,
    mapViewSavedState: Bundle?,
    canSaveRecord: Boolean,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartRide: () -> Unit,
    onFinishRide: () -> Unit,
    onSaveRecord: () -> Unit,
    onCenterMyLocation: () -> Unit,
    onMapReady: (MapView) -> Unit,
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.inverseSurface)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                MapView(context).apply {
                    onCreate(mapViewSavedState)
                    onMapReady(this)
                }
            },
            update = { onMapReady(it) },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .safeDrawingPadding()
                .navigationBarsPadding()
                .padding(horizontal = GajaSpacing.ScreenPadding, vertical = GajaSpacing.Medium),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Medium)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "뒤로",
                                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                                )
                            }
                            Column(modifier = Modifier.padding(end = GajaSpacing.Medium)) {
                                Text(
                                    screenTitle,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    statusMessage,
                                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.88f),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f), shape = CircleShape) {
                        IconButton(onClick = onCenterMyLocation) {
                            Icon(
                                Icons.Outlined.MyLocation,
                                contentDescription = "내 위치",
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small), modifier = Modifier.fillMaxWidth()) {
                    FloatingHudChip(title = "풍향", value = "NW", modifier = Modifier.weight(1f))
                    FloatingHudChip(title = "풍속", value = "12m/s", modifier = Modifier.weight(1f))
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GajaSpacing.CardPadding, vertical = GajaSpacing.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = if (isRiding) "현재 속도" else "출발 준비",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = if (isRiding) "24.5" else "0.0",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            text = if (isRiding) "km/h" else "km/h 대기",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        GpsPill(text = if (permissionState == FreeRideActivity.PermissionState.GRANTED && latestLocation != null) "GPS 신호 양호" else "GPS 확인 중")
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(GajaSpacing.Small)) {
                if (permissionState != FreeRideActivity.PermissionState.GRANTED) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Column(modifier = Modifier.padding(GajaSpacing.Medium), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("위치 권한 필요", color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "주행을 시작하려면 위치 권한을 허용해 주세요.",
                                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.88f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onRequestPermission) { Text("권한 요청") }
                                OutlinedButton(onClick = onOpenSettings) { Text("설정 열기") }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(GajaSpacing.Small), modifier = Modifier.fillMaxWidth()) {
                    MinimalHudPanel(modifier = Modifier.weight(1f), title = "날씨", value = weatherSummary)
                    MinimalHudPanel(modifier = Modifier.weight(1f), title = "정책", value = policySummary)
                }

                if (!policyBanner.isNullOrBlank()) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                        Text(
                            text = policyBanner,
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f), shape = MaterialTheme.shapes.large) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(GajaSpacing.Medium),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!isRiding) {
                            Button(onClick = onStartRide, enabled = permissionState == FreeRideActivity.PermissionState.GRANTED && latestLocation != null, modifier = Modifier.weight(1f)) {
                                Text(if (rideMode == RideMode.FREE_RIDE) "주행 시작" else "코스 시작")
                            }
                        } else {
                            Button(onClick = onFinishRide, modifier = Modifier.weight(1f)) {
                                Text("주행 종료")
                            }
                        }
                        OutlinedButton(onClick = onSaveRecord, enabled = canSaveRecord, modifier = Modifier.weight(1f)) {
                            Text(if (isSaving) "저장 중" else "기록 저장")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingHudChip(modifier: Modifier = Modifier, title: String, value: String) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun MinimalHudPanel(modifier: Modifier = Modifier, title: String, value: String) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.88f), style = MaterialTheme.typography.labelMedium)
            Text(value, color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun GpsPill(text: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            text = "• $text",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun Double.format(digits: Int): String = String.format("%.${digits}f", this)

@Composable
private fun stringResourceCompat(@StringRes id: Int): String = LocalContext.current.getString(id)

private fun Bundle.readLocationCompat(key: String): Location? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, Location::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}
