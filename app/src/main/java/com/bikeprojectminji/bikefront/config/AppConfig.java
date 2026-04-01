package com.bikeprojectminji.bikefront.config;

public final class AppConfig {

    public static final String API_BASE_URL = "http://10.0.2.2:8080";
    public static final String MAP_STYLE_URL = "asset://osm_raster_style.json";
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int READ_TIMEOUT_MS = 5000;

    private AppConfig() {
    }
}
