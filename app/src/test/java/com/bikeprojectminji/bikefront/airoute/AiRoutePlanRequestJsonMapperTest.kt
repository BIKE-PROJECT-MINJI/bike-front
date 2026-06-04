package com.bikeprojectminji.bikefront.airoute

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AiRoutePlanRequestJsonMapperTest {

    @Test
    fun `request json includes selected destination coordinates when address candidate is chosen`() {
        val payload = AiRoutePlanRequestJsonMapper.toJson(
            AiRoutePlanRequest(
                lat = 37.5665,
                lon = 126.9780,
                destinationLat = 37.6026,
                destinationLon = 126.9803,
                destinationLabel = "북악스카이웨이 팔각정",
                rideStyle = "SCENERY_FIRST",
            )
        )

        assertEquals(37.5665, payload.getDouble("lat"), 0.0)
        assertEquals(126.9780, payload.getDouble("lon"), 0.0)
        assertEquals(37.6026, payload.getDouble("destinationLat"), 0.0)
        assertEquals(126.9803, payload.getDouble("destinationLon"), 0.0)
        assertEquals("북악스카이웨이 팔각정", payload.getString("destinationLabel"))
        assertFalse(payload.has("query"))
    }
}

