package com.bikeprojectminji.bikefront

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.bikeprojectminji.bikefront.R
import com.bikeprojectminji.bikefront.auth.AuthProfileActivity
import org.junit.Rule
import org.junit.Test

class AuthKakaoLoginAndroidSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AuthProfileActivity>()

    @Test
    fun loginScreenShowsKakaoPolicyVersionsAndMissingKeyState() {
        composeRule.waitUntil(timeoutMillis = 45_000) {
            composeRule.onAllNodesWithText("카카오로 계속하기").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("카카오로 계속하기").assertIsDisplayed()
        composeRule.onNodeWithText("privacy-2026-05-24 · terms-2026-05-24 · location-2026-05-24").assertIsDisplayed()

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        if (targetContext.getString(R.string.kakao_native_app_key).isNotBlank()) {
            return
        }

        composeRule.onNodeWithText("카카오로 계속하기").performClick()

        composeRule.onNodeWithText("카카오 네이티브 앱 키를 설정하면 카카오 로그인을 사용할 수 있습니다.").assertIsDisplayed()
    }
}
