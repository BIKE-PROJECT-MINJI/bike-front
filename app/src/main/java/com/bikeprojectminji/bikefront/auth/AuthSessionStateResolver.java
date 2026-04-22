package com.bikeprojectminji.bikefront.auth;

public final class AuthSessionStateResolver {

    private AuthSessionStateResolver() {
    }

    public static AuthSessionState resolve(AuthSession session, long nowMillis) {
        if (session == null) {
            return new AuthSessionState(false, false, false, false);
        }

        boolean hasRefreshToken = !session.getRefreshToken().isBlank();
        boolean refreshExpired = hasRefreshToken && session.getRefreshTokenExpiresAtEpochMillis() <= nowMillis;
        boolean signedIn = hasRefreshToken && !refreshExpired;
        boolean hasUsableAccessToken = signedIn
                && !session.getAccessToken().isBlank()
                && session.getAccessTokenExpiresAtEpochMillis() > nowMillis;
        boolean needsRefresh = signedIn && !hasUsableAccessToken;
        return new AuthSessionState(signedIn, needsRefresh, refreshExpired, hasUsableAccessToken);
    }
}
