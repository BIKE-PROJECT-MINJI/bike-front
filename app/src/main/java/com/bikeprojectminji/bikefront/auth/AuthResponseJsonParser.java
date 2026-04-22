package com.bikeprojectminji.bikefront.auth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuthResponseJsonParser {

    private AuthResponseJsonParser() {
    }

    public static AuthLoginGateway.LoginResult parseLoginResult(String email, String responseText) {
        return new AuthLoginGateway.LoginResult(
                email,
                extractString(responseText, "displayName", ""),
                extractString(responseText, "accessToken", ""),
                extractString(responseText, "refreshToken", ""),
                extractLong(responseText, "accessExpiresInSec", 0L),
                extractLong(responseText, "refreshExpiresInSec", 0L),
                extractLong(responseText, "userId", -1L)
        );
    }

    public static AuthLoginGateway.ProfileResult parseProfileResult(String responseText) {
        return new AuthLoginGateway.ProfileResult(
                extractLong(responseText, "userId", -1L),
                extractString(responseText, "email", ""),
                extractString(responseText, "displayName", ""),
                extractString(responseText, "profileImageUrl", "")
        );
    }

    public static AuthLoginGateway.ActivitySummaryResult parseActivitySummaryResult(String responseText) {
        return new AuthLoginGateway.ActivitySummaryResult(
                new AuthLoginGateway.WeeklyActivitySummaryResult(
                        extractDouble(responseText, "distanceKm", 0.0),
                        extractLong(responseText, "rideCount", 0L),
                        extractLong(responseText, "durationMinutes", 0L),
                        extractLong(responseText, "savedCourseCount", 0L)
                ),
                new AuthLoginGateway.OverallActivitySummaryResult(
                        extractDouble(responseText, "totalDistanceKm", 0.0),
                        extractLong(responseText, "totalRides", 0L),
                        extractDouble(responseText, "avgSpeedKmh", 0.0),
                        extractLong(responseText, "totalElevationM", 0L)
                )
        );
    }

    private static String extractString(String responseText, String fieldName, String fallbackValue) {
        if (responseText == null || responseText.isBlank()) {
            return fallbackValue;
        }
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"")
                .matcher(responseText);
        return matcher.find() ? matcher.group(1) : fallbackValue;
    }

    private static long extractLong(String responseText, String fieldName, long fallbackValue) {
        if (responseText == null || responseText.isBlank()) {
            return fallbackValue;
        }
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*(-?\\d+)")
                .matcher(responseText);
        if (!matcher.find()) {
            return fallbackValue;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            return fallbackValue;
        }
    }

    private static double extractDouble(String responseText, String fieldName, double fallbackValue) {
        if (responseText == null || responseText.isBlank()) {
            return fallbackValue;
        }
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(fieldName) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
                .matcher(responseText);
        if (!matcher.find()) {
            return fallbackValue;
        }
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException exception) {
            return fallbackValue;
        }
    }
}
