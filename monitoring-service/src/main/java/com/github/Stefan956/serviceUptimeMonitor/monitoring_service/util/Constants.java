package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.util;

public final class Constants {

    private Constants() {}

    // Error messages
    public static final String SERVICE_NOT_FOUND_PREFIX = "Service not found: ";

    // Alert Service API paths
    public static final String ALERT_STATUS_CHANGE_PATH = "/api/alerts/status-change";

    // API base paths (optional — only extract if shared or frequently referenced)
    public static final String API_MONITORING_BASE = "/api/monitoring";
    public static final String API_MONITORING_READ_BASE = API_MONITORING_BASE + "/read";
}
