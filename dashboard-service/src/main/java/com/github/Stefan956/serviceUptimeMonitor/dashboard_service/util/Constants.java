package com.github.Stefan956.serviceUptimeMonitor.dashboard_service.util;

import java.time.Duration;

public final class Constants {

    private Constants() {}

    public static final String MONITORING_SERVICE_CURRENT_STATUSES = "/api/monitoring/read/current-statuses";
    public static final String MONITORING_SERVICE_HISTORY = "/api/monitoring/read/history/{serviceId}";
    public static final String MONITORING_SERVICES_ALL_SERVICES = "/api/monitoring/services";
    public static final String MONITORING_SERVICES_FETCHED_SERVICES_BY_ID = "/api/monitoring/services/{id}";

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);


}
