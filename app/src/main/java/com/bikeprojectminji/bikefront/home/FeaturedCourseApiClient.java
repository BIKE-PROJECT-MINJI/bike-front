package com.bikeprojectminji.bikefront.home;

import com.bikeprojectminji.bikefront.config.AppConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import java.net.HttpURLConnection;
import java.net.URL;

public class FeaturedCourseApiClient {

    public FeaturedCourseResponse fetchFeaturedCourses() throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(AppConfig.API_BASE_URL + "/api/v1/courses/featured");
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
                throw new IllegalStateException("빈 응답을 받았습니다.");
            }

            String responseBody = readBody(inputStream);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("추천 코스를 불러오지 못했습니다.");
            }

            JSONObject root = new JSONObject(responseBody);
            JSONObject data = root.optJSONObject("data");

            if (data == null) {
                throw new IllegalStateException("추천 코스 응답 형식이 올바르지 않습니다.");
            }

            String sortingMode = data.optString("sortingMode", "fallback");
            JSONArray coursesJson = data.optJSONArray("courses");
            List<FeaturedCourseUiModel> courses = new ArrayList<>();

            if (coursesJson != null) {
                for (int i = 0; i < coursesJson.length(); i++) {
                    JSONObject item = coursesJson.optJSONObject(i);

                    if (item == null) {
                        continue;
                    }

                    courses.add(new FeaturedCourseUiModel(
                            item.optLong("id"),
                            item.optString("title", ""),
                            item.optDouble("distanceKm", 0),
                            item.optInt("estimatedDurationMin", 0),
                            item.has("distanceFromUserM") && !item.isNull("distanceFromUserM")
                                    ? item.optInt("distanceFromUserM")
                                    : null,
                            item.optInt("featuredRank", 0)
                    ));
                }
            }

            return new FeaturedCourseResponse(sortingMode, courses);
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
