package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

import java.util.UUID;

public record MonitoredServiceDto(
        UUID id,
        String name,
        String url,
        int checkIntervalSeconds,
        boolean enabled
) {
}
// Service metadata
// Used by Admin views, Alert Service context, Configuration UIs