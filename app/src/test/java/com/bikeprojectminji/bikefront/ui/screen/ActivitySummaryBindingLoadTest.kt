package com.bikeprojectminji.bikefront.ui.screen

import com.bikeprojectminji.bikefront.auth.AuthLoginGateway
import com.bikeprojectminji.bikefront.auth.AuthSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivitySummaryBindingLoadTest {

    @Test
    fun `expired access token refreshes renewable session before loading summary`() {
        val sessionPort = FakeActivitySummarySessionPort(
            signedIn = true,
            accessToken = "",
            refreshToken = "refresh-token",
            refreshExpired = false,
            displayName = "bikeoasis",
            profileImageUrl = "https://example.com/me.png",
        )
        val expectedSummary = sampleSummary(distanceKm = 54.3, rideCount = 2)
        val gateway = FakeAuthLoginGateway(
            refreshResult = AuthLoginGateway.LoginResult(
                "bikeoasis@example.com",
                "bikeoasis",
                "new-access-token",
                "new-refresh-token",
                900,
                1209600,
                1L,
            ),
            summaryResult = expectedSummary,
        )

        var captured: ActivitySummaryLoadResult? = null

        loadActivitySummary(
            sessionPort = sessionPort,
            gateway = gateway,
            currentTimeMillisProvider = { 1_000L },
        ) { captured = it }

        assertEquals(listOf("refresh-token"), gateway.refreshRequests)
        assertEquals(listOf("new-access-token"), gateway.summaryRequests)
        assertNotNull(sessionPort.savedSession)
        assertEquals("new-access-token", sessionPort.savedSession?.accessToken)
        assertEquals("new-refresh-token", sessionPort.savedSession?.refreshToken)
        assertFalse(sessionPort.cleared)
        assertTrue(captured is ActivitySummaryLoadResult.Success)
        val success = captured as ActivitySummaryLoadResult.Success
        assertEquals(expectedSummary.weeklySummary.distanceKm, success.summary.weeklySummary.distanceKm, 0.0)
    }

    @Test
    fun `renewable session retries once when protected summary call fails first`() {
        val sessionPort = FakeActivitySummarySessionPort(
            signedIn = true,
            accessToken = "stale-access-token",
            refreshToken = "refresh-token",
            refreshExpired = false,
            displayName = "bikeoasis",
            profileImageUrl = "",
        )
        val expectedSummary = sampleSummary(distanceKm = 88.1, rideCount = 4)
        val gateway = FakeAuthLoginGateway(
            refreshResult = AuthLoginGateway.LoginResult(
                "bikeoasis@example.com",
                "bikeoasis",
                "renewed-access-token",
                "renewed-refresh-token",
                900,
                1209600,
                1L,
            ),
            summaryResult = expectedSummary,
            failFirstSummaryRequest = true,
        )

        var captured: ActivitySummaryLoadResult? = null

        loadActivitySummary(
            sessionPort = sessionPort,
            gateway = gateway,
            currentTimeMillisProvider = { 2_000L },
        ) { captured = it }

        assertEquals(listOf("stale-access-token", "renewed-access-token"), gateway.summaryRequests)
        assertEquals(listOf("refresh-token"), gateway.refreshRequests)
        assertFalse(sessionPort.cleared)
        assertTrue(captured is ActivitySummaryLoadResult.Success)
        assertEquals("renewed-access-token", sessionPort.savedSession?.accessToken)
    }

    @Test
    fun `generic summary failure keeps renewable session without refresh retry`() {
        val sessionPort = FakeActivitySummarySessionPort(
            signedIn = true,
            accessToken = "still-valid-access-token",
            refreshToken = "refresh-token",
            refreshExpired = false,
            displayName = "bikeoasis",
            profileImageUrl = "",
        )
        val gateway = FakeAuthLoginGateway(
            refreshResult = AuthLoginGateway.LoginResult(
                "bikeoasis@example.com",
                "bikeoasis",
                "renewed-access-token",
                "renewed-refresh-token",
                900,
                1209600,
                1L,
            ),
            summaryResult = sampleSummary(),
            failureMessage = "네트워크 오류",
        )

        var captured: ActivitySummaryLoadResult? = null

        loadActivitySummary(
            sessionPort = sessionPort,
            gateway = gateway,
            currentTimeMillisProvider = { 3_000L },
        ) { captured = it }

        assertEquals(listOf("still-valid-access-token"), gateway.summaryRequests)
        assertTrue(gateway.refreshRequests.isEmpty())
        assertTrue(sessionPort.savedSession == null)
        assertFalse(sessionPort.cleared)
        assertEquals(ActivitySummaryLoadResult.Failure("네트워크 오류"), captured)
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

private class FakeActivitySummarySessionPort(
    private val signedIn: Boolean,
    private var accessToken: String,
    private var refreshToken: String,
    private val refreshExpired: Boolean,
    private val displayName: String,
    private val profileImageUrl: String,
) : ActivitySummarySessionPort {
    var savedSession: AuthSession? = null
        private set
    var cleared: Boolean = false
        private set

    override fun isSignedIn(): Boolean = signedIn

    override fun accessToken(): String = accessToken

    override fun refreshToken(): String = refreshToken

    override fun isRefreshExpired(): Boolean = refreshExpired

    override fun displayName(): String = displayName

    override fun profileImageUrl(): String = profileImageUrl

    override fun saveSession(session: AuthSession) {
        savedSession = session
        accessToken = session.accessToken
        refreshToken = session.refreshToken
    }

    override fun clear() {
        cleared = true
        accessToken = ""
        refreshToken = ""
    }
}

private class FakeAuthLoginGateway(
    private val refreshResult: AuthLoginGateway.LoginResult,
    private val summaryResult: AuthLoginGateway.ActivitySummaryResult,
    private val failFirstSummaryRequest: Boolean = false,
    private val failureMessage: String? = null,
) : AuthLoginGateway {
    val refreshRequests = mutableListOf<String>()
    val summaryRequests = mutableListOf<String>()
    private var summaryAttemptCount: Int = 0

    override fun register(email: String, password: String, displayName: String, callback: AuthLoginGateway.Callback) {
        throw UnsupportedOperationException("not used in this test")
    }

    override fun login(email: String, password: String, callback: AuthLoginGateway.Callback) {
        throw UnsupportedOperationException("not used in this test")
    }

    override fun kakaoLogin(kakaoAccessToken: String, callback: AuthLoginGateway.Callback) {
        throw UnsupportedOperationException("not used in this test")
    }

    override fun refresh(refreshToken: String, callback: AuthLoginGateway.Callback) {
        refreshRequests += refreshToken
        callback.onSuccess(refreshResult)
    }

    override fun getMyProfile(accessToken: String, callback: AuthLoginGateway.ProfileCallback) {
        throw UnsupportedOperationException("not used in this test")
    }

    override fun getMyActivitySummary(accessToken: String, callback: AuthLoginGateway.ActivitySummaryCallback) {
        summaryRequests += accessToken
        summaryAttemptCount += 1
        if (failureMessage != null) {
            callback.onFailure(failureMessage)
            return
        }
        if (failFirstSummaryRequest && summaryAttemptCount == 1) {
            callback.onFailure("로그인 정보가 필요합니다.")
            return
        }
        callback.onSuccess(summaryResult)
    }
}
