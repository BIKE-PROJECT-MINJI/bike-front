package com.bikeprojectminji.bikefront.weather;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.HttpURLConnection;
import org.junit.Test;

public class WeatherHttpErrorPolicyTest {

    @Test
    public void shouldTreatNotFoundAsEmpty() {
        assertTrue(WeatherHttpErrorPolicy.shouldTreatAsEmpty(HttpURLConnection.HTTP_NOT_FOUND));
    }

    @Test
    public void shouldNotTreatServerErrorAsEmpty() {
        assertFalse(WeatherHttpErrorPolicy.shouldTreatAsEmpty(HttpURLConnection.HTTP_INTERNAL_ERROR));
    }

    @Test
    public void resolveFailureMessageFallsBackWhenBackendMessageIsBlank() {
        assertEquals(
                "날씨 정보를 불러오지 못했습니다.",
                WeatherHttpErrorPolicy.resolveFailureMessage(" ", "날씨 정보를 불러오지 못했습니다.")
        );
    }
}
