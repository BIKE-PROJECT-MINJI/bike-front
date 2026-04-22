package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.auth.AuthLoginGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RideStartActivitySummaryStateTest {

    @Test
    fun `real weekly summary binds dashboard values from backend dto`() {
        val state = RideStartActivitySummaryState.from(
            summary = sampleSummary(
                distanceKm = 87.4,
                rideCount = 3,
                durationMinutes = 245,
                savedCourseCount = 8,
            )
        )

        assertTrue(state is RideStartActivitySummaryState.Ready)
        state as RideStartActivitySummaryState.Ready
        assertEquals("87.4", state.primaryDistanceText)
        assertEquals("3회 주행", state.rideCountText)
        assertEquals("4.1시간", state.durationText)
        assertEquals("8개 저장", state.savedCourseText)
        assertEquals(false, state.isEmptyWeek)
    }

    @Test
    fun `empty week stays explicit instead of showing fake progress`() {
        val state = RideStartActivitySummaryState.from(summary = sampleSummary())

        assertTrue(state is RideStartActivitySummaryState.Ready)
        state as RideStartActivitySummaryState.Ready
        assertEquals("0.0", state.primaryDistanceText)
        assertEquals(true, state.isEmptyWeek)
        assertEquals("이번 주 첫 라이딩을 시작해 보세요.", state.helperText)
    }

    @Test
    fun `signed out session shows explicit summary sign in prompt`() {
        val state = RideStartActivitySummaryState.fromSignedOut()

        assertTrue(state is RideStartActivitySummaryState.SignedOut)
        state as RideStartActivitySummaryState.SignedOut
        assertEquals("로그인하면 이번 주 활동이 표시됩니다.", state.message)
    }

    @Test
    fun `api failure shows explicit dashboard fallback state`() {
        val state = RideStartActivitySummaryState.fromFailure("활동 요약을 불러오지 못했습니다.")

        assertTrue(state is RideStartActivitySummaryState.Error)
        state as RideStartActivitySummaryState.Error
        assertEquals("활동 요약을 불러오지 못했습니다.", state.message)
    }

    private fun sampleSummary(
        distanceKm: Double = 0.0,
        rideCount: Long = 0,
        durationMinutes: Long = 0,
        savedCourseCount: Long = 0,
        totalDistanceKm: Double = 1248.0,
        totalRides: Long = 42,
        avgSpeedKmh: Double = 18.5,
        totalElevationM: Long = 0,
    ): AuthLoginGateway.ActivitySummaryResult {
        return AuthLoginGateway.ActivitySummaryResult(
            AuthLoginGateway.WeeklyActivitySummaryResult(distanceKm, rideCount, durationMinutes, savedCourseCount),
            AuthLoginGateway.OverallActivitySummaryResult(totalDistanceKm, totalRides, avgSpeedKmh, totalElevationM),
        )
    }
}
