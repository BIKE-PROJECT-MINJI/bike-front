package com.bikeprojectminji.bikefront.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthSessionStore {

    private static final String PREF_NAME = "auth_session";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_PROFILE_IMAGE_URL = "profile_image_url";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at";
    private static final String KEY_REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at";

    private final SharedPreferences sharedPreferences;

    public AuthSessionStore(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSignedIn() {
        return resolveState().isSignedIn();
    }

    public boolean needsRefresh() {
        return resolveState().isNeedsRefresh();
    }

    public boolean hasUsableAccessToken() {
        return resolveState().isHasUsableAccessToken();
    }

    public boolean isRefreshExpired() {
        return resolveState().isRefreshExpired();
    }


    public String getDisplayName() {
        return sharedPreferences.getString(KEY_DISPLAY_NAME, "") == null ? "" : sharedPreferences.getString(KEY_DISPLAY_NAME, "");
    }

    public String getProfileImageUrl() {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE_URL, "") == null ? "" : sharedPreferences.getString(KEY_PROFILE_IMAGE_URL, "");
    }

    public String getAccessToken() {
        AuthSession session = getStoredSession();
        return resolveState().isHasUsableAccessToken() && session != null ? session.getAccessToken() : "";
    }

    public String getRefreshToken() {
        AuthSession session = getStoredSession();
        return session == null ? "" : session.getRefreshToken();
    }

    public long getAccessTokenExpiresAtEpochMillis() {
        return sharedPreferences.getLong(KEY_ACCESS_TOKEN_EXPIRES_AT, 0L);
    }

    public long getRefreshTokenExpiresAtEpochMillis() {
        return sharedPreferences.getLong(KEY_REFRESH_TOKEN_EXPIRES_AT, 0L);
    }

    public AuthSession getStoredSession() {
        String displayName = getDisplayName();
        String profileImageUrl = getProfileImageUrl();
        String accessToken = valueOrEmpty(sharedPreferences.getString(KEY_ACCESS_TOKEN, ""));
        String refreshToken = valueOrEmpty(sharedPreferences.getString(KEY_REFRESH_TOKEN, ""));
        long accessExpiry = getAccessTokenExpiresAtEpochMillis();
        long refreshExpiry = getRefreshTokenExpiresAtEpochMillis();
        if (displayName.isBlank() && profileImageUrl.isBlank() && accessToken.isBlank() && refreshToken.isBlank() && accessExpiry == 0L && refreshExpiry == 0L) {
            return null;
        }
        return new AuthSession(displayName, profileImageUrl, accessToken, refreshToken, accessExpiry, refreshExpiry);
    }

    public void saveSession(AuthSession session) {
        sharedPreferences.edit()
                .putString(KEY_DISPLAY_NAME, session.getDisplayName())
                .putString(KEY_PROFILE_IMAGE_URL, session.getProfileImageUrl())
                .putString(KEY_ACCESS_TOKEN, session.getAccessToken())
                .putString(KEY_REFRESH_TOKEN, session.getRefreshToken())
                .putLong(KEY_ACCESS_TOKEN_EXPIRES_AT, session.getAccessTokenExpiresAtEpochMillis())
                .putLong(KEY_REFRESH_TOKEN_EXPIRES_AT, session.getRefreshTokenExpiresAtEpochMillis())
                .commit();
    }

    public void clear() {
        sharedPreferences.edit().clear().commit();
    }

    private AuthSessionState resolveState() {
        return AuthSessionStateResolver.resolve(getStoredSession(), System.currentTimeMillis());
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
