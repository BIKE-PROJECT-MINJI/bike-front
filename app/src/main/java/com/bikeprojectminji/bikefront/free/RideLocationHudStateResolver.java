package com.bikeprojectminji.bikefront.free;

public final class RideLocationHudStateResolver {

    private RideLocationHudStateResolver() {
    }

    public static RideLocationHudState resolve(
            boolean permissionGranted,
            boolean hasAcceptedLocation,
            boolean hasLocationQualityIssue,
            String permissionMessage,
            String loadingMessage,
            String qualityMessage
    ) {
        if (!permissionGranted) {
            return new RideLocationHudState("권한 필요", null);
        }
        if (!hasAcceptedLocation) {
            return new RideLocationHudState("확인 중", hasLocationQualityIssue ? qualityMessage : loadingMessage);
        }
        if (hasLocationQualityIssue) {
            return new RideLocationHudState("위치 확보됨", qualityMessage);
        }
        return new RideLocationHudState("위치 확보됨", null);
    }
}
