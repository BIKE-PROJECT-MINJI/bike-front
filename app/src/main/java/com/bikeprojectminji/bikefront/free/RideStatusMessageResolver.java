package com.bikeprojectminji.bikefront.free;

public final class RideStatusMessageResolver {

    private RideStatusMessageResolver() {
    }

    public static String resolve(
            String defaultMessage,
            String policyBannerMessage,
            String policyPendingMessage,
            String defaultPolicyPendingMessage,
            String speedMessage,
            String defaultSpeedMessage,
            String weatherMessage,
            String defaultWeatherMessage
    ) {
        if (isMeaningful(policyBannerMessage)) {
            return policyBannerMessage;
        }
        if (isMeaningful(policyPendingMessage) && !policyPendingMessage.equals(defaultPolicyPendingMessage)) {
            return policyPendingMessage;
        }
        if (isMeaningful(speedMessage) && !speedMessage.equals(defaultSpeedMessage)) {
            return speedMessage;
        }
        if (isMeaningful(weatherMessage) && !weatherMessage.equals(defaultWeatherMessage)) {
            return weatherMessage;
        }
        return defaultMessage;
    }

    private static boolean isMeaningful(String value) {
        return value != null && !value.isBlank();
    }
}
