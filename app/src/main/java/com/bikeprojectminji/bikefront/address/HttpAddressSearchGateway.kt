package com.bikeprojectminji.bikefront.address

import android.os.Handler
import android.os.Looper
import com.bikeprojectminji.bikefront.config.AppConfig
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HttpAddressSearchGateway(
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor(),
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) : AddressSearchGateway {

    override fun search(
        query: String,
        accessToken: String,
        onSuccess: (AddressSearchUiModel) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            onFailure("목적지 주소나 장소명을 입력해 주세요.")
            return
        }
        executorService.execute {
            var connection: HttpURLConnection? = null
            try {
                connection = openConnection(normalizedQuery, accessToken)
                val responseCode = connection.responseCode
                val responseText = readBody(if (responseCode >= 400) connection.errorStream else connection.inputStream)
                if (responseCode in 200..299) {
                    val result = AddressSearchJsonMapper.toUiModel(JSONObject(responseText))
                    mainHandler.post { onSuccess(result) }
                    return@execute
                }
                mainHandler.post { onFailure(extractMessage(responseText)) }
            } catch (exception: Exception) {
                mainHandler.post { onFailure("주소 검색 서버에 연결하지 못했습니다.") }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun openConnection(query: String, accessToken: String): HttpURLConnection {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        val url = URL("${AppConfig.API_BASE_URL}/api/v1/addresses/search?query=$encodedQuery&page=1&size=3")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = AppConfig.CONNECT_TIMEOUT_MS
        connection.readTimeout = AppConfig.READ_TIMEOUT_MS
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        if (accessToken.isNotBlank()) {
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
        }
        return connection
    }

    private fun readBody(inputStream: InputStream?): String {
        return inputStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
    }

    private fun extractMessage(responseText: String): String {
        return runCatching {
            JSONObject(responseText).optString("message", "주소 검색을 완료하지 못했습니다.")
        }.getOrDefault("주소 검색을 완료하지 못했습니다.")
    }
}

