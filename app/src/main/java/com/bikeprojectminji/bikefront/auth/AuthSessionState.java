package com.bikeprojectminji.bikefront.auth;

public class AuthSessionState {

    private final boolean signedIn;
    private final boolean needsRefresh;
    private final boolean refreshExpired;
    private final boolean hasUsableAccessToken;

    public AuthSessionState(boolean signedIn, boolean needsRefresh, boolean refreshExpired, boolean hasUsableAccessToken) {
        this.signedIn = signedIn;
        this.needsRefresh = needsRefresh;
        this.refreshExpired = refreshExpired;
        this.hasUsableAccessToken = hasUsableAccessToken;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public boolean isNeedsRefresh() {
        return needsRefresh;
    }

    public boolean isRefreshExpired() {
        return refreshExpired;
    }

    public boolean isHasUsableAccessToken() {
        return hasUsableAccessToken;
    }
}
