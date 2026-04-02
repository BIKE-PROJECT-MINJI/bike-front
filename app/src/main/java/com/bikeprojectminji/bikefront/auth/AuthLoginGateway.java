package com.bikeprojectminji.bikefront.auth;

public interface AuthLoginGateway {

    void register(String email, String password, String displayName, Callback callback);

    void login(String email, String password, Callback callback);

    interface Callback {
        void onSuccess(LoginResult result);

        void onFailure(String message);
    }

    class LoginResult {
        private final String email;
        private final String displayName;
        private final String accessToken;

        public LoginResult(String email, String displayName, String accessToken) {
            this.email = email;
            this.displayName = displayName;
            this.accessToken = accessToken;
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
    }
}
