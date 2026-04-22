package com.bikeprojectminji.bikefront.auth;

public class AuthSession {

    private final String displayName;
    private final String profileImageUrl;
    private final String accessToken;
    private final String refreshToken;
    private final long accessTokenExpiresAtEpochMillis;
    private final long refreshTokenExpiresAtEpochMillis;

    public AuthSession(
            String displayName,
            String profileImageUrl,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresAtEpochMillis,
            long refreshTokenExpiresAtEpochMillis
    ) {
        this.displayName = displayName == null ? "" : displayName;
        this.profileImageUrl = profileImageUrl == null ? "" : profileImageUrl;
        this.accessToken = accessToken == null ? "" : accessToken;
        this.refreshToken = refreshToken == null ? "" : refreshToken;
        this.accessTokenExpiresAtEpochMillis = accessTokenExpiresAtEpochMillis;
        this.refreshTokenExpiresAtEpochMillis = refreshTokenExpiresAtEpochMillis;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getAccessTokenExpiresAtEpochMillis() {
        return accessTokenExpiresAtEpochMillis;
    }

    public long getRefreshTokenExpiresAtEpochMillis() {
        return refreshTokenExpiresAtEpochMillis;
    }
}
