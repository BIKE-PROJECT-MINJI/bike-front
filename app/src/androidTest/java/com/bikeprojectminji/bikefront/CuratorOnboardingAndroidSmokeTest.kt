package com.bikeprojectminji.bikefront

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.bikeprojectminji.bikefront.curator.CuratorOnboardingActivity
import com.bikeprojectminji.bikefront.curator.CuratorTravelPreferenceStore
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CuratorOnboardingAndroidSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<CuratorOnboardingActivity>()

    @Before
    fun clearPreference() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        CuratorTravelPreferenceStore(context).clear()
    }

    @Test
    fun collectsCuratorPreferenceAndSurvivesActivityRecreation() {
        composeRule.onNodeWithText("자전거 여행 취향").assertIsDisplayed()
        captureScreenshot("onboarding-light.png")
        composeRule.onNodeWithText("카페/목적지").performClick()
        composeRule.onNodeWithText("자전거도로 우선").performClick()
        composeRule.onNodeWithText("다음").performClick()

        composeRule.onNodeWithText("편한 거리와 피하고 싶은 조건").assertIsDisplayed()
        composeRule.onNodeWithText("길게").performClick()
        composeRule.onNodeWithText("차 많은 길").performClick()
        composeRule.onNodeWithText("미세먼지").performClick()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("편한 거리와 피하고 싶은 조건").assertIsDisplayed()
        composeRule.onNodeWithText("다음").performClick()

        composeRule.onNodeWithText("선호 경로 준비 완료").assertIsDisplayed()
        composeRule.onNodeWithText("카페/목적지").assertIsDisplayed()
        composeRule.onNodeWithText("자전거도로 우선").assertIsDisplayed()
        composeRule.onNodeWithText("길게").assertIsDisplayed()
        composeRule.onNodeWithText("차 많은 길", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("미세먼지", substring = true).performScrollTo().assertIsDisplayed()
        setNightMode(true)
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("선호 경로 준비 완료").assertIsDisplayed()
        captureScreenshot("onboarding-dark.png")
        setNightMode(false)
        composeRule.onNodeWithText("홈으로 가기").performClick()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(CuratorTravelPreferenceStore(context).isCompleted())
    }

    private fun captureScreenshot(filename: String) {
        InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand("screencap -p /sdcard/$filename")
            .close()
    }

    private fun setNightMode(enabled: Boolean) {
        val command = if (enabled) "cmd uimode night yes" else "cmd uimode night no"
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).close()
    }
}
