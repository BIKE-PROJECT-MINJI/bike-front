package com.bikeprojectminji.bikefront.config;

import com.bikeprojectminji.bikefront.BuildConfig;

public final class AppConfig {

    public static final String API_BASE_URL = BuildConfig.API_BASE_URL;
    public static final String MAP_STYLE_URL = "asset://osm_raster_style.json";
    public static final int CONNECT_TIMEOUT_MS = 5000;
    public static final int READ_TIMEOUT_MS = 5000;

    private AppConfig() {
    }
}
