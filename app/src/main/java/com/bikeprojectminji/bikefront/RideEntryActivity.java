package com.bikeprojectminji.bikefront;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

public class RideEntryActivity extends AppCompatActivity {

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_DISTANCE_TEXT = "extra_distance_text";
    private static final String EXTRA_DURATION_TEXT = "extra_duration_text";
    private static final String KEY_PERMISSION_STATE = "key_permission_state";

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
    private Button ridePermissionRetryButton;
    private Button ridePermissionSettingsButton;

    private PermissionUiState permissionUiState = PermissionUiState.REQUESTING;
    private boolean openedSettings;

    private final ActivityResultLauncher<String> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::handleLocationPermissionResult
    );

    public static Intent newIntent(Context context, String title, String distanceText, String durationText) {
        Intent intent = new Intent(context, RideEntryActivity.class);
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
        ridePermissionRetryButton = findViewById(R.id.ridePermissionRetryButton);
        ridePermissionSettingsButton = findViewById(R.id.ridePermissionSettingsButton);

        Intent intent = getIntent();
        titleTextView.setText(intent.getStringExtra(EXTRA_TITLE));
        distanceTextView.setText(intent.getStringExtra(EXTRA_DISTANCE_TEXT));
        durationTextView.setText(intent.getStringExtra(EXTRA_DURATION_TEXT));

        ridePermissionRetryButton.setOnClickListener(v -> requestLocationPermission());
        ridePermissionSettingsButton.setOnClickListener(v -> openAppSettings());

        if (savedInstanceState != null) {
            String savedState = savedInstanceState.getString(KEY_PERMISSION_STATE, PermissionUiState.REQUESTING.name());
            permissionUiState = PermissionUiState.valueOf(savedState);
            if (hasLocationPermission()) {
                resumeRideFlow();
            } else if (permissionUiState == PermissionUiState.REQUESTING) {
                renderPermissionState(PermissionUiState.NEED_PERMISSION);
            } else {
                renderPermissionState(permissionUiState);
            }
        } else {
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
                break;
            case NEED_PERMISSION:
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                rideFlowStatusTextView.setText(R.string.ride_placeholder_message);
                break;
            case DENIED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                rideFlowStatusTextView.setText(R.string.ride_placeholder_message);
                break;
            case SETTINGS_REQUIRED:
                ridePermissionMessageTextView.setText(R.string.ride_permission_settings_message);
                ridePermissionBlockedFeaturesTextView.setText(R.string.ride_location_status_blocked);
                ridePermissionRetryButton.setEnabled(true);
                ridePermissionRetryButton.setVisibility(View.VISIBLE);
                ridePermissionSettingsButton.setVisibility(View.VISIBLE);
                rideFlowStatusTextView.setText(R.string.ride_placeholder_message);
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
