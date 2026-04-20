package com.bikeprojectminji.bikefront.home;

import com.bikeprojectminji.bikefront.config.AppConfig;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class FeaturedCourseApiClient implements FeaturedCourseRepository {

    public FeaturedCourseResponse fetchFeaturedCourses() throws Exception {
        return loadFeaturedCourses();
    }

    @Override
    public FeaturedCourseResponse loadFeaturedCourses() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(AppConfig.API_BASE_URL + "/api/v1/courses/featured").openConnection();
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(AppConfig.CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(AppConfig.READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST ? connection.getErrorStream() : connection.getInputStream();
            if (inputStream == null) {
                return new FeaturedCourseResponse(List.of());
            }

            String body = readBody(inputStream);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("추천 코스를 불러오지 못했습니다.");
            }

            JSONObject data = new JSONObject(body).optJSONObject("data");
            if (data == null) {
                return new FeaturedCourseResponse(List.of());
            }

            JSONArray coursesJson = data.optJSONArray("courses");
            List<FeaturedCourseUiModel> courses = new ArrayList<>();
            if (coursesJson != null) {
                for (int index = 0; index < coursesJson.length(); index++) {
                    JSONObject item = coursesJson.optJSONObject(index);
                    if (item == null) {
                        continue;
                    }
                    courses.add(new FeaturedCourseUiModel(
                            item.optLong("id"),
                            item.optString("title", ""),
                            item.optDouble("distanceKm", 0.0),
                            item.optInt("estimatedDurationMin", 0),
                            item.optInt("featuredRank", index + 1)
                    ));
                }
            }
            return new FeaturedCourseResponse(courses);
        } finally {
            connection.disconnect();
        }
    }

    private String readBody(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try {
            String line = reader.readLine();
            while (line != null) {
                builder.append(line);
                line = reader.readLine();
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }
}
