package com.bikeprojectminji.bikefront.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthResponseJsonParserTest {

    @Test
    fun `login parser reads renewable auth fields from backend wrapper`() {
        val result = AuthResponseJsonParser.parseLoginResult(
            "bikeoasis@example.com",
            """
            {
              "code": 200,
              "data": {
                "accessToken": "access-token",
                "refreshToken": "refresh-token",
                "accessExpiresInSec": 3600,
                "refreshExpiresInSec": 1209600,
                "userId": 1,
                "displayName": "bikeoasis"
              }
            }
            """.trimIndent(),
        )

        assertEquals("bikeoasis@example.com", result.email)
        assertEquals("bikeoasis", result.displayName)
        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals(3600L, result.accessExpiresInSec)
        assertEquals(1209600L, result.refreshExpiresInSec)
        assertEquals(1L, result.userId)
    }

    @Test
    fun `profile parser reads image url from profile me wrapper`() {
        val result = AuthResponseJsonParser.parseProfileResult(
            """
            {
              "code": 200,
              "data": {
                "userId": 1,
                "email": "bikeoasis@example.com",
                "displayName": "bikeoasis",
                "profileImageUrl": "https://example.com/me.png"
              }
            }
            """.trimIndent(),
        )

        assertEquals(1L, result.userId)
        assertEquals("bikeoasis@example.com", result.email)
        assertEquals("bikeoasis", result.displayName)
        assertEquals("https://example.com/me.png", result.profileImageUrl)
    }
}
