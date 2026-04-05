package com.bikeprojectminji.bikefront.weather;

import java.net.HttpURLConnection;

final class WeatherHttpErrorPolicy {

    private WeatherHttpErrorPolicy() {
    }

    static boolean shouldTreatAsEmpty(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_NOT_FOUND;
    }

    static String resolveFailureMessage(String backendMessage, String fallbackErrorMessage) {
        if (backendMessage == null || backendMessage.isBlank()) {
            return fallbackErrorMessage;
        }
        return backendMessage;
    }
}
