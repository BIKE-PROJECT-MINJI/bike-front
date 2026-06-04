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
        assertEquals("bikeoasis@example.com", session.email)
        assertEquals(1L, session.userId)
        assertEquals("email", session.loginProvider)
        assertEquals("https://example.com/me.png", session.profileImageUrl)
        assertEquals("access-token", session.accessToken)
        assertEquals("refresh-token", session.refreshToken)
        assertEquals(310_000L, session.accessTokenExpiresAtEpochMillis)
        assertEquals(1_210_000L, session.refreshTokenExpiresAtEpochMillis)
    }

    @Test
    fun `factory keeps profile metadata from profile me response`() {
        val loginResult = AuthLoginGateway.LoginResult(
            "",
            "gaja-rider",
            "access-token",
            "refresh-token",
            300L,
            1_200L,
            7L,
        )
        val profileResult = AuthLoginGateway.ProfileResult(
            7L,
            "kakao-rider@example.com",
            "카카오라이더",
            "https://example.com/kakao.png",
        )

        val session = AuthSessionFactory.create(
            loginResult,
            profileResult,
            "kakao",
            "https://example.com/fallback.png",
            10_000L,
        )

        assertEquals("카카오라이더", session.displayName)
        assertEquals("kakao-rider@example.com", session.email)
        assertEquals(7L, session.userId)
        assertEquals("kakao", session.loginProvider)
        assertEquals("https://example.com/kakao.png", session.profileImageUrl)
    }
}
