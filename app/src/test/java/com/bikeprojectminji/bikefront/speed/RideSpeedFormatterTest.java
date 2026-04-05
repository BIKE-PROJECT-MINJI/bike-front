package com.bikeprojectminji.bikefront.speed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.location.Location;
import org.junit.Test;

public class RideSpeedFormatterTest {

    private final RideSpeedFormatter formatter = new RideSpeedFormatter();

    @Test
    public void formatReturnsLoadingStateWhenLocationMissing() {
        RideSpeedUiState state = formatter.format(null, null);

        assertEquals("--", state.getSpeedText());
        assertEquals("현재 위치를 확인하는 중입니다.", state.getMessage());
    }

    @Test
    public void normalizeSpeedRoundsAndZeroesSubOneKmh() {
        assertEquals(13, RideSpeedFormatter.normalizeSpeedKmh(12.96f));
        assertEquals(0, RideSpeedFormatter.normalizeSpeedKmh(0.99f));
    }

    @Test
    public void resolveAccuracyMessageShowsWeakGuidanceOnlyWhenAccuracyIsLow() {
        assertEquals("현재 위치 정보가 불안정합니다.", RideSpeedFormatter.resolveAccuracyMessage(true, 80f));
        assertNull(RideSpeedFormatter.resolveAccuracyMessage(true, 25f));
        assertNull(RideSpeedFormatter.resolveAccuracyMessage(false, 0f));
    }
}
