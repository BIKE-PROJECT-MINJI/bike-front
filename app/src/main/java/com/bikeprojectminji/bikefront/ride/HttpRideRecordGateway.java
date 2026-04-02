package com.bikeprojectminji.bikefront.ride;

import android.os.Handler;
import android.os.Looper;

import com.bikeprojectminji.bikefront.config.AppConfig;

import org.json.JSONArray;
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

public class HttpRideRecordGateway implements RideRecordGateway {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void saveRideRecord(String accessToken, RideRecordDraft draft, Callback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/ride-records");
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
                connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("startedAt", draft.getStartedAt().toString());
                body.put("endedAt", draft.getEndedAt().toString());

                JSONObject summary = new JSONObject();
                summary.put("distanceM", draft.getDistanceM());
                summary.put("durationSec", draft.getDurationSec());
                body.put("summary", summary);

                JSONArray points = new JSONArray();
                for (RideRecordPoint point : draft.getRoutePoints()) {
                    JSONObject pointJson = new JSONObject();
                    pointJson.put("pointOrder", point.getPointOrder());
                    pointJson.put("latitude", point.getLatitude());
                    pointJson.put("longitude", point.getLongitude());
                    points.put(pointJson);
                }
                body.put("routePoints", points);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();
                String responseText = readBody(responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
                JSONObject root = responseText.isBlank() ? new JSONObject() : new JSONObject(responseText);
                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject data = root.optJSONObject("data");
                    long rideRecordId = data == null ? -1L : data.optLong("rideRecordId", -1L);
                    long finalRideRecordId = rideRecordId;
                    mainHandler.post(() -> callback.onSuccess(new RideRecordSaveResult(finalRideRecordId)));
                    return;
                }
                String message = root.optString("message", "주행 기록을 저장하지 못했습니다.");
                mainHandler.post(() -> callback.onFailure(message));
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onFailure("주행 기록을 저장하지 못했습니다. 서버 상태를 다시 확인해 주세요."));
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
