package com.bikeprojectminji.bikefront.auth;

public final class AuthSessionFactory {

    private AuthSessionFactory() {
    }

    public static AuthSession create(AuthLoginGateway.LoginResult loginResult, String profileImageUrl, long nowMillis) {
        return create(loginResult, loginResult.getDisplayName(), profileImageUrl, nowMillis);
    }

    public static AuthSession create(
            AuthLoginGateway.LoginResult loginResult,
            String displayName,
            String profileImageUrl,
            long nowMillis
    ) {
        return new AuthSession(
                displayName,
                profileImageUrl,
                loginResult.getAccessToken(),
                loginResult.getRefreshToken(),
                nowMillis + (Math.max(0L, loginResult.getAccessExpiresInSec()) * 1000L),
                nowMillis + (Math.max(0L, loginResult.getRefreshExpiresInSec()) * 1000L)
        );
    }
}
