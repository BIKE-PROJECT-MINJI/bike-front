package com.bikeprojectminji.bikefront.airoute

import org.json.JSONObject

data class AiRoutePlanRequest(
    val lat: Double,
    val lon: Double,
    val destinationLat: Double? = null,
    val destinationLon: Double? = null,
    val destinationLabel: String,
    val rideStyle: String,
)

data class AiRoutePlanUiModel(
    val summary: String,
    val confidence: String,
    val recommendationScore: AiRouteRecommendationScoreUiModel,
    val explanation: AiRouteExplanationUiModel,
    val evidenceBadges: List<AiRouteEvidenceBadgeUiModel>,
    val routePoints: List<AiRoutePointUiModel>,
    val risks: List<AiRouteRiskUiModel>,
    val actions: List<String>,
    val aiGenerated: Boolean,
)

data class AiRouteRecommendationScoreUiModel(
    val total: Int,
    val scenery: Int,
    val bikePath: Int,
    val safety: Int,
    val condition: Int,
    val preferenceFit: Int,
    val distancePenalty: Int,
    val unknownPenalty: Int,
)

data class AiRouteExplanationUiModel(
    val headline: String,
    val reason: String,
    val caution: String,
    val nextAction: String,
)

data class AiRouteEvidenceBadgeUiModel(
    val source: String,
    val label: String,
    val status: AiRouteEvidenceStatusUi,
    val severity: AiRouteEvidenceSeverityUi,
    val summary: String,
) {
    val statusLabel: String
        get() = status.displayText
}

enum class AiRouteEvidenceStatusUi(val displayText: String) {
    Verified("확인됨"),
    Warning("주의"),
    Failed("확인 실패"),
    Unknown("정보 없음"),
}

enum class AiRouteEvidenceSeverityUi {
    Info,
    Low,
    Medium,
    High,
    Unknown,
}

data class AiRoutePointUiModel(
    val label: String,
    val lat: Double,
    val lon: Double,
)

data class AiRouteRiskUiModel(
    val label: String,
    val severity: String,
    val summary: String,
)

object AiRoutePlanJsonMapper {

    fun toUiModel(payload: JSONObject): AiRoutePlanUiModel {
        val routePointsJson = payload.optJSONArray("routePoints")
        val risksJson = payload.optJSONArray("risks")
        val actionsJson = payload.optJSONArray("actions")
        val badgesJson = payload.optJSONArray("evidenceBadges")
        return AiRoutePlanUiModel(
            summary = payload.optString("summary", "조건 기반 경로를 준비했습니다."),
            confidence = payload.optString("confidence", "low"),
            recommendationScore = mapScore(payload),
            explanation = mapExplanation(payload.optJSONObject("explanation")),
            evidenceBadges = buildList {
                if (badgesJson != null) {
                    for (index in 0 until badgesJson.length()) {
                        val item = badgesJson.optJSONObject(index) ?: continue
                        add(mapEvidenceBadge(item))
                    }
                }
            },
            routePoints = buildList {
                if (routePointsJson != null) {
                    for (index in 0 until routePointsJson.length()) {
                        val item = routePointsJson.optJSONObject(index) ?: continue
                        add(
                            AiRoutePointUiModel(
                                label = item.optString("label", "경유지"),
                                lat = item.optDouble("lat", 0.0),
                                lon = item.optDouble("lon", 0.0),
                            ),
                        )
                    }
                }
            },
            risks = buildList {
                if (risksJson != null) {
                    for (index in 0 until risksJson.length()) {
                        val item = risksJson.optJSONObject(index) ?: continue
                        add(
                            AiRouteRiskUiModel(
                                label = item.optString("label", "확인 필요"),
                                severity = item.optString("severity", "unknown"),
                                summary = item.optString("summary", ""),
                            ),
                        )
                    }
                }
            },
            actions = buildList {
                if (actionsJson != null) {
                    for (index in 0 until actionsJson.length()) {
                        add(actionsJson.optString(index))
                    }
                }
            }.filter { it.isNotBlank() },
            aiGenerated = payload.optBoolean("aiGenerated", false),
        )
    }

