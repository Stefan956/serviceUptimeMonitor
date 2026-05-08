package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.util;

/**
 * Constants used across test classes.
 */
public final class TestConstants {

    private TestConstants() {
        // Utility class - prevent instantiation
    }

    // API Endpoints
    public static final String API_MONITORING = "/api/monitoring";
    public static final String API_MONITORING_SERVICES = API_MONITORING + "/services";
    public static final String API_MONITORING_READ = API_MONITORING + "/read";
    public static final String API_MONITORING_CURRENT_STATUSES = API_MONITORING_READ + "/current-statuses";
    public static final String API_MONITORING_HISTORY = API_MONITORING_READ + "/history";

    // Test URLs
    public static final String TEST_SERVICE_URL = "http://test.com/health";
    public static final String TEST_SERVICE_URL_2 = "http://test2.com/health";
    public static final String ALERT_SERVICE_URL = "http://localhost:8081";

    // Test Service Names
    public static final String TEST_SERVICE_NAME = "Test Service";
    public static final String TEST_SERVICE_NAME_2 = "Test Service 2";

    // Default Values
    public static final int DEFAULT_CHECK_INTERVAL = 60;
    public static final int DEFAULT_HTTP_STATUS_CODE = 200;
    public static final long DEFAULT_RESPONSE_TIME_MS = 100L;

    // Error Messages
    public static final String SERVICE_NOT_FOUND_MESSAGE = "Service not found";
    public static final String ENTITY_NOT_FOUND_MESSAGE = "Service not found";

    // Test Durations
    public static final int MINUTES_AGO_5 = 5;
    public static final int MINUTES_AGO_10 = 10;
    public static final int MINUTES_AGO_15 = 15;
}
