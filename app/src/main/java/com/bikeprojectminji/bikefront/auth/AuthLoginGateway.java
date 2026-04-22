package com.bikeprojectminji.bikefront.auth;

public interface AuthLoginGateway {

    void register(String email, String password, String displayName, Callback callback);

    void login(String email, String password, Callback callback);

    void refresh(String refreshToken, Callback callback);

    void getMyProfile(String accessToken, ProfileCallback callback);

    void getMyActivitySummary(String accessToken, ActivitySummaryCallback callback);

    interface Callback {
        void onSuccess(LoginResult result);

        void onFailure(String message);
    }

    interface ProfileCallback {
        void onSuccess(ProfileResult result);

        void onFailure(String message);
    }

    interface ActivitySummaryCallback {
        void onSuccess(ActivitySummaryResult result);

        void onFailure(String message);
    }

    class LoginResult {
        private final String email;
        private final String displayName;
        private final String accessToken;
        private final String refreshToken;
        private final long accessExpiresInSec;
        private final long refreshExpiresInSec;
        private final long userId;

        public LoginResult(
                String email,
                String displayName,
                String accessToken,
                String refreshToken,
                long accessExpiresInSec,
                long refreshExpiresInSec,
                long userId
        ) {
            this.email = email;
            this.displayName = displayName;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessExpiresInSec = accessExpiresInSec;
            this.refreshExpiresInSec = refreshExpiresInSec;
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public long getAccessExpiresInSec() {
            return accessExpiresInSec;
        }

        public long getRefreshExpiresInSec() {
            return refreshExpiresInSec;
        }

        public long getUserId() {
            return userId;
        }
    }

    class ProfileResult {
        private final long userId;
        private final String email;
        private final String displayName;
        private final String profileImageUrl;

        public ProfileResult(long userId, String email, String displayName, String profileImageUrl) {
            this.userId = userId;
            this.email = email;
            this.displayName = displayName;
            this.profileImageUrl = profileImageUrl;
        }

        public long getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }
    }

    class ActivitySummaryResult {
        private final WeeklyActivitySummaryResult weeklySummary;
        private final OverallActivitySummaryResult overallSummary;

        public ActivitySummaryResult(
                WeeklyActivitySummaryResult weeklySummary,
                OverallActivitySummaryResult overallSummary
        ) {
            this.weeklySummary = weeklySummary;
            this.overallSummary = overallSummary;
        }

        public WeeklyActivitySummaryResult getWeeklySummary() {
            return weeklySummary;
        }

        public OverallActivitySummaryResult getOverallSummary() {
            return overallSummary;
        }
    }

    class WeeklyActivitySummaryResult {
        private final double distanceKm;
        private final long rideCount;
        private final long durationMinutes;
        private final long savedCourseCount;

        public WeeklyActivitySummaryResult(double distanceKm, long rideCount, long durationMinutes, long savedCourseCount) {
            this.distanceKm = distanceKm;
            this.rideCount = rideCount;
            this.durationMinutes = durationMinutes;
            this.savedCourseCount = savedCourseCount;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public long getRideCount() {
            return rideCount;
        }

        public long getDurationMinutes() {
            return durationMinutes;
        }

        public long getSavedCourseCount() {
            return savedCourseCount;
        }
    }

    class OverallActivitySummaryResult {
        private final double totalDistanceKm;
        private final long totalRides;
        private final double avgSpeedKmh;
        private final long totalElevationM;

        public OverallActivitySummaryResult(double totalDistanceKm, long totalRides, double avgSpeedKmh, long totalElevationM) {
            this.totalDistanceKm = totalDistanceKm;
            this.totalRides = totalRides;
            this.avgSpeedKmh = avgSpeedKmh;
            this.totalElevationM = totalElevationM;
        }

        public double getTotalDistanceKm() {
            return totalDistanceKm;
        }

        public long getTotalRides() {
            return totalRides;
        }

        public double getAvgSpeedKmh() {
            return avgSpeedKmh;
        }

        public long getTotalElevationM() {
            return totalElevationM;
        }
    }
}
