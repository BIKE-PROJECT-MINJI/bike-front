package com.bikeprojectminji.bikefront.airoute

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bikeprojectminji.bikefront.BuildConfig
import com.bikeprojectminji.bikefront.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.TimeUnit

class AiRouteWebSocketGateway(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build(),
) {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun requestPlan(
        request: AiRoutePlanRequest,
        onSuccess: (AiRoutePlanUiModel) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val webSocketRequest = Request.Builder()
            .url(webSocketUrl())
            .build()

        client.newWebSocket(webSocketRequest, object : WebSocketListener() {
            private val completed = AtomicBoolean(false)

            override fun onOpen(webSocket: WebSocket, response: Response) {
                debug("opened")
                webSocket.send(request.toJson().toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                debug("message received")
                val payload = JSONObject(text)
                when (payload.optString("type")) {
                    "plan" -> if (completed.compareAndSet(false, true)) {
                        postSuccess(payload.getJSONObject("data").toUiModel(), onSuccess)
                        webSocket.close(1000, null)
                    }
                    "error" -> if (completed.compareAndSet(false, true)) {
                        postFailure(payload.optString("message", "AI 경로를 만들지 못했습니다."), onFailure)
                        webSocket.close(1000, null)
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                debug("failed: ${t.message}")
                if (completed.compareAndSet(false, true)) {
                    postFailure(t.message ?: "AI 경로 서버에 연결하지 못했습니다.", onFailure)
                }
            }
        })
    }

    private fun webSocketUrl(): String {
        return AppConfig.API_BASE_URL
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://") + "/ws/v1/ai-routes"
    }

    private fun AiRoutePlanRequest.toJson(): JSONObject {
        return AiRoutePlanRequestJsonMapper.toJson(this)
    }

    private fun JSONObject.toUiModel(): AiRoutePlanUiModel {
        return AiRoutePlanJsonMapper.toUiModel(this)
    }

    private fun postSuccess(result: AiRoutePlanUiModel, onSuccess: (AiRoutePlanUiModel) -> Unit) {
        mainHandler.post { onSuccess(result) }
    }

    private fun postFailure(message: String, onFailure: (String) -> Unit) {
        mainHandler.post { onFailure(message) }
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("AiRouteWebSocketGateway", message)
        }
    }
}
