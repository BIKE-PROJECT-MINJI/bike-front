package com.bikeprojectminji.bikefront.free

import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyEvaluationGateway
import com.bikeprojectminji.bikefront.ridepolicy.RidePolicyUiModel
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

    @Test
    fun `loop completion stays blocked at start until rider leaves start zone and returns`() {
        val controller = FreeRideTrackingController(startedAtElapsedRealtimeMillis = 1_000L)
        val routePoints = loopRoutePoints()

        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0000, longitude = 127.0000))
        controller.updateActivePolicyResult(matchedActivePolicyResult(), activePolicyUiState())

        assertFalse(controller.courseCompletionCheck(routePoints)!!.eligible)

        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0030, longitude = 127.0000))
        assertFalse(controller.courseCompletionCheck(routePoints)!!.eligible)

        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0001, longitude = 127.0001))
        assertTrue(controller.courseCompletionCheck(routePoints)!!.eligible)
    }

    @Test
    fun `loop completion remains blocked when active policy match quality is not trusted`() {
        val controller = FreeRideTrackingController(startedAtElapsedRealtimeMillis = 1_000L)
        val routePoints = loopRoutePoints()

        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0000, longitude = 127.0000))
        controller.updateActivePolicyResult(unmatchedActivePolicyResult(), activePolicyUiState())
        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0030, longitude = 127.0000))
        controller.courseCompletionCheck(routePoints)

        controller.onLocationSample(FreeRideLocationSample(latitude = 37.0001, longitude = 127.0001))

        assertFalse(controller.courseCompletionCheck(routePoints)!!.eligible)
    }

    private fun loopRoutePoints(): List<CourseRoutePointsGateway.RoutePoint> {
        return listOf(
            CourseRoutePointsGateway.RoutePoint(1, 37.0000, 127.0000),
            CourseRoutePointsGateway.RoutePoint(2, 37.0030, 127.0000),
            CourseRoutePointsGateway.RoutePoint(3, 37.0002, 127.0001),
        )
    }

    private fun matchedActivePolicyResult(): RidePolicyEvaluationGateway.EvaluationResult {
        return RidePolicyEvaluationGateway.EvaluationResult(
            "ACTIVE",
            RidePolicyEvaluationGateway.GateResult("UNDETERMINED", "UNKNOWN", Double.NaN, Double.NaN),
            RidePolicyEvaluationGateway.GateResult("ON_ROUTE", "WITHIN_ROUTE_THRESHOLD", Double.NaN, Double.NaN),
            "MATCHED",
            "현재 코스를 따라 주행 중입니다.",
        )
    }

    private fun unmatchedActivePolicyResult(): RidePolicyEvaluationGateway.EvaluationResult {
        return RidePolicyEvaluationGateway.EvaluationResult(
            "ACTIVE",
            RidePolicyEvaluationGateway.GateResult("UNDETERMINED", "UNKNOWN", Double.NaN, Double.NaN),
            RidePolicyEvaluationGateway.GateResult("UNDETERMINED", "COURSE_PATH_INVALID", Double.NaN, Double.NaN),
            "UNDETERMINED",
            "현재 위치를 다시 확인해 주세요.",
        )
    }

    private fun activePolicyUiState(): RidePolicyUiModel {
        return RidePolicyUiModel("주행 중", "현재 코스를 따라 주행 중입니다.", "", false, "", 0, 0, 0)
    }
}
