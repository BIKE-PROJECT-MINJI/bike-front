package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.auth.AuthLoginGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MyInfoActivitySummaryStateTest {

    @Test
    fun `profile content binds lifetime stats from shared backend dto`() {
        val state = ProfileState.Loaded(
            displayName = "bikeoasis",
            activitySummary = sampleSummary(
                totalDistanceKm = 1248.3,
                totalRides = 42,
                avgSpeedKmh = 18.5,
                totalElevationM = 0,
            ),
        )

        assertEquals("1,248.3 km", state.totalDistance)
        assertEquals("0 m", state.totalElevation)
        assertEquals("42", state.totalRides)
        assertEquals("18.5 km/h", state.avgSpeed)
    }

    @Test
    fun `empty week still preserves overall stats from same source of truth`() {
        val state = ProfileState.Loaded(
            displayName = "bikeoasis",
            activitySummary = sampleSummary(
                distanceKm = 0.0,
                rideCount = 0,
                durationMinutes = 0,
                savedCourseCount = 0,
                totalDistanceKm = 301.5,
                totalRides = 12,
                avgSpeedKmh = 22.3,
                totalElevationM = 450,
            ),
        )

        assertEquals(true, state.isWeeklySummaryEmpty)
        assertEquals("301.5 km", state.totalDistance)
        assertEquals("450 m", state.totalElevation)
    }

    @Test
    fun `signed out profile state stays explicit`() {
        val state: ProfileState = ProfileState.NotLoggedIn

        assertTrue(state is ProfileState.NotLoggedIn)
    }

    private fun sampleSummary(
        distanceKm: Double = 17.2,
        rideCount: Long = 1,
        durationMinutes: Long = 52,
        savedCourseCount: Long = 3,
        totalDistanceKm: Double = 1248.0,
        totalRides: Long = 42,
        avgSpeedKmh: Double = 18.5,
        totalElevationM: Long = 8520,
    ): AuthLoginGateway.ActivitySummaryResult {
        return AuthLoginGateway.ActivitySummaryResult(
            AuthLoginGateway.WeeklyActivitySummaryResult(distanceKm, rideCount, durationMinutes, savedCourseCount),
            AuthLoginGateway.OverallActivitySummaryResult(totalDistanceKm, totalRides, avgSpeedKmh, totalElevationM),
        )
    }
}
