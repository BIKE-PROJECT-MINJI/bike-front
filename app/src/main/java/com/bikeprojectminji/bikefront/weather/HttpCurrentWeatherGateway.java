package com.bikeprojectminji.bikefront.weather;

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

public class HttpCurrentWeatherGateway implements CurrentWeatherGateway {

    private static final String FALLBACK_ERROR_MESSAGE = "날씨 정보를 불러오지 못했습니다.";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void loadCurrent(double latitude, double longitude, Callback callback) {
        executorService.execute(() -> {
            try {
                WeatherResult result = requestCurrent(latitude, longitude);
                if (result == null) {
                    mainHandler.post(callback::onEmpty);
                } else {
                    mainHandler.post(() -> callback.onSuccess(result));
                }
            } catch (Exception exception) {
                String message = exception.getMessage();
                mainHandler.post(() -> callback.onFailure(resolveErrorMessage(message)));
            }
        });
    }

    private WeatherResult requestCurrent(double latitude, double longitude) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/weather/current?lat=" + latitude + "&lon=" + longitude);
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
                throw new Exception(FALLBACK_ERROR_MESSAGE);
            }

            String responseBody = readBody(inputStream);
            JSONObject root = new JSONObject(responseBody);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception(root.optString("message", FALLBACK_ERROR_MESSAGE));
            }

            if (root.isNull("data")) {
                return null;
            }

            JSONObject data = root.optJSONObject("data");
            if (data == null) {
                return null;
            }

            JSONObject weather = data.optJSONObject("weather");
            JSONObject wind = data.optJSONObject("wind");

            return new WeatherResult(
                    weather != null && !weather.isNull("temperatureC") ? weather.optInt("temperatureC") : null,
                    weather != null ? weather.optString("sky", "") : "",
                    wind != null && !wind.isNull("speedKmh") ? wind.optInt("speedKmh") : null,
                    wind != null ? wind.optString("directionText", "") : "",
                    wind != null && !wind.isNull("directionDeg") ? wind.optInt("directionDeg") : null,
                    data.optBoolean("stale", false),
                    data.optBoolean("forecastFallbackUsed", false)
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

    private String resolveErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return FALLBACK_ERROR_MESSAGE;
        }
        return message;
    }
}
