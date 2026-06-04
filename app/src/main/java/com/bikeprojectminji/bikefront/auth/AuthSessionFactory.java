package com.bikeprojectminji.bikefront.auth;

public final class AuthSessionFactory {

    private AuthSessionFactory() {
    }

    public static AuthSession create(AuthLoginGateway.LoginResult loginResult, String profileImageUrl, long nowMillis) {
        return create(loginResult, loginResult.getDisplayName(), profileImageUrl, "email", nowMillis);
    }

    public static AuthSession create(
            AuthLoginGateway.LoginResult loginResult,
            String displayName,
            String profileImageUrl,
            long nowMillis
    ) {
        return create(loginResult, displayName, profileImageUrl, "email", nowMillis);
    }

    public static AuthSession create(
            AuthLoginGateway.LoginResult loginResult,
            String displayName,
            String profileImageUrl,
            String loginProvider,
            long nowMillis
    ) {
        return new AuthSession(
                displayName,
                loginResult.getEmail(),
                loginResult.getUserId(),
                loginProvider,
                profileImageUrl,
                loginResult.getAccessToken(),
                loginResult.getRefreshToken(),
                nowMillis + (Math.max(0L, loginResult.getAccessExpiresInSec()) * 1000L),
                nowMillis + (Math.max(0L, loginResult.getRefreshExpiresInSec()) * 1000L)
        );
    }

    public static AuthSession create(
            AuthLoginGateway.LoginResult loginResult,
            AuthLoginGateway.ProfileResult profileResult,
            String loginProvider,
            String fallbackProfileImageUrl,
            long nowMillis
    ) {
        String displayName = profileResult.getDisplayName().isBlank() ? loginResult.getDisplayName() : profileResult.getDisplayName();
        String profileImageUrl = profileResult.getProfileImageUrl().isBlank() ? fallbackProfileImageUrl : profileResult.getProfileImageUrl();
        String email = profileResult.getEmail().isBlank() ? loginResult.getEmail() : profileResult.getEmail();
        long userId = profileResult.getUserId() > 0L ? profileResult.getUserId() : loginResult.getUserId();
        return new AuthSession(
                displayName,
                email,
                userId,
                loginProvider,
                profileImageUrl,
                loginResult.getAccessToken(),
                loginResult.getRefreshToken(),
                nowMillis + (Math.max(0L, loginResult.getAccessExpiresInSec()) * 1000L),
                nowMillis + (Math.max(0L, loginResult.getRefreshExpiresInSec()) * 1000L)
        );
    }
}