    private fun mapScore(payload: JSONObject): AiRouteRecommendationScoreUiModel {
        val breakdown = payload.optJSONObject("scoreBreakdown")
        return AiRouteRecommendationScoreUiModel(
            total = payload.optInt("recommendationScore", breakdown?.optInt("total", 0) ?: 0).coerceIn(0, 100),
            scenery = breakdown?.optInt("scenery", 0)?.coerceIn(0, 100) ?: 0,
            bikePath = breakdown?.optInt("bikePath", 0)?.coerceIn(0, 100) ?: 0,
            safety = breakdown?.optInt("safety", 0)?.coerceIn(0, 100) ?: 0,
            condition = breakdown?.optInt("condition", 0)?.coerceIn(0, 100) ?: 0,
            preferenceFit = breakdown?.optInt("preferenceFit", 0)?.coerceIn(0, 100) ?: 0,
            distancePenalty = breakdown?.optInt("distancePenalty", 0)?.coerceAtLeast(0) ?: 0,
            unknownPenalty = breakdown?.optInt("unknownPenalty", 0)?.coerceAtLeast(0) ?: 0,
        )
    }

    private fun mapExplanation(payload: JSONObject?): AiRouteExplanationUiModel {
        return AiRouteExplanationUiModel(
            headline = payload?.optString("headline")?.takeIf { it.isNotBlank() } ?: "조건 기반 경로를 준비했습니다.",
            reason = payload?.optString("reason")?.takeIf { it.isNotBlank() } ?: "경로 조건을 확인해 추천했어요.",
            caution = payload?.optString("caution")?.takeIf { it.isNotBlank() } ?: "일부 조건은 정보 없음 상태라 출발 전 확인이 필요해요.",
            nextAction = payload?.optString("nextAction")?.takeIf { it.isNotBlank() } ?: "이 경로로 출발",
        )
    }

    private fun mapEvidenceBadge(payload: JSONObject): AiRouteEvidenceBadgeUiModel {
        return AiRouteEvidenceBadgeUiModel(
            source = payload.optString("source", ""),
            label = payload.optString("label", "근거"),
            status = mapStatus(payload.optString("status", "UNKNOWN")),
            severity = mapSeverity(payload.optString("severity", "UNKNOWN")),
            summary = payload.optString("summary", ""),
        )
    }

    private fun mapStatus(value: String): AiRouteEvidenceStatusUi {
        return when (value.uppercase()) {
            "VERIFIED" -> AiRouteEvidenceStatusUi.Verified
            "WARNING" -> AiRouteEvidenceStatusUi.Warning
            "FAILED" -> AiRouteEvidenceStatusUi.Failed
            else -> AiRouteEvidenceStatusUi.Unknown
        }
    }

    private fun mapSeverity(value: String): AiRouteEvidenceSeverityUi {
        return when (value.uppercase()) {
            "INFO" -> AiRouteEvidenceSeverityUi.Info
            "LOW" -> AiRouteEvidenceSeverityUi.Low
            "MEDIUM" -> AiRouteEvidenceSeverityUi.Medium
            "HIGH" -> AiRouteEvidenceSeverityUi.High
            else -> AiRouteEvidenceSeverityUi.Unknown
        }
    }
}

object AiRoutePlanRequestJsonMapper {

    fun toJson(request: AiRoutePlanRequest): JSONObject {
        return JSONObject()
            .put("lat", request.lat)
            .put("lon", request.lon)
            .put("destinationLabel", request.destinationLabel)
            .put("rideStyle", request.rideStyle)
            .apply {
                request.destinationLat?.let { put("destinationLat", it) }
                request.destinationLon?.let { put("destinationLon", it) }
            }
    }
}
