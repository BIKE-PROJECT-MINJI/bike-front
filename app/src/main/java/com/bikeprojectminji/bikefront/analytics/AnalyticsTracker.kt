package com.bikeprojectminji.bikefront.analytics

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.bikeprojectminji.bikefront.BuildConfig
import com.bikeprojectminji.bikefront.auth.AuthSessionStore
import com.bikeprojectminji.bikefront.config.AppConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.concurrent.thread

class AnalyticsTracker(context: Context) {
    private val authSessionStore = AuthSessionStore(context)
    private val preferences: SharedPreferences = context.getSharedPreferences("analytics_session", Context.MODE_PRIVATE)

    fun track(eventName: String, screenName: String, properties: Map<String, Any?> = emptyMap()) {
        val token = authSessionStore.accessToken
        if (token.isBlank()) {
            debugLog("skip event=$eventName screen=$screenName reason=no_token")
            return
        }
        val payload = JSONObject().apply {
            put("eventName", eventName)
            put("screenName", screenName)
            put("sessionId", sessionId())
            put("appVersion", BuildConfig.VERSION_NAME)
            put("osName", "android")
            put("deviceType", "android")
            put("properties", JSONObject().apply {
                properties.forEach { (key, value) ->
                    when (value) {
                        null -> put(key, JSONObject.NULL)
                        is Number, is Boolean, is String -> put(key, value)
                        else -> put(key, value.toString())
                    }
                }
            })
        }
        thread(name = "analytics-track") {
            runCatching {
                val connection = (URL(AppConfig.API_BASE_URL + "/api/v1/events").openConnection() as HttpURLConnection)
                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = AppConfig.CONNECT_TIMEOUT_MS
                    connection.readTimeout = AppConfig.READ_TIMEOUT_MS
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("Accept", "application/json")
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.outputStream.use { it.write(payload.toString().toByteArray()) }
                    val responseCode = connection.responseCode
                    debugLog("sent event=$eventName screen=$screenName responseCode=$responseCode")
                    responseCode
                } finally {
                    connection.disconnect()
                }
            }.onFailure {
                debugError("event send failed event=$eventName screen=$screenName", it)
            }
        }
    }

    private fun debugLog(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("AnalyticsTracker", message)
        }
    }

    private fun debugError(message: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("AnalyticsTracker", message, throwable)
        }
    }

    private fun sessionId(): String {
        val existing = preferences.getString("session_id", null)
        if (!existing.isNullOrBlank()) return existing
        val created = "android_${Build.MODEL}_${UUID.randomUUID()}"
        preferences.edit().putString("session_id", created).apply()
        return created
    }
}
