package com.bikeprojectminji.bikefront.course;

import android.os.Handler;
import android.os.Looper;

import com.bikeprojectminji.bikefront.config.AppConfig;

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

public class HttpCourseWriteGateway implements CourseWriteGateway {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void createCourse(String accessToken, CreateCourseDraft draft, Callback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/courses");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("sourceRideRecordId", draft.getSourceRideRecordId());
                body.put("name", draft.getName());
                body.put("description", draft.getDescription());
                body.put("visibility", draft.getVisibility());

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                String responseText = readBody(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
                JSONObject root = responseText.isBlank() ? new JSONObject() : new JSONObject(responseText);
                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject data = root.optJSONObject("data");
                    long courseId = data == null ? -1L : data.optLong("courseId", -1L);
                    String visibility = data == null ? draft.getVisibility() : data.optString("visibility", draft.getVisibility());
                    mainHandler.post(() -> callback.onSuccess(new CourseCreateResult(courseId, visibility)));
                    return;
                }
                String message = root.optString("message", "코스를 저장하지 못했습니다.");
                mainHandler.post(() -> callback.onFailure(message));
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onFailure("코스를 저장하지 못했습니다. 서버 상태를 다시 확인해 주세요."));
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
