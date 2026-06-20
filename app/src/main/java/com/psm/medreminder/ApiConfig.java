package com.psm.medreminder;

public final class ApiConfig {
    private ApiConfig() {
    }

    public static String endpoint(String path) {
        String baseUrl = BuildConfig.API_BASE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + path;
    }
}
