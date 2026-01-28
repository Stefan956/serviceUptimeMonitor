package com.github.Stefan956.serviceUptimeMonitor.monitoring_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MonitoredServiceResponseDto(
        UUID id,
        String name,
        String url,
        int checkIntervalSeconds,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
// Service metadata
// Used by Admin views, Alert Service context, Configuration UIs