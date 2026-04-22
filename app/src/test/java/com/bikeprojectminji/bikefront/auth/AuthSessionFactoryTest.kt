package com.bikeprojectminji.bikefront.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthSessionFactoryTest {

    @Test
    fun `factory converts response expiry seconds into absolute epoch millis`() {
        val loginResult = AuthLoginGateway.LoginResult(
            "bikeoasis@example.com",
            "bikeoasis",
            "access-token",
            "refresh-token",
            300L,
            1_200L,
            1L,
        )

        val session = AuthSessionFactory.create(loginResult, "https://example.com/me.png", 10_000L)

        assertEquals("bikeoasis", session.displayName)
        assertEquals("https://example.com/me.png", session.profileImageUrl)
        assertEquals("access-token", session.accessToken)
        assertEquals("refresh-token", session.refreshToken)
        assertEquals(310_000L, session.accessTokenExpiresAtEpochMillis)
        assertEquals(1_210_000L, session.refreshTokenExpiresAtEpochMillis)
    }
}
