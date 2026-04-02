package com.bikeprojectminji.bikefront;

import android.annotation.SuppressLint;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bikeprojectminji.bikefront.auth.AuthProfileActivity;
import com.bikeprojectminji.bikefront.auth.AuthSessionStore;
import com.bikeprojectminji.bikefront.course.CourseEditorActivity;
import com.bikeprojectminji.bikefront.ride.HttpRideRecordGateway;
import com.bikeprojectminji.bikefront.ride.RideRecordGateway;
import com.bikeprojectminji.bikefront.ridepolicy.HttpRidePolicyEvaluationGateway;
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway;
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiMapper;
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiModel;
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway;
import com.bikeprojectminji.bikefront.ridemap.HttpCourseRoutePointsGateway;
import com.bikeprojectminji.bikefront.speed.RideSpeedFormatter;
import com.bikeprojectminji.bikefront.speed.RideSpeedUiState;
import com.bikeprojectminji.bikefront.weather.CurrentWeatherGateway;
import com.bikeprojectminji.bikefront.weather.HttpCurrentWeatherGateway;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;

import com.bikeprojectminji.bikefront.config.AppConfig;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RideEntryActivity extends AppCompatActivity {

    private static final String EXTRA_COURSE_ID = "extra_course_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_DISTANCE_TEXT = "extra_distance_text";
    private static final String EXTRA_DURATION_TEXT = "extra_duration_text";
    private static final String KEY_PERMISSION_STATE = "key_permission_state";
    private static final String KEY_RIDE_PHASE = "key_ride_phase";
    private static final String KEY_ACTIVE_RIDE_STARTED_AT = "key_active_ride_started_at";
    private static final String KEY_RECORDED_POINT_ORDERS = "key_recorded_point_orders";
    private static final String KEY_RECORDED_POINT_LATITUDES = "key_recorded_point_latitudes";
    private static final String KEY_RECORDED_POINT_LONGITUDES = "key_recorded_point_longitudes";
    private static final String PHASE_PRE_START = "PRE_START";
    private static final String PHASE_ACTIVE = "ACTIVE";
    private static final String PHASE_COMPLETE = "COMPLETE";
    private static final String ROUTE_SOURCE_ID = "ride_route_source";
    private static final String ROUTE_LAYER_ID = "ride_route_layer";
    private static final String LOCATION_SOURCE_ID = "ride_location_source";
    private static final String LOCATION_LAYER_ID = "ride_location_layer";
    private static final long LOCATION_UPDATE_INTERVAL_MS = 2_000L;
    private static final float LOCATION_UPDATE_DISTANCE_M = 5f;
    private static final long ACTIVE_POLICY_REEVALUATION_INTERVAL_MS = 6_000L;
    private static final float ACTIVE_POLICY_REEVALUATION_DISTANCE_M = 15f;
    private static final long WEATHER_REFRESH_INTERVAL_MS = 300_000L;
    private static final float WEATHER_REFRESH_DISTANCE_M = 1_000f;

    private enum PermissionUiState {
        REQUESTING,
        NEED_PERMISSION,
        DENIED,
        SETTINGS_REQUIRED,
        GRANTED
    }

    private TextView ridePermissionMessageTextView;
    private TextView ridePermissionBlockedFeaturesTextView;
    private TextView rideFlowStatusTextView;
    private TextView rideMapStatusTextView;
    private TextView ridePolicyBannerTextView;
    private View rideSummaryContainer;
    private View ridePermissionContainer;
    private TextView rideSpeedValueTextView;
    private TextView rideSpeedMessageTextView;
    private TextView rideWeatherValueTextView;
    private TextView rideWeatherStatusTextView;
    private TextView rideWindValueTextView;
    private TextView rideWindStatusTextView;
    private TextView ridePolicyStateTextView;
    private TextView ridePolicyMessageTextView;
    private Button rideMyLocationButton;
    private Button rideStartButton;
    private Button rideFinishButton;
    private Button ridePermissionRetryButton;
    private Button ridePermissionSettingsButton;
    private View ridePostRideContainer;
    private TextView ridePostRideMessageTextView;
    private TextView ridePostRideTitleTextView;
    private Button ridePostRideSaveButton;
    private Button ridePostRideLaterButton;
    private MapView rideMapView;
    private AuthSessionStore authSessionStore;
    private RideSaveCoordinator rideSaveCoordinator;

    private PermissionUiState permissionUiState = PermissionUiState.REQUESTING;
    private boolean openedSettings;
    private long courseId;
    private String ridePhase = PHASE_PRE_START;
    private boolean routeLoaded;
    private boolean routeLoadFailed;
    private boolean hasCenteredOnRoute;
    private boolean hasCenteredOnRouteWithLocation;
    private boolean locationUpdatesRegistered;
    private boolean policyRequestInFlight;
    private boolean weatherRequestInFlight;
    private boolean startRequested;
    private Location latestLocation;
    private Location previousLocation;
    private Location lastPolicyEvaluationLocation;
    private Location lastWeatherRefreshLocation;
    private List<CourseRoutePointsGateway.RoutePoint> currentRoutePoints = Collections.emptyList();
    private MapLibreMap rideMapLibreMap;
    private String currentMapMessage;
    private String currentFlowMessage;
    private long lastPolicyEvaluationAtMillis;
    private long lastWeatherRefreshAtMillis;
    private OffsetDateTime activeRideStartedAt;
    private final List<RideRecordGateway.RideRecordPoint> recordedRidePoints = new ArrayList<>();

    private final RidePolicyEvaluationGateway ridePolicyEvaluationGateway = new HttpRidePolicyEvaluationGateway();
    private final RidePolicyUiMapper ridePolicyUiMapper = new RidePolicyUiMapper();
    private final CourseRoutePointsGateway courseRoutePointsGateway = new HttpCourseRoutePointsGateway();
    private final RideSpeedFormatter rideSpeedFormatter = new RideSpeedFormatter();
    private final CurrentWeatherGateway currentWeatherGateway = new HttpCurrentWeatherGateway();
    private final RideRecordGateway rideRecordGateway = new HttpRideRecordGateway();
    private final LocationListener foregroundLocationListener = location -> {
        if (location == null || isFinishing() || isDestroyed()) {
            return;
        }

        handleResolvedLocation(location, true, false);
    };

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::handleLocationPermissionResult
    );

    private final ActivityResultLauncher<Intent> authProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    persistRideRecordAndOpenEditor();
                } else {
                    showPostRideMessage(getString(R.string.ride_finish_login_required_message));
                }
            }
    );

    public static Intent newIntent(Context context, long courseId, String title, String distanceText, String durationText) {
        Intent intent = new Intent(context, RideEntryActivity.class);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_DISTANCE_TEXT, distanceText);
        intent.putExtra(EXTRA_DURATION_TEXT, durationText);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_ride_entry);

        rideMapView = findViewById(R.id.rideMapView);
        rideMapView.onCreate(savedInstanceState);
        TextView screenTitleTextView = findViewById(R.id.rideTitleTextView);
        rideSummaryContainer = findViewById(R.id.rideSummaryContainer);
        ridePermissionContainer = findViewById(R.id.ridePermissionContainer);
        TextView titleTextView = findViewById(R.id.rideCourseTitleTextView);
        TextView distanceTextView = findViewById(R.id.rideCourseDistanceTextView);
        TextView durationTextView = findViewById(R.id.rideCourseDurationTextView);
        ridePermissionMessageTextView = findViewById(R.id.ridePermissionMessageTextView);
        ridePermissionBlockedFeaturesTextView = findViewById(R.id.ridePermissionBlockedFeaturesTextView);
        rideFlowStatusTextView = findViewById(R.id.rideFlowStatusTextView);
        rideMapStatusTextView = findViewById(R.id.rideMapStatusTextView);
        ridePolicyBannerTextView = findViewById(R.id.ridePolicyBannerTextView);
        rideSpeedValueTextView = findViewById(R.id.rideSpeedValueTextView);
        rideSpeedMessageTextView = findViewById(R.id.rideSpeedMessageTextView);
        rideWeatherValueTextView = findViewById(R.id.rideWeatherValueTextView);
        rideWeatherStatusTextView = findViewById(R.id.rideWeatherStatusTextView);
        rideWindValueTextView = findViewById(R.id.rideWindValueTextView);
        rideWindStatusTextView = findViewById(R.id.rideWindStatusTextView);
        ridePolicyStateTextView = findViewById(R.id.ridePolicyStateTextView);
        ridePolicyMessageTextView = findViewById(R.id.ridePolicyMessageTextView);
        rideMyLocationButton = findViewById(R.id.rideMyLocationButton);
        rideStartButton = findViewById(R.id.rideStartButton);
        rideFinishButton = findViewById(R.id.rideFinishButton);
        ridePermissionRetryButton = findViewById(R.id.ridePermissionRetryButton);
        ridePermissionSettingsButton = findViewById(R.id.ridePermissionSettingsButton);
        ridePostRideContainer = findViewById(R.id.ridePostRideContainer);
        ridePostRideTitleTextView = findViewById(R.id.ridePostRideTitleTextView);
        ridePostRideMessageTextView = findViewById(R.id.ridePostRideMessageTextView);
        ridePostRideSaveButton = findViewById(R.id.ridePostRideSaveButton);
        ridePostRideLaterButton = findViewById(R.id.ridePostRideLaterButton);

        authSessionStore = new AuthSessionStore(this);
        RideSaveCoordinator retainedCoordinator = (RideSaveCoordinator) getLastCustomNonConfigurationInstance();
        rideSaveCoordinator = retainedCoordinator != null ? retainedCoordinator : new RideSaveCoordinator(rideRecordGateway);
        rideSaveCoordinator.attach(this);

        Intent intent = getIntent();
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L);
        screenTitleTextView.setText(intent.getStringExtra(EXTRA_TITLE));
        titleTextView.setText(intent.getStringExtra(EXTRA_TITLE));
        distanceTextView.setText(intent.getStringExtra(EXTRA_DISTANCE_TEXT));
        durationTextView.setText(intent.getStringExtra(EXTRA_DURATION_TEXT));

        rideMapView.getMapAsync(mapLibreMap -> {
            rideMapLibreMap = mapLibreMap;
            rideMapLibreMap.addOnCameraIdleListener(this::updateMyLocationButtonVisibility);
            mapLibreMap.setStyle(AppConfig.MAP_STYLE_URL, style -> {
                initializeMapStyle(style);
                renderMapOverlays();
                if (!hasCenteredOnRoute) {
                    centerCameraToRouteIfPossible();
                }
                renderCurrentLocationOnMap();
            });
        });

        rideStartButton.setOnClickListener(v -> requestRideStart());
        rideFinishButton.setOnClickListener(v -> completeRide());
        rideMyLocationButton.setOnClickListener(v -> centerCameraOnCurrentLocation());
        ridePermissionRetryButton.setOnClickListener(v -> requestLocationPermission());
        ridePermissionSettingsButton.setOnClickListener(v -> openAppSettings());
        ridePostRideSaveButton.setOnClickListener(v -> continueToSaveFlow());
        ridePostRideLaterButton.setOnClickListener(v -> finish());

        loadRoutePoints();

        if (savedInstanceState != null) {
            String savedState = savedInstanceState.getString(KEY_PERMISSION_STATE, PermissionUiState.REQUESTING.name());
            permissionUiState = PermissionUiState.valueOf(savedState);
            ridePhase = savedInstanceState.getString(KEY_RIDE_PHASE, PHASE_PRE_START);
            String startedAt = savedInstanceState.getString(KEY_ACTIVE_RIDE_STARTED_AT);
            if (startedAt != null && !startedAt.isBlank()) {
                activeRideStartedAt = OffsetDateTime.parse(startedAt);
            }
            restoreRecordedRidePoints(savedInstanceState);
            if (hasLocationPermission()) {
                resumeRideFlow();
            } else if (permissionUiState == PermissionUiState.REQUESTING) {
                renderPermissionState(PermissionUiState.NEED_PERMISSION);
            } else {
                renderPermissionState(permissionUiState);
            }
        } else {
            renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
            requestLocationPermission();
        }

        renderRideStartButton();
        renderPostRideState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rideMapView.onResume();

        if (hasLocationPermission()) {
            startForegroundLocationUpdates();
        }

        if (openedSettings) {
            openedSettings = false;
            if (hasLocationPermission()) {
                resumeRideFlow();
            } else {
                renderPermissionState(PermissionUiState.SETTINGS_REQUIRED);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        rideMapView.onSaveInstanceState(outState);
        outState.putString(KEY_PERMISSION_STATE, permissionUiState.name());
        outState.putString(KEY_RIDE_PHASE, ridePhase);
        outState.putString(KEY_ACTIVE_RIDE_STARTED_AT, activeRideStartedAt == null ? null : activeRideStartedAt.toString());
        saveRecordedRidePoints(outState);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        rideSaveCoordinator.detach();
        return rideSaveCoordinator;
    }

    @Override
    protected void onStart() {
        super.onStart();
        rideMapView.onStart();
    }

    @Override
    protected void onPause() {
        stopForegroundLocationUpdates();
        rideMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        rideMapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        rideSaveCoordinator.detach();
        rideMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        rideMapView.onLowMemory();
    }

    private void requestLocationPermission() {
        if (hasLocationPermission()) {
            startForegroundLocationUpdates();
            resumeRideFlow();
            return;
        }

        renderPermissionState(PermissionUiState.REQUESTING);
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void handleLocationPermissionResult(boolean granted) {
        if (granted) {
            startForegroundLocationUpdates();
            resumeRideFlow();
            return;
        }

        stopForegroundLocationUpdates();

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            renderPermissionState(PermissionUiState.DENIED);
            return;
        }

        renderPermissionState(PermissionUiState.SETTINGS_REQUIRED);
    }

    private void resumeRideFlow() {
        renderPermissionState(PermissionUiState.GRANTED);
        Location seedLocation = resolveBestLastKnownLocation();
        if (seedLocation != null) {
            handleResolvedLocation(seedLocation, true, true);
            return;
        }

        renderSpeedCard();
        renderWeatherLoading();
        renderCurrentLocationOnMap();
        renderMapOverlays();
        renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
    }

    private void renderPermissionState(PermissionUiState newState) {
        permissionUiState = newState;

        switch (newState) {
            case REQUESTING:
                ridePermissionContainer.setVisibility(View.VISIBLE);
                ridePermissionMessageTextView.setText(R.string.ride_permission_resuming_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(false);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.GONE);
                currentFlowMessage = getString(R.string.ride_permission_resuming_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
                break;
            case NEED_PERMISSION:
                ridePermissionContainer.setVisibility(View.VISIBLE);
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                currentFlowMessage = getString(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case DENIED:
                ridePermissionContainer.setVisibility(View.VISIBLE);
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                currentFlowMessage = getString(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case SETTINGS_REQUIRED:
                ridePermissionContainer.setVisibility(View.VISIBLE);
                ridePermissionMessageTextView.setText(R.string.ride_permission_settings_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                currentFlowMessage = getString(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case GRANTED:
                ridePermissionContainer.setVisibility(View.GONE);
                ridePermissionMessageTextView.setText(R.string.ride_permission_resuming_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_resumed);
                ridePermissionRetryButton.setVisibility(View.GONE);
                ridePermissionSettingsButton.setVisibility(View.GONE);
                currentFlowMessage = getString(R.string.ride_location_status_resumed);
                break;
        }

        renderRideStartButton();
        syncGuidanceMessages();
    }

    private void handleResolvedLocation(Location location, boolean allowPreStartEvaluation, boolean forceWeatherRefresh) {
        if (location == null) {
            return;
        }

        if (latestLocation != null && latestLocation.getTime() != location.getTime()) {
            previousLocation = latestLocation;
        }

        latestLocation = location;
        recordRidePointIfNeeded(location);
        renderSpeedCard();
        renderCurrentLocationOnMap();
        centerCameraToRouteIfPossible();
        renderMapOverlays();

        if (PHASE_ACTIVE.equals(ridePhase)) {
            maybeEvaluateActivePolicy(location);
        } else if (allowPreStartEvaluation) {
            evaluateRidePolicy(location, ridePhase, true);
        }

        maybeRefreshWeather(location, forceWeatherRefresh);
    }

    private void evaluateRidePolicy(Location location, String phase, boolean force) {
        if (courseId <= 0) {
            renderPolicyError(getString(R.string.ride_policy_missing_course_message));
            return;
        }

        if (policyRequestInFlight) {
            return;
        }

        if (!force && PHASE_ACTIVE.equals(phase) && !shouldReevaluateActivePolicy(location)) {
            return;
        }

        policyRequestInFlight = true;
        lastPolicyEvaluationAtMillis = System.currentTimeMillis();
        lastPolicyEvaluationLocation = new Location(location);
        renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
        ridePolicyEvaluationGateway.evaluate(courseId, phase, location, new RidePolicyEvaluationGateway.Callback() {
            @Override
            public void onSuccess(RidePolicyEvaluationGateway.EvaluationResult result) {
                policyRequestInFlight = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (PHASE_PRE_START.equals(phase) && "ELIGIBLE".equals(result.getStartGate().getStatus())) {
                    if (startRequested) {
                        startRequested = false;
                        ridePhase = PHASE_ACTIVE;
                        if (activeRideStartedAt == null) {
                            activeRideStartedAt = OffsetDateTime.now();
                        }
                        renderRideStartButton();
                        evaluateRidePolicy(location, PHASE_ACTIVE, true);
                        return;
                    }

                    ridePhase = PHASE_PRE_START;
                    renderRideStartButton();
                    renderPolicyResult(ridePolicyUiMapper.map(result));
                    return;
                }

                ridePhase = result.getPhase();
                startRequested = false;
                renderRideStartButton();
                renderPolicyResult(ridePolicyUiMapper.map(result));
            }

            @Override
            public void onFailure(String message) {
                policyRequestInFlight = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                startRequested = false;
                renderRideStartButton();
                renderPolicyError(message);
            }
        });
    }

    private void requestRideStart() {
        if (latestLocation == null) {
            renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
            return;
        }

        startRequested = true;
        renderRideStartButton();
        evaluateRidePolicy(latestLocation, PHASE_PRE_START, true);
    }

    private void completeRide() {
        ensureRecordedFallbackPoint();
        ridePhase = PHASE_COMPLETE;
        renderRideStartButton();
        renderPostRideState();
        showPostRideMessage(getString(R.string.ride_finish_saved_preview_message));
    }

    private void renderRideStartButton() {
        if (!hasLocationPermission() || PHASE_ACTIVE.equals(ridePhase) || PHASE_COMPLETE.equals(ridePhase)) {
            rideStartButton.setVisibility(View.GONE);
        } else {
            rideStartButton.setVisibility(View.VISIBLE);
            rideStartButton.setEnabled(!startRequested);
        }

        rideFinishButton.setVisibility(PHASE_ACTIVE.equals(ridePhase) ? View.VISIBLE : View.GONE);
    }

    private void renderPostRideState() {
        if (PHASE_COMPLETE.equals(ridePhase)) {
            ridePostRideContainer.setVisibility(View.VISIBLE);
            return;
        }
        ridePostRideContainer.setVisibility(View.GONE);
    }

    private void continueToSaveFlow() {
        if (!authSessionStore.isSignedIn()) {
            Intent intent = new Intent(this, AuthProfileActivity.class);
            intent.putExtra(AuthProfileActivity.EXTRA_REASON, getString(R.string.ride_finish_login_required_message));
            authProfileLauncher.launch(intent);
            return;
        }
        persistRideRecordAndOpenEditor();
    }

    private void openCourseEditor(long rideRecordId) {
        Intent intent = new Intent(this, CourseEditorActivity.class);
        intent.putExtra(CourseEditorActivity.EXTRA_SOURCE_SUMMARY, getString(R.string.ride_finish_saved_preview_message));
        intent.putExtra(CourseEditorActivity.EXTRA_RIDE_RECORD_ID, rideRecordId);
        startActivity(intent);
    }

    private void showPostRideMessage(String message) {
        ridePostRideMessageTextView.setText(message);
    }

    private void persistRideRecordAndOpenEditor() {
        if (activeRideStartedAt == null || latestLocation == null) {
            showPostRideMessage(getString(R.string.ride_finish_save_failed_message));
            return;
        }

        if (recordedRidePoints.isEmpty()) {
            recordRidePointIfNeeded(latestLocation);
        }

        rideSaveCoordinator.startSave(
                authSessionStore.getAccessToken(),
                new RideRecordGateway.RideRecordDraft(
                        activeRideStartedAt,
                        OffsetDateTime.now(),
                        calculateDistanceMeters(),
                        calculateDurationSec(),
                        new ArrayList<>(recordedRidePoints)
                )
        );
    }

    private int calculateDistanceMeters() {
        float distance = 0f;
        for (int i = 1; i < recordedRidePoints.size(); i++) {
            RideRecordGateway.RideRecordPoint before = recordedRidePoints.get(i - 1);
            RideRecordGateway.RideRecordPoint after = recordedRidePoints.get(i);
            float[] sectionDistance = new float[1];
            Location.distanceBetween(before.getLatitude(), before.getLongitude(), after.getLatitude(), after.getLongitude(), sectionDistance);
            distance += sectionDistance[0];
        }
        return Math.round(distance);
    }

    private int calculateDurationSec() {
        if (activeRideStartedAt == null) {
            return 0;
        }
        return (int) java.time.Duration.between(activeRideStartedAt, OffsetDateTime.now()).getSeconds();
    }

    private void ensureRecordedFallbackPoint() {
        if (latestLocation == null || !recordedRidePoints.isEmpty()) {
            return;
        }
        recordedRidePoints.add(new RideRecordGateway.RideRecordPoint(
                1,
                latestLocation.getLatitude(),
                latestLocation.getLongitude()
        ));
    }

    private void saveRecordedRidePoints(@NonNull Bundle outState) {
        ArrayList<Integer> pointOrders = new ArrayList<>();
        ArrayList<Double> latitudes = new ArrayList<>();
        ArrayList<Double> longitudes = new ArrayList<>();
        for (RideRecordGateway.RideRecordPoint point : recordedRidePoints) {
            pointOrders.add(point.getPointOrder());
            latitudes.add(point.getLatitude());
            longitudes.add(point.getLongitude());
        }
        outState.putIntegerArrayList(KEY_RECORDED_POINT_ORDERS, pointOrders);
        outState.putSerializable(KEY_RECORDED_POINT_LATITUDES, latitudes);
        outState.putSerializable(KEY_RECORDED_POINT_LONGITUDES, longitudes);
    }

    @SuppressWarnings("unchecked")
    private void restoreRecordedRidePoints(@NonNull Bundle savedInstanceState) {
        recordedRidePoints.clear();
        ArrayList<Integer> pointOrders = savedInstanceState.getIntegerArrayList(KEY_RECORDED_POINT_ORDERS);
        ArrayList<Double> latitudes = (ArrayList<Double>) savedInstanceState.getSerializable(KEY_RECORDED_POINT_LATITUDES);
        ArrayList<Double> longitudes = (ArrayList<Double>) savedInstanceState.getSerializable(KEY_RECORDED_POINT_LONGITUDES);
        if (pointOrders == null || latitudes == null || longitudes == null) {
            return;
        }
        int pointCount = Math.min(pointOrders.size(), Math.min(latitudes.size(), longitudes.size()));
        for (int index = 0; index < pointCount; index++) {
            recordedRidePoints.add(new RideRecordGateway.RideRecordPoint(
                    pointOrders.get(index),
                    latitudes.get(index),
                    longitudes.get(index)
            ));
        }
    }

    void renderRideSaveInFlight() {
        ridePostRideSaveButton.setEnabled(false);
        ridePostRideTitleTextView.setText(R.string.ride_finish_summary_title);
        showPostRideMessage(getString(R.string.ride_finish_saving_message));
    }

    void renderRideSaveSuccess(RideRecordGateway.RideRecordSaveResult result) {
        ridePostRideSaveButton.setEnabled(true);
        ridePostRideTitleTextView.setText(R.string.ride_finish_summary_title);
        showPostRideMessage(getString(R.string.ride_finish_saved_success_message, result.getRideRecordId()));
        openCourseEditor(result.getRideRecordId());
    }

    void renderRideSaveFailure(String message) {
        ridePostRideSaveButton.setEnabled(true);
        ridePostRideTitleTextView.setText(R.string.ride_finish_summary_title);
        showPostRideMessage(message);
    }

    private static final class RideSaveCoordinator {
        private final RideRecordGateway rideRecordGateway;
        private RideEntryActivity activity;
        private boolean inFlight;
        private RideRecordGateway.RideRecordSaveResult pendingSuccess;
        private String pendingFailure;

        private RideSaveCoordinator(RideRecordGateway rideRecordGateway) {
            this.rideRecordGateway = rideRecordGateway;
        }

        void attach(RideEntryActivity activity) {
            this.activity = activity;
            if (inFlight) {
                activity.renderRideSaveInFlight();
            } else if (pendingSuccess != null) {
                RideRecordGateway.RideRecordSaveResult result = pendingSuccess;
                pendingSuccess = null;
                activity.renderRideSaveSuccess(result);
            } else if (pendingFailure != null) {
                String message = pendingFailure;
                pendingFailure = null;
                activity.renderRideSaveFailure(message);
            }
        }

        void detach() {
            this.activity = null;
        }

        void startSave(String accessToken, RideRecordGateway.RideRecordDraft draft) {
            if (inFlight) {
                return;
            }
            inFlight = true;
            if (activity != null) {
                activity.renderRideSaveInFlight();
            }
            rideRecordGateway.saveRideRecord(accessToken, draft, new RideRecordGateway.Callback() {
                @Override
                public void onSuccess(RideRecordGateway.RideRecordSaveResult result) {
                    inFlight = false;
                    if (activity != null) {
                        activity.renderRideSaveSuccess(result);
                        return;
                    }
                    pendingSuccess = result;
                }

                @Override
                public void onFailure(String message) {
                    inFlight = false;
                    if (activity != null) {
                        activity.renderRideSaveFailure(message);
                        return;
                    }
                    pendingFailure = message;
                }
            });
        }
    }

    private void recordRidePointIfNeeded(Location location) {
        if (!PHASE_ACTIVE.equals(ridePhase)) {
            return;
        }
        if (!recordedRidePoints.isEmpty()) {
            RideRecordGateway.RideRecordPoint lastPoint = recordedRidePoints.get(recordedRidePoints.size() - 1);
            float[] sectionDistance = new float[1];
            Location.distanceBetween(lastPoint.getLatitude(), lastPoint.getLongitude(), location.getLatitude(), location.getLongitude(), sectionDistance);
            if (sectionDistance[0] < 5f) {
                return;
            }
        }
        recordedRidePoints.add(new RideRecordGateway.RideRecordPoint(
                recordedRidePoints.size() + 1,
                location.getLatitude(),
                location.getLongitude()
        ));
    }

    private boolean shouldReevaluateActivePolicy(Location location) {
        if (lastPolicyEvaluationLocation == null || lastPolicyEvaluationAtMillis == 0L) {
            return true;
        }

        long elapsedMillis = System.currentTimeMillis() - lastPolicyEvaluationAtMillis;
        if (elapsedMillis >= ACTIVE_POLICY_REEVALUATION_INTERVAL_MS) {
            return true;
        }

        return location.distanceTo(lastPolicyEvaluationLocation) >= ACTIVE_POLICY_REEVALUATION_DISTANCE_M;
    }

    private void maybeEvaluateActivePolicy(Location location) {
        evaluateRidePolicy(location, PHASE_ACTIVE, false);
    }

    private Location resolveBestLastKnownLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return null;
            }

            List<String> providers = locationManager.getProviders(true);
            Location newestLocation = null;
            for (String provider : providers) {
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate == null) {
                    continue;
                }
                if (newestLocation == null || candidate.getTime() > newestLocation.getTime()) {
                    newestLocation = candidate;
                }
            }

            return newestLocation;
        } catch (SecurityException exception) {
            renderPolicyError(getString(R.string.ride_policy_location_error_message));
            return null;
        }
    }

    private void renderSpeedCard() {
        RideSpeedUiState uiState = rideSpeedFormatter.format(latestLocation, previousLocation);
        rideSpeedValueTextView.setText(uiState.getSpeedText());
        if (uiState.getMessage() == null || uiState.getMessage().isBlank()) {
            rideSpeedMessageTextView.setText(R.string.ride_speed_message_default);
        } else {
            rideSpeedMessageTextView.setText(uiState.getMessage());
        }
    }

    private void maybeRefreshWeather(Location location, boolean force) {
        if (location == null) {
            renderWeatherLoading();
            return;
        }

        if (weatherRequestInFlight) {
            return;
        }

        if (!force && !shouldRefreshWeather(location)) {
            return;
        }

        weatherRequestInFlight = true;
        lastWeatherRefreshAtMillis = System.currentTimeMillis();
        lastWeatherRefreshLocation = new Location(location);

        currentWeatherGateway.loadCurrent(location.getLatitude(), location.getLongitude(), new CurrentWeatherGateway.Callback() {
            @Override
            public void onSuccess(CurrentWeatherGateway.WeatherResult result) {
                weatherRequestInFlight = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                renderWeatherResult(result);
            }

            @Override
            public void onEmpty() {
                weatherRequestInFlight = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                renderWeatherEmpty();
            }

            @Override
            public void onFailure(String message) {
                weatherRequestInFlight = false;
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                renderWeatherFailure(message);
            }
        });
    }

    private boolean shouldRefreshWeather(Location location) {
        if (lastWeatherRefreshLocation == null || lastWeatherRefreshAtMillis == 0L) {
            return true;
        }

        long elapsedMillis = System.currentTimeMillis() - lastWeatherRefreshAtMillis;
        if (elapsedMillis >= WEATHER_REFRESH_INTERVAL_MS) {
            return true;
        }

        return location.distanceTo(lastWeatherRefreshLocation) >= WEATHER_REFRESH_DISTANCE_M;
    }

    private void renderWeatherLoading() {
        rideWeatherValueTextView.setText(R.string.ride_weather_loading_value);
        rideWeatherStatusTextView.setText(R.string.ride_weather_loading_message);
        rideWindValueTextView.setText(R.string.ride_wind_loading_value);
        rideWindStatusTextView.setText(R.string.ride_wind_loading_message);
    }

    private void renderWeatherEmpty() {
        rideWeatherValueTextView.setText(R.string.ride_weather_empty_value);
        rideWeatherStatusTextView.setText(R.string.ride_weather_empty_message);
        rideWindValueTextView.setText(R.string.ride_wind_empty_value);
        rideWindStatusTextView.setText(R.string.ride_wind_empty_message);
    }

    private void renderWeatherFailure(String message) {
        rideWeatherValueTextView.setText(R.string.ride_weather_empty_value);
        rideWeatherStatusTextView.setText(message == null || message.isBlank()
                ? getString(R.string.ride_weather_empty_message)
                : message);
        rideWindValueTextView.setText(R.string.ride_wind_empty_value);
        rideWindStatusTextView.setText(R.string.ride_wind_empty_message);
    }

    private void renderWeatherResult(CurrentWeatherGateway.WeatherResult result) {
        if (result.getTemperatureC() == null) {
            rideWeatherValueTextView.setText(R.string.ride_weather_empty_value);
        } else {
            rideWeatherValueTextView.setText(getString(R.string.ride_weather_temperature_format, result.getTemperatureC()));
        }

        if (result.isStale()) {
            rideWeatherStatusTextView.setText(R.string.ride_weather_stale_message);
        } else if (result.getSky() != null && !result.getSky().isBlank()) {
            rideWeatherStatusTextView.setText(result.getSky());
        } else {
            rideWeatherStatusTextView.setText(R.string.ride_weather_message_default);
        }

        if (result.getWindSpeedKmh() == null) {
            rideWindValueTextView.setText(R.string.ride_wind_empty_value);
        } else {
            String directionText = (result.getWindDirectionText() == null || result.getWindDirectionText().isBlank())
                    ? getString(R.string.ride_wind_direction_unknown)
                    : result.getWindDirectionText();
            rideWindValueTextView.setText(getString(R.string.ride_wind_speed_format, directionText, result.getWindSpeedKmh()));
        }

        if (result.isStale()) {
            rideWindStatusTextView.setText(R.string.ride_weather_stale_message);
        } else {
            rideWindStatusTextView.setText(R.string.ride_wind_message_default);
        }
    }

    private void loadRoutePoints() {
        if (courseId <= 0) {
            routeLoadFailed = true;
            routeLoaded = true;
            renderMapOverlays();
            return;
        }

        routeLoaded = false;
        routeLoadFailed = false;
        renderMapOverlays();

        courseRoutePointsGateway.loadRoutePoints(courseId, new CourseRoutePointsGateway.Callback() {
            @Override
            public void onSuccess(CourseRoutePointsGateway.RoutePointsResult result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                currentRoutePoints = result.getPoints();
                routeLoaded = true;
                routeLoadFailed = false;
                renderRouteOnMap();
                centerCameraToRouteIfPossible();
                renderMapOverlays();
            }

            @Override
            public void onFailure(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                currentRoutePoints = Collections.emptyList();
                routeLoaded = true;
                routeLoadFailed = true;
                renderRouteOnMap();
                renderMapOverlays();
            }
        });
    }

    private void initializeMapStyle(Style style) {
        if (style.getSource(ROUTE_SOURCE_ID) == null) {
            style.addSource(new GeoJsonSource(ROUTE_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[]{})));
        }
        if (style.getLayer(ROUTE_LAYER_ID) == null) {
            LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);
            routeLayer.setProperties(
                    PropertyFactory.lineColor(ContextCompat.getColor(this, R.color.route_line)),
                    PropertyFactory.lineWidth(4f),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
            );
            style.addLayer(routeLayer);
        }
        if (style.getSource(LOCATION_SOURCE_ID) == null) {
            style.addSource(new GeoJsonSource(LOCATION_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[]{})));
        }
        if (style.getLayer(LOCATION_LAYER_ID) == null) {
            CircleLayer locationLayer = new CircleLayer(LOCATION_LAYER_ID, LOCATION_SOURCE_ID);
            locationLayer.setProperties(
                    PropertyFactory.circleColor(ContextCompat.getColor(this, R.color.info_text)),
                    PropertyFactory.circleRadius(6f),
                    PropertyFactory.circleStrokeWidth(2f),
                    PropertyFactory.circleStrokeColor(ContextCompat.getColor(this, R.color.surface))
            );
            style.addLayer(locationLayer);
        }
    }

    private void renderRouteOnMap() {
        if (rideMapLibreMap == null || rideMapLibreMap.getStyle() == null) {
            return;
        }

        GeoJsonSource source = rideMapLibreMap.getStyle().getSourceAs(ROUTE_SOURCE_ID);
        if (source == null) {
            return;
        }

        if (currentRoutePoints.isEmpty()) {
            source.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));
            return;
        }

        List<Point> coordinates = new ArrayList<>();
        for (CourseRoutePointsGateway.RoutePoint point : currentRoutePoints) {
            coordinates.add(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        }
        Feature routeFeature = Feature.fromGeometry(LineString.fromLngLats(coordinates));
        source.setGeoJson(FeatureCollection.fromFeature(routeFeature));
    }

    private void renderCurrentLocationOnMap() {
        if (rideMapLibreMap == null || rideMapLibreMap.getStyle() == null) {
            return;
        }

        GeoJsonSource source = rideMapLibreMap.getStyle().getSourceAs(LOCATION_SOURCE_ID);
        if (source == null) {
            return;
        }

        if (latestLocation == null) {
            source.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{}));
            return;
        }

        Feature locationFeature = Feature.fromGeometry(
                Point.fromLngLat(latestLocation.getLongitude(), latestLocation.getLatitude())
        );
        source.setGeoJson(FeatureCollection.fromFeature(locationFeature));
    }

    private void centerCameraToRouteIfPossible() {
        if (rideMapLibreMap == null || currentRoutePoints.isEmpty()) {
            return;
        }

        if (latestLocation != null && !hasCenteredOnRouteWithLocation) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (CourseRoutePointsGateway.RoutePoint point : currentRoutePoints) {
                boundsBuilder.include(new LatLng(point.getLatitude(), point.getLongitude()));
            }
            boundsBuilder.include(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()));
            rideMapLibreMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            boundsBuilder.build(),
                            getResources().getDimensionPixelSize(R.dimen.ride_map_camera_padding)
                    )
            );
            hasCenteredOnRoute = true;
            hasCenteredOnRouteWithLocation = true;
            updateMyLocationButtonVisibility();
            return;
        }

        if (hasCenteredOnRoute) {
            updateMyLocationButtonVisibility();
            return;
        }

        if (currentRoutePoints.size() == 1) {
            CourseRoutePointsGateway.RoutePoint point = currentRoutePoints.get(0);
            rideMapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(point.getLatitude(), point.getLongitude()))
                            .zoom(15d)
                            .build()
            ));
            hasCenteredOnRoute = true;
            updateMyLocationButtonVisibility();
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (CourseRoutePointsGateway.RoutePoint point : currentRoutePoints) {
            boundsBuilder.include(new LatLng(point.getLatitude(), point.getLongitude()));
        }
        rideMapLibreMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(),
                        getResources().getDimensionPixelSize(R.dimen.ride_map_camera_padding)
                )
        );
        hasCenteredOnRoute = true;
        updateMyLocationButtonVisibility();
    }

    private void centerCameraOnCurrentLocation() {
        if (rideMapLibreMap == null || latestLocation == null) {
            return;
        }

        rideMapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(latestLocation.getLatitude(), latestLocation.getLongitude()))
                        .zoom(16d)
                        .build()
        ));
        updateMyLocationButtonVisibility();
    }

    private void renderMapOverlays() {
        if (!hasLocationPermission()) {
            currentMapMessage = getString(R.string.ride_map_permission_blocked_message);
            rideMyLocationButton.setVisibility(View.GONE);
            syncGuidanceMessages();
            return;
        }

        if (latestLocation == null) {
            currentMapMessage = getString(R.string.ride_map_location_loading_message);
            rideMyLocationButton.setVisibility(View.GONE);
            syncGuidanceMessages();
            return;
        }

        if (!routeLoaded) {
            currentMapMessage = getString(R.string.ride_map_route_loading_message);
            updateMyLocationButtonVisibility();
            syncGuidanceMessages();
            return;
        }

        if (routeLoadFailed) {
            currentMapMessage = getString(R.string.ride_map_route_error_message);
            updateMyLocationButtonVisibility();
            syncGuidanceMessages();
            return;
        }

        if (currentRoutePoints.isEmpty()) {
            currentMapMessage = getString(R.string.ride_map_route_empty_message);
            updateMyLocationButtonVisibility();
            syncGuidanceMessages();
            return;
        }

        currentMapMessage = null;
        updateMyLocationButtonVisibility();
        syncGuidanceMessages();
    }

    private void renderPolicyPending(String stateLabel, String message) {
        ridePolicyStateTextView.setText(stateLabel);
        ridePolicyStateTextView.setTextColor(ContextCompat.getColor(this, R.color.info_text));
        ridePolicyMessageTextView.setText(message);
        ridePolicyBannerTextView.setVisibility(View.GONE);
        currentFlowMessage = message;
        syncGuidanceMessages();
    }

    private void renderPolicyError(String message) {
        ridePolicyStateTextView.setText(R.string.ride_policy_error_label);
        ridePolicyStateTextView.setTextColor(ContextCompat.getColor(this, R.color.error_text));
        ridePolicyMessageTextView.setText(message);
        ridePolicyBannerTextView.setVisibility(View.GONE);
        currentFlowMessage = message;
        syncGuidanceMessages();
    }

    private void renderPolicyResult(RidePolicyUiModel uiModel) {
        ridePolicyStateTextView.setText(uiModel.getStateLabel());
        ridePolicyStateTextView.setTextColor(ContextCompat.getColor(this, uiModel.getStateTextColorResId()));
        ridePolicyMessageTextView.setText(uiModel.getMessage());

        if (uiModel.isShowBanner()) {
            ridePolicyBannerTextView.setText(uiModel.getBannerMessage());
            ridePolicyBannerTextView.setTextColor(ContextCompat.getColor(this, uiModel.getBannerTextColorResId()));
            ridePolicyBannerTextView.setBackgroundColor(ContextCompat.getColor(this, uiModel.getBannerBackgroundColorResId()));
            ridePolicyBannerTextView.setVisibility(View.VISIBLE);
            currentFlowMessage = null;
        } else {
            ridePolicyBannerTextView.setVisibility(View.GONE);
            currentFlowMessage = uiModel.getMessage();
        }

        syncGuidanceMessages();
    }

    private void syncGuidanceMessages() {
        if (ridePolicyBannerTextView.getVisibility() == View.VISIBLE) {
            rideMapStatusTextView.setVisibility(View.GONE);
            rideFlowStatusTextView.setVisibility(View.GONE);
            return;
        }

        if (currentMapMessage != null && !currentMapMessage.isBlank()) {
            rideMapStatusTextView.setVisibility(View.VISIBLE);
            rideMapStatusTextView.setText(currentMapMessage);
            rideFlowStatusTextView.setVisibility(View.GONE);
            return;
        }

        if (currentFlowMessage != null && !currentFlowMessage.isBlank()) {
            rideFlowStatusTextView.setVisibility(View.VISIBLE);
            rideFlowStatusTextView.setText(currentFlowMessage);
            rideMapStatusTextView.setVisibility(View.GONE);
            return;
        }

        rideMapStatusTextView.setVisibility(View.GONE);
        rideFlowStatusTextView.setVisibility(View.GONE);
    }

    private void updateMyLocationButtonVisibility() {
        if (rideMapLibreMap == null || latestLocation == null || !hasLocationPermission()) {
            rideMyLocationButton.setVisibility(View.GONE);
            return;
        }

        LatLng target = rideMapLibreMap.getCameraPosition().target;
        float[] distance = new float[1];
        Location.distanceBetween(
                latestLocation.getLatitude(),
                latestLocation.getLongitude(),
                target.getLatitude(),
                target.getLongitude(),
                distance
        );
        rideMyLocationButton.setVisibility(distance[0] > 30f ? View.VISIBLE : View.GONE);
    }

    private void openAppSettings() {
        openedSettings = true;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void startForegroundLocationUpdates() {
        if (locationUpdatesRegistered || !hasLocationPermission()) {
            return;
        }

        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return;
            }

            registerProviderUpdates(locationManager, LocationManager.GPS_PROVIDER);
            registerProviderUpdates(locationManager, LocationManager.NETWORK_PROVIDER);
            locationUpdatesRegistered = true;
        } catch (SecurityException ignored) {
            locationUpdatesRegistered = false;
        }
    }

    @SuppressLint("MissingPermission")
    private void registerProviderUpdates(LocationManager locationManager, String provider) {
        if (!locationManager.isProviderEnabled(provider)) {
            return;
        }

        locationManager.requestLocationUpdates(
                provider,
                LOCATION_UPDATE_INTERVAL_MS,
                LOCATION_UPDATE_DISTANCE_M,
                foregroundLocationListener,
                getMainLooper()
        );
    }

    private void stopForegroundLocationUpdates() {
        if (!locationUpdatesRegistered) {
            return;
        }

        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.removeUpdates(foregroundLocationListener);
            }
        } catch (SecurityException ignored) {
        } finally {
            locationUpdatesRegistered = false;
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
