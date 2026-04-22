package com.bikeprojectminji.bikefront.speed;

import android.location.Location;

public class RideSpeedSample {

    private final double latitude;
    private final double longitude;
    private final long timeMillis;
    private final boolean hasAccuracy;
    private final float accuracyMeters;
    private final boolean hasSpeed;
    private final float speedMetersPerSec;

    public RideSpeedSample(
            double latitude,
            double longitude,
            long timeMillis,
            boolean hasAccuracy,
            float accuracyMeters,
            boolean hasSpeed,
            float speedMetersPerSec
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeMillis = timeMillis;
        this.hasAccuracy = hasAccuracy;
        this.accuracyMeters = accuracyMeters;
        this.hasSpeed = hasSpeed;
        this.speedMetersPerSec = speedMetersPerSec;
    }

    public static RideSpeedSample fromLocation(Location location) {
        if (location == null) {
            return null;
        }

        return new RideSpeedSample(
                location.getLatitude(),
                location.getLongitude(),
                location.getTime(),
                location.hasAccuracy(),
                location.getAccuracy(),
                location.hasSpeed(),
                location.getSpeed()
        );
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public boolean hasAccuracy() {
        return hasAccuracy;
    }

    public float getAccuracyMeters() {
        return accuracyMeters;
    }

    public boolean hasSpeed() {
        return hasSpeed;
    }

    public float getSpeedMetersPerSec() {
        return speedMetersPerSec;
    }
}
