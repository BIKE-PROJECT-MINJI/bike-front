package com.bikeprojectminji.bikefront.free;

public final class RideLocationHudState {

    private final String value;
    private final String message;

    public RideLocationHudState(String value, String message) {
        this.value = value;
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
