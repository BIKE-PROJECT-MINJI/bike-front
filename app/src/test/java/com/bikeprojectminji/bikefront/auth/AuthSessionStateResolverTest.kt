package com.bikeprojectminji.bikefront.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionStateResolverTest {

    @Test
    fun `refresh token alive keeps session signed in even when access token expired`() {
        val session = AuthSession(
            "bikeoasis",
            "https://example.com/me.png",
            "expired-access",
            "refresh-token",
            9_000L,
            20_000L,
        )

        val state = AuthSessionStateResolver.resolve(session, 10_000L)

        assertTrue(state.isSignedIn)
        assertTrue(state.isNeedsRefresh)
        assertFalse(state.isRefreshExpired)
        assertFalse(state.isHasUsableAccessToken)
    }

    @Test
    fun `expired refresh token signs the user out`() {
        val session = AuthSession(
            "bikeoasis",
            "",
            "expired-access",
            "expired-refresh",
            9_000L,
            9_999L,
        )

        val state = AuthSessionStateResolver.resolve(session, 10_000L)

        assertFalse(state.isSignedIn)
        assertTrue(state.isRefreshExpired)
        assertFalse(state.isHasUsableAccessToken)
        assertFalse(state.isNeedsRefresh)
    }

    @Test
    fun `valid access token stays immediately usable`() {
        val session = AuthSession(
            "bikeoasis",
            "",
            "access-token",
            "refresh-token",
            15_000L,
            20_000L,
        )

        val state = AuthSessionStateResolver.resolve(session, 10_000L)

        assertTrue(state.isSignedIn)
        assertTrue(state.isHasUsableAccessToken)
        assertFalse(state.isNeedsRefresh)
        assertFalse(state.isRefreshExpired)
    }
}
