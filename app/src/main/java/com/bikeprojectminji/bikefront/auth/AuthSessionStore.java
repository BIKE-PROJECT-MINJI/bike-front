package com.bikeprojectminji.bikefront.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthSessionStore {

    private static final String PREF_NAME = "auth_session";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_PROFILE_IMAGE_URL = "profile_image_url";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private final SharedPreferences sharedPreferences;

    public AuthSessionStore(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSignedIn() {
        return !getAccessToken().isBlank();
    }


    public String getDisplayName() {
        return sharedPreferences.getString(KEY_DISPLAY_NAME, "") == null ? "" : sharedPreferences.getString(KEY_DISPLAY_NAME, "");
    }

    public String getProfileImageUrl() {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE_URL, "") == null ? "" : sharedPreferences.getString(KEY_PROFILE_IMAGE_URL, "");
    }

    public String getAccessToken() {
        String saved = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        return saved == null ? "" : saved;
    }

    public void saveSession(String displayName, String profileImageUrl, String accessToken) {
        sharedPreferences.edit()
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_PROFILE_IMAGE_URL, profileImageUrl)
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .commit();
    }

    public void clear() {
        sharedPreferences.edit().clear().commit();
    }
}
