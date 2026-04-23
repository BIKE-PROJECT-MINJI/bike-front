package com.bikeprojectminji.bikefront.free

import com.bikeprojectminji.bikefront.ride.RideRecordGateway
import com.bikeprojectminji.bikefront.ridemap.CourseRoutePointsGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeRideSaveCoordinatorTest {

    @Test
    fun `prepare blocks when access token is blank`() {
        val result = FreeRideSaveCoordinator.prepare(
            accessToken = "",
            trackedPoints = emptyList(),
            routePoints = emptyList(),
            distanceMeters = 0,
            startedAtMillis = 1000L,
            endedAtMillis = 2000L,
        )

        assertTrue(result is FreeRideSavePreparation.Blocked)
        assertEquals("로그인이 필요합니다.", (result as FreeRideSavePreparation.Blocked).message)
        assertTrue(result.requiresAuth)
    }

    @Test
    fun `prepare uses route points when tracked points are missing`() {
        val result = FreeRideSaveCoordinator.prepare(
            accessToken = "token",
            trackedPoints = emptyList(),
            routePoints = listOf(CourseRoutePointsGateway.RoutePoint(0, 37.0, 127.0)),
            distanceMeters = 1200,
            startedAtMillis = 1_000L,
            endedAtMillis = 121_000L,
        )

        assertTrue(result is FreeRideSavePreparation.Ready)
        val ready = result as FreeRideSavePreparation.Ready
        assertEquals(1, ready.draft.routePoints.size)
        assertEquals(1.2, ready.distanceKm, 0.0)
        assertEquals(2, ready.durationMinutes)
    }

    @Test
    fun `prepare uses active duration when paused time should be excluded`() {
        val result = FreeRideSaveCoordinator.prepare(
            accessToken = "token",
            trackedPoints = listOf(RideRecordGateway.RideRecordPoint(0, 37.0, 127.0)),
            routePoints = emptyList(),
            distanceMeters = 1200,
            startedAtMillis = 1_000L,
            endedAtMillis = 121_000L,
            activeDurationMillis = 61_000L,
        )

        assertTrue(result is FreeRideSavePreparation.Ready)
        val ready = result as FreeRideSavePreparation.Ready
        assertEquals(1, ready.durationMinutes)
        assertEquals(61, ready.draft.durationSec)
    }

    @Test
    fun `short ride duration under ten seconds is blocked before save request`() {
        assertTrue(FreeRideSaveCoordinator.isShortRideDuration(9))
    }

    @Test
    fun `ten second duration is not treated as short ride`() {
        assertTrue(!FreeRideSaveCoordinator.isShortRideDuration(10))
    }

}
