package com.bikeprojectminji.bikefront.free

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeRideSaveFailureResolverTest {

    @Test
    fun `exact short ride backend message maps to dedicated exit dialog`() {
        val state = FreeRideSaveFailureResolver.resolve(
            message = "주행 시작 후 10초 미만 기록은 저장되지 않습니다.",
            fallbackMessage = "주행 기록을 저장하지 못했습니다."
        )

        assertTrue(state is FreeRideSaveFailureUiState.ShortRide)
        assertEquals(
            "주행 시작 후 10초 미만 기록은 저장되지 않습니다.",
            (state as FreeRideSaveFailureUiState.ShortRide).message
        )
    }

    @Test
    fun `other save failures stay on generic processing failure path`() {
        val state = FreeRideSaveFailureResolver.resolve(
            message = "주행 기록을 저장하지 못했습니다. 서버 상태를 다시 확인해 주세요.",
            fallbackMessage = "주행 기록을 저장하지 못했습니다."
        )

        assertTrue(state is FreeRideSaveFailureUiState.Generic)
        assertEquals(
            "주행 기록을 저장하지 못했습니다. 서버 상태를 다시 확인해 주세요.",
            (state as FreeRideSaveFailureUiState.Generic).message
        )
    }

    @Test
    fun `blank failure message falls back to generic default message`() {
        val state = FreeRideSaveFailureResolver.resolve(
            message = "   ",
            fallbackMessage = "주행 기록을 저장하지 못했습니다."
        )

        assertTrue(state is FreeRideSaveFailureUiState.Generic)
        assertEquals("주행 기록을 저장하지 못했습니다.", (state as FreeRideSaveFailureUiState.Generic).message)
    }
}
