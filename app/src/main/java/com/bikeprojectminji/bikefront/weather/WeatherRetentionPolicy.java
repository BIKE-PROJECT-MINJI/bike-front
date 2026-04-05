package com.bikeprojectminji.bikefront.weather;

public final class WeatherRetentionPolicy {

    private WeatherRetentionPolicy() {
    }

    public static boolean canRetainLastSuccess(boolean hasLastSuccess, long elapsedMillis, long maxRetentionMillis) {
        return hasLastSuccess && elapsedMillis <= maxRetentionMillis;
    }
}
