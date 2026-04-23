package com.bikeprojectminji.bikefront.ridepolicy;

import android.location.Location;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpRidePolicyEvaluationGateway implements RidePolicyEvaluationGateway {

    private static final String FALLBACK_ERROR_MESSAGE = "주행 정책을 확인하지 못했습니다. 다시 시도해 주세요.";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void evaluate(long courseId, String phase, Location location, Callback callback) {
        executorService.execute(() -> {
            try {
                EvaluationResult result = requestEvaluation(courseId, phase, location);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception exception) {
                String message = exception.getMessage();
                mainHandler.post(() -> callback.onFailure(resolveErrorMessage(message)));
            }
        });
    }

    private EvaluationResult requestEvaluation(long courseId, String phase, Location location) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/courses/" + courseId + "/ride-policy/evaluate");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.getOutputStream().write(buildRequestBody(phase, location).getBytes(StandardCharsets.UTF_8));

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

            JSONObject startGate = data.optJSONObject("startGate");
            JSONObject offRoute = data.optJSONObject("offRoute");

            return new EvaluationResult(
                    data.optString("phase", phase),
                    parseGate(startGate),
                    parseGate(offRoute),
                    data.optString("overallState", "UNDETERMINED"),
                    data.optString("defaultMessage", FALLBACK_ERROR_MESSAGE)
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildRequestBody(String phase, Location location) throws Exception {
        JSONObject payload = new JSONObject();
        JSONObject locationJson = new JSONObject();

        payload.put("phase", phase);
        locationJson.put("lat", location.getLatitude());
        locationJson.put("lon", location.getLongitude());
        locationJson.put("accuracyM", location.hasAccuracy() ? location.getAccuracy() : 100d);
        locationJson.put("capturedAt", toOffsetDateTime(location));
        payload.put("location", locationJson);

        return payload.toString();
    }

    private String toOffsetDateTime(Location location) {
        long capturedAtMillis = location.getTime() > 0 ? location.getTime() : System.currentTimeMillis();
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(capturedAtMillis), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private GateResult parseGate(JSONObject gate) {
        if (gate == null) {
            return new GateResult("UNDETERMINED", "UNKNOWN_GATE", Double.NaN, Double.NaN);
        }

        return new GateResult(
                gate.optString("status", "UNDETERMINED"),
                gate.optString("reasonCode", "UNKNOWN_REASON"),
                gate.has("distanceM") ? gate.optDouble("distanceM", Double.NaN) : Double.NaN,
                gate.has("thresholdM") ? gate.optDouble("thresholdM", Double.NaN) : Double.NaN
        );
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
