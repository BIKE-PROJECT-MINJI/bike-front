package com.bikeprojectminji.bikefront.course;

import android.os.Handler;
import android.os.Looper;

import com.bikeprojectminji.bikefront.config.AppConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpCourseShareGateway implements CourseShareGateway {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void shareCourse(String accessToken, long courseId, Callback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/courses/" + courseId + "/share");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);

                int responseCode = connection.getResponseCode();
                String responseText = readBody(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
                JSONObject root = responseText.isBlank() ? new JSONObject() : new JSONObject(responseText);
                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject data = root.optJSONObject("data");
                    String shareType = data == null ? "" : data.optString("shareType", "");
                    String visibility = data == null ? "" : data.optString("visibility", "");
                    String shareUrl = data == null ? "" : data.optString("shareUrl", "");
                    String shareToken = data == null ? "" : data.optString("shareToken", "");
                    mainHandler.post(() -> callback.onSuccess(new ShareResult(shareType, visibility, shareUrl, shareToken)));
                    return;
                }

                String message = root.optString("message", "코스 공유 정보를 불러오지 못했습니다.");
                mainHandler.post(() -> callback.onFailure(message));
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onFailure("코스 공유 정보를 불러오지 못했습니다. 서버 상태를 다시 확인해 주세요."));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
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
