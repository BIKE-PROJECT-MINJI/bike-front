package com.bikeprojectminji.bikefront.weather;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeatherHudValueFormatterTest {

    @Test
    public void formatTemperatureReturnsCelsiusText() {
        assertEquals("12°C", WeatherHudValueFormatter.formatTemperature(12));
    }

    @Test
    public void formatWindReturnsCompassDirectionAndSpeed() {
        assertEquals("🧭 북서 14km/h", WeatherHudValueFormatter.formatWind("북서", 14));
    }

    @Test
    public void formatWindFallsBackToUnknownDirectionWhenDirectionMissing() {
        assertEquals("🧭 방향 미확인 14km/h", WeatherHudValueFormatter.formatWind("", 14));
    }

    @Test
    public void shouldShowStaleReturnsInputFlag() {
        assertTrue(WeatherHudValueFormatter.shouldShowStale(true));
        assertFalse(WeatherHudValueFormatter.shouldShowStale(false));
    }
}
