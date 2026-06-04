package com.bikeprojectminji.bikefront

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class RideStartPhysicalBackendSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeRequestsAiRouteFromPhysicalDeviceBackend() {
        composeRule.waitUntil(timeoutMillis = 45_000) {
            composeRule.onAllNodesWithText("Route Coach").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("출발 판단을 먼저 계산해요").assertIsDisplayed()
        composeRule.onNodeWithTag("ai-route-plan-button").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 60_000) {
            composeRule.onAllNodesWithText("오늘의 추천 경로").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("오늘의 추천 경로").assertIsDisplayed()
        composeRule.onAllNodesWithText("추천점수", substring = true)[0].performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("경치 70").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("자전거길 70").performScrollTo().assertIsDisplayed()
        assertWeatherEvidenceBadgeIsDisplayed()
        composeRule.onNodeWithText("공사 정보 없음").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("노면 정보 없음").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("조건 다시 계산").performScrollTo().assertIsDisplayed()
    }

    private fun assertWeatherEvidenceBadgeIsDisplayed() {
        val allowedWeatherBadges = listOf("날씨 확인됨", "날씨 주의", "날씨 정보 없음", "날씨 확인 실패")
        composeRule.waitUntil(timeoutMillis = 30_000) {
            allowedWeatherBadges.any { text ->
                composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
            }
        }
        val actual = allowedWeatherBadges.first { text ->
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(actual).performScrollTo().assertIsDisplayed()
    }
}
