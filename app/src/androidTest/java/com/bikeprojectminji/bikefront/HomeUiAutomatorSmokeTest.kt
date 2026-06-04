package com.bikeprojectminji.bikefront

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeUiAutomatorSmokeTest {

    @Test
    fun launchAppShowsRouteCoachEntryPoint() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: error("앱 launch intent를 찾을 수 없습니다.")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launchIntent)

        assertTrue(
            "앱 프로세스가 foreground에 올라와야 합니다.",
            device.wait(Until.hasObject(By.pkg(context.packageName)), 45_000)
        )
        assertTrue(
            "Compose 홈의 Route Coach 섹션이 UIAutomator 트리에 노출되어야 합니다.",
            device.wait(Until.hasObject(By.textContains("Route Coach")), 45_000)
        )
        assertTrue(
            "목적지 검색 진입점이 UIAutomator 트리에 노출되어야 합니다.",
            device.hasObject(By.textContains("목적지 검색")) || device.hasObject(By.textContains("주소 또는 장소명"))
        )
    }
}
