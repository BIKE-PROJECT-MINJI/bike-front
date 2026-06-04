package com.bikeprojectminji.bikefront.airoute

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRoutePlanUiModelMapperAndroidTest {

    @Test
    fun mapsScoreExplanationAndEvidenceBadgesFromBackendResponse() {
        val result = AiRoutePlanJsonMapper.toUiModel(
            JSONObject(
                """
                {
                  "summary": "강변 자전거길 위주 추천",
                  "confidence": "high",
                  "recommendationScore": 82,
                  "scoreBreakdown": {
                    "scenery": 78,
                    "bikePath": 84,
                    "safety": 76,
                    "condition": 72,
                    "preferenceFit": 88,
                    "distancePenalty": 4,
                    "unknownPenalty": 8
                  },
                  "explanation": {
                    "headline": "강변 자전거길 위주로 편하게 갈 수 있어요.",
                    "reason": "경치 78, 자전거도로 84 기준으로 골랐어요.",
                    "caution": "공사/노면 정보는 일부 구간 정보 없음이라 출발 전 확인이 필요해요.",
                    "nextAction": "이 경로로 출발"
                  },
                  "evidenceBadges": [
                    {
                      "source": "weather",
                      "label": "날씨",
                      "status": "VERIFIED",
                      "severity": "INFO",
                      "summary": "맑음, 북동풍 21km/h"
                    }
                  ],
                  "routePoints": [],
                  "risks": [],
                  "actions": ["이 경로로 출발"],
                  "aiGenerated": true
                }
                """.trimIndent()
            )
        )

        assertEquals(82, result.recommendationScore.total)
        assertEquals(78, result.recommendationScore.scenery)
        assertEquals("강변 자전거길 위주로 편하게 갈 수 있어요.", result.explanation.headline)
        assertEquals("이 경로로 출발", result.explanation.nextAction)
        assertEquals(1, result.evidenceBadges.size)
        assertEquals(AiRouteEvidenceStatusUi.Verified, result.evidenceBadges[0].status)
        assertEquals("확인됨", result.evidenceBadges[0].statusLabel)
        assertTrue(result.aiGenerated)
    }

    @Test
    fun usesSafeFallbackCopyWhenNewCuratorFieldsAreMissing() {
        val result = AiRoutePlanJsonMapper.toUiModel(
            JSONObject(
                """
                {
                  "summary": "기존 응답",
                  "confidence": "low",
                  "routePoints": [],
                  "risks": [],
                  "actions": [],
                  "aiGenerated": false
                }
                """.trimIndent()
            )
        )

        assertEquals(0, result.recommendationScore.total)
        assertEquals("조건 기반 경로를 준비했습니다.", result.explanation.headline)
        assertEquals("경로 조건을 확인해 추천했어요.", result.explanation.reason)
        assertEquals("일부 조건은 정보 없음 상태라 출발 전 확인이 필요해요.", result.explanation.caution)
        assertEquals("이 경로로 출발", result.explanation.nextAction)
        assertTrue(result.evidenceBadges.isEmpty())
    }

    @Test
    fun mapsUnknownAndFailedEvidenceToUserSafeLabels() {
        val result = AiRoutePlanJsonMapper.toUiModel(
            JSONObject(
                """
                {
                  "evidenceBadges": [
                    {
                      "source": "construction",
                      "label": "공사",
                      "status": "UNKNOWN",
                      "severity": "UNKNOWN",
                      "summary": "공사 정보 미확인"
                    },
                    {
                      "source": "surface",
                      "label": "노면",
                      "status": "FAILED",
                      "severity": "UNKNOWN",
                      "summary": "노면 조회 실패"
                    }
                  ]
                }
                """.trimIndent()
            )
        )

        assertEquals(AiRouteEvidenceStatusUi.Unknown, result.evidenceBadges[0].status)
        assertEquals("정보 없음", result.evidenceBadges[0].statusLabel)
        assertEquals(AiRouteEvidenceStatusUi.Failed, result.evidenceBadges[1].status)
        assertEquals("확인 실패", result.evidenceBadges[1].statusLabel)
    }
}
