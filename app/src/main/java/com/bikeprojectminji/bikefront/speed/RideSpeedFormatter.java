package com.bikeprojectminji.bikefront.speed;

import android.location.Location;

public class RideSpeedFormatter {

    private static final float MIN_DISTANCE_METERS = 1f;
    private static final long MIN_ELAPSED_MILLIS = 1_000L;
    private static final String NO_SIGNAL_MESSAGE = "현재 위치 신호를 기다리는 중입니다.";
    private static final String LOW_ACCURACY_MESSAGE = "현재 위치 정보가 불안정합니다.";
    private static final String STATIONARY_MESSAGE = "정지 상태입니다.";

    public RideSpeedUiState format(Location currentLocation, Location previousLocation) {
        return format(RideSpeedSample.fromLocation(currentLocation), RideSpeedSample.fromLocation(previousLocation));
    }

    public RideSpeedUiState format(RideSpeedSample currentLocation, RideSpeedSample previousLocation) {
        if (currentLocation == null) {
            return new RideSpeedUiState("--", NO_SIGNAL_MESSAGE);
        }

        int speedKmh = resolveSpeedKmh(currentLocation, previousLocation);
        String message = resolveSpeedMessage(currentLocation, speedKmh);

        return new RideSpeedUiState(speedKmh + "km/h", message);
    }

    private String resolveSpeedMessage(RideSpeedSample currentLocation, int speedKmh) {
        String accuracyMessage = resolveAccuracyMessage(currentLocation.hasAccuracy(), currentLocation.getAccuracyMeters());
        if (!accuracyMessage.isBlank()) {
            return accuracyMessage;
        }
        if (speedKmh == 0) {
            return STATIONARY_MESSAGE;
        }
        return "";
    }

    private int resolveSpeedKmh(RideSpeedSample currentLocation, RideSpeedSample previousLocation) {
        if (currentLocation.hasSpeed()) {
            return normalizeSpeedKmh(currentLocation.getSpeedMetersPerSec() * 3.6f);
        }

        if (previousLocation == null) {
            return 0;
        }

        long elapsedMillis = Math.abs(currentLocation.getTimeMillis() - previousLocation.getTimeMillis());
        float distanceMeters = distanceMeters(currentLocation, previousLocation);

        if (elapsedMillis < MIN_ELAPSED_MILLIS || distanceMeters < MIN_DISTANCE_METERS) {
            return 0;
        }

        float elapsedSeconds = elapsedMillis / 1000f;
        float fallbackSpeedKmh = (distanceMeters / elapsedSeconds) * 3.6f;
        return normalizeSpeedKmh(fallbackSpeedKmh);
    }

    private float distanceMeters(RideSpeedSample currentLocation, RideSpeedSample previousLocation) {
        double earthRadiusMeters = 6_371_000d;
        double latDistance = Math.toRadians(currentLocation.getLatitude() - previousLocation.getLatitude());
        double lonDistance = Math.toRadians(currentLocation.getLongitude() - previousLocation.getLongitude());
        double startLat = Math.toRadians(previousLocation.getLatitude());
        double endLat = Math.toRadians(currentLocation.getLatitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(startLat) * Math.cos(endLat)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) (earthRadiusMeters * c);
    }

    static String resolveAccuracyMessage(boolean hasAccuracy, float accuracyMeters) {
        if (hasAccuracy && accuracyMeters > 50f) {
            return LOW_ACCURACY_MESSAGE;
        }
        return "";
    }

    static int normalizeSpeedKmh(float speedKmh) {
        if (speedKmh < 1f) {
            return 0;
        }

        return Math.round(speedKmh);
    }
}
