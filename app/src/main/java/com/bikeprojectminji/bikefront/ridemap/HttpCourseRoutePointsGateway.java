package com.bikeprojectminji.bikefront.ridemap;

import android.os.Handler;
import android.os.Looper;

import com.bikeprojectminji.bikefront.config.AppConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpCourseRoutePointsGateway implements CourseRoutePointsGateway {

    private static final String FALLBACK_ERROR_MESSAGE = "경로를 불러오지 못했습니다.";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void loadRoutePoints(long courseId, Callback callback) {
        loadRoutePoints(courseId, "", callback);
    }

    @Override
    public void loadRoutePoints(long courseId, String accessToken, Callback callback) {
        executorService.execute(() -> {
            try {
                RoutePointsResult result = requestRoutePoints(courseId, accessToken);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception exception) {
                String message = exception.getMessage();
                mainHandler.post(() -> callback.onFailure(resolveErrorMessage(message)));
            }
        });
    }

    private RoutePointsResult requestRoutePoints(long courseId, String accessToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/courses/" + courseId + "/route-points");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            if (accessToken != null && !accessToken.isBlank()) {
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }

            int responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (inputStream == null) {
                throw new Exception(FALLBACK_ERROR_MESSAGE);
            }

            String responseBody = readBody(inputStream);
            JSONObject root = new JSONObject(responseBody);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception(root.optString("message", FALLBACK_ERROR_MESSAGE));
            }

            JSONObject data = root.optJSONObject("data");
            if (data == null) {
                throw new Exception(FALLBACK_ERROR_MESSAGE);
            }

            JSONArray pointsJson = data.optJSONArray("points");
            List<RoutePoint> points = new ArrayList<>();
            if (pointsJson != null) {
                for (int i = 0; i < pointsJson.length(); i++) {
                    JSONObject point = pointsJson.optJSONObject(i);
                    if (point == null) {
                        continue;
                    }

                    points.add(new RoutePoint(
                            point.optInt("pointOrder", i),
                            point.optDouble("latitude", 0d),
                            point.optDouble("longitude", 0d)
                    ));
                }
            }

            return new RoutePointsResult(data.optLong("courseId", courseId), points);
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

    private String resolveErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return FALLBACK_ERROR_MESSAGE;
        }

        return message;
    }
}
