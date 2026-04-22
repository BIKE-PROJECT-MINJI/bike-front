package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapPreviewRouteCallbackDispatcherTest {

    @Test
    fun `route load failure dispatch does not emit empty route points callback`() {
        val events = mutableListOf<String>()

        MapPreviewRouteCallbackDispatcher.dispatchRouteLoadFailed(
            onRouteLoadFailed = { message -> events += "failed:$message" },
            message = "경로 로드 실패",
        )

        assertEquals(listOf("failed:경로 로드 실패"), events)
    }

    @Test
    fun `route points success dispatch emits loaded points callback`() {
        var loadedPoints: List<CourseRoutePointsGateway.RoutePoint>? = null

        val points = listOf(
            CourseRoutePointsGateway.RoutePoint(0, 37.5665, 126.9780),
            CourseRoutePointsGateway.RoutePoint(1, 37.5651, 126.98955),
        )

        MapPreviewRouteCallbackDispatcher.dispatchRoutePointsLoaded(
            onRoutePointsLoaded = { loadedPoints = it },
            points = points,
        )

        assertTrue(loadedPoints === points)
    }
}
