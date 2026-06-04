package com.bikeprojectminji.bikefront.curator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratorTravelPreferenceTest {

    @Test
    fun defaultPreferenceUsesScenicTravelDefaults() {
        val preference = CuratorTravelPreference.default()

        assertEquals(RidePurpose.SCENIC_TRAVEL, preference.ridePurpose)
        assertEquals(RoutePriority.SCENERY_FIRST, preference.routePriority)
        assertEquals(DistanceComfort.MEDIUM, preference.distanceComfort)
        assertTrue(preference.avoidConditions.isEmpty())
    }

    @Test
    fun togglesAvoidConditionWithoutDuplicatingSelection() {
        val preference = CuratorTravelPreference.default()
            .toggleAvoidCondition(AvoidCondition.TRAFFIC)
            .toggleAvoidCondition(AvoidCondition.FINE_DUST)
            .toggleAvoidCondition(AvoidCondition.TRAFFIC)

        assertFalse(preference.avoidConditions.contains(AvoidCondition.TRAFFIC))
        assertTrue(preference.avoidConditions.contains(AvoidCondition.FINE_DUST))
    }

    @Test
    fun serializesStableValuesForFutureServerContract() {
        val preference = CuratorTravelPreference(
            ridePurpose = RidePurpose.CAFE_DESTINATION,
            routePriority = RoutePriority.BIKE_PATH_FIRST,
            distanceComfort = DistanceComfort.LONG,
            avoidConditions = setOf(AvoidCondition.TRAFFIC, AvoidCondition.BAD_SURFACE),
        )

        val payload = CuratorTravelPreferencePayloadMapper.toMap(preference)

        assertEquals("CAFE_DESTINATION", payload["ridePurpose"])
        assertEquals("BIKE_PATH_FIRST", payload["routePriority"])
        assertEquals("LONG", payload["distanceComfort"])
        assertEquals("TRAFFIC,BAD_SURFACE", payload["avoidCondition"])
    }
}
