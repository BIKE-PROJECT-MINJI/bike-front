package com.bikeprojectminji.bikefront.sheet;

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

import android.os.Handler;
import android.os.Looper;

public class HttpCourseDetailGateway implements CourseDetailGateway {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void loadCourseDetail(long courseId, Callback callback) {
        executorService.execute(() -> {
            try {
                CourseDetail courseDetail = requestCourseDetail(courseId);
                mainHandler.post(() -> callback.onSuccess(courseDetail));
            } catch (Exception exception) {
                String message = exception.getMessage();
                mainHandler.post(() -> callback.onFailure(message));
            }
        });
    }

    private CourseDetail requestCourseDetail(long courseId) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/courses/" + courseId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (inputStream == null) {
                throw new Exception("코스 정보를 불러오지 못했습니다.");
            }

            String responseBody = readBody(inputStream);
            JSONObject root = new JSONObject(responseBody);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception(root.optString("message", "코스 정보를 불러오지 못했습니다."));
            }

            JSONObject data = root.optJSONObject("data");
            if (data == null) {
                throw new Exception("코스 정보를 불러오지 못했습니다.");
            }

            return new CourseDetail(
                    data.optLong("id"),
                    data.optString("title", ""),
                    data.optDouble("distanceKm", 0),
                    data.optInt("estimatedDurationMin", 0)
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readBody(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();
        return builder.toString();
    }
}
