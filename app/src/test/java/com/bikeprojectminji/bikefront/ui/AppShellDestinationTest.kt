package com.bikeprojectminji.bikefront.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoursesAppShellRouteTest {

    @Test
    fun `top level destinations expose separate home and courses routes`() {
        val routes = AppShellDestination.entries.map { it.route }

        assertEquals(listOf(BikeFrontRoute.RIDE_START, BikeFrontRoute.COURSES), routes)
    }

    @Test
    fun `app shell chrome only stays on top level destinations`() {
        assertTrue(BikeFrontRoute.isTopLevelDestination(BikeFrontRoute.RIDE_START))
        assertTrue(BikeFrontRoute.isTopLevelDestination(BikeFrontRoute.COURSES))
        assertFalse(BikeFrontRoute.isTopLevelDestination(BikeFrontRoute.coursePre(7L)))
        assertFalse(BikeFrontRoute.isTopLevelDestination(BikeFrontRoute.FREE_RIDE_PRE))
    }

    @Test
    fun `course pre route builder preserves selected course navigation contract`() {
        assertEquals("course_pre/42", BikeFrontRoute.coursePre(42L))
    }
}
