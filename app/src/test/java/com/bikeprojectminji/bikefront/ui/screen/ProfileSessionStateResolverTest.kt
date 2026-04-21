package com.bikeprojectminji.bikefront.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSessionStateResolverTest {

    @Test
    fun `signed out session stays logged out`() {
        val snapshot = ProfileSessionStateResolver.resolve(
            isSignedIn = false,
            displayName = "bikeoasis"
        )

        assertFalse(snapshot.loggedIn)
        assertEquals(null, snapshot.displayName)
    }

    @Test
    fun `signed in session keeps saved display name`() {
        val snapshot = ProfileSessionStateResolver.resolve(
            isSignedIn = true,
            displayName = "bikeoasis"
        )

        assertTrue(snapshot.loggedIn)
        assertEquals("bikeoasis", snapshot.displayName)
    }

    @Test
    fun `signed in session falls back to default display name when blank`() {
        val snapshot = ProfileSessionStateResolver.resolve(
            isSignedIn = true,
            displayName = ""
        )

        assertTrue(snapshot.loggedIn)
        assertEquals("bikeoasis", snapshot.displayName)
    }
}
