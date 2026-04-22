package com.bikeprojectminji.bikefront.free

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeRideTrackingControllerTest {

    @Test
    fun `pause stops elapsed accumulation and tracked points until resumed`() {
        val controller = FreeRideTrackingController(startedAtElapsedRealtimeMillis = 1_000L)

        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0, longitude = 127.0))
        controller.pauseTracking(nowElapsedRealtimeMillis = 11_000L)
        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0005, longitude = 127.0005))

        assertFalse(controller.isTrackingActive)
        assertEquals(1, controller.trackedPoints.size)
        assertEquals(10_000L, controller.activeElapsedMillis(nowElapsedRealtimeMillis = 20_000L))

        controller.resumeTracking(nowElapsedRealtimeMillis = 21_000L)
        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0010, longitude = 127.0010))

        assertTrue(controller.isTrackingActive)
        assertEquals(2, controller.trackedPoints.size)
        assertEquals(11_000L, controller.activeElapsedMillis(nowElapsedRealtimeMillis = 22_000L))
    }
}
