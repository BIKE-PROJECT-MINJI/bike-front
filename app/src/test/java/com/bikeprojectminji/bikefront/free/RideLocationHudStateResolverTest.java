package com.bikeprojectminji.bikefront.free;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class RideLocationHudStateResolverTest {

    @Test
    public void resolveReturnsPermissionStateWhenPermissionDenied() {
        RideLocationHudState state = RideLocationHudStateResolver.resolve(
                false,
                false,
                false,
                "위치 권한이 필요합니다.",
                "현재 위치를 확인하는 중입니다.",
                "현재 위치 정보가 불안정합니다."
        );

        assertEquals("권한 필요", state.getValue());
        assertNull(state.getMessage());
    }

    @Test
    public void resolveReturnsLoadingStateBeforeLocationFix() {
        RideLocationHudState state = RideLocationHudStateResolver.resolve(
                true,
                false,
                false,
                "위치 권한이 필요합니다.",
                "현재 위치를 확인하는 중입니다.",
                "현재 위치 정보가 불안정합니다."
        );

        assertEquals("확인 중", state.getValue());
        assertEquals("현재 위치를 확인하는 중입니다.", state.getMessage());
    }

    @Test
    public void resolveShowsQualityGuidanceWithoutCoordinates() {
        RideLocationHudState state = RideLocationHudStateResolver.resolve(
                true,
                true,
                true,
                "위치 권한이 필요합니다.",
                "현재 위치를 확인하는 중입니다.",
                "현재 위치 정보가 불안정합니다."
        );

        assertEquals("위치 확보됨", state.getValue());
        assertEquals("현재 위치 정보가 불안정합니다.", state.getMessage());
    }

    @Test
    public void resolveReturnsStableStateWhenLocationIsHealthy() {
        RideLocationHudState state = RideLocationHudStateResolver.resolve(
                true,
                true,
                false,
                "위치 권한이 필요합니다.",
                "현재 위치를 확인하는 중입니다.",
                "현재 위치 정보가 불안정합니다."
        );

        assertEquals("위치 확보됨", state.getValue());
        assertNull(state.getMessage());
    }
}
