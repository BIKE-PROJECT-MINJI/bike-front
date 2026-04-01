package com.bikeprojectminji.bikefront.speed;

import android.location.Location;

public class RideSpeedFormatter {

    private static final float MIN_DISTANCE_METERS = 1f;
    private static final long MIN_ELAPSED_MILLIS = 1_000L;

    public RideSpeedUiState format(Location currentLocation, Location previousLocation) {
        if (currentLocation == null) {
            return new RideSpeedUiState("--", "현재 위치를 확인하는 중입니다.");
        }

        int speedKmh = resolveSpeedKmh(currentLocation, previousLocation);
        String message = null;

        if (currentLocation.hasAccuracy() && currentLocation.getAccuracy() > 50f) {
            message = "현재 위치 정보가 불안정합니다.";
        }

        return new RideSpeedUiState(speedKmh + "km/h", message);
    }

    private int resolveSpeedKmh(Location currentLocation, Location previousLocation) {
        if (currentLocation.hasSpeed()) {
            return normalizeSpeedKmh(currentLocation.getSpeed() * 3.6f);
        }

        if (previousLocation == null) {
            return 0;
        }

        long elapsedMillis = Math.abs(currentLocation.getTime() - previousLocation.getTime());
        float distanceMeters = currentLocation.distanceTo(previousLocation);

        if (elapsedMillis < MIN_ELAPSED_MILLIS || distanceMeters < MIN_DISTANCE_METERS) {
            return 0;
        }

        float elapsedSeconds = elapsedMillis / 1000f;
        float fallbackSpeedKmh = (distanceMeters / elapsedSeconds) * 3.6f;
        return normalizeSpeedKmh(fallbackSpeedKmh);
    }

    private int normalizeSpeedKmh(float speedKmh) {
        if (speedKmh < 1f) {
            return 0;
        }

        return Math.round(speedKmh);
    }
}
