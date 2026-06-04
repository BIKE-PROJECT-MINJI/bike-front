package com.bikeprojectminji.bikefront.auth;

import android.os.Handler;
import android.os.Looper;

import com.bikeprojectminji.bikefront.config.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpAuthLoginGateway implements AuthLoginGateway {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void register(String email, String password, String displayName, Callback callback) {
        executeAuth("/api/v1/auth/register", buildRegisterBody(email, password, displayName), email, callback);
    }

    @Override
    public void login(String email, String password, Callback callback) {
        executeAuth("/api/v1/auth/login", buildLoginBody(email, password), email, callback);
    }

    @Override
    public void kakaoLogin(String kakaoAccessToken, Callback callback) {
        executeAuth("/api/v1/auth/kakao/login", KakaoLoginRequestBodyFactory.create(kakaoAccessToken), "", callback);
    }

    @Override
    public void refresh(String refreshToken, Callback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/auth/refresh");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(buildRefreshBody(refreshToken).toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                String responseText = readBody(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
                if (responseCode >= 200 && responseCode < 300) {
                    LoginResult result = AuthResponseJsonParser.parseLoginResult("", responseText);
                    mainHandler.post(() -> callback.onSuccess(result));
                    return;
                }

                postFailure(responseText, callback, "로그인 정보가 만료되었습니다. 다시 로그인해 주세요.");
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onFailure("세션을 갱신하지 못했습니다. 서버 상태를 다시 확인해 주세요."));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    public void getMyProfile(String accessToken, ProfileCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/profile/me");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);

                int responseCode = connection.getResponseCode();
                String responseText = readBody(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
                if (responseCode >= 200 && responseCode < 300) {
                    ProfileResult result = AuthResponseJsonParser.parseProfileResult(responseText);
                    mainHandler.post(() -> callback.onSuccess(result));
                    return;
                }

                postProfileFailure(responseText, callback, "프로필 정보를 확인하지 못했습니다.");
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onFailure("프로필 정보를 확인하지 못했습니다."));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    public void getMyActivitySummary(String accessToken, ActivitySummaryCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/profile/me/activity-summary");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);

                int responseCode = connection.getResponseCode();
                String responseText = readBody(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
                if (responseCode >= 200 && responseCode < 300) {
                    ActivitySummaryResult result = AuthResponseJsonParser.parseActivitySummaryResult(responseText);
                    mainHandler.post(() -> callback.onSuccess(result));
                    return;
                }

                postActivitySummaryFailure(responseText, callback, "활동 요약을 확인하지 못했습니다.");
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onFailure("활동 요약을 확인하지 못했습니다."));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void executeAuth(String path, JSONObject requestJson, String email, Callback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(AppConfig.API_BASE_URL + path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                String responseText = readBody(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
                if (responseCode >= 200 && responseCode < 300) {
                    LoginResult result = AuthResponseJsonParser.parseLoginResult(email, responseText);
                    mainHandler.post(() -> callback.onSuccess(result));
                    return;
                }

                postFailure(responseText, callback, "현재 서버에서 로그인을 준비 중입니다. 잠시 후 다시 시도해 주세요.");
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onFailure("현재 서버에서 로그인을 사용할 수 없습니다. 공개 코스만 둘러볼 수 있습니다."));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private JSONObject buildRegisterBody(String email, String password, String displayName) {
        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("email", email);
            requestJson.put("password", password);
            requestJson.put("displayName", displayName);
            requestJson.put("profileImageUrl", JSONObject.NULL);
            return requestJson;
        } catch (JSONException exception) {
            throw new IllegalStateException("회원가입 요청을 만들 수 없습니다.", exception);
        }
    }

    private JSONObject buildRefreshBody(String refreshToken) {
        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("refreshToken", refreshToken);
            return requestJson;
        } catch (JSONException exception) {
            throw new IllegalStateException("세션 갱신 요청을 만들 수 없습니다.", exception);
        }
    }

    private void postFailure(String responseText, Callback callback, String fallbackMessage) {
        String message = extractMessage(responseText, fallbackMessage);
        String finalMessage = message;
        mainHandler.post(() -> callback.onFailure(finalMessage));
    }

    private void postProfileFailure(String responseText, ProfileCallback callback, String fallbackMessage) {
        String message = extractMessage(responseText, fallbackMessage);
        String finalMessage = message;
        mainHandler.post(() -> callback.onFailure(finalMessage));
    }

    private void postActivitySummaryFailure(String responseText, ActivitySummaryCallback callback, String fallbackMessage) {
        String message = extractMessage(responseText, fallbackMessage);
        String finalMessage = message;
        mainHandler.post(() -> callback.onFailure(finalMessage));
    }

    private String extractMessage(String responseText, String fallbackMessage) {
        if (responseText == null || responseText.isBlank()) {
            return fallbackMessage;
        }
        try {
            JSONObject root = new JSONObject(responseText);
            return root.optString("message", fallbackMessage);
        } catch (JSONException exception) {
            return fallbackMessage;
        }
    }

    private JSONObject buildLoginBody(String email, String password) {
        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("email", email);
            requestJson.put("password", password);
            return requestJson;
        } catch (JSONException exception) {
            throw new IllegalStateException("로그인 요청을 만들 수 없습니다.", exception);
        }
    }

    private String readBody(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
