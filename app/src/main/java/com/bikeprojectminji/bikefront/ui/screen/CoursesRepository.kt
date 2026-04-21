package com.bikeprojectminji.bikefront.ui.screen

import android.util.Log

import android.content.Context
import com.bikeprojectminji.bikefront.config.AppConfig
import com.bikeprojectminji.bikefront.course.RecordedCourseStore
import com.bikeprojectminji.bikefront.home.FeaturedCourseApiClient
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class CoursesRepository(
    context: Context,
    private val featuredCourseApiClient: FeaturedCourseApiClient = FeaturedCourseApiClient(),
) {

    private val recordedCourseStore = RecordedCourseStore(context)

    fun fetchFeaturedCourses(): List<CourseCardUiModel> {
        return featuredCourseApiClient.fetchFeaturedCourses().courses.map {
            CourseCardUiModel(
                id = it.id,
                title = it.title,
                distanceKm = it.distanceKm,
                estimatedDurationMin = it.estimatedDurationMin,
                featuredRank = it.featuredRank,
            )
        }
    }

    fun fetchAllCourses(cursor: String? = null, limit: Int = 10): CoursesPageUiModel {
        val query = buildString {
            append("?limit=")
            append(limit)
            if (!cursor.isNullOrBlank()) {
                append("&cursor=")
                append(cursor)
            }
        }
        val connection = (URL(AppConfig.API_BASE_URL + "/api/v1/courses" + query).openConnection() as HttpURLConnection)

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = AppConfig.CONNECT_TIMEOUT_MS
            connection.readTimeout = AppConfig.READ_TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            Log.d("CoursesRepository", "fetchAllCourses responseCode=$responseCode url=${AppConfig.API_BASE_URL}/api/v1/courses$query")
            val inputStream = if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                connection.errorStream
            } else {
                connection.inputStream
            } ?: error("빈 응답을 받았습니다.")

            val body = readBody(inputStream)
            Log.d("CoursesRepository", "fetchAllCourses body=$body")
            if (responseCode != HttpURLConnection.HTTP_OK) {
                error("전체 코스를 불러오지 못했습니다.")
            }

            val data = JSONObject(body).optJSONObject("data")
                ?: error("전체 코스 응답 형식이 올바르지 않습니다.")
            Log.d("CoursesRepository", "fetchAllCourses data=$data")
            val itemsJson = data.optJSONArray("items")
            Log.d("CoursesRepository", "fetchAllCourses itemsLength=${itemsJson?.length() ?: -1}")
            val items = buildList {
                if (itemsJson != null) {
                    for (i in 0 until itemsJson.length()) {
                        val item = itemsJson.optJSONObject(i) ?: continue
                        Log.d("CoursesRepository", "fetchAllCourses item[$i]=$item")
                        add(
                            CourseCardUiModel(
                                id = item.optLong("id"),
                                title = item.optString("title", ""),
                                distanceKm = item.optDouble("distanceKm", 0.0),
                                estimatedDurationMin = item.optInt("estimatedDurationMin", 0),
                                isRecorded = item.optBoolean("recorded", false),
                            ),
                        )
                    }
                }
            }
            Log.d("CoursesRepository", "fetchAllCourses mappedItems=${items.size}")
            val mergedItems = mergeRecordedCourses(items)
            Log.d("CoursesRepository", "fetchAllCourses mergedItems=${mergedItems.size}")

            CoursesPageUiModel(
                items = mergedItems,
                hasNext = data.optBoolean("hasNext", false),
                nextCursor = data.optString("nextCursor").takeIf { it.isNotBlank() && it != "null" },
            )
        } finally {
            connection.disconnect()
        }
    }

    fun fetchCourseDetail(courseId: Long): CourseCardUiModel {
        val connection = (URL(AppConfig.API_BASE_URL + "/api/v1/courses/$courseId").openConnection() as HttpURLConnection)

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = AppConfig.CONNECT_TIMEOUT_MS
            connection.readTimeout = AppConfig.READ_TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            val inputStream = if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                connection.errorStream
            } else {
                connection.inputStream
            } ?: error("빈 응답을 받았습니다.")

            val body = readBody(inputStream)
            if (responseCode != HttpURLConnection.HTTP_OK) {
                error("코스 정보를 불러오지 못했습니다.")
            }

            val data = JSONObject(body).optJSONObject("data")
                ?: error("코스 상세 응답 형식이 올바르지 않습니다.")
            CourseCardUiModel(
                id = data.optLong("id"),
                title = data.optString("title", ""),
                distanceKm = data.optDouble("distanceKm", 0.0),
                estimatedDurationMin = data.optInt("estimatedDurationMin", 0),
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun readBody(inputStream: InputStream): String {
        val builder = StringBuilder()
        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                builder.append(line)
                line = reader.readLine()
            }
        }
        return builder.toString()
    }

    private fun mergeRecordedCourses(items: List<CourseCardUiModel>): List<CourseCardUiModel> {
        val recorded = recordedCourseStore.load().map {
            CourseCardUiModel(
                id = it.id,
                title = it.title,
                distanceKm = it.distanceKm,
                estimatedDurationMin = it.estimatedDurationMin,
                isRecorded = true,
            )
        }
        if (recorded.isEmpty()) return items
        val existingIds = items.map { it.id }.toSet()
        return recorded.filterNot { it.id in existingIds } + items
    }
}
