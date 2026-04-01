package com.bikeprojectminji.bikefront.weather;

public interface CurrentWeatherGateway {

    final class WeatherResult {
        private final Integer temperatureC;
        private final String sky;
        private final Integer windSpeedKmh;
        private final String windDirectionText;
        private final Integer windDirectionDeg;
        private final boolean stale;
        private final boolean forecastFallbackUsed;

        public WeatherResult(
                Integer temperatureC,
                String sky,
                Integer windSpeedKmh,
                String windDirectionText,
                Integer windDirectionDeg,
                boolean stale,
                boolean forecastFallbackUsed
        ) {
            this.temperatureC = temperatureC;
            this.sky = sky;
            this.windSpeedKmh = windSpeedKmh;
            this.windDirectionText = windDirectionText;
            this.windDirectionDeg = windDirectionDeg;
            this.stale = stale;
            this.forecastFallbackUsed = forecastFallbackUsed;
        }

        public Integer getTemperatureC() { return temperatureC; }
        public String getSky() { return sky; }
        public Integer getWindSpeedKmh() { return windSpeedKmh; }
        public String getWindDirectionText() { return windDirectionText; }
        public Integer getWindDirectionDeg() { return windDirectionDeg; }
        public boolean isStale() { return stale; }
        public boolean isForecastFallbackUsed() { return forecastFallbackUsed; }
    }

    interface Callback {
        void onSuccess(WeatherResult result);
        void onEmpty();
        void onFailure(String message);
    }

    void loadCurrent(double latitude, double longitude, Callback callback);
}
