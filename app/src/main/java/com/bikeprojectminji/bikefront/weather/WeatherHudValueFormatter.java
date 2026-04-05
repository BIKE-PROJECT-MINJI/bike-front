package com.bikeprojectminji.bikefront.weather;

public final class WeatherHudValueFormatter {

    private WeatherHudValueFormatter() {
    }

    public static String formatTemperature(Integer temperatureC) {
        return temperatureC != null ? temperatureC + "°C" : "--";
    }

    public static String formatWind(String directionText, Integer speedKmh) {
        if (speedKmh == null) {
            return "--";
        }
        String safeDirection = (directionText == null || directionText.isBlank()) ? "방향 미확인" : directionText;
        return "🧭 " + safeDirection + " " + speedKmh + "km/h";
    }

    public static boolean shouldShowStale(boolean stale) {
        return stale;
    }
}
