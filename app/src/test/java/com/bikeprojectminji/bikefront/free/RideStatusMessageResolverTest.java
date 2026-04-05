package com.bikeprojectminji.bikefront.free;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RideStatusMessageResolverTest {

    @Test
    public void resolvePrefersPolicyBannerOverEverythingElse() {
        String result = RideStatusMessageResolver.resolve(
                "자유 주행을 시작할 준비가 되었습니다.",
                "위치 정보를 다시 확인해 주세요.",
                "현재 위치를 확인하는 중입니다.",
                "현재 위치 정보가 불안정합니다.",
                "현재 속도를 표시합니다.",
                "갱신 지연",
                "현재 기온 정보입니다."
        );

        assertEquals("위치 정보를 다시 확인해 주세요.", result);
    }

    @Test
    public void resolvePrefersLocationQualityOverWeatherStateWhenNoPolicyMessage() {
        String result = RideStatusMessageResolver.resolve(
                "자유 주행을 시작할 준비가 되었습니다.",
                null,
                "현재 위치를 확인하는 중입니다.",
                "현재 위치 정보가 불안정합니다.",
                "현재 속도를 표시합니다.",
                "갱신 지연",
                "현재 기온 정보입니다."
        );

        assertEquals("현재 위치 정보가 불안정합니다.", result);
    }

    @Test
    public void resolveFallsBackToWeatherStateWhenHigherPriorityMessagesAreAbsent() {
        String result = RideStatusMessageResolver.resolve(
                "자유 주행을 시작할 준비가 되었습니다.",
                null,
                "현재 위치를 확인하는 중입니다.",
                "현재 속도를 표시합니다.",
                "현재 속도를 표시합니다.",
                "갱신 지연",
                "현재 기온 정보입니다."
        );

        assertEquals("갱신 지연", result);
    }

    @Test
    public void resolveReturnsDefaultWhenOnlyDefaultMessagesExist() {
        String result = RideStatusMessageResolver.resolve(
                "자유 주행을 시작할 준비가 되었습니다.",
                null,
                "현재 위치를 확인하는 중입니다.",
                "현재 속도를 표시합니다.",
                "현재 속도를 표시합니다.",
                "현재 기온 정보입니다.",
                "현재 기온 정보입니다."
        );

        assertEquals("자유 주행을 시작할 준비가 되었습니다.", result);
    }

    @Test
    public void resolveIgnoresDefaultPendingPolicyMessage() {
        String result = RideStatusMessageResolver.resolve(
                "자유 주행을 시작할 준비가 되었습니다.",
                "현재 위치를 확인하는 중입니다.",
                "현재 위치를 확인하는 중입니다.",
                "현재 위치 정보가 불안정합니다.",
                "현재 속도를 표시합니다.",
                "현재 기온 정보입니다.",
                "현재 기온 정보입니다."
        );

        assertEquals("현재 위치 정보가 불안정합니다.", result);
    }
}
