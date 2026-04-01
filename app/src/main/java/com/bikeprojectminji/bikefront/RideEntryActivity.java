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
    private TextView ridePolicyBannerTextView;
    private TextView ridePolicyStateTextView;
    private TextView ridePolicyMessageTextView;
    private Button ridePermissionRetryButton;
    private Button ridePermissionSettingsButton;

    private PermissionUiState permissionUiState = PermissionUiState.REQUESTING;
    private boolean openedSettings;
    private long courseId;
    private String ridePhase = PHASE_PRE_START;

    private final RidePolicyEvaluationGateway ridePolicyEvaluationGateway = new HttpRidePolicyEvaluationGateway();
    private final RidePolicyUiMapper ridePolicyUiMapper = new RidePolicyUiMapper();

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
        setContentView(R.layout.activity_ride_entry);

        TextView titleTextView = findViewById(R.id.rideCourseTitleTextView);
        TextView distanceTextView = findViewById(R.id.rideCourseDistanceTextView);
        TextView durationTextView = findViewById(R.id.rideCourseDurationTextView);
        ridePermissionMessageTextView = findViewById(R.id.ridePermissionMessageTextView);
        ridePermissionBlockedFeaturesTextView = findViewById(R.id.ridePermissionBlockedFeaturesTextView);
        rideFlowStatusTextView = findViewById(R.id.rideFlowStatusTextView);
        ridePolicyBannerTextView = findViewById(R.id.ridePolicyBannerTextView);
        ridePolicyStateTextView = findViewById(R.id.ridePolicyStateTextView);
        ridePolicyMessageTextView = findViewById(R.id.ridePolicyMessageTextView);
        ridePermissionRetryButton = findViewById(R.id.ridePermissionRetryButton);
        ridePermissionSettingsButton = findViewById(R.id.ridePermissionSettingsButton);

        Intent intent = getIntent();
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L);
        titleTextView.setText(intent.getStringExtra(EXTRA_TITLE));
        distanceTextView.setText(intent.getStringExtra(EXTRA_DISTANCE_TEXT));
        durationTextView.setText(intent.getStringExtra(EXTRA_DURATION_TEXT));

        ridePermissionRetryButton.setOnClickListener(v -> requestLocationPermission());
        ridePermissionSettingsButton.setOnClickListener(v -> openAppSettings());

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
        outState.putString(KEY_PERMISSION_STATE, permissionUiState.name());
        outState.putString(KEY_RIDE_PHASE, ridePhase);
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
                rideFlowStatusTextView.setText(R.string.ride_permission_resuming_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
                break;
            case NEED_PERMISSION:
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                rideFlowStatusTextView.setText(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case DENIED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                rideFlowStatusTextView.setText(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case SETTINGS_REQUIRED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_settings_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                rideFlowStatusTextView.setText(R.string.ride_placeholder_message);
                renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_permission_blocked_message));
                break;
            case GRANTED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_resuming_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_resumed);
                ridePermissionRetryButton.setVisibility(View.GONE);
                ridePermissionSettingsButton.setVisibility(View.GONE);
                rideFlowStatusTextView.setText(R.string.ride_location_status_resumed);
                break;
        }
    }

    private void refreshRidePolicy() {
        if (courseId <= 0) {
            renderPolicyError(getString(R.string.ride_policy_missing_course_message));
            return;
        }

        Location location = resolveBestLastKnownLocation();
        if (location == null) {
            renderPolicyPending(getString(R.string.ride_policy_pending_label), getString(R.string.ride_policy_loading_message));
            return;
        }

        evaluateRidePolicy(location, ridePhase);
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
            Location latestLocation = null;
            for (String provider : providers) {
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate == null) {
                    continue;
                }
                if (latestLocation == null || candidate.getTime() > latestLocation.getTime()) {
                    latestLocation = candidate;
                }
            }

            return latestLocation;
        } catch (SecurityException exception) {
            renderPolicyError(getString(R.string.ride_policy_location_error_message));
            return null;
        }
    }

    private void renderPolicyPending(String stateLabel, String message) {
        ridePolicyStateTextView.setText(stateLabel);
        ridePolicyStateTextView.setTextColor(ContextCompat.getColor(this, R.color.info_text));
        ridePolicyMessageTextView.setText(message);
        ridePolicyBannerTextView.setVisibility(View.GONE);
        rideFlowStatusTextView.setVisibility(View.VISIBLE);
        rideFlowStatusTextView.setText(message);
    }

    private void renderPolicyError(String message) {
        ridePolicyStateTextView.setText(R.string.ride_policy_error_label);
        ridePolicyStateTextView.setTextColor(ContextCompat.getColor(this, R.color.error_text));
        ridePolicyMessageTextView.setText(message);
        ridePolicyBannerTextView.setVisibility(View.GONE);
        rideFlowStatusTextView.setVisibility(View.VISIBLE);
        rideFlowStatusTextView.setText(message);
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
            rideFlowStatusTextView.setVisibility(View.GONE);
        } else {
            ridePolicyBannerTextView.setVisibility(View.GONE);
            rideFlowStatusTextView.setVisibility(View.VISIBLE);
            rideFlowStatusTextView.setText(uiModel.getMessage());
        }
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
