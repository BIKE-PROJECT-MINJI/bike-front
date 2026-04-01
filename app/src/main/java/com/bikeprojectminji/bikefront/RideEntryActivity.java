package com.bikeprojectminji.bikefront;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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

import com.bikeprojectminji.bikefront.ridepolicy.HttpRidePolicyEvaluationGateway;
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway;
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiMapper;
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiModel;
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway;
import com.bikeprojectminji.bikefront.ridemap.HttpCourseRoutePointsGateway;
import com.bikeprojectminji.bikefront.speed.RideSpeedFormatter;
import com.bikeprojectminji.bikefront.speed.RideSpeedUiState;

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
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;

import com.bikeprojectminji.bikefront.config.AppConfig;

import java.util.Collections;
import java.util.List;

public class RideEntryActivity extends AppCompatActivity {

    private static final String EXTRA_COURSE_ID = "extra_course_id";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_DISTANCE_TEXT = "extra_distance_text";
    private static final String EXTRA_DURATION_TEXT = "extra_duration_text";
    private static final String KEY_PERMISSION_STATE = "key_permission_state";
    private static final String KEY_RIDE_PHASE = "key_ride_phase";
    private static final String PHASE_PRE_START = "PRE_START";
    private static final String PHASE_ACTIVE = "ACTIVE";
    private static final String ROUTE_SOURCE_ID = "ride_route_source";
    private static final String ROUTE_LAYER_ID = "ride_route_layer";
    private static final String LOCATION_SOURCE_ID = "ride_location_source";
    private static final String LOCATION_LAYER_ID = "ride_location_layer";

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
    private TextView rideSpeedValueTextView;
    private TextView rideSpeedMessageTextView;
    private TextView ridePolicyStateTextView;
    private TextView ridePolicyMessageTextView;
    private Button rideMyLocationButton;
    private Button ridePermissionRetryButton;
    private Button ridePermissionSettingsButton;
    private MapView rideMapView;

    private PermissionUiState permissionUiState = PermissionUiState.REQUESTING;
    private boolean openedSettings;
    private long courseId;
    private String ridePhase = PHASE_PRE_START;
    private boolean routeLoaded;
    private boolean routeLoadFailed;
    private boolean hasCenteredOnRoute;
    private boolean hasCenteredOnRouteWithLocation;
    private Location latestLocation;
    private Location previousLocation;
    private List<CourseRoutePointsGateway.RoutePoint> currentRoutePoints = Collections.emptyList();
    private MapLibreMap rideMapLibreMap;
    private String currentMapMessage;
    private String currentFlowMessage;

    private final RidePolicyEvaluationGateway ridePolicyEvaluationGateway = new HttpRidePolicyEvaluationGateway();
    private final RidePolicyUiMapper ridePolicyUiMapper = new RidePolicyUiMapper();
    private final CourseRoutePointsGateway courseRoutePointsGateway = new HttpCourseRoutePointsGateway();
    private final RideSpeedFormatter rideSpeedFormatter = new RideSpeedFormatter();

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::handleLocationPermissionResult
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
        ridePolicyStateTextView = findViewById(R.id.ridePolicyStateTextView);
        ridePolicyMessageTextView = findViewById(R.id.ridePolicyMessageTextView);
        rideMyLocationButton = findViewById(R.id.rideMyLocationButton);
        ridePermissionRetryButton = findViewById(R.id.ridePermissionRetryButton);
        ridePermissionSettingsButton = findViewById(R.id.ridePermissionSettingsButton);

        Intent intent = getIntent();
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L);
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

        rideMyLocationButton.setOnClickListener(v -> centerCameraOnCurrentLocation());
        ridePermissionRetryButton.setOnClickListener(v -> requestLocationPermission());
        ridePermissionSettingsButton.setOnClickListener(v -> openAppSettings());

        loadRoutePoints();

        if (savedInstanceState != null) {
            String savedState = savedInstanceState.getString(KEY_PERMISSION_STATE, PermissionUiState.REQUESTING.name());
            permissionUiState = PermissionUiState.valueOf(savedState);
            ridePhase = savedInstanceState.getString(KEY_RIDE_PHASE, PHASE_PRE_START);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        rideMapView.onResume();

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        rideMapView.onStart();
    }

    @Override
    protected void onPause() {
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
            resumeRideFlow();
            return;
        }

        renderPermissionState(PermissionUiState.REQUESTING);
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void handleLocationPermissionResult(boolean granted) {
        if (granted) {
            resumeRideFlow();
            return;
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            renderPermissionState(PermissionUiState.DENIED);
            return;
        }

        renderPermissionState(PermissionUiState.SETTINGS_REQUIRED);
    }

    private void resumeRideFlow() {
        renderPermissionState(PermissionUiState.GRANTED);
        refreshRidePolicy();
    }

    private void renderPermissionState(PermissionUiState newState) {
        permissionUiState = newState;

        switch (newState) {
            case REQUESTING:
                ridePermissionMessageTextView.setText(R.string.ride_permission_resuming_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(false);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.GONE);
                currentFlowMessage = getString(R.string.ride_permission_resuming_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
                break;
            case NEED_PERMISSION:
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                currentFlowMessage = getString(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case DENIED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                currentFlowMessage = getString(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case SETTINGS_REQUIRED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_settings_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                currentFlowMessage = getString(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case GRANTED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_resuming_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_resumed);
                ridePermissionRetryButton.setVisibility(View.GONE);
                ridePermissionSettingsButton.setVisibility(View.GONE);
                currentFlowMessage = getString(R.string.ride_location_status_resumed);
                break;
        }

        syncGuidanceMessages();
    }

    private void refreshRidePolicy() {
        if (courseId <= 0) {
            renderPolicyError(getString(R.string.ride_policy_missing_course_message));
            return;
        }

        latestLocation = resolveBestLastKnownLocation();
        renderSpeedCard();
        renderCurrentLocationOnMap();
        renderMapOverlays();

        if (latestLocation == null) {
            renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
            return;
        }

        evaluateRidePolicy(latestLocation, ridePhase);
    }

    private void evaluateRidePolicy(Location location, String phase) {
        renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
        ridePolicyEvaluationGateway.evaluate(courseId, phase, location, new RidePolicyEvaluationGateway.Callback() {
            @Override
            public void onSuccess(RidePolicyEvaluationGateway.EvaluationResult result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (PHASE_PRE_START.equals(phase) && "ELIGIBLE".equals(result.getStartGate().getStatus())) {
                    ridePhase = PHASE_ACTIVE;
                    evaluateRidePolicy(location, PHASE_ACTIVE);
                    return;
                }

                ridePhase = result.getPhase();
                renderPolicyResult(ridePolicyUiMapper.map(result));
            }

            @Override
            public void onFailure(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                renderPolicyError(message);
            }
        });
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

            if (newestLocation != null && latestLocation != null && newestLocation != latestLocation) {
                previousLocation = latestLocation;
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
                    PropertyFactory.lineCap("round"),
                    PropertyFactory.lineJoin("round")
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

        List<Point> coordinates = currentRoutePoints.stream()
                .map(point -> Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                .toList();
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

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
