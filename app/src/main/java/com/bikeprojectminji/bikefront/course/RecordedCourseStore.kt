package com.bikeprojectminji.bikefront.course

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class RecordedCourseStore(context: Context) {

    private val preferences = context.getSharedPreferences("recorded_courses", Context.MODE_PRIVATE)

    fun save(course: RecordedCourseItem) {
        val existing = load().toMutableList()
        existing.removeAll { it.id == course.id }
        existing.add(0, course)
        val payload = JSONArray()
        existing.forEach {
            payload.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("distanceKm", it.distanceKm)
                    .put("estimatedDurationMin", it.estimatedDurationMin),
            )
        }
        preferences.edit().putString(KEY_ITEMS, payload.toString()).apply()
    }

    fun load(): List<RecordedCourseItem> {
        val raw = preferences.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        RecordedCourseItem(
                            id = item.optLong("id"),
                            title = item.optString("title"),
                            distanceKm = item.optDouble("distanceKm", 0.0),
                            estimatedDurationMin = item.optInt("estimatedDurationMin", 0),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val KEY_ITEMS = "items"
    }
}

data class RecordedCourseItem(
    val id: Long,
    val title: String,
    val distanceKm: Double,
    val estimatedDurationMin: Int,
)
