package com.bikeprojectminji.bikefront.airoute

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRoutePlanPresentationTest {

    @Test
    fun buildsCuratorExplanationRowsAndEvidenceLabels() {
        val plan = AiRoutePlanJsonMapper.toUiModel(
            JSONObject(
                """
                {
                  "summary": "북악스카이웨이까지 경치 우선 경로입니다.",
                  "recommendationScore": 82,
                  "scoreBreakdown": {
                    "scenery": 91,
                    "bikePath": 78,
                    "safety": 72,
                    "condition": 68,
                    "preferenceFit": 88,
                    "distancePenalty": 4,
                    "unknownPenalty": 8
                  },
                  "explanation": {
                    "headline": "북악스카이웨이까지 경치 우선 자전거 여행길을 골랐어요.",
                    "reason": "추천점수 82점, 경치 91점, 자전거도로 78점 기준입니다.",
                    "caution": "공사/노면 정보는 일부 구간 정보 없음이라 출발 전 확인이 필요해요.",
                    "nextAction": "이 경로로 출발"
                  },
                  "evidenceBadges": [
                    {"source": "weather", "label": "날씨", "status": "VERIFIED", "severity": "INFO", "summary": "맑음"},
                    {"source": "closure", "label": "통제", "status": "WARNING", "severity": "MEDIUM", "summary": "우회 권장"},
                    {"source": "surface", "label": "노면", "status": "FAILED", "severity": "UNKNOWN", "summary": "조회 실패"},
                    {"source": "construction", "label": "공사", "status": "UNKNOWN", "severity": "UNKNOWN", "summary": "정보 없음"}
                  ],
                  "routePoints": [],
                  "risks": [],
                  "actions": [],
                  "aiGenerated": true
                }
                """.trimIndent()
            )
        )

        val presentation = AiRoutePlanPresentation.from(plan)

        assertEquals("추천점수 82", presentation.scoreLabel)
        assertEquals(listOf("경치 91", "자전거길 78", "안전 72", "조건 68"), presentation.scoreChips)
        assertEquals(listOf("날씨 확인됨", "통제 주의", "노면 확인 실패", "공사 정보 없음"), presentation.evidenceLabels)
        assertEquals("공사/노면 정보는 일부 구간 정보 없음이라 출발 전 확인이 필요해요.", presentation.cautionText)
        assertFalse(presentation.cautionText.contains("안전합니다"))
    }

    @Test
    fun keepsUnknownProviderCopyConservativeWhenEvidenceIsMissing() {
        val plan = AiRoutePlanJsonMapper.toUiModel(JSONObject("""{"summary":"기존 응답"}"""))

        val presentation = AiRoutePlanPresentation.from(plan)

        assertEquals("추천점수 대기", presentation.scoreLabel)
        assertTrue(presentation.evidenceLabels.isEmpty())
        assertEquals("일부 조건은 정보 없음 상태라 출발 전 확인이 필요해요.", presentation.cautionText)
        assertFalse(presentation.cautionText.contains("안전합니다"))
    }
}
