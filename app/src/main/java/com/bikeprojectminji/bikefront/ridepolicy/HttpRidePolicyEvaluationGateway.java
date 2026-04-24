package com.bikeprojectminji.bikefront.ridepolicy;

import android.location.Location;
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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpRidePolicyEvaluationGateway implements RidePolicyEvaluationGateway {

    private static final String FALLBACK_ERROR_MESSAGE = "주행 정책을 확인하지 못했습니다. 다시 시도해 주세요.";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void evaluate(long courseId, String phase, Location location, List<TraceLocation> trace, Callback callback) {
        executorService.execute(() -> {
            try {
                EvaluationResult result = requestEvaluation(courseId, phase, location, trace);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception exception) {
                String message = exception.getMessage();
                mainHandler.post(() -> callback.onFailure(resolveErrorMessage(message)));
            }
        });
    }

    private EvaluationResult requestEvaluation(long courseId, String phase, Location location, List<TraceLocation> trace) throws Exception {
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
            connection.getOutputStream().write(buildRequestBody(phase, location, trace).getBytes(StandardCharsets.UTF_8));

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
            JSONObject completion = data.optJSONObject("completion");

            return new EvaluationResult(
                    data.optString("phase", phase),
                    parseGate(startGate),
                    parseOffRoute(offRoute),
                    parseCompletion(completion),
                    data.optString("overallState", "UNDETERMINED"),
                    data.optString("defaultMessage", FALLBACK_ERROR_MESSAGE)
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildRequestBody(String phase, Location location, List<TraceLocation> trace) throws Exception {
        JSONObject payload = new JSONObject();

        payload.put("phase", phase);
        payload.put(
                "location",
                buildLocationJson(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.hasAccuracy() ? location.getAccuracy() : 100d,
                        location.getTime() > 0 ? location.getTime() : System.currentTimeMillis()
                )
        );

        if ("ACTIVE".equals(phase) && trace != null && !trace.isEmpty()) {
            JSONArray traceJson = new JSONArray();
            for (TraceLocation traceLocation : trace) {
                traceJson.put(
                        buildLocationJson(
                                traceLocation.getLatitude(),
                                traceLocation.getLongitude(),
                                traceLocation.getAccuracyM(),
                                traceLocation.getCapturedAtMillis()
                        )
                );
            }
            payload.put("trace", traceJson);
        }

        return payload.toString();
    }

    private JSONObject buildLocationJson(double latitude, double longitude, double accuracyM, long capturedAtMillis) throws Exception {
        JSONObject locationJson = new JSONObject();
        locationJson.put("lat", latitude);
        locationJson.put("lon", longitude);
        locationJson.put("accuracyM", accuracyM);
        locationJson.put("capturedAt", toOffsetDateTime(capturedAtMillis));
        return locationJson;
    }

    private String toOffsetDateTime(long capturedAtMillis) {
        long resolvedCapturedAtMillis = capturedAtMillis > 0 ? capturedAtMillis : System.currentTimeMillis();
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(resolvedCapturedAtMillis), ZoneId.systemDefault())
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

    private OffRouteResult parseOffRoute(JSONObject offRoute) {
        if (offRoute == null) {
            return new OffRouteResult("UNDETERMINED", "UNKNOWN_REASON", null, null, null, null, null);
        }

        return new OffRouteResult(
                offRoute.optString("status", "UNDETERMINED"),
                offRoute.optString("reasonCode", "UNKNOWN_REASON"),
                offRoute.has("distanceM") ? offRoute.optInt("distanceM") : null,
                offRoute.has("candidateThresholdM") ? offRoute.optInt("candidateThresholdM") : null,
                offRoute.has("warningThresholdSec") ? offRoute.optInt("warningThresholdSec") : null,
                offRoute.has("recoveryThresholdM") ? offRoute.optInt("recoveryThresholdM") : null,
                offRoute.has("durationSec") ? offRoute.optInt("durationSec") : null
        );
    }

    private CompletionResult parseCompletion(JSONObject completion) {
        if (completion == null) {
            return new CompletionResult("UNDETERMINED", "UNKNOWN_REASON", null, null, null, null, null, null);
        }

        return new CompletionResult(
                completion.optString("status", "UNDETERMINED"),
                completion.optString("reasonCode", "UNKNOWN_REASON"),
                completion.has("coveragePercent") ? completion.optInt("coveragePercent") : null,
                completion.has("coverageThresholdPercent") ? completion.optInt("coverageThresholdPercent") : null,
                completion.has("loopCourse") ? completion.optBoolean("loopCourse") : null,
                completion.has("leftStartZone") ? completion.optBoolean("leftStartZone") : null,
                completion.has("distanceM") ? completion.optInt("distanceM") : null,
                completion.has("thresholdM") ? completion.optInt("thresholdM") : null
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
