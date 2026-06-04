package com.bikeprojectminji.bikefront.auth;

public class AuthSession {

    private final String displayName;
    private final String email;
    private final long userId;
    private final String loginProvider;
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
        this(displayName, "", -1L, "", profileImageUrl, accessToken, refreshToken, accessTokenExpiresAtEpochMillis, refreshTokenExpiresAtEpochMillis);
    }

    public AuthSession(
            String displayName,
            String email,
            long userId,
            String loginProvider,
            String profileImageUrl,
            String accessToken,
            String refreshToken,
            long accessTokenExpiresAtEpochMillis,
            long refreshTokenExpiresAtEpochMillis
    ) {
        this.displayName = displayName == null ? "" : displayName;
        this.email = email == null ? "" : email;
        this.userId = userId;
        this.loginProvider = loginProvider == null ? "" : loginProvider;
        this.profileImageUrl = profileImageUrl == null ? "" : profileImageUrl;
        this.accessToken = accessToken == null ? "" : accessToken;
        this.refreshToken = refreshToken == null ? "" : refreshToken;
        this.accessTokenExpiresAtEpochMillis = accessTokenExpiresAtEpochMillis;
        this.refreshTokenExpiresAtEpochMillis = refreshTokenExpiresAtEpochMillis;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public long getUserId() {
        return userId;
    }

    public String getLoginProvider() {
        return loginProvider;
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
