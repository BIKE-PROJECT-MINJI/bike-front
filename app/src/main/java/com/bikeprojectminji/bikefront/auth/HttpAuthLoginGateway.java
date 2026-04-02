package com.bikeprojectminji.bikefront.auth;

import android.os.Handler;
import android.os.Looper;

import com.bikeprojectminji.bikefront.config.AppConfig;

import org.json.JSONObject;
import org.json.JSONException;

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
        executeAuth("/api/v1/auth/register", buildRegisterBody(email, password, displayName), email, displayName, callback);
    }

    @Override
    public void login(String email, String password, Callback callback) {
        executeAuth("/api/v1/auth/login", buildLoginBody(email, password), email, null, callback);
    }

    private void executeAuth(String path, JSONObject requestJson, String email, String fallbackDisplayName, Callback callback) {
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
                    JSONObject root = new JSONObject(responseText);
                    JSONObject data = root.getJSONObject("data");
                    mainHandler.post(() -> callback.onSuccess(new LoginResult(
                            email,
                            data.optString("displayName", fallbackDisplayName == null ? "" : fallbackDisplayName),
                            data.optString("accessToken", "")
                    )));
                    return;
                }

                String message = "현재 서버에서 로그인을 준비 중입니다. 잠시 후 다시 시도해 주세요.";
                if (responseText != null && !responseText.isBlank()) {
                    JSONObject root = new JSONObject(responseText);
                    message = root.optString("message", message);
                }
                String finalMessage = message;
                mainHandler.post(() -> callback.onFailure(finalMessage));
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
