package com.bikeprojectminji.bikefront.speed;

public class RideSpeedUiState {

    private final String speedText;
    private final String message;

    public RideSpeedUiState(String speedText, String message) {
        this.speedText = speedText;
        this.message = message;
    }

    public String getSpeedText() {
        return speedText;
    }

    public String getMessage() {
        return message;
    }
}
