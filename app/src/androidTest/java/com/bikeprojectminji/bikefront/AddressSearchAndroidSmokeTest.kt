package com.bikeprojectminji.bikefront

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class AddressSearchAndroidSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun routeCoachShowsAddressSearchInputAndFailureState() {
        composeRule.waitUntil(timeoutMillis = 45_000) {
            composeRule.onAllNodesWithText("목적지 검색").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("주소 또는 장소명").assertIsDisplayed()
        composeRule.onNodeWithText("주소 또는 장소명").performTextInput("북악스카이웨이")
        composeRule.onNodeWithText("검색").performClick()

        composeRule.waitUntil(timeoutMillis = 30_000) {
            composeRule.onAllNodesWithText("주소 후보를 찾는 중").fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithText("목적지 검색").assertIsDisplayed()
    }
}
