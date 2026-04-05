package com.bikeprojectminji.bikefront.weather;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeatherRetentionPolicyTest {

    @Test
    public void canRetainLastSuccessWithinRetentionWindow() {
        assertTrue(WeatherRetentionPolicy.canRetainLastSuccess(true, 59L * 60L * 1000L, 60L * 60L * 1000L));
        assertTrue(WeatherRetentionPolicy.canRetainLastSuccess(true, 60L * 60L * 1000L, 60L * 60L * 1000L));
    }

    @Test
    public void cannotRetainWithoutLastSuccessOrAfterWindow() {
        assertFalse(WeatherRetentionPolicy.canRetainLastSuccess(false, 1L, 60L * 60L * 1000L));
        assertFalse(WeatherRetentionPolicy.canRetainLastSuccess(true, 60L * 60L * 1000L + 1L, 60L * 60L * 1000L));
    }
}
