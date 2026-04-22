package com.bikeprojectminji.bikefront.speed;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RideSpeedFormatterTest {

    private final RideSpeedFormatter formatter = new RideSpeedFormatter();

    @Test
    public void formatReturnsLoadingStateWhenLocationMissing() {
        RideSpeedUiState state = formatter.format((RideSpeedSample) null, (RideSpeedSample) null);

        assertEquals("--", state.getSpeedText());
        assertEquals("현재 위치 신호를 기다리는 중입니다.", state.getMessage());
    }

    @Test
    public void normalizeSpeedRoundsAndZeroesSubOneKmh() {
        assertEquals(13, RideSpeedFormatter.normalizeSpeedKmh(12.96f));
        assertEquals(0, RideSpeedFormatter.normalizeSpeedKmh(0.99f));
    }

    @Test
    public void resolveAccuracyMessageShowsWeakGuidanceOnlyWhenAccuracyIsLow() {
        assertEquals("현재 위치 정보가 불안정합니다.", RideSpeedFormatter.resolveAccuracyMessage(true, 80f));
        assertEquals("", RideSpeedFormatter.resolveAccuracyMessage(true, 25f));
        assertEquals("", RideSpeedFormatter.resolveAccuracyMessage(false, 0f));
    }

    @Test
    public void formatShowsStationaryMessageWhenSpeedIsZeroWithFreshLocation() {
        RideSpeedUiState state = formatter.format(
                new RideSpeedSample(37.0, 127.0, 2_000L, true, 10f, true, 0f),
                null
        );

        assertEquals("0km/h", state.getSpeedText());
        assertEquals("정지 상태입니다.", state.getMessage());
    }

    @Test
    public void formatPrioritizesLowAccuracyMessageOverStationaryState() {
        RideSpeedUiState state = formatter.format(
                new RideSpeedSample(37.0, 127.0, 2_000L, true, 80f, true, 0f),
                null
        );

        assertEquals("0km/h", state.getSpeedText());
        assertEquals("현재 위치 정보가 불안정합니다.", state.getMessage());
    }

    @Test
    public void formatRefreshesHudSpeedWhenFreshMovingSampleArrives() {
        RideSpeedUiState waiting = formatter.format((RideSpeedSample) null, (RideSpeedSample) null);
        RideSpeedUiState moving = formatter.format(
                new RideSpeedSample(37.0, 127.0, 3_000L, true, 10f, true, 3f),
                null
        );

        assertEquals("현재 위치 신호를 기다리는 중입니다.", waiting.getMessage());
        assertEquals("11km/h", moving.getSpeedText());
        assertEquals("", moving.getMessage());
    }
}
